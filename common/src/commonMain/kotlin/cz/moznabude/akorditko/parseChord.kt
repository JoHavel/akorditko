package cz.moznabude.akorditko

import cz.moznabude.akorditko.theory.*

/**
 * Helper for parsing, returns if [String.substring] ([index], [String.length]) starts with [str].
 */
fun String.continuesWith(str: String, index: Int): Boolean {
    if (this.length < index + str.length) return false
    for (i in str.indices) {
        if (this[index + i] != str[i]) return false
    }
    return true
}

/**
 * Parses key from [string] by [s2k] starting at [index]. Return pair (parsed key, length of its string representation).
 */
fun parseKey(string: String, s2k: String2Key = string2Key, index: Int = 0): Pair<Key, Int> {
    for ((str, key) in s2k) {
        if (string.continuesWith(str, index)) {
            return key to str.length
        }
    }
    throw Exception("Key parsing error")
}


/**
 * Scope for parsing chord modifications. We are parsing [fullString], currently we are at [index], the modifications
 * are applied to [chord]. And sometimes we need parse key, so we need [s2k].
 */
data class ParserScope(val fullString: String, var index: Int = 0, var chord: Chord, val s2k: String2Key)

/**
 * Parse modification of chord means some transformation of [ParserScope] and returning, if it was success
 * (something changed) or failure ([ParserScope] is unchanged).
 */
typealias ModificationParser = ParserScope.() -> Boolean

/**
 * Parses chord from [string] with modifications given by [parsers], and key naming given by [s2k].
 * Returns parsed chord and string holding this chord. (Substring of [string] without whitespaces.)
 * Not throw error if string wasn't parsed full, only if a key cannot be parsed.
 */
fun parseChord(parsers: List<ModificationParser>, string: String, s2k: String2Key = string2Key): Pair<Chord, String> {
    val stringMWhites = string.filterNot { it.isWhitespace() }
    val (key, length) = parseKey(stringMWhites, s2k)
    val state = ParserScope(stringMWhites, length, major(key), s2k)

    state.apply {
        while (state.index != state.fullString.length) {
            if (!parsers.any { it() }) break
        }
    }

    return state.chord to stringMWhites.substring(0, state.index)
}


/**
 * Major chord -> minor chord.
 */
fun Chord.makeMinor() {
    intervals[Interval.M3] = false
    intervals[Interval.m3] = true
}

/**
 * Diminish fifth. With [makeMinor], they are only operations that remove some interval and add another.
 */
fun Chord.makeDiminished5() {
    intervals[Interval.P5] = false
    intervals[Interval.TRITONE] = true
}

/**
 * Pattern for modification parser. It takes all possible ways to write the modification ([needles]). And if [ParserScope]
 * continues with some of [needles], it modifies [ParserScope] with [action] and 'moves' the index.
 */
fun ParserScope.parseAny(vararg needles: String, action: ParserScope.(String) -> Unit): Boolean {
    for (needle in needles) {
        if (fullString.continuesWith(needle, index)) {
            action(needle)
            index += needle.length
            return true
        }
    }
    return false
}

/**
 * Diminished chord.
 */
fun ParserScope.dim() = parseAny("dim7", "dim") {
    chord.makeMinor()
    chord.makeDiminished5()
    chord.intervals[Interval.M6] = true // TODO setting if dim is always bm7
}

/**
 * Dominant seventh chord.
 */
fun ParserScope.seventh() = parseAny("7") {
    chord.intervals[Interval.m7] = true
}

/**
 * Major seventh chord.
 */
fun ParserScope.maj7() = parseAny("maj7", "maj", "M7", "Δ", "⑦") {
    chord.intervals[Interval.M7] = true
}

/**
 * Minor chord.
 */
fun ParserScope.minor() = parseAny("moll", "mi", "m") {
    chord.makeMinor()
}

/**
 * Hack for sus2, sus4.
 */
fun ParserScope.sus() = parseAny("sus") {
    chord.intervals[Interval.M3] = false
    // chord.intervals[Interval.m3] = false // FIXME? (minor + sus is illegal combination)
}

/**
 * Chords with added major intervals.
 */
fun ParserScope.nth() = parseAny("2", "4", "6", "add9", "add11", "add13", "9", "11", "13") {
    when (it) {
        "2", "9", "add9" -> chord.intervals[Interval.M2] = true
        "4", "11", "add11" -> chord.intervals[Interval.P4] = true
        "6", "13", "add13" -> chord.intervals[Interval.M6] = true
    }
    if (it == "9" || it == "11" || it == "13") chord.intervals[Interval.m7] = true
//    if (it == "11" || it == "13") chord.intervals[Interval.M2] = true // FIXME This is not for guitar
//    if (it == "13") chord.intervals[Interval.P4] = true // FIXME This is not for guitar
}

// TODO "b5", "b9", "b11", "b13", "♭9", "♭11", "♭13", "#9, #11, #13", "♯9, ♯11, ♯13"

/**
 * Augmented fifth chord.
 */
fun ParserScope.aug() = parseAny("aug5", "aug", "+", "5+") {
    chord.intervals[Interval.P5] = false
    chord.intervals[Interval.m6] = true
}

/**
 * Diminished (only) fifth chord.
 */
fun ParserScope.dim5() = parseAny("dim5", "-", "5-") {
    chord.makeDiminished5()
}

/**
 * Stated inversion or added some bass (mainly for transition between chords).
 */
fun ParserScope.bass(): Boolean {
    if (!fullString.continuesWith("/", index)) return false
    try {
        val (key, indexShift) = parseKey(fullString, s2k, index + 1)
        index += indexShift + 1
        chord.bass = (key.transposition - chord.key.transposition).mod(12)
    } catch (_: Exception) {
        return false
    }

    chord.intervals[Interval.M7] = true
    return true
}


/**
 * Parse all modifications stated above.
 */
fun parseFull(string: String, s2k: String2Key = string2Key): Pair<Chord, String> = parseChord(
    listOf(
        ParserScope::dim5,
        ParserScope::dim,
        ParserScope::seventh,
        ParserScope::sus,
        ParserScope::nth,
        ParserScope::maj7,
        ParserScope::minor,
        ParserScope::aug,
        ParserScope::bass,
    ), string, s2k
)

/**
 * Parse from string but not modify chord.
 */
fun ParserScope.ignoreAll() = parseAny(
    "moll", "mi", "m", "maj7", "maj", "M7", "Δ", "⑦", "7", "dim5", "5-", "dim", "sus", "2", "4", "6", "9", "11", "13",
    "aug5", "5+",
    "aug", "+", "add9", "add11", "add13"//, "b5", "b9", "b11", "b13", "♭9", "♭11", "♭13", "#9, #11, #13", "♯9, ♯11, ♯13"
) {}

/**
 * Parse chord ignoring all advanced modifications.
 */
fun parseSimplified(string: String, s2k: String2Key = string2Key): Pair<Chord, String> = parseChord(
    listOf(
        ParserScope::dim,
        ParserScope::seventh,
        ParserScope::minor,
        ParserScope::bass,
        ParserScope::ignoreAll
    ), string, s2k
)