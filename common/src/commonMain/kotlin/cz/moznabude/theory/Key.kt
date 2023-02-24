package cz.moznabude.theory

enum class Key(val transposition: Int) {
    C(0),
    G(7), D(2), A(9), E(4), B(11), Fs(6), Cs(1), Gs(8), Ds(3), As(10), Es(5),
    F(-7), Bb(-2), Eb(-9), Ab(-4), Db(-11), Gb(-6), Cb(-1), Fb(-8), Bbb(-3), Ebb(-10), Abb(-5);
}

typealias String2Key = List<Pair<String, Key>>

private val unicodeS2K: String2Key =
    Key.values().map { it.toString().replace("s", "♯").replace("b", "♭") to it }

private val nonUnicodeS2K: String2Key =
    Key.values().map { it.toString().replace("s", "#") to it }.filterNot { it.first.length == 1 }

val string2Key: String2Key = (unicodeS2K + nonUnicodeS2K).sortedByDescending { it.first.length }

private fun Key.toStringWithH() = if (this == Key.B) "H" else this.toString()

private val unicodeS2KWithH: String2Key =
    Key.values().map { it.toStringWithH().replace("s", "♯").replace("b", "♭") to it }

private val nonUnicodeS2KWithH: String2Key =
    Key.values().map { it.toStringWithH().replace("s", "#") to it }.filterNot { it.first.length == 1 }

val string2KeyWithH: String2Key = (unicodeS2K + nonUnicodeS2K + ("B" to Key.Bb)).sortedByDescending { it.first.length }
