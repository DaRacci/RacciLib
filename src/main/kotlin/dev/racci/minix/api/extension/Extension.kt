package dev.racci.minix.api.extension

import dev.racci.minix.api.extensions.WithPlugin
import dev.racci.minix.api.plugin.Minix
import dev.racci.minix.api.plugin.MinixPlugin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

abstract class Extension<P : MinixPlugin> : KoinComponent, WithPlugin<P> {

    abstract val name: String

    open val bindToKClass: KClass<*>? = null

    open val minix by inject<Minix>()

    open val log get() = plugin.log

    open val dependencies: ImmutableList<KClass<out Extension<*>>> = persistentListOf()

    open var state: ExtensionState = ExtensionState.UNLOADED

    open val loaded: Boolean get() = state == ExtensionState.LOADED

    abstract suspend fun handleEnable()

    suspend fun handleSetup() {
        setState(ExtensionState.LOADING)
        log.debug { "Running handleSetup() for $name" }

        @Suppress("TooGenericExceptionCaught")
        try {
            handleEnable()
        } catch (t: Throwable) {
            setState(ExtensionState.FAILED_LOADING)
            log.error { "Minix ran into an error while starting up ${this.name}" }
            log.throwing(t)
        }

        setState(ExtensionState.LOADED)
    }

    open suspend fun setState(state: ExtensionState) {
        minix.send(ExtensionStateEvent(this, state))
        this.state = state
    }

    open suspend fun handleUnload() {}

    open suspend fun doUnload() {
        var error: Throwable? = null

        setState(ExtensionState.UNLOADING)

        @Suppress("TooGenericExceptionCaught")
        try {
            this.handleUnload()
        } catch (t: Throwable) {
            error = t
            setState(ExtensionState.FAILED_UNLOADING)
        }

        if (error != null) {
            throw error
        }

        setState(ExtensionState.UNLOADED)
    }
}
