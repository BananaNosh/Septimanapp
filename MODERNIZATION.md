# Modernization notes

Self-contained notes for a **future, separate session**. This documents one specific modernization:
upgrading the build toolchain so the project's build/test tooling works with **modern Android
devices (API 35/36+)**. It is independent of any current feature work — do it on its own branch.

## Why (the concrete problem this fixes)

Running instrumented tests on a physical **Android 16 (API 36)** device fails, even though the tests
themselves pass:

- `./gradlew connectedDebugAndroidTest` (and the Android Studio ▶ on an instrumented test, which
  goes through the same Gradle/UTP path) fails with `There were failing tests`, and the XML report
  shows `tests="0"`. The real cause, from `--info`, is that **AGP 7.4.2's bundled `ddmlib` cannot
  talk to the API 36 device**:
  ```
  java.lang.IllegalAccessError: class com.google.protobuf.GeneratedMessageV3 tried to access
    method 'boolean com.google.protobuf.CodedInputStream.shouldDiscardUnknownFields()'
    at com.android.ddmlib.internal.DeviceClientMonitorTask$TrackAppProcessor.onMessage(...)
  ```
  A protobuf conflict in ddmlib's `track-app` process monitor crashes the UTP test driver. Fixed in
  the ddmlib that ships with **AGP 8.x**.
- Separately, **Espresso input injection is broken on API 35/36**: `Espresso.onView` / click / type
  crash with `NoSuchMethodException: android.hardware.input.InputManager.getInstance` (that static
  was removed). Even espresso 3.6.1 hits it. A modern espresso (3.6.1+ on a fixed AGP, or newer) is
  needed; otherwise instrumented tests must avoid Espresso.

Workaround used today (no green checkmark): verify via `adb shell am instrument` (see
`EnrolmentValidationTest`, which deliberately avoids Espresso). Modernizing removes the need for that.

## Current state (as of this writing)

| Thing | Version |
|---|---|
| Android Gradle Plugin | **7.4.2** (`build.gradle` buildscript classpath) |
| Gradle wrapper | **8.5** (`gradle/wrapper/gradle-wrapper.properties`) |
| Kotlin | **1.7.22** (`ext.kotlin_version` in `build.gradle`) |
| JDK (running Gradle) | **17** (already OK for AGP 8) |
| compileSdk / targetSdk / minSdk | 34 / 34 / 19 (`app/build.gradle`) |
| Dagger | 2.42 (kapt) |
| google-services plugin | 4.3.13 |
| firebase-crashlytics-gradle | 2.9.1 |
| firebase-bom | 30.2.0 |

App plugins: `com.android.application`, `kotlin-android`, **`kotlin-android-extensions`**,
`kotlin-kapt`, `com.google.firebase.crashlytics`, `com.google.gms.google-services`.

## The main blocker: `kotlin-android-extensions` (synthetics)

The `kotlin-android-extensions` Gradle plugin (the `kotlinx.android.synthetic.*` imports) was
**removed in Kotlin 1.8.0**, so it cannot survive a Kotlin bump that AGP 8 expects. It must be
removed and the synthetic view access migrated to **ViewBinding** (preferred) or `findViewById`.

Files using synthetics (4):
- `app/src/main/java/.../activity/MainActivity.kt`
- `app/src/main/java/.../fragments/HorariumFragment.kt`
- `app/src/main/java/.../fragments/EnrolmentFragment.kt`
- `app/src/main/java/.../fragments/MapFragment.kt`

Layouts they reach into (ViewBinding will generate a `*Binding` for each): `activity_main`,
`app_bar_main`, `content_main`, `nav_header_main`, `view_impressum`, `fragment_horarium`,
`fragment_enrolment`, `fragment_map`. Note several are `<include>`d/merged into `activity_main`
(app_bar_main, content_main, nav_header_main) and `view_impressum` is a nav-drawer header view —
ViewBinding handles includes via the generated child-binding fields.

## Target state

