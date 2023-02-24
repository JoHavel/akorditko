package cz.moznabude

import cz.moznabude.theory.Chord
import cz.moznabude.theory.Intervals

const val concertA = 9
const val diff2midi = 60

val standardGuitarTuning = listOf(-20, -15, -10, -5, -1, 4)

class FretEngine(private val tuning: List<Int>) {
    fun getFrets(chord: Chord): MutableList<List<Int>> {
        val ans = mutableListOf<List<Int>>()

        chord.bass = chord.bass ?: 0
        chord.intervals[chord.bass!!] = true
        chord.intervals2notes()

        for (position in 0..10) { // 11 + 1 = 0
            val admissibleFrets = List(tuning.size) { mutableListOf<Int>() }
            for (i in tuning.indices) {
                val string = tuning[i].mod(12)
                if (chord.intervals[string]) admissibleFrets[i].add(0)

                for (finger in 1..3) {
                    val fret = (string + position + finger) % 12
                    if (chord.intervals[fret]) admissibleFrets[i].add(position + finger)
                }
            }

            val different = chord.intervals.count { it }
            val chosen = Intervals(12) { false }
            val frets = ArrayDeque<Int>()
            var chosenN = 0

            fun dfs(depth: Int) {
                if (depth == tuning.size) {
                    if (chosenN == different) ans.add(frets.toList())
                } else {
                    if (chosenN == 0) dfs(depth + 1)
                    for (fret in admissibleFrets[depth]) {
                        val note = (fret + tuning[depth]).mod(12)

                        fun goDeeper() {
                            frets.addLast(fret)
                            dfs(depth + 1)
                            frets.removeLast()
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

            dfs(0)
        }

        return ans
    }
}