package cz.moznabude.akorditko.theory

/**
 * We represent "set" of intervals by [List] of [Boolean]s where true means there is that interval and false means this interval is not in this set.
 *
 * Preferably indexed by [Interval] and of size 12.
 */
typealias Intervals = MutableList<Boolean>

// Constructor for [Intervals]
@Suppress("FunctionName")
inline fun Intervals(init: (index: Int) -> Boolean = { false }) = MutableList(12, init)

/**
 * "Rotate" [Intervals] (used mainly if [Intervals] represents pitches)
 */
fun Intervals.rotate(n: Int) = Intervals { this[(it - n).mod(12)] }

/**
 * Basic (0–12) music intervals, number means difference (in semitones) between pitches.
 * P = perfect, m = minor, M = Major,
 * 1 = unison, 2 = second, ..., 7 = seventh, 8 = octave
 */
@Suppress("EnumEntryName") // For m = minor in enum entry name
enum class Interval {
    P1, m2, M2, m3, M3, P4,
    TRITONE,
    P5, m6, M6, m7, M7, P8;
}

// Access [Intervals] by [Interval] instead of number
operator fun Intervals.get(interval: Interval) = this[interval.ordinal]
operator fun Intervals.set(interval: Interval, value: Boolean) {
    this[interval.ordinal] = value
}