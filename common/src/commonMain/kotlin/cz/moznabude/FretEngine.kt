package cz.moznabude

import cz.moznabude.theory.Chord
import cz.moznabude.theory.Intervals
import kotlin.math.max
import kotlin.math.min

const val concertA = 9
const val diff2midi = 60

val standardGuitarTuning = listOf(-20, -15, -10, -5, -1, 4)

class FretEngine(private val tuning: List<Int>) {
    fun getFrets(chord: Chord): List<List<Int>> {
        val ans = mutableListOf<List<Int>>()

        chord.bass = chord.bass ?: 0
        chord.intervals[chord.bass!!] = true
        chord.intervals2notes()

        val admissibleFrets = List(tuning.size) { mutableListOf<Int>() }

        for (i in tuning.indices) {
            for (fret in 0..13) {
//                if (chord.intervals[string]) admissibleFrets[i].add(0)
                val note = (tuning[i] + fret).mod(12)
                if (chord.intervals[note]) admissibleFrets[i].add(fret)
            }
        }

        val different = chord.intervals.count { it }
        val chosen = Intervals(12) { false }
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

        return ans.filter(::suitable).sortedBy { it.filterNot { fret -> fret == 0 }.min() }
    }
}