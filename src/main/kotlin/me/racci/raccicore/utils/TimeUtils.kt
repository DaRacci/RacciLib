@file:OptIn(ExperimentalTime::class)
package me.racci.raccicore.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

fun now(): Instant = Clock.System.now()

object TimeConversionUtils {

    val Long.millisecondToTick      : Duration  get() = toDouble().millisecondToTick
    val Int.millisecondToTick       : Duration  get() = toDouble().millisecondToTick
    val Double.millisecondToTick    : Duration  get() = Duration.milliseconds(this / 50)
    val Duration.millisecondToTick  : Double    get() = toDouble(DurationUnit.MILLISECONDS) * 50

    val Long.tickToMillisecond      : Duration  get() = toDouble().tickToMillisecond
    val Int.tickToMillisecond       : Duration  get() = toDouble().tickToMillisecond
    val Double.tickToMillisecond    : Duration  get() = Duration.milliseconds(this * 50)
    val Duration.tickToMillisecond  : Double    get() = toDouble(DurationUnit.MILLISECONDS) / 50

    val Long.millisecondToSecond    : Duration  get() = toDouble().millisecondToSecond
    val Int.millisecondToSecond     : Duration  get() = toDouble().millisecondToSecond
    val Double.millisecondToSecond  : Duration  get() = Duration.seconds(this * 1000)
    val Duration.millisecondToSecond: Double    get() = toDouble(DurationUnit.SECONDS) / 1000

    val Long.secondToMillisecond    : Duration  get() = toDouble().secondToMillisecond
    val Int.secondToMillisecond     : Duration  get() = toDouble().secondToMillisecond
    val Double.secondToMillisecond  : Duration  get() = Duration.milliseconds(this / 1000)
    val Duration.secondToMillisecond: Double    get() = toDouble(DurationUnit.MILLISECONDS) * 1000

    val Long.minuteToSeconds        : Duration  get() = toDouble().minuteToSeconds
    val Int.minuteToSeconds         : Duration  get() = toDouble().minuteToSeconds
    val Double.minuteToSeconds      : Duration  get() = Duration.milliseconds(this * 60)
    val Duration.minuteToSeconds    : Double    get() = toDouble(DurationUnit.SECONDS) / 60

    val Long.secondToMinute         : Duration  get() = toDouble().secondToMinute
    val Int.secondToMinute          : Duration  get() = toDouble().secondToMinute
    val Double.secondToMinute       : Duration  get() = Duration.milliseconds(this / 60)
    val Duration.secondToMinute     : Double    get() = toDouble(DurationUnit.MINUTES) * 60

    val Long.hourToMinute           : Duration  get() = toDouble().hourToMinute
    val Int.hourToMinute            : Duration  get() = toDouble().hourToMinute
    val Double.hourToMinute         : Duration  get() = Duration.seconds(this * 60)
    val Duration.hourToMinute       : Double    get() = toDouble(DurationUnit.MINUTES) / 60

    val Long.minuteToHour           : Duration  get() = toDouble().minuteToHour
    val Int.minuteToHour            : Duration  get() = toDouble().minuteToHour
    val Double.minuteToHour         : Duration  get() = Duration.milliseconds(this / 60)
    val Duration.minuteToHour       : Double    get() = toDouble(DurationUnit.HOURS) * 60

}