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
 * Standard ukulele tuning (G4, C4, E4, A4)
 */
val standardUkuleleTuning = listOf(7, 0, 4, 9)

/**
 * Barre chord [at] n-th fret, [from] m-th string.
 */
data class Barre(val at: Int, val from: Int)

/**
 * Fingering on guitar consisting of [frets] (from the leftest played string to the rightest string on instrument,
 * 0 = empty string, otherwise fret (from smallest) where finger is) and maybe [barre].
 */
data class Fingering(val frets: List<Int>, val barre: Barre? = null) {
    fun minFret() = barre?.at ?: (frets.filter { it != 0 }.minOrNull() ?: 0)

    fun max() = max(frets.max(), barre?.at ?: 0)
}


/**
 * Engine ([Chord] -> pitches[^1]) for plucked string instrument which is defined by its [tuning].
 * [^1]: In this case not pitches but fret for every string.
 */
class FretEngine(private val tuning: List<Int>) {

    /**
     * Returns [List] of variants how to play [chord]. Each variant has number -- the fret -- for every string starting
     * from last string, ending at first string which may be played.
     */
    fun getFingerings(chord: Chord): List<Pair<List<Fingering>, String>> {
        chord.prepare()
        val admissibleFrets = getAdmissibleFrets(chord)
        val potentialFingerings = getPotentialFingerings(chord, admissibleFrets)

        val admissibleFingerings = potentialFingerings.filter { it.admissible() }.sortedBy(Fingering::minFret)
        val admissibleBarreFingerings =
            potentialFingerings.map { it.toBare() }.filter { it.admissible() }.sortedBy(Fingering::minFret)
                .filterNot { it.barre!!.from == tuning.size - 1 }

        fun rightBass(fingering: Fingering): Boolean =
            fingering.frets.reversed().zip(tuning.reversed()).map { it.first + it.second }.min().mod(12) == chord.bass

        return listOf(
            admissibleFingerings.filter(::rightBass).removeDuplicate() to "normal:",
            admissibleBarreFingerings.filter(::rightBass).removeDuplicate() to "barre:",
            admissibleFingerings.filterNot(::rightBass).removeDuplicate() to "no bass:",
            admissibleBarreFingerings.filterNot(::rightBass).removeDuplicate() to "barre,\n no bass:"
        )
    }

    /**
     * Prepares [Chord] for [getFingerings]
     */
    private fun Chord.prepare() {
        bass = bass ?: 0
        intervals[bass!!] = true
        intervals2pitches()
    }

    /**
     * Returns all finger positions that can be in [chord]
     */
    private fun getAdmissibleFrets(chord: Chord): List<List<Int>> {
        val admissibleFrets = List(tuning.size) { mutableListOf<Int>() }

        for (i in tuning.indices) {
            for (fret in 0..13) {
//                if (chord.intervals[string]) admissibleFrets[i].add(0)
                val note = (tuning[i] + fret).mod(12)
                if (chord.intervals[note]) admissibleFrets[i].add(fret)
            }
        }

        return admissibleFrets
    }

    /**
     * Returns all [Fingering]s that could be right ones (it has all tones of [chord] but uses only [admissibleFrets] and is in range of three frets)
     */
    private fun getPotentialFingerings(chord: Chord, admissibleFrets: List<List<Int>>): List<Fingering> {
        val potentialFingerings = mutableListOf<Fingering>()

        val different = chord.intervals.count { it }
        val chosen = Intervals()
        val chosenFrets = ArrayDeque<Int>()
        var chosenN = 0

        fun dfs(depth: Int, low: Int, high: Int) {
            if (high - low > 2) return
            if (depth == tuning.size) {
                if (chosenN == different) potentialFingerings.add(Fingering(chosenFrets.toList()))
            } else {
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
                if (chosenN == 0) dfs(depth + 1, low, high)
            }
        }

        dfs(0, 13, 0)

        return potentialFingerings
    }

    /**
     * Returns if [Fingering] makes sense (can be played with human hand and is not unnecessarily complicated)
     * FIXME maybe it makes sense to use 12th fret
     */
    private fun Fingering.admissible(): Boolean =
        (frets.count { it != 0 && (barre == null || it != barre.at) } <= if (barre == null) 4 else 3)
                && !frets.contains(12)
                && (barre == null || !frets.subList(barre.from - tuning.size + frets.size, frets.size).contains(0) && barre.at != 0)


    /**
     * Clean list from "duplicates" (when one [Fingering] is part (prefix) of other one) leaving only longest [Fingering]s of the "same" ones.
     */
    private fun List<Fingering>.removeDuplicate(): List<Fingering> {
        val ans = mutableListOf<Fingering>()
        for (fingering in this)
            if (ans.isEmpty() || ans.last().isNotPrefixOf(fingering)) ans.add(fingering)

        return ans
    }

    /**
     * Returns if [Fingering] is not part (prefix) of [other]
     */
    private fun Fingering.isNotPrefixOf(other: Fingering): Boolean = other.frets.size > frets.size ||
            frets.subList(frets.size - other.frets.size, frets.size) != other.frets

    /**
     * Converts [Fingering] to barre [Fingering]
     */
    private fun Fingering.toBare(): Fingering {
        val minFret = minFret()
        var from = frets.indexOf(minFret)
        while (from > 0 && frets[from - 1] != 0) from--
        return Fingering(frets, Barre(minFret, tuning.size - frets.size + from))
    }
}