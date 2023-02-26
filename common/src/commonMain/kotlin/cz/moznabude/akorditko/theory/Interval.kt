package cz.moznabude.akorditko.theory

typealias Intervals = MutableList<Boolean>

@Suppress("FunctionName")
inline fun Intervals(size: Int, init: (index: Int) -> Boolean) = MutableList(size, init)

//inline fun <reified T> MutableList<T>.rotate(n: Int): MutableList<T> = MutableList(size) { this[(it - n).mod(size)] }
fun Intervals.rotate(n: Int) = Intervals(size) { this[(it - n).mod(size)] }

// P = perfect, m = minor, M = Major,
// 1 = unison, 2 = second, ..., 7 = seventh, 8 = octave
@Suppress("EnumEntryName") // For m = minor in enum entry name
enum class Interval {
    P1, m2, M2, m3, M3, P4,
    TRITONE,
    P5, m6, M6, m7, M7, P8;
}

operator fun Intervals.get(interval: Interval) = this[interval.ordinal]
operator fun Intervals.set(interval: Interval, value: Boolean) {
    this[interval.ordinal] = value
}