Pick the **current latest** at execution time, but as a guide:
- AGP **8.7+** (8.9.x ideal) — needed for ddmlib that supports API 36 devices.
- Gradle wrapper to match AGP (AGP 8.7→Gradle 8.9, 8.9→8.11). Bump
  `gradle/wrapper/gradle-wrapper.properties`.
- Kotlin **1.9.x+** (or 2.0.x) — required once synthetics are gone; align kapt or migrate to **KSP**
  for Dagger (`com.google.devtools.ksp`) which is faster and the modern path.
- `buildFeatures { viewBinding true }` in `app/build.gradle` (the commented placeholder is already
  there) — then delete `apply plugin: 'kotlin-android-extensions'`.
- Bump test libs to modern espresso/test if you want Espresso back (espresso `3.6.1`, test runner
  `1.6.2`, rules `1.6.1` are already set; ext.junit `1.2.1` if you reintroduce `AndroidJUnit4`).
- Bump `google-services` (→ 4.4.x), `firebase-crashlytics-gradle` (→ 3.x), `firebase-bom`
  (→ current) to versions compatible with AGP 8 / newer Gradle.

## Step-by-step

1. **Branch** off `master` (e.g. `chore/agp8-modernization`). Keep it isolated from feature work.
2. Move plugin classpaths to versions above (or migrate `build.gradle` buildscript to the
   `plugins {}` / version-catalog style — optional).
3. Enable `viewBinding`, remove the `kotlin-android-extensions` plugin line.
4. **Migrate the 4 files** off synthetics to ViewBinding:
   - Activities: `private lateinit var binding: ActivityMainBinding`; `binding =
     ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)`; replace
     `toolbar`/`drawer_layout`/`nav_view`/etc. with `binding.…`. Included layouts are nested
     bindings (`binding.appBarMain.toolbar`, etc. — exact field names depend on the `<include>`
     ids). The nav header / `view_impressum` is obtained from the `NavigationView` header view, not
     the activity binding — bind it with `NavHeaderMainBinding.bind(navView.getHeaderView(0))` (or
     `ViewImpressumBinding`), matching how the synthetic `nav_view.privacyTV` / `debugTV` are reached
     today.
   - Fragments: inflate the binding in `onCreateView`, use `binding.…` in `onViewCreated`, null it in
     `onDestroyView`.
   - `EnrolmentFragment` has the most field access (form inputs incl. the new room/age/student
     views) — do it carefully and re-run `EnrolmentValidationTest` afterward.
5. Bump Kotlin; align kapt or switch Dagger to KSP.
6. Fix AGP 8 breaking changes as they surface: non-transitive R classes
   (`android.nonTransitiveRClass`), `BuildConfig` now opt-in (`buildFeatures { buildConfig true }` —
   the app reads `BuildConfig.DEBUG`), removed `package` from manifest (already using `namespace`),
   any Kotlin API-level warnings-as-errors, deprecated `onBackPressed` (already deprecated in code).
7. **Resolve resources / lint**: pre-existing lint errors are unrelated (see the project memory:
   missing translations + a `nav_drawer_count_down_pattern` format mismatch).

## Verification (the whole point)

On the Android 16 device, after modernization, the green path must work without adb hacks:
```
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nobodysapps.septimanapp.EnrolmentValidationTest
```
should report the 2 tests passing (not `tests="0"`), and the same ▶ run should pass in Android
Studio. Also confirm a normal build + unit tests still pass:
```
./gradlew assembleDebug testDebugUnitTest lintDebug
```
(Expect the same pre-existing unit-test/lint failures noted in the project memory — nothing new.)
Once Espresso works again on the device, `EnrolmentValidationTest` may optionally be rewritten to use
Espresso instead of the manual UI-thread driving, though the current approach also works on modern
APIs and needs no Espresso.

## Compatibility quick-reference

- AGP ↔ Gradle minimums: 8.0→8.0, 8.2→8.2, 8.3→8.4, 8.5→8.7, 8.7→8.9, 8.9→8.11.
- AGP 8.x requires JDK 17 (have it).
- `kotlin-android-extensions` removed in Kotlin 1.8.0 → must migrate before/with the Kotlin bump.
