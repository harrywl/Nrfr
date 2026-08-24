# Nrfr

[简体中文](README.md) | [English](README_EN.md)

A Shizuku-powered, root-free SIM carrier configuration tool.

## Features

- Supports Android 16 through a main APK and a helper APK.
- Uses Shizuku to obtain shell capabilities and apply carrier overrides.
- Displays and manages the current configuration and override state for SIM 1 and SIM 2.
- Follows the system language by default and supports 简体中文, 繁體中文, English, 日本語, 한국어, and Español.
- Languages can be changed under **Settings → Language**; language names are always displayed in their native form.

### Update (v1.0.4)

- Added a Settings page and in-app language switching, with the system language used by default.
- Unified versioning and local Release signing for the main app and helper.
- Stabilized the build toolchain and added the complete Gradle Wrapper and basic unit tests.
- Removed the desktop client and added automatic dual-APK releases triggered by `v*` tags.
- See the [changelog](CHANGELOG.md) for complete details.

## Installation

Android 8 or later is required. [Shizuku](https://github.com/RikkaApps/Shizuku) must also be installed and running.

Download both APKs for the same version from [Releases](https://github.com/baiyanwu/Nrfr/releases):

- `nrfr-<version>.apk`
- `nrfr-instrumentation-target-<version>.apk`

When using ADB, install the helper first:

```bash
adb install -r nrfr-instrumentation-target-v1.0.4.apk
adb install -r nrfr-v1.0.4.apk
```

Both APKs are required. The helper package is `com.github.nrfr.instrumentationtarget`; it has no launcher icon and does not need to be opened directly.

## Building

JDK 17 and Gradle 8.9 are required:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :instrumentation-target:assembleDebug
```

Outputs:

- `app/build/outputs/apk/debug/app-debug.apk`
- `instrumentation-target/build/outputs/apk/debug/instrumentation-target-debug.apk`

## Publishing

[Build Release](.github/workflows/build.yml) runs when a `v*` tag is pushed. It tests and packages the app, generates release notes and SHA-256 checksums, and creates the corresponding GitHub Release:

```bash
git tag v1.0.4
git push origin v1.0.4
```

The workflow can also be started manually with a version number and an optional prerelease flag. Each release contains:

- `nrfr-<version>.apk`
- `nrfr-instrumentation-target-<version>.apk`
- `SHA256SUMS.txt`

The automated workflow currently publishes Debug-signed APKs. Formal release signing requires a signing key and additional GitHub Secrets configuration.

## License and disclaimer

This repository is forked from [Ackites/Nrfr](https://github.com/Ackites/Nrfr), focuses on Android 16 compatibility, and removes the desktop client.

This project follows the [Apache-2.0 License](LICENSE) and retains upstream attribution, but it is not an official upstream release.

This tool is intended for learning and research. Carrier overrides may affect network behavior, roaming, and carrier features. Use it at your own risk.
