# Akorditko ('Chord maker')
TODO


## Building and development
The project is written in [Kotlin](https://kotlinlang.org/) for it is multiplatform (so akorditko can run in JS, Android, and on desktop)
and has a 'multiplatform' UI framework [Compose](https://www.jetbrains.com/lp/compose-mpp/) (so we needn't write it three times).

The project is built by Gradle (its [configuration](build.gradle.kts) is written in Kotlin). Build needs JDK18 (desktop + Gradle), JDK1.8 (Android), Android SDK (API 33)
and many others, which Gradle installs automatically.

TODO build.

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
  3. And then we show this to user with "[Compose](https://www.jetbrains.com/lp/compose-mpp/) magic", which is only part 
  of the project, which is not (entirely) in the [module common](common/src/commonMain/kotlin/cz/moznabude/akorditko).

### Compose thing
Currently, [Compose](https://www.jetbrains.com/lp/compose-mpp/) is implemented for Android+desktop and separately
for the web. So UI must be written two times. Moreover, Android and desktop need other configurations (Activities and so on).

Therefore, UI for Android and desktop is implemented in the [module common](common/src/commonMain/kotlin/cz/moznabude/akorditko/App.kt)
because I hope that one day it will work for JS too, and its configuration for Android is in the [module android](android), and
the entry point for desktop is in the [module desktop](desktop). JS has its implementation of UI in the [module js](js).