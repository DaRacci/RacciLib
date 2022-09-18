package dev.racci.minix.api.utils.collections

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import dev.racci.minix.api.utils.UtilObject
import dev.racci.minix.api.utils.safeCast
import dev.racci.minix.api.utils.unsafeCast
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Utilities for Generic Collections.
 */
object CollectionUtils : UtilObject by UtilObject {

    /**
     * Checks if the collection contains the [String] by IgnoreCase.
     *
     * @param element The [String] to look for.
     * @return True if the collection contains the [String]
     */
    fun Collection<String>.containsIgnoreCase(
        element: String
    ): Boolean = any { it.equals(element, true) }

    /**
     * Get the element at this index and unsafe cast it to the specified type.
     *
     * @param T the cast type.
     * @param index the index of the element.
     * @return the cast element at this index.
     */
    @Throws(ClassCastException::class)
    fun <T> Collection<*>.getCast(
        index: Int
    ): T = elementAtOrNull(index).unsafeCast()

    /**
     * Get the element at this index and safe cast it to the specified type.
     *
     * @param T the cast type.
     * @param index the index of the element.
     * @return the cast element at this index.
     */
    inline fun <reified T> Collection<*>.getCastOrNull(
        index: Int
    ): T? = elementAtOrNull(index).safeCast()

    /**
     * Get the element at this index and safe cast it to the specified type.
     *
     * @param T the cast type.
     * @param index the index of the element.
     * @param def the default value if the result is null.
     * @return the cast element at this index, or the default value.
     */
    inline fun <reified T> Collection<*>.getCastOrDef(
        index: Int,
        def: () -> T
    ): T = elementAtOrNull(index).safeCast() ?: def()

    /**
     * Find the first element that matches the name given.
     *
     * @param name the name to match.
     * @param ignoreCase true if the name should be matched ignoring case.
     * @return the first element that matches the name.
     */
    fun Collection<String>.find(
        name: String,
        ignoreCase: Boolean = false
    ): String? = find { it.equals(name, ignoreCase) }

    fun <T> Collection<KProperty<T>>.find(
        name: String,
        ignoreCase: Boolean = false
    ): KProperty<T>? = find { it.name.equals(name, ignoreCase) }

    fun <T> Collection<KFunction<T>>.find(
        name: String,
        ignoreCase: Boolean = false
    ): KFunction<T>? = find { it.name.equals(name, ignoreCase) }

    fun <T> Collection<T>.first(
        name: String,
        ignoreCase: Boolean = false,
        selector: (T) -> String
    ): T = first { selector(it).equals(name, ignoreCase) }

    fun Collection<String>.first(
        name: String,
        ignoreCase: Boolean = false
    ): String = first { it.equals(name, ignoreCase) }

    fun <T> Collection<KProperty<T>>.first(
        name: String,
        ignoreCase: Boolean = false
    ): KProperty<T> = first { it.name.equals(name, ignoreCase) }

    fun <R : Any, T> Collection<KProperty1<R, T>>.first(
        name: String,
        ignoreCase: Boolean = false
    ): KProperty1<R, T> = first { it.name.equals(name, ignoreCase) }

    fun <T> Collection<KFunction<T>>.first(
        name: String,
        ignoreCase: Boolean = false
    ): KFunction<T> = first { it.name.equals(name, ignoreCase) }

    fun <T> Collection<T>.find(
        name: String,
        ignoreCase: Boolean = false,
        selector: (T) -> String
    ): T? = find { selector(it).equals(name, ignoreCase) }

    /**
     * Checks if the array contains the [String] by IgnoreCase.
     *
     * @param element The [String] to look for.
     * @return True if the array contains the [String]
     */
    fun Array<String>.containsIgnoreCase(
        element: String
    ): Boolean = any { it.equals(element, true) }

    /**
     * Get the element at this index and unsafe cast it to the specified type.
     *
     * @param T the cast type.
     * @param index the index of the element.
     * @return the cast element at this index.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(ClassCastException::class)
    fun <T> Array<*>.getCast(
        index: Int
    ): T = elementAtOrNull(index) as T

    /**
     * Get the element at this index and safe cast it to the specified type.
     *
     * @param T the cast type.
     * @param index the index of the element.
     * @return the cast element at this index.
     */
    inline fun <reified T> Array<*>.getCastOrNull(
        index: Int
    ): T? = elementAtOrNull(index).safeCast()

    /**
     * Get the element at this index and safe cast it to the specified type.
     *
     * @param T the cast type.
     * @param index the index of the element.
     * @param def the default value if the result is null.
     * @return the cast element at this index, or the default value.
     */
    inline fun <reified T> Array<*>.getCastOrDef(
        index: Int,
        def: () -> T
    ): T = elementAtOrNull(index).safeCast() ?: def()

