package cz.moznabude.akorditko

import cz.moznabude.akorditko.theory.Chord
import cz.moznabude.akorditko.theory.Intervals
import kotlin.math.max
import kotlin.math.min

/**
 * ConcertA in our numbering
 */
const val concertA = 9

/**
 * Midi note number is our number + 60
 */
const val diff2midi = 60

/**
 * Standard guitar tuning (E2, A2, D3, G3, H3, E4)
 */
val standardGuitarTuning = listOf(-20, -15, -10, -5, -1, 4)

/**
 * Engine ([Chord] -> pitches[^1]) for plucked string instrument which is defined by its [tuning].
 * [^1]: In this case not pitches but fret for every string.
 */
class FretEngine(private val tuning: List<Int>) {

    /**
     * Returns [List] of variants how to play [chord]. Each variant has number -- the fret -- for every string starting
     * from last string, ending at first string which may be played.
     */
    fun getFrets(chord: Chord): List<List<Int>> {
        val ans = mutableListOf<List<Int>>()

        chord.bass = chord.bass ?: 0
        chord.intervals[chord.bass!!] = true
        chord.intervals2pitches()

        val admissibleFrets = List(tuning.size) { mutableListOf<Int>() }

        for (i in tuning.indices) {
            for (fret in 0..13) {
//                if (chord.intervals[string]) admissibleFrets[i].add(0)
                val note = (tuning[i] + fret).mod(12)
                if (chord.intervals[note]) admissibleFrets[i].add(fret)
            }
        }

        val different = chord.intervals.count { it }
        val chosen = Intervals()
        val chosenFrets = ArrayDeque<Int>()
        var chosenN = 0

        fun dfs(depth: Int, low: Int, high: Int) {
            if (high - low > 2) return
            if (depth == tuning.size) {
                if (chosenN == different) ans.add(chosenFrets.toList())
            } else {
                if (chosenN == 0) dfs(depth + 1, low, high)
                for (fret in admissibleFrets[depth]) {
                    val note = (fret + tuning[depth]).mod(12)

                    fun goDeeper() {
                        chosenFrets.addLast(fret)
                        if (fret == 0) dfs(depth + 1, low, high)
                        else dfs(depth + 1, min(low, fret), max(high, fret))
                        chosenFrets.removeLast()
                    }

                    if (!chosen[note]) {
                        chosen[note] = true
                        chosenN++
                        goDeeper()
                        chosenN--
                        chosen[note] = false
                    } else {
                        goDeeper()
                    }
                }
            }
        }

        dfs(0, 13, 0)

        fun suitable(frets: List<Int>): Boolean =
            frets.reversed().zip(tuning.reversed()).map { it.first + it.second }.min().mod(12) == chord.bass
                    && frets.count { it != 0 } <= 4 && !frets.contains(12)

        return ans.filter(::suitable).sortedBy { it.filterNot { fret -> fret == 0 }.minOrNull() ?: 0 }
    }
}