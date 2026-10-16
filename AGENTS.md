# Pet Rap Sheet: notes for SDLC workers

A native Android delight toy: pick a pet photo and get a deadpan police booking pack (mugshot,
charge sheet, court summons). The product rules below are the ones that matter for every task.

## Stack

- Kotlin and Jetpack Compose in a single activity (`app.petrapsheet.MainActivity`), with minSdk 26 and compileSdk/targetSdk 37.
- Gradle wrapper, with versions in `gradle/libs.versions.toml`. AGP 9 has built-in Kotlin, so there is no
  `kotlin-android` plugin. Only the Compose compiler plugin is applied.
- Tests are JVM-only: JUnit 4, Robolectric and Roborazzi, under `app/src/test/`.
- Toolchain at fixed paths: JDK 21 at `~/.local/jdk-21`, Android SDK at `~/.local/android-sdk`.
  `scripts/env.sh` exports `JAVA_HOME`, `ANDROID_HOME` and `PATH` for them. Don't add
  `local.properties`; it is git-ignored.

## Build and test

```sh
bash scripts/check.sh               # the SDLC check: must pass before review
bash scripts/record-screenshots.sh  # re-record Roborazzi reference PNGs
```

`scripts/check.sh` runs
`./gradlew --no-daemon --console=plain testDebugUnitTest verifyRoborazziDebug lintDebug assembleDebug`.
That is unit tests, screenshot *verification* against the committed PNGs, lint with errors
fatal, and a debug APK. It takes about 40–60 s warm and about 70 s in a fresh clone (the
Gradle caches in `~/.gradle` are shared). The first build after adding a dependency is slower. For a
quicker loop while you work, source `scripts/env.sh` and run a single Gradle task, e.g.
`./gradlew testDebugUnitTest --tests '*Manifest*'`.

- **Screenshots.** References live in `app/src/test/screenshots/`. Name them by the test's
  `package.Class.method`, using `captureRoboImage()` with no path. If you change the UI on purpose,
  run `scripts/record-screenshots.sh` and look at the new PNGs before you commit them. Never
  re-record just to make a failing check pass. On a mismatch, Roborazzi writes `*_compare.png` and
  `*_actual.png` diffs, which are git-ignored. Screenshot tests need
  `@GraphicsMode(GraphicsMode.Mode.NATIVE)` and a fixed `@Config(qualifiers = ...)`.
- **Robolectric on SDK 37** needs the `--add-opens` JVM args in `app/build.gradle.kts` and
  Espresso ≥ 3.7, which is pinned in the test dependencies. Keep both.
- **Known flake.** Once, on the very first build (cold dex cache), the Gradle daemon JVM
  crashed with SIGSEGV inside R8 (`Gradle build daemon disappeared unexpectedly`, plus an
  `hs_err_pid*.log`). That's a JVM/JIT crash, not your code. Re-run the check. If it happens again
  and again, report it rather than changing build settings.
- **CPU.** Agent work shares the machine. `gradle.properties` caps Gradle at 4 workers. Don't
  run Gradle and the emulator at the same time, and don't run the emulator alongside other heavy work.
- **Don't** commit anything under `build/` or `smoke/`, and don't commit `local.properties` or any keystore.

## Emulator smoke test (optional evidence, not part of the check)

This needs a writable `/dev/kvm` (check with `test -w /dev/kvm`). Build first, then:

```sh
bash scripts/check.sh
bash scripts/emulator-smoke.sh   # boots AVD prs-pixel headless, installs, launches,
                                 # writes smoke/screen.png, shuts the emulator down
```

The script runs `emulator -avd prs-pixel -no-window -no-audio -gpu swiftshader_indirect` and
waits for `sys.boot_completed=1`. Booting takes a minute or two. Attach the screenshot as
evidence when a task changes visible UI. If KVM isn't available, say so in your report and
rely on the Roborazzi screenshots.

## Product rules (from the brief, and not negotiable)

- **Privacy.**
  - No `INTERNET` permission, ever. `ManifestPermissionsTest` fails if the merged manifest
    requests it, including from a library.
  - No analytics, ads, accounts, backend or network calls. Everything is generated on the device.
  - Pick photos with the Android Photo Picker, so there's no storage permission. If you add
    camera capture, use a camera intent, not the `CAMERA` permission.
  - Share through a FileProvider and the system share sheet. Save through MediaStore.
  - Cloud backup and device transfer are switched off (`allowBackup=false` and
    `data_extraction_rules.xml`).
  - The About screen says "Nothing leaves your phone."
- **Parody safety.**
  - Every rendered document carries a small "Parody. Not an official document." line.
  - Seals, badges and agency names are invented. Never use real police, state or municipal
    insignia or names.
  - Charges are only naughty, harmless pet behaviour. Nothing about abuse, real crimes against
    people, slurs or anything cruel.
  - Bundle only fonts under the OFL or Apache licence, and commit the licence text with each font.
- **Identity.** Never put the owner's name, email or account usernames in code, metadata, signing
  certs or network requests. Where an org, author or vendor string is needed, use
  `pet-rap-sheet`.

## Sensitive paths

Changes to `.sdlc.json`, `scripts/`, `app/src/main/AndroidManifest.xml` or `gradle/` get a
security review. Keep such changes minimal and explain them.