    fun Array<String>.find(
        name: String,
        ignoreCase: Boolean = false
    ): String? = find { it.equals(name, ignoreCase) }

    fun <T> Array<KProperty<T>>.find(
        name: String,
        ignoreCase: Boolean = false
    ): KProperty<T>? = find { it.name.equals(name, ignoreCase) }

    fun <T> Array<KFunction<T>>.find(
        name: String,
        ignoreCase: Boolean = false
    ): KFunction<T>? = find { it.name.equals(name, ignoreCase) }

    fun <T> Array<T>.find(
        name: String,
        ignoreCase: Boolean = false,
        selector: (T) -> String
    ): T? = find { selector(it).equals(name, ignoreCase) }

    /**
     * Checks if the map contains the [String] as a key by IgnoreCase.
     *
     * @param V The value Type.
     * @param key The [String] to look for.
     * @return True if the map contains the key of [String]
     */
    fun <V> Map<String, V>.containsKeyIgnoreCase(
        key: String
    ): Boolean = keys.containsIgnoreCase(key)

    /**
     * Attempts to find and retrieve the key matching the [String] by IgnoreCase.
     *
     * @param V The value Type.
     * @param key The [String] to get.
     * @return True if the map contains the key of [String]
     */
    fun <V> Map<String, V>.getIgnoreCase(
        key: String
    ): V? = entries.find { it.key.equals(key, true) }?.value

    /**
     * Compute an action with each item of this collection
     * before removing it from the collection.
     *
     * @param T The Object Type.
     * @param onRemove The Action to execute.
     */
    inline fun <T> MutableCollection<T>.clear(
        onRemove: (T) -> Unit
    ) {
        toMutableList().forEach {
            remove(it)
            onRemove(it)
        }
    }

    /**
     * Compute an action with each entry of this map
     * before removing it from the collection.
     *
     * @param K The key type.
     * @param V The value type.
     * @param onRemove The Action to execute.
     */
    inline fun <K, V> MutableMap<K, V>.clear(
        onRemove: (K, V) -> Unit
    ) = keys.toMutableSet().forEach {
        onRemove(it, remove(it)!!)
    }

    /**
     * Compute an action with each entry of this map
     * before removing it from the collection.
     *
     * @param K The key type.
     * @param V The value type.
     * @param onRemove The Action to execute.
     */
    inline fun <K, V> MutableMap<K, V>.clear(
        onRemove: V.() -> Unit
    ) = entries.toMutableSet().forEach { (key, _) ->
        onRemove(remove(key)!!)
    }

    /**
     * Compute an action with the item at this index and remove it from the collection.
     *
     * @param K The key type.
     * @param V The value type.
     * @param key The item key.
     * @param onRemove The action to perform.
     * @return True if the item was removed.
     */
    inline fun <K, V> MutableMap<K, V>.computeAndRemove(
        key: K,
        onRemove: (K, V) -> Unit
    ): Boolean {
        if (isEmpty() || key !in this) return false
        val value = this[key] ?: return false

        onRemove(key, value)
        return remove(key) != null
    }

    /**
     * Compute an action with the item at this index and remove it from the collection.
     *
     * @param K The key type.
     * @param V The value type.
     * @param key The item key.
     * @param onRemove The action to perform.
     * @return True if the item was removed.
     */
    inline fun <K, V> MutableMap<K, V>.computeAndRemove(
        key: K,
        onRemove: V.() -> Unit
    ): Boolean {
        if (isEmpty() || key !in this) return false
        val value = this[key] ?: return false

        onRemove(value)
        return remove(key) != null
    }

    operator fun <K, V> Map<K, V>.get(
        key: K,
        default: V
    ): V = getOrDefault(key, default)

    inline fun <reified T> Map<*, *>.getCast(
        key: Any
    ): T = this[key].unsafeCast()

    inline fun <reified T> Map<*, *>.getCastOrNull(
        key: Any
    ): T? = this[key].safeCast()

    inline fun <reified T> Map<*, *>.getCastOrDef(
        key: Any,
        def: () -> T
    ): T = this[key].safeCast() ?: def()

    fun <K, V> cacheOf(
        build: K.() -> V
    ): LoadingCache<K, V> = cacheOf(build) {}

    inline fun <K, V> cacheOf(
        noinline build: K.() -> V,
        builder: Caffeine<K, V>.() -> Unit
    ): LoadingCache<K, V> = Caffeine.newBuilder()
        .removalListener<K, V> { _, _, _ -> }
        .apply(builder)
        .build(build)
}
