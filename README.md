# Akorditko ('Chord maker')
Application for showing all possible ways to play a given chord on guitar (or ukulele).

## Running
- 🌐 Web version is running at <https://moznabude.cz/akorditko>.
- 📱Android application (.apk) is at [GitHub](https://github.com/JoHavel/akorditko/releases/download/v2.0.0/akorditko.apk).
- 💻🪟 On Windows app can be installed with [installer from GitHub](https://github.com/JoHavel/akorditko/releases/download/v2.0.0/Akorditko-2.0.0.msi).
- 💻🐧 I haven't compiled an installer for Linux yet.
- 🌐 Web version can be embedded to any HTML by adding [this .js](https://github.com/JoHavel/akorditko/releases/download/v2.0.0/js.js) and inserting `div` with `id="akorditko"` as in [.html](https://github.com/JoHavel/akorditko/releases/download/v2.0.0/index.html). Settings can be removed with CSS: `#akorditkoSettings {display: none;}`, but currently, there is no easy way to set defaults. 

### Manual
- For getting fingering, input the chord in the text field and fingerings will be shown in the standard way (gray strings are skipped ones; dots are fingers; empty black strings are played but not "pressed" by the left hand).
    - Under the text field, there is a currently parsed chord.
    - `♯` can be written as `#` and `♭` as `b` (`is` or `es` is not supported).
    - If you find some chord that the app can't parse, don't hesitate to create [issue](https://github.com/JoHavel/akorditko/issues) or write to jonas.havelka at moznabude.cz
- Barre chords are now supported!
- Other instruments (like ukulele or guitar with nonstandard tuning) are now supported! (Only plucked string instrument.)
  - The buttons on the left side. After clicking `Other`, there is an editable list: strings from left to right, 0 = [Middle C](https://en.wikipedia.org/wiki/C_(musical_note)#Middle_C), ±1 means ±1 semi-tone. After clicking `Back`, the button returns instead of the editable list.
- German nomenclature where `B` is written as `H` is now supported!
  - The selector at right top of the app.
- Support of a "partial from the right" barre chords or skipped strings in the middle or at the right is not planned.  
- Support of minor chord written as small key (`c` for `Cmi` etc.) is not planned. (First letter is automatically converted to uppercase.)

## Building and development
The project is written in [Kotlin](https://kotlinlang.org/) for it is multiplatform (so akorditko can run in JS, Android, and on desktop)
and has a 'multiplatform' UI framework [Compose](https://www.jetbrains.com/lp/compose-mpp/) (so we needn't write it three times).

The project is built by Gradle (its [configuration](build.gradle.kts) is written in Kotlin). Build needs JDK18 (desktop + Gradle), JDK1.8 (Android), Android SDK (API 33)
and many others, which Gradle installs automatically.

### Compile and run
`gradlew run` for desktop, `gradlew jsBrowserRun` for JS. Android request some IDE (or you can assemble .apk with `gradlew assembleDebug` and install `android/build/outputs/apk/debug/android-debug.apk` manually.)

### Compile and release
`gradlew assemble` assembles JS (basic html + one JS) and android (apk), result are:
```
android/build/outputs/apk/release/android-release-unsigned.apk

js/build/distributions/index.html
js/build/distributions/js.js
```

`gradlew desktop:packageReleaseMsi` or `gradlew desktop:packageReleaseDeb` assembles desktop installer package, found in
```
desktop/build/compose/binaries/main-release/
```

### Parts
Most of the project is in the [module common](common/src/commonMain/kotlin/cz/moznabude/akorditko), which implements things
regardless of compilation (same code for JS, JVM, and JVM on Android).

- Some used music theory converted to [code](common/src/commonMain/kotlin/cz/moznabude/akorditko/theory), including
  - [keys](common/src/commonMain/kotlin/cz/moznabude/akorditko/theory/Key.kt) and their relative positions (in semitones) to key C;
  - [intervals](common/src/commonMain/kotlin/cz/moznabude/akorditko/theory/Interval.kt) denoted by number of semitones;
  - [chords](common/src/commonMain/kotlin/cz/moznabude/akorditko/theory/Chord.kt) represented as a set of intervals, key, and bass (inversion),
  the idea is: this representation of the chord allows us to parse all modifications of the chord regardless of key. 
- Machinery for converting string to a visual representation of chord for guitar (ukulele, etc.).
  1. We need to convert the string to our representation of the chord for which we have [parser](common/src/commonMain/kotlin/cz/moznabude/akorditko/parseChord.kt).
  2. From our representation we construct all possible ways to play this chord. For this, we have 'Engines', for example [this one](common/src/commonMain/kotlin/cz/moznabude/akorditko/FretEngine.kt) for plucked string instruments.
     - [Fret engine](common/src/commonMain/kotlin/cz/moznabude/akorditko/FretEngine.kt): Firstly we get all possible "positions of finger" (tones) of the chord. From those, we create all "fingerings" for the chord that uses fingers only in range of 3 frets. Then we filter out unplayable and left only reasonable fingerings divided to groups: normal, barre, with wrong bass and barre with wrong bass.
  3. And then we show this to user with "[Compose](https://www.jetbrains.com/lp/compose-mpp/) magic", which is only part 
  of the project, which is not (entirely) in the [module common](common/src/commonMain/kotlin/cz/moznabude/akorditko).

### Compose thing
Currently, [Compose](https://www.jetbrains.com/lp/compose-mpp/) is implemented for Android+desktop and separately
for the web. So UI must be written two times. Moreover, Android and desktop need other configurations (Activities and so on).

Therefore, UI for Android and desktop is implemented in the [module common](common/src/commonMain/kotlin/cz/moznabude/akorditko/App.kt)
because I hope that one day it will work for JS too, and its configuration for Android is in the [module android](android), and
the entry point for desktop is in the [module desktop](desktop). JS has its implementation of UI in the [module js](js), the 'copy' of Android and desktop GUI written for JS is in [module js](js/src/jsMain/kotlin/cz/moznabude/akorditko/App.kt) too.
