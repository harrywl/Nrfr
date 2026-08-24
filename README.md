# Nrfr

[简体中文](README.md) | [English](README_EN.md)

基于 Shizuku 的免 Root SIM 卡运营商修改工具。

A Shizuku-powered, root-free SIM carrier configuration tool.

## 功能

- 通过主应用和 helper 双 APK 适配 Android 16。
- 使用 Shizuku 获取 shell 能力并写入运营商覆盖配置。
- 查看和管理 SIM1、SIM2 的当前配置及覆盖状态。
- 默认跟随系统语言，支持简体中文、繁體中文、English、日本語、한국어和 Español。
- 在“设置 → 语言”中切换语言；语言名称始终以各自原文显示。

### 更新（v1.0.4）

- 新增设置页面和应用内语言切换，默认跟随系统语言。
- 主应用与 helper 统一版本及本地 Release 签名配置。
- 稳定构建工具链，补齐 Gradle Wrapper 和基础单元测试。
- 删除桌面客户端，使用 `v*` 标签自动构建并发布双 APK。
- 完整内容见 [更新日志](CHANGELOG.md)。

## 安装

需要 Android 8 或更高版本，并已安装、启用 [Shizuku](https://github.com/RikkaApps/Shizuku)。

从 [Releases](https://github.com/baiyanwu/Nrfr/releases) 下载同一版本的两个 APK：

- `nrfr-<version>.apk`
- `nrfr-instrumentation-target-<version>.apk`

通过 ADB 安装时，先安装 helper：

```bash
adb install -r nrfr-instrumentation-target-v1.0.4.apk
adb install -r nrfr-v1.0.4.apk
```

两个 APK 缺一不可。helper 包名为 `com.github.nrfr.instrumentationtarget`，没有桌面图标，无需单独打开。

## 构建

需要 JDK 17 和 Gradle 8.9：

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :instrumentation-target:assembleDebug
```

产物位置：

- `app/build/outputs/apk/debug/app-debug.apk`
- `instrumentation-target/build/outputs/apk/debug/instrumentation-target-debug.apk`

## 发布

[Build Release](.github/workflows/build.yml) 在推送 `v*` 标签时自动测试、打包、生成更新日志和 SHA-256 校验文件，并创建对应的 GitHub Release：

```bash
git tag v1.0.4
git push origin v1.0.4
```

也可以在 GitHub Actions 中手动填写版本号并选择是否标记为预发布。每次发布包含：

- `nrfr-<version>.apk`
- `nrfr-instrumentation-target-<version>.apk`
- `SHA256SUMS.txt`

当前自动发布的是 Debug 签名 APK；正式签名需要另行配置签名密钥和 GitHub Secrets。

## 开源与免责
本仓库 fork 自 [Ackites/Nrfr](https://github.com/Ackites/Nrfr)，重点适配 Android 16，移除桌面客户端。

本项目遵循 [Apache-2.0 License](LICENSE)，保留原项目归属说明，但不是上游官方版本。

本工具仅供学习和研究。修改运营商配置可能影响网络、漫游及运营商功能，使用者需自行承担风险。
