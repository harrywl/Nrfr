# Nrfr Android 16 Fork

[简体中文](README.md) | [English](README_EN.md)

免 Root 的 SIM 运营商配置覆盖工具。这个仓库 fork 自 [Ackites/Nrfr](https://github.com/Ackites/Nrfr)，当前分支重点适配 Android 16，并保留 Apache-2.0 许可证和原项目归属说明。

原项目 README 已归档到 [docs/upstream/README.md](docs/upstream/README.md)。

## 主要变化

- 适配 Android 16 对 `CarrierConfigManager.overrideConfig` 的 shell 调用限制。
- 使用主 APK + helper APK 的双 APK 模式执行 instrumentation。
- 通过 Shizuku 启动 shell，再由 instrumentation 采用 `MODIFY_PHONE_STATE` 权限写入运营商覆盖配置。
- 支持展示 SIM1/SIM2 当前覆盖配置，并标记已覆盖状态。
- 将保存/还原操作放到后台线程，避免 UI 卡顿或 ANR。
- 将长选择列表改为底部弹层和懒加载列表，降低首次打开选择框的卡顿。
- 新增设置页，首页右上角齿轮可进入语言和关于选项。
- 默认跟随手机系统语言，也可在应用内切换简体中文、繁體中文、English、日本語、한국어或 Español。

## 语言设置

应用首次安装后默认跟随手机系统语言。点击首页右上角齿轮，进入“设置 → 语言”即可手动切换；选择结果会保存在本机并立即生效。

语言选择列表中的语言名称始终使用各自原文显示，不会随当前界面语言翻译，避免用户无法识别目标语言。不在支持列表中的系统语言会回退为 English。

“关于”页面位于“设置 → 关于”，继续复用原有项目说明、维护信息和开源链接。

## 安装方式

推荐使用桌面客户端安装。用户只需要在客户端里点击一次安装，客户端会自动安装：

- `nrfr.apk`
- `nrfr-instrumentation-target.apk`

手动安装 Release APK 时，需要两个 APK 都安装。只安装 `nrfr.apk` 可以打开应用，但保存/还原运营商配置会因为缺少 helper APK 而失败。

helper APK 的包名是 `com.github.nrfr.instrumentationtarget`，没有桌面图标，正常情况下用户不需要直接打开它。

## 使用前提

- Android 8 及以上。
- Android 16 建议使用本 fork 的双 APK 版本。
- 手机端安装并启用 Shizuku。
- 通过 USB 调试或 Wi-Fi 调试完成 Shizuku 授权。

## 构建

这个仓库没有提交 `gradlew` 脚本。可使用本机 Gradle，或先生成 wrapper。

构建 Android 双 APK：

```bash
gradle wrapper
./gradlew :app:assembleDebug :instrumentation-target:assembleDebug
```

产物位置：

- 主应用：`app/build/outputs/apk/debug/app-debug.apk`
- helper：`instrumentation-target/build/outputs/apk/debug/instrumentation-target-debug.apk`

构建桌面客户端前需要先构建前端：

```bash
cd nrfr-client/frontend
npm install
npm run build

cd ..
wails build
```

## 发布说明

Release ZIP 中应包含：

- `resources/nrfr.apk`
- `resources/nrfr-instrumentation-target.apk`
- `resources/shizuku.apk`
- `platform-tools/`

GitHub Release 单独 APK 资源也应同时上传：

- `nrfr-<version>.apk`
- `nrfr-instrumentation-target-<version>.apk`

## 合规说明

本仓库是 [Ackites/Nrfr](https://github.com/Ackites/Nrfr) 的派生版本。原项目基于 Apache-2.0 许可证发布，本仓库保留 [LICENSE](LICENSE)，并在修改过的主要文件中标注了 fork 变更说明。

本 fork 不是上游官方版本。如需查看上游文档和原始项目信息，请阅读 [docs/upstream/README.md](docs/upstream/README.md)。

## 免责声明

本工具仅供学习和研究使用。修改运营商配置可能影响设备网络、漫游、运营商功能或区域识别行为。请自行确认风险并自行承担后果。
