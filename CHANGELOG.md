# 更新日志 / Changelog

本文件记录 Nrfr 各版本的重要变更。

This file records notable changes for each Nrfr release.

## v1.0.4 — 2026-08-24

### 中文

#### 新增

- 新增设置页面，提供语言和关于入口。
- 新增应用内语言切换，默认跟随系统，并支持简体中文、繁體中文、English、日本語、한국어和 Español；语言选择器中的名称始终以各自原文显示。
- 新增本地 Release 签名配置，主应用与 helper 共用 `local.properties` 中的签名信息，签名文件不会提交到仓库。
- 补齐 Gradle Wrapper，并增加运营商配置解析、Instrumentation 结果解析、Shell 参数转义和预设数据校验等基础单元测试。

#### 改进

- 主应用与 helper 共用版本号，当前同步为 `1.0.4`（versionCode `4`）。
- 将 AGP 从 `8.7.0-rc01` 升级到稳定版 `8.7.0`，使用 Gradle `8.9`，并将 Compose BOM 升级到稳定版 `2024.09.03`。
- 改进 Shizuku 监听器的注册和移除逻辑，完善运营商配置操作的错误提示与多语言显示。
- 删除桌面客户端及相关内容，项目仅保留 Android 主应用和 helper。
- 发布流程改为推送 `v*` 标签后自动测试、打包两个 APK、生成更新说明与 SHA-256 校验文件，并创建 GitHub Release。

### English

#### Added

- Added a Settings page with Language and About entries.
- Added in-app language switching. The app follows the system language by default and supports 简体中文, 繁體中文, English, 日本語, 한국어, and Español. Language names are always shown in their native form.
- Added local Release signing configuration shared by the main app and helper through `local.properties`; signing files remain outside version control.
- Added the complete Gradle Wrapper and basic unit tests for carrier configuration parsing, instrumentation result parsing, shell argument quoting, and preset-data validation.

#### Improved

- Synchronized the main app and helper at version `1.0.4` (versionCode `4`).
- Upgraded AGP from `8.7.0-rc01` to stable `8.7.0`, retained Gradle `8.9`, and upgraded the Compose BOM to stable `2024.09.03`.
- Improved Shizuku listener registration and removal, with clearer and fully localized carrier-configuration errors.
- Removed the desktop client and related content; the project now contains only the Android app and helper.
- Updated publishing so a pushed `v*` tag runs tests, builds both APKs, generates release notes and SHA-256 checksums, and creates a GitHub Release.
