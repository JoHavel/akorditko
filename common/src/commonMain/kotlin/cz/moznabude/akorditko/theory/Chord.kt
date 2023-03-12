package cz.moznabude.akorditko.theory

/**
 * Hold data about chord, chord is determined by [intervals] in it, its [key] ("pitch of [Interval.P1] in [intervals]") and
 * its [bass] (= lowest pitch, it determines inversion of chord).
 *
 * Moreover, we can "rotate" (by [intervals2pitches]) the intervals in the way, that [Interval.P1] will be C, and it will
 * give us the actual pitches (modulo 12 semitones) in this case [inC] will be false. [inC] true means we parse chord as
 * C chord (and key is held only in [key]).
 */
data class Chord(var intervals: Intervals, var key: Key, var bass: Int? = null, private var inC: Boolean = true) {
    /**
     * Transforms [intervals] and [bass] to pitches (respecting the [key]).
     */
    fun intervals2pitches() {
        if (inC) {
            inC = false
            val transposition = key.transposition.mod(12)
            intervals = intervals.rotate(transposition)
            bass = bass?.plus(transposition)?.mod(12)
        }
    }
}

/**
 * Returns major chord in given [key].
 */
fun major(key: Key): Chord {
    val intervals = Intervals()
    intervals[Interval.P1] = true
    intervals[Interval.M3] = true
    intervals[Interval.P5] = true
    return Chord(intervals, key)
}
