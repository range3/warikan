# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

"warikan" (割り勘, bill-splitting) is a single-module Android app (`:app`, package `com.range3.warikan`) for splitting a drinking-party bill with seniority-based slopes. It is an early prototype iterated by trying builds on a real phone. **`docs/SPEC.md` is the living spec** — read it first and append one-line decisions there instead of writing ADRs.

- `domain/` — pure Kotlin, `@Serializable` model (`Bill`, `Group`, `Share.Fixed`/`Share.Sloped`, `Prepaid`, `Preset`, `DefaultPresets`), `Bill.calculate()` and `Bill.switchShare()`. All money is `Long` yen with exact integer rounding (sloped base kept as a fraction). Covered by `CalculatorTest`.
- `data/AppStore` persists the whole `SavedState` (bill + presets) as one JSON string in Preferences DataStore. Decode failures fall back to defaults (no migrations).
- `ui/WarikanViewModel` holds state as Compose `mutableStateOf`, saves on every change once `loaded`; it takes a nullable `AppStore` so previews can use `WarikanViewModel()`. Two screens (`WarikanScreen`, `SettingsScreen`) switched by a boolean in `MainActivity` + `BackHandler` — no Navigation library. No DI.
- Icons are local vector drawables in `res/drawable` (material3 1.4 no longer pulls in `material-icons`).
- Launcher icon: adaptive icon only (`mipmap-anydpi`, no legacy PNGs since minSdk 34). Foreground is `drawable-nodpi/ic_launcher_foreground.png` cut from `art/icon/app_icon_source.jpg` by `art/icon/make_icon.ps1`; background is the `ic_launcher_background` color; `ic_launcher_monochrome.xml` is a hand-made vector of the same coin stacks for themed icons.

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

## Releases

APKs are distributed via GitHub Releases. Pushing a tag `vMAJOR.MINOR.PATCH` (on the `android` branch) runs `.github/workflows/release.yml`: unit tests → `assembleRelease` → `gh release create` with `warikan-<tag>.apk` attached. `versionName`/`versionCode` come from the tag (`-PversionName`, `-PversionCode = MAJOR*10000 + MINOR*100 + PATCH`); local builds default to `dev`/`1`.

Release signing is configured only when `SIGNING_STORE_FILE` (+ `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`) env vars are set; CI gets them from repo secrets (`SIGNING_KEYSTORE_BASE64` holds the keystore). To smoke-test an R8 release build locally, point those vars at `~/.android/debug.keystore` (`android` / `androiddebugkey`).

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
