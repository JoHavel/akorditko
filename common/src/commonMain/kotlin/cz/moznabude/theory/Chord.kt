package cz.moznabude.theory

data class Chord(var intervals: Intervals, var key: Key, var bass: Int? = null, private var inC: Boolean = true) {
    fun intervals2notes() {
        if (inC) {
            inC = false
            val transposition = key.transposition.mod(12)
            intervals = intervals.rotate(transposition)
            bass = bass?.plus(transposition)?.mod(12)
        }
    }
}

fun major(key: Key): Chord {
    val intervals = Intervals(12) { false }
    intervals[Interval.P1] = true
    intervals[Interval.M3] = true
    intervals[Interval.P5] = true
    return Chord(intervals, key)
}
