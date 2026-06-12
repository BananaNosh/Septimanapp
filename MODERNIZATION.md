# Modernization notes

**Status: the build-toolchain modernization described here is DONE** (landed in the build). This file
is now a record of what was done, plus the one remaining caveat for running instrumented tests on
very new Android devices.

## Done — current toolchain

| Thing | Version |
|---|---|
| Android Gradle Plugin | **8.10.1** |
| Gradle wrapper | **8.11.1** |
| Kotlin | **1.9.24** |
| JDK | 17 |
| Dagger processing | **KSP** (`com.google.devtools.ksp` 1.9.24-1.0.20), no longer kapt |
| compileSdk / targetSdk / minSdk | 34 / 34 / 19 |

- **ViewBinding** is enabled (`buildFeatures { viewBinding = true; buildConfig = true }`). The
  `kotlin-android-extensions` plugin and every `kotlinx.android.synthetic.*` usage were removed —
  `MainActivity`, `HorariumFragment`, `EnrolmentFragment`, `MapFragment` now use `binding.…`.
- Plugin resolution moved to `pluginManagement` in `settings.gradle`; `androidx.lifecycle` pinned to
  2.5.1 via a resolution strategy in the root `build.gradle`.

`./gradlew assembleDebug`, `testDebugUnitTest`, and `assembleDebugAndroidTest` all build.

## Instrumented tests on Android 16 (API 36) devices

The old AGP 7.4.2 ddmlib `IllegalAccessError` (protobuf `track-app`) that broke
`connectedAndroidTest` is **fixed** by AGP 8.10.1. On the physical Pixel 7 (Android 16):

- `./gradlew connectedDebugAndroidTest` (and the Android Studio ▶, which routes through it) now
  **runs green** on the device (verified: `Starting 2 tests / Finished 2 tests / BUILD SUCCESSFUL`).
  An occasional flaky `Process crashed` / `0 tests` can happen on the first launch — just re-run.
- Running directly via adb also works as a check:
  ```
  ./gradlew installDebug installDebugAndroidTest
  adb shell am instrument -w -r -e class com.nobodysapps.septimanapp.EnrolmentValidationTest \
    com.nobodysapps.septimanapp.test/androidx.test.runner.AndroidJUnitRunner
  ```
  → `OK (2 tests)`.
- **Espresso is still intentionally avoided**: its input injection (`InputManager.getInstance`)
  crashes on API 35/36, so `EnrolmentValidationTest` drives views on the UI thread instead. The
  `androidTest` deps use runner 1.6.2 / rules 1.6.1 with no `espresso-core`.

## Possible further modernization (optional, not required)

- Move dependencies to a Gradle **version catalog** (`gradle/libs.versions.toml`) — an IDE shelf
  attempted this but it was not landed.
- Bump `google-services` (4.3.13) / `firebase-crashlytics-gradle` (2.9.1) / `firebase-bom` (30.2.0)
  to current.
- Re-introduce Espresso once a version handles API 36 input injection, and re-check whether
  `connectedAndroidTest` runs on the device (or just rely on an emulator in CI).
