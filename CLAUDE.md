# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Septimanapp is an Android app for participants of the [European Latin Week](https://www.lateinwochen.de/)
("Septimana Latina Europaea"). It shows the event schedule (horarium), a map of locations, an
enrolment flow, and a countdown to the next event. The app is multilingual (Latin `la`, German `de`,
English `en`, French `fr`); Latin and German are the only languages with full horarium content.

## Build & Test

The project uses the Gradle wrapper (`./gradlew`). It is Java/Kotlin Android, **not** the Compose stack.

```bash
./gradlew assembleDebug              # build debug APK
./gradlew test                       # all JVM unit tests (Robolectric)
./gradlew testDebugUnitTest          # debug-variant unit tests only
./gradlew connectedAndroidTest       # instrumented tests (needs device/emulator)
./gradlew lint                       # Android lint

# Run a single unit test class or method:
./gradlew testDebugUnitTest --tests "com.nobodysapps.septimanapp.JsonConverterTest"
./gradlew testDebugUnitTest --tests "*.MainViewModelTest.someMethod"
```

Unit tests use **Robolectric** + Mockito and run on the JVM (`includeAndroidResources = true`), so most
"Android" logic is testable without a device. Note `kapt.incremental.apt=true` and the `--add-opens`
JVM args in `gradle.properties` are required for Dagger annotation processing on modern JDKs.

`google-services.json` (Firebase) is required for the build to succeed. It is checked into git, so
it is present automatically in fresh clones and `git worktree` checkouts.

### Git worktrees
`local.properties` is **gitignored** and does not propagate to a new `git worktree`. It holds the
Android SDK path (`sdk.dir`), so without it Gradle fails with `SDK location not found`. After creating
a worktree, copy it from the main checkout before building:

```bash
git worktree add /tmp/septimana-wt HEAD
cp local.properties /tmp/septimana-wt/local.properties   # only gitignored file the build needs
```

(`google-services.json` is tracked, so it carries over and does **not** need copying.)

## Architecture

MVVM with manual Dagger 2 dependency injection. There is a single `:app` module.

### Dependency injection (Dagger 2 + dagger-android)
- `SeptimanappApplication.onCreate()` builds `DaggerSeptimanappApplicationComponent`, passing
  `ContextModule(applicationContext)` and `SharedPreferencesModule()`, then `inject(this)`.
- `SeptimanappApplicationComponent` wires all modules. Activities/Fragments/Receivers are injected via
  `@ContributesAndroidInjector` (see `SeptimanappApplicationModule.kt`) and call `AndroidInjection.inject(this)`.
- **ViewModels** are provided through a multibinding map (`ViewModelModule`): each ViewModel is bound
  `@IntoMap @ViewModelKey(...)`, and `ViewModelFactory` resolves the right `Provider` by class. To add a
  ViewModel, add an `@IntoMap @ViewModelKey(...)` provider method in `ViewModelModule`.

### Persistence (no database)
All app state lives in **SharedPreferences**, serialized to JSON with Gson via `JsonConverter`.
- `*Storage` classes (`HorariumStorage`, `LocationStorage`, `EventInfoStorage`, `EnrolInformationStorage`)
  each own a set of preference keys and read/write JSON blobs. Keys are namespaced by year and/or locale
  (e.g. `horarium_2025_la`, `locations_braunfels`).
- `StorageManager.resetAfterSeptimana()` clears per-event state when the stored event year changes.
- Enrolment data (PII) lives in a separate **EncryptedSharedPreferences** file (`enrol_prefs`,
  provided via the `@EnrolPrefs` Dagger qualifier and excluded from backups); `EnrolPrefsMigration`
  moves legacy plaintext values over on first start.

### Content loading from assets
Horaria and locations are bundled as JSON in `app/src/main/assets/` and imported into SharedPreferences
**once per app version** (`SeptimanappApplication.doOnFirstStartOfVersion()`, gated by `VERSION_ALREADY_RUN_ON`).
- Horarium files follow the naming convention `horarium_<year>_<locale>.json` (only locales in
  `ALLOWED_HORARIUM_LOCALES` = `la`, `de` are imported). The `.doc`/`.docx`/`.pdf` source documents
  live in `docs/horarium-sources/` (kept out of `assets/` so they don't ship in the APK).
- Location files follow `locations_<septimanaLocation>.json`, matched to a `SeptimanaLocation` enum key.
- **The event date and location are hardcoded** in `SeptimanappApplication.storeEventInfo()`. Updating to a
  new year means adding `horarium_<year>_*.json` assets, bumping `versionCode`/`versionName` in
  `app/build.gradle` (so the first-run import re-triggers), and editing `storeEventInfo()`.

### UI
- `SeptimanappActivity` is the base `AppCompatActivity`: it applies the persisted locale via `LocaleHelper`
  in `attachBaseContext`, handles a small permission-request framework (`withPermission` / `PermissionListener`),
  and shows the language-choice dialog on first run.
- `MainActivity` hosts the navigation drawer and swaps fragments (`HorariumFragment`, `EnrolmentFragment`,
  `MapFragment`). The map uses **osmdroid** (OpenStreetMap), not Google Maps.
- View binding is via the legacy **kotlin-android-extensions synthetics** (`import kotlinx.android.synthetic...`),
  not ViewBinding/Compose. Match this pattern when editing existing screens.

### Localization
`LocaleHelper` (an `object`) persists and applies the chosen language independently of system settings;
`SettingsActivity.SettingsFragment.KEY_PREF_LANGUAGE` is the preference key. The horarium has its own
language toggle (`HorariumViewModel.horariumLanguage`) separate from the app UI locale, defaulting to
Latin unless the device locale is German.

### Notifications
`AlarmScheduler` registers `AlarmManager` alarms that fire `AlarmReceiver`; `NotificationHelper` builds
the channel and notifications; `NotificationActionReceiver` handles action buttons. Enrolment reminders
are scheduled in `SeptimanappApplication.setupReminder()` at offsets defined by
`NotificationHelper.ENROL_REMINDER_TIMES` relative to the event start.

## Conventions
- Kotlin official code style (`kotlin.code.style=official`).
- `minSdkVersion 23`, `targetSdkVersion 35`; multidex is enabled.
- The third-party `Android-Week-View` fork and `osmdroid` come from JitPack/extra Maven repos declared in
  the root `build.gradle`.
