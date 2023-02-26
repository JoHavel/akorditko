package cz.moznabude

import cz.moznabude.theory.*

fun String.continuesWith(str: String, index: Int): Boolean {
    if (this.length < index + str.length) return false
    for (i in str.indices) {
        if (this[index + i] != str[i]) return false
    }
    return true
}

fun parseKey(string: String, s2k: String2Key = string2Key, index: Int = 0): Pair<Key, Int> {
    for ((str, key) in s2k) {
        if (string.continuesWith(str, index)) {
            return key to str.length
        }
    }
    throw Exception("Key parsing error")
}




data class ParserScope(val fullString: String, var index: Int = 0, var chord: Chord, val s2k: String2Key)
typealias ModificationParser = ParserScope.() -> Boolean

fun parseChord(parsers: List<ModificationParser>, string: String, s2k: String2Key = string2Key): Pair<Chord, Int> {
    val stringMWhites = string.filterNot { it.isWhitespace() }
    val (key, length) = parseKey(stringMWhites, s2k)
    val state = ParserScope(stringMWhites, length, major(key), s2k)

    state.apply {
        while (state.index != state.fullString.length) {
            if (!parsers.any { it() }) break
        }
    }

    return state.chord to state.index
}




fun Chord.makeMinor() {
    intervals[Interval.M3] = false
    intervals[Interval.m3] = true
}

fun Chord.makeAugmented() {
    intervals[Interval.P5] = false
    intervals[Interval.TRITONE] = true
}

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

fun ParserScope.dim() = parseAny("dim") {
    chord.makeMinor()
    chord.makeAugmented()
    chord.intervals[Interval.m7] = true // TODO setting if dim is always 7
}

fun ParserScope.seventh() = parseAny("7") {
    chord.intervals[Interval.m7] = true
}

fun ParserScope.maj7() = parseAny("maj7", "maj", "M7", "Δ", "⑦") {
    chord.intervals[Interval.M7] = true
}

fun ParserScope.minor() = parseAny("moll", "mi", "m") {
    chord.makeMinor()
}

// sus on next line is hack for sus2, sus4
fun ParserScope.sus() = parseAny("sus") {
    chord.intervals[Interval.M3] = false
    // chord.intervals[Interval.m3] = false // FIXME? (minor + sus is illegal combination)
}

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

fun ParserScope.aug() = parseAny("aug5", "aug", "+") {
    chord.makeAugmented()
}

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



fun parseFull(string: String, s2k: String2Key = string2Key): Pair<Chord, Int> = parseChord(
    listOf(
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

fun ParserScope.ignoreAll() = parseAny(
    "moll", "mi", "m", "maj7", "maj", "M7", "Δ", "⑦", "7", "dim", "sus", "2", "4", "6", "9", "11", "13", "aug5",
    "aug", "+", "add9", "add11", "add13"//, "b5", "b9", "b11", "b13", "♭9", "♭11", "♭13", "#9, #11, #13", "♯9, ♯11, ♯13"
) {}

fun parseSimplified(string: String, s2k: String2Key = string2Key): Pair<Chord, Int> = parseChord(
    listOf(
        ParserScope::dim,
        ParserScope::seventh,
        ParserScope::minor,
        ParserScope::bass,
        ParserScope::ignoreAll
    ), string, s2k
)