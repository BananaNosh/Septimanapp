# Septimanapp — Future Extensions & Technical Backlog

Ideas for future development, collected during a full app review (July 2026). Roughly ordered by
impact within each section.

## Feature extensions

### Remote content updates (highest impact)
Fetch `horarium_<year>_<locale>.json`, `proposita_<year>.json`, and the event info (dates,
location) from a static HTTPS URL — even GitHub Pages suffices — with the bundled assets as
offline fallback. This removes the biggest maintenance burden: today every content change
requires editing `SeptimanappApplication.storeEventInfo()`, bumping `versionCode`, and shipping
a release (see CLAUDE.md "Content loading from assets"). With remote content, schedule fixes
during the event week become possible. A minimal first step without any server logic: move the
hardcoded event dates/location into an `event_info.json` asset so a new year is data-only.

### Personal schedule
Let users star horarium events and their chosen proposita, with an optional notification before
each starred event. The infrastructure exists: `AlarmScheduler` + `NotificationHelper` already
schedule reminder notifications, and `HorariumView` renders per-event UI.

### Organizer push announcements
Firebase is already wired (analytics + crashlytics). Adding Firebase Cloud Messaging would let
organizers push announcements ("room change", "bus leaves at 9:00") during the event week —
low effort, high value on site.

### Enrolment over HTTPS
Enrolment is currently a plaintext `ACTION_SEND` email handoff to a hardcoded address
(`EnrolmentFragment.sendEnrolment()`), German-only template. A direct HTTPS POST (or a hosted
form) would give control over transport security, allow confirmation feedback, and remove the
dependency on the user's mail app. Short of that: add non-German email templates and disclose
the email data flow in a privacy notice.

### Offline map tiles
Rural venues often have poor connectivity. osmdroid supports pre-cached tile archives; bundling
or pre-downloading tiles for the venue area would make the map reliable on site.

### More horarium languages
The app UI supports `la`, `de`, `en`, `fr`, but horarium content exists only for `la`/`de`
(`ALLOWED_HORARIUM_LOCALES`). An English horarium would widen the audience.

### Play Store readiness
A privacy policy and an accurate Data Safety declaration (the enrolment form collects PII
including allergen/health data), an R8-shrunk release build, and CI-built release artifacts.

## Technical backlog

- **Enable R8** in release (`minifyEnabled true`). Prerequisite: `-keep` rules for Gson model
  classes and especially the WeekView fork — the horarium JSON serializes the library's private
  field names (`mId`, `mStartTime`, …), so obfuscating them breaks deserialization silently.
- **Decouple storage JSON from the WeekView fork**: introduce an app-owned event DTO mapped to
  `WeekViewEvent` at the view boundary. Unblocks R8 and any future calendar-widget replacement;
  the JitPack fork (`BananaNosh:Android-Week-View`) is unmaintained.
- **Extract an `EnrolmentViewModel`**: `EnrolmentFragment` (~600 lines) is the only screen
  without one; validation logic exists twice (`EnrolInformation.isValid()` vs.
  `missingFieldLabels()`) and must be kept in sync by hand — derive both from one field list.
- **Migrate `LocaleHelper` to `AppCompatDelegate.setApplicationLocales`** (appcompat ≥ 1.6),
  replacing the custom `attachBaseContext`/`recreate()` machinery. Latin ("la") is a valid
  language tag; the custom Latin `DateFormatSymbols` in `LocalizationHelper` stays.
- **Replace the home-grown permission framework** in `SeptimanappActivity` with
  `ActivityResultContracts.RequestPermission` (also fixes the open "never ask again" TODO).
- **`onBackPressed()` → `OnBackPressedDispatcher`** in `MainActivity`.
- **Security-crypto deprecation**: enrolment data uses
  `androidx.security:security-crypto` (EncryptedSharedPreferences), which Google has deprecated
  without a direct replacement. It works and is widely used; if it breaks on a future Android
  version, migrate to a small Keystore-backed AES-GCM wrapper.
- **Build hygiene**: Kotlin 2.x + version catalog (`libs.versions.toml`); drop
  `androidx.legacy:legacy-support-v4` and Jetifier; remove the leftover `kapt.incremental.apt`
  property and vestigial Maven repos (spring/jboss/jenkins) from the root `build.gradle`;
  enable `android.nonTransitiveRClass`.
- **Move storage I/O off the main thread**: the first-run asset import parses all years'
  horaria synchronously in `Application.onCreate`, and the enrolment form writes prefs on every
  keystroke — move the import to a background dispatcher and debounce the autosave.
- **Known latent bug**: `HorariumView` computes day spans via `DAY_OF_YEAR` diff, which breaks
  across a year boundary (noted in the file itself).
- **More tests**: the enrol-reminder state machine (`AlarmReceiver`,
  `NotificationActionReceiver`, enrol states in `EnrolInformationStorage`) is the most
  regression-prone untested logic.
