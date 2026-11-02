# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

"warikan" (割り勘, bill-splitting) is a single-module Android app (`:app`, package `com.range3.warikan`) for splitting a drinking-party bill with seniority-based slopes. It is an early prototype iterated by trying builds on a real phone. **`docs/SPEC.md` is the living spec** — read it first and append one-line decisions there instead of writing ADRs.

- `domain/` — pure Kotlin model (`Bill`, `Group`, `Share.Fixed`/`Share.Sloped`, `Prepaid`) and `Bill.calculate()`; all money is `Long` yen with integer ceil math. Covered by `CalculatorTest`.
- `ui/WarikanViewModel` holds the `Bill` as Compose state and derives the result; `ui/WarikanScreen` is the single screen. No navigation, persistence, or DI yet.
- Icons are local vector drawables in `res/drawable` (material3 1.4 no longer pulls in `material-icons`).

## Commands

Run from the repo root (Windows: `./gradlew` works in Git Bash; `gradlew.bat` in PowerShell).

```sh
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew testDebugUnitTest             # JVM unit tests (app/src/test)
./gradlew connectedDebugAndroidTest     # instrumented tests (app/src/androidTest), needs a device
./gradlew lintDebug                     # Android Lint

# single unit test class / method
./gradlew testDebugUnitTest --tests "com.range3.warikan.ExampleUnitTest"
./gradlew testDebugUnitTest --tests "com.range3.warikan.ExampleUnitTest.addition_isCorrect"

# single instrumented test class
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.range3.warikan.ExampleInstrumentedTest
```

## Build setup (non-obvious points)

- **Very new toolchain**: AGP 9.5, Gradle 9.6, Kotlin 2.2, compileSdk/targetSdk 37, **minSdk 34**. The Gradle daemon JVM is Java 25 (`gradle/gradle-daemon-jvm.properties`, auto-provisioned via the foojay resolver); app bytecode targets Java 11.
- **AGP built-in Kotlin**: there is no `org.jetbrains.kotlin.android` plugin — AGP 9 compiles Kotlin itself. Only the Compose compiler plugin (`kotlin-compose`) is applied. Don't add the Kotlin Android plugin back.
- **New AGP DSL**: `compileSdk { version = release(37) }` and `buildTypes.release.optimization { enable = true; packageScope = ... }` replace the old `compileSdk = 37` / `isMinifyEnabled` / `proguardFiles(...)`. R8 keep rules live in `app/src/main/keepRules/` (all files there are merged), not `proguard-rules.pro`.
- Dependencies and plugin versions are managed only through the version catalog `gradle/libs.versions.toml`; Compose library versions come from the Compose BOM. Repositories are declared centrally in `settings.gradle.kts` (`FAIL_ON_PROJECT_REPOS`).
- Configuration cache is enabled (`gradle.properties`), so build logic must be configuration-cache compatible.

## UI

Jetpack Compose + Material 3 only (no XML layouts). `ui/theme/WarikanTheme` uses dynamic color on Android 12+, which is always true given minSdk 34, so the static `DarkColorScheme`/`LightColorScheme` only apply when called with `dynamicColor = false`.

## Repository conventions

- GitHub repo `range3/warikan` is shared with an older **web** version of the app. Its `main` (and `v1`) branches are the web app; this native Android app lives on the **`android` branch, an orphan branch with unrelated history**. Treat `android` as the base branch here: branch from it, open PRs against it (`gh pr create --base android`), and never merge/rebase/PR between `android` and `main`.
- `adb` is not on PATH; use `<sdk.dir from local.properties>/platform-tools/adb` (e.g. `adb devices -l` to check the connected phone before `installDebug` / `connectedDebugAndroidTest`).

- `.gitattributes` normalizes to LF; `*.bat` is CRLF and `*.jar` is binary.
- Device-specific IDE files (`.idea/deviceManager.xml`, `.idea/deploymentTargetSelector.xml`), `.kotlin/`, and `local.properties` are git-ignored.
