# Migration to targetSdk 37 (Android 17)

October 2026: `compileSdk`/`targetSdk` raised from 35 to 37. This file records what had to change, and
why, and which of the platform's behaviour changes were checked and found not to apply.

## Toolchain

compileSdk 37 requires **AGP 9.1.1+**, which pulls in a major toolchain jump:

| Component | Before | After | Note |
|---|---|---|---|
| Android Gradle Plugin | 8.10.1 | 9.4.1 | AGP 9 compiles Kotlin itself ("built-in Kotlin") |
| Gradle wrapper | 8.11.1 | 9.6.0 | minimum for AGP 9.4 |
| Kotlin | 1.9.24 | 2.4.20 | `kotlin-android` plugin and `kotlinOptions` removed (AGP 9) |
| KSP | 1.9.24-1.0.20 | 2.3.12 | KSP2; versioned independently of Kotlin now |
| Dagger | 2.51.1 | 2.60.1 | KSP2 support |
| google-services plugin | 4.3.13 | 4.4.4 | AGP 9 new-DSL compatible |
| Crashlytics plugin | 2.9.1 | 3.0.8 | AGP 9 new-DSL compatible |

## Unit tests

- **Robolectric 4.14.1 → 4.17**, the first release supporting SDK 37. Robolectric runs the tests against
  the targetSdk, and the SDK 36/37 framework jars need **Java 21**. The build still runs on JDK 17;
  only the test JVM is launched on a Java 21 toolchain (`tasks.withType(Test)` in `app/build.gradle`,
  auto-provisioned via the foojay resolver in `settings.gradle`), with the `--add-opens` flags from
  <https://robolectric.org/getting-started/>.
- **Mockito 4.6.1 → 5.24.0**: the ByteBuddy bundled with 4.6.1 cannot instrument classes on Java 21
  ("Could not modify all classes"). `mockito-inline` is gone; Mockito 5 is inline by default.
- CI (`.github/workflows/android.yml`) now sets up JDK 21.

## App code: predictive back (targetSdk 36+)

Apps targeting Android 16+ get predictive back by default: a back gesture **no longer calls
`Activity.onBackPressed()`** and no longer dispatches `KEYCODE_BACK`. Lint reports this as the error
`GestureBackNavigation`.

`MainActivity.onBackPressed()` used to (1) close the open drawer, (2) pop the fragment back stack and
re-check the matching drawer item, (3) otherwise finish. Without changes, (1) would have closed the app
instead of the drawer (DrawerLayout 1.1.1 has no back handling of its own), and (2) would still pop —
the FragmentManager has its own back callback — but leave the wrong drawer item checked. Now:

- `closeDrawerOnBack` (an `OnBackPressedCallback`) closes the drawer; it is enabled only while the drawer is
  open (drawer listener, plus a sync in `onPostCreate` for a drawer restored open after rotation) and is
  registered after the FragmentManager's callback, so it wins.
- The back-stack listener derives the checked drawer item from the fragment now shown
  (`NAV_ITEM_FRAGMENTS`), instead of from the popped entry's name.
- `SettingsActivity`'s Up button calls `onBackPressedDispatcher.onBackPressed()`.

Covered by `MainActivityBackTest` (Robolectric, SDK 37).

## Behaviour changes checked and not applicable

Android 16 (API 36):
- *Edge-to-edge opt-out removed* — the app was already made edge-to-edge for targetSdk 35 and never used
  `windowOptOutEdgeToEdgeEnforcement`.
- *Orientation/resizability/aspect-ratio ignored on ≥600dp screens* — no such restrictions in the manifest
  and no `setRequestedOrientation()`.
- *`elegantTextHeight` ignored*, *`scheduleAtFixedRate` catch-up*, health permissions, Bluetooth bond
  intents, MediaStore version, local-network permission (opt-in) — not used.

Android 17 (API 37):
- *Static final fields unmodifiable via reflection*, *lock-free `MessageQueue`* — no such reflection in the
  app; libraries can only be verified on an Android 17 device.
- *Certificate Transparency and ECH on by default* — all endpoints (OSM tiles, Firebase) are public
  HTTPS hosts with CT-logged certificates.
- *Large-screen orientation opt-out removed* — nothing to opt out of (see above).
- Local network permission, background audio, background activity starts, native `System.load`, contacts
  provider, SMS OTP, widget memory limit, Content Capture, Bluetooth RFCOMM — not used.

Alarms use plain `AlarmManager.set()` and notifications already handle `POST_NOTIFICATIONS`; neither
changed in 36/37.

## Cleanups done along the way

- Removed the `multidex` dependency (`minSdk ≥ 21` needs none). This exposed that `MainActivity` imported
  **`androidx.multidex.BuildConfig`** instead of the app's own, so `BuildConfig.DEBUG` was always false and
  the debug label in the impressum never appeared in debug builds. It now does.
- Gradle 10 deprecations fixed: `prop value` → `prop = value` syntax, and the root `ext` property read from
  inside `allprojects` (implicit parent-project lookup).
- Removed the unused `kapt.incremental.apt` property and the redundant `kotlin-stdlib-jdk8` dependency.

## Deliberately left for later

- `android.enableJetifier=true` and `android.nonFinalResIds=false` are deprecated (removed in AGP 10).
  Jetifier may still be needed by the old Week-View fork / osmdroid; test before dropping it.
- Deprecated API usages (`setHasOptionsMenu`, `Locale(String)`, `getSerializableExtra`, …) only warn.
- Several dependencies (Firebase BoM 30.2.0, AndroidX core/appcompat, Gson, …) have newer versions;
  remember the `lifecycle_version` force-pin in the root `build.gradle` when bumping AndroidX.
