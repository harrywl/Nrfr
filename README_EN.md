# Nrfr Android 16 Fork

[简体中文](README.md) | [English](README_EN.md)

A root-free tool for overriding SIM carrier configuration. This repository is forked from [Ackites/Nrfr](https://github.com/Ackites/Nrfr). The current branch focuses on Android 16 compatibility while retaining the Apache-2.0 License and attribution to the original project.

The upstream README is archived at [docs/upstream/README.md](docs/upstream/README.md).

## Major changes

- Adapted to Android 16 restrictions on shell calls to `CarrierConfigManager.overrideConfig`.
- Uses a dual-APK architecture consisting of the main APK and a helper APK that runs instrumentation.
- Starts a shell through Shizuku, then writes carrier overrides from instrumentation with the `MODIFY_PHONE_STATE` permission.
- Displays the current override configuration for SIM 1 and SIM 2 and marks active overrides.
- Runs save and restore operations on background threads to avoid UI stalls and ANRs.
- Uses bottom sheets and lazy lists for long selection lists to reduce the delay when opening them for the first time.
- Adds a Settings screen. The gear icon in the top-right corner of the home screen opens the Language and About options.
- Follows the phone's system language by default and supports manual switching between 简体中文, 繁體中文, English, 日本語, 한국어, and Español.

## Language settings

After a fresh installation, the app follows the phone's system language by default. Tap the gear icon in the top-right corner of the home screen, then open **Settings → Language** to choose a language manually. The selection is saved locally and takes effect immediately.

Language names in the picker are always shown as autonyms rather than being translated into the current UI language, so users can recognize the language they want to select. System languages outside the supported list fall back to English.

The existing project description, maintenance information, and open-source links remain available under **Settings → About**.

## Installation

Installing through the desktop client is recommended. Users only need to click Install once; the client automatically installs:

- `nrfr.apk`
- `nrfr-instrumentation-target.apk`

Both APKs are required for a manual release installation. Installing only `nrfr.apk` allows the app to open, but saving or restoring carrier configuration fails because the helper APK is missing.

The helper package is named `com.github.nrfr.instrumentationtarget`. It has no launcher icon and normally does not need to be opened directly.

## Requirements

- Android 8 or later.
- The dual-APK build from this fork is recommended for Android 16.
- Shizuku must be installed and running on the phone.
- Shizuku authorization must be completed through USB debugging or wireless debugging.

## Building

This repository does not include the `gradlew` script. Use a locally installed Gradle distribution or generate the wrapper first.

Build both Android APKs:

```bash
gradle wrapper
./gradlew :app:assembleDebug :instrumentation-target:assembleDebug
```

Output paths:

- Main application: `app/build/outputs/apk/debug/app-debug.apk`
- Helper: `instrumentation-target/build/outputs/apk/debug/instrumentation-target-debug.apk`

Build the frontend before building the desktop client:

```bash
cd nrfr-client/frontend
npm install
npm run build

cd ..
wails build
```

## Release packaging

The release ZIP should contain:

- `resources/nrfr.apk`
- `resources/nrfr-instrumentation-target.apk`
- `resources/shizuku.apk`
- `platform-tools/`

Standalone APK assets uploaded to a GitHub Release should include both:

- `nrfr-<version>.apk`
- `nrfr-instrumentation-target-<version>.apk`

## Compliance and attribution

This repository is a derivative of [Ackites/Nrfr](https://github.com/Ackites/Nrfr). The upstream project is released under the Apache-2.0 License. This repository retains the [LICENSE](LICENSE) file and identifies fork-specific changes in the main modified source files.

This fork is not an official upstream release. See [docs/upstream/README.md](docs/upstream/README.md) for the upstream documentation and original project information.

## Disclaimer

This tool is intended for learning and research. Changing carrier configuration may affect network behavior, roaming, carrier-specific features, or regional detection. Review the risks before applying changes; you are responsible for the outcome.
