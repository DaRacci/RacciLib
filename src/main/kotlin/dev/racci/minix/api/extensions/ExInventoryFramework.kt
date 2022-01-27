@file:Suppress("UNUSED")

package dev.racci.minix.api.extensions

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.PatternPane
import com.github.stefvanschie.inventoryframework.pane.util.Pattern
import dev.racci.minix.api.annotations.MinixDsl
import org.jetbrains.annotations.ApiStatus

@MinixDsl
@ApiStatus.AvailableSince("1.0.0")
inline fun PatternPane.dsl(
    x: Int,
    y: Int,
    length: Int,
    height: Int,
    priority: Pane.Priority,
    pattern: Pattern,
    unit: PatternPane.() -> Unit,
) = PatternPane(x, y, length, height, priority, pattern).also(unit)

@MinixDsl
@ApiStatus.AvailableSince("1.0.0")
inline fun ChestGui.dsl(
    rows: Int,
    title: String,
    unit: ChestGui.() -> Unit,
) = ChestGui(rows, title).also(unit)
