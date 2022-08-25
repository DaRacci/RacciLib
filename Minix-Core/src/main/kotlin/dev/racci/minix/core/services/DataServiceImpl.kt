package dev.racci.minix.core.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.benmanes.caffeine.cache.RemovalCause
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.racci.minix.api.annotations.MappedConfig
import dev.racci.minix.api.annotations.MappedExtension
import dev.racci.minix.api.data.MinixConfig
import dev.racci.minix.api.exceptions.MissingAnnotationException
import dev.racci.minix.api.exceptions.MissingPluginException
import dev.racci.minix.api.plugin.Minix
import dev.racci.minix.api.plugin.MinixPlugin
import dev.racci.minix.api.plugin.logger.MinixLogger
import dev.racci.minix.api.serializables.Serializer
import dev.racci.minix.api.services.DataService
import dev.racci.minix.api.updater.providers.UpdateProvider
import dev.racci.minix.api.utils.Closeable
import dev.racci.minix.api.utils.getKoin
import dev.racci.minix.api.utils.kotlin.ifInitialized
import dev.racci.minix.api.utils.safeCast
import dev.racci.minix.api.utils.unsafeCast
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@MappedExtension(Minix::class, "Data Service", bindToKClass = DataService::class)
class DataServiceImpl(override val plugin: Minix) : DataService() {
    @OptIn(DelicateCoroutinesApi::class)
    private val threadContext = object : Closeable<ExecutorCoroutineDispatcher>() {
        override fun create() = newSingleThreadContext("Data Service Thread")
        override fun onClose() { value.value?.close() }
    }

    val configDataHolder: LoadingCache<KClass<MinixConfig<MinixPlugin>>, ConfigData<MinixPlugin, MinixConfig<MinixPlugin>>> = Caffeine.newBuilder()
        .executor(threadContext.get().executor)
        .removalListener<KClass<*>, ConfigData<*, *>> { key, value, cause ->
            if (key == null || value == null || cause == RemovalCause.REPLACED) return@removalListener
            log.info(scope = SCOPE) { "Saving and disposing configurate class ${key.simpleName}" }

            value.configInstance.handleUnload()
            if (value.configLoader.canSave()) {
                value.save()
            }
        }
        .build(::ConfigData)

    private val dataSource = lazy {
        HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${plugin.dataFolder.path}/database.db"
            connectionTestQuery = "SELECT 1"
            addDataSourceProperty("cachePrepStmts", true)
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }.let(::HikariDataSource)
    }
    val database by lazy { Database.connect(dataSource.value) }

    override suspend fun handleLoad() {
        if (!plugin.dataFolder.exists() && !plugin.dataFolder.mkdirs()) {
            log.error(scope = SCOPE) { "Failed to create data folder!" }
        }
    }

    override suspend fun handleUnload() {
        dataSource.ifInitialized(HikariDataSource::close)
        configDataHolder.invalidateAll()
    }

    override fun <P : MinixPlugin, T : MinixConfig<P>> getConfig(kClass: KClass<T>): T? = configDataHolder[kClass.unsafeCast()].configInstance as? T

    class ConfigData<P : MinixPlugin, T : MinixConfig<P>>(val kClass: KClass<T>) {
        val mappedConfig: MappedConfig = this.kClass.findAnnotation() ?: throw MissingAnnotationException(this.kClass, MappedConfig::class.unsafeCast())
        val configInstance: T
        val file: File
        val node: CommentedConfigurationNode
        val configLoader: HoconConfigurationLoader

        fun save() {
            this.node.set(this.kClass, this.configInstance)
            this.configLoader.save(updateNode())
        }

        private fun updateNode(): CommentedConfigurationNode {
            if (!node.virtual()) { // we only want to migrate existing data
                val trans = createVersionBuilder()
                val startVersion = trans.version(node)

                trans.apply(node)

                val endVersion = trans.version(node)
                if (startVersion != endVersion) { // we might not have made any changes
                    getKoin().get<MinixLogger>().info { "Updated config schema from $startVersion to $endVersion" }
                }
            }

            return node
        }

        private fun createVersionBuilder(): ConfigurationTransformation.Versioned {
            val builder = ConfigurationTransformation.versionedBuilder()
            for ((version, transformation) in configInstance.versionTransformations) {
                builder.versionKey()
                builder.addVersion(version, transformation)
            }

            return builder.build()
        }

        private fun buildConfigLoader() = HoconConfigurationLoader.builder()
            .file(file)
            .prettyPrinting(true)
            .defaultOptions { options ->
                options.acceptsType(kClass.java)
                options.shouldCopyDefaults(true)
                options.serializers { serializerBuilder ->
                    serializerBuilder.registerAnnotatedObjects(objectMapperFactory())
                        .registerAll(TypeSerializerCollection.defaults())
                        .registerAll(ConfigurateComponentSerializer.builder().build().serializers())
                        .registerAll(UpdateProvider.UpdateProviderSerializer.serializers)
                        .registerAll(Serializer.serializers)
                        .also { getSerializerCollection()?.let(it::registerAll) } // User defined serializers
                }
            }.build()

        private fun getSerializerCollection(): TypeSerializerCollection? {
            val extraSerializers = mappedConfig.serializers.asList().listIterator()
            val collection = TypeSerializerCollection.builder()
            while (extraSerializers.hasNext()) {
                val nextClazz = extraSerializers.next()
                val serializer = extraSerializers.runCatching {
                    next().unsafeCast<KClass<TypeSerializer<*>>>().let {
                        it.objectInstance ?: it.createInstance()
                    }
                }.getOrNull() ?: continue
                collection.register(nextClazz.java, serializer.safeCast())
            }

            return collection.build()
        }

        private fun ensureDirectory() {
            if (!configInstance.plugin.dataFolder.exists() && !configInstance.plugin.dataFolder.mkdirs()) {
                getKoin().get<MinixLogger>().warn { "Failed to create directory: ${configInstance.plugin.dataFolder.absolutePath}" }
            }
        }

        init {
            println("Building new config on thread: " + Thread.currentThread().name)

            if (!this.kClass.hasAnnotation<ConfigSerializable>()) throw MissingAnnotationException(this.kClass, ConfigSerializable::class.unsafeCast())

            val plugin = getKoin().getOrNull<MinixPlugin>(this.mappedConfig.parent) ?: throw MissingPluginException("Could not find plugin instance for ${this.mappedConfig.parent}")
            this.file = plugin.dataFolder.resolve(this.mappedConfig.file)

            ensureDirectory()
            this.configLoader = buildConfigLoader()

            try {
                this.node = this.configLoader.load()
                this.configInstance = this.node.get(kClass) ?: throw RuntimeException("Could not load configurate class ${this.kClass.simpleName}")

                if (!this.file.exists()) {
                    this.save()
                }

                this.configInstance.load()
            } catch (e: ConfigurateException) {
                getKoin().get<MinixLogger>().error(e) { "Failed to load configurate file ${this.file.name}" }
                throw e
            }
        }
    }

    object PluginData : IdTable<String>("plugin") {

        override val id: Column<EntityID<String>> = text("name").entityId()
        var newVersion = text("new_version")
        var oldVersion = text("old_version")
    }

    class DataHolder(plugin: EntityID<String>) : Entity<String>(plugin) {

        companion object : EntityClass<String, DataHolder>(PluginData), KoinComponent {

            fun getOrNull(id: String): DataHolder? = find { PluginData.id eq id }.firstOrNull()

            fun getOrNull(plugin: Plugin): DataHolder? = getOrNull(plugin.name)

            operator fun get(plugin: Plugin): DataHolder = get(plugin.name)
        }

        var newVersion by PluginData.newVersion
        var oldVersion by PluginData.oldVersion
    }

    companion object : ExtensionCompanion<DataServiceImpl>() {
        const val SCOPE = "data"
    }
}
