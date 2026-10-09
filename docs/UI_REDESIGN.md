# NekoBox Android 原生界面改版

基于 MatsuriDayo/NekoBoxForAndroid commit `5768494d8ae3c74a057bb6d46c0f8dc071b0d821`（1.4.2）修改。

## 已实现的首版范围

- Kotlin/XML 原生界面，没有嵌入网页。
- 首页上方显示连接卡片：连接状态、开关、真实代理上传/下载速率。点击卡片调用既有连接测试；测试失败后恢复状态并显示错误。
- 首页、分组、路由、设置四个底部导航入口，接入既有 Fragment。抽屉保留日志、工具等入口。保存并恢复当前目的页面。
- 原有 VPN 授权 Activity、服务启动/停止、节点切换与重载、订阅及路由后端继续使用。
- 浅灰背景、平面圆角节点行、蓝色操作入口、统一次级文字，以及明暗配色。
- 原有主题色编号不变；增加末尾蓝色选项作为新版主题。全新安装默认使用新版主题，老用户可在主题颜色选择器选择最后一个蓝色选项。
- 节点选择 Activity 隐藏连接卡片。移除依赖悬浮按钮的滚动处理及失效的旧底栏设置。

本轮未重做全部协议编辑表单、订阅编辑页和高级设置，也未添加原型中的新功能。底部第三项为项目现有的「路由」，不是演示原型的「连接记录」。

## 构建

准备 JDK 17、Android SDK 35、Build Tools 35.0.1、NDK 25.0.8775105、Go 1.25；需要网络下载 Gradle 和依赖。核心版本仍由仓库 `buildScript/lib/core/get_source_env.sh` 锁定。

```bash
# 先配置 ANDROID_HOME 与 local.properties 的 sdk.dir
./run lib core
./run init action gradle
./gradlew :app:assembleOssDebug
```

首条命令生成 `app/libs/libcore.aar`，第二条准备规则资源。APK 输出在 `app/build/outputs/apk/oss/debug/`。Debug 包名使用项目现有 debug 后缀，可与正式版本并存。

也可将此仓库放在自己的 GitHub 仓库，手动运行新增的 `UI Debug Build` workflow，下载 `nekobox-ui-debug` artifact。该工作流尚未实际执行验证。

## 当前验证结果与限制

- XML 解析、项目资源引用、移除的 ViewBinding 引用、git diff 空白检查已完成。
- 本地 `:app:assembleOssDebug` 未完成：下载 Gradle 8.10.2 时返回 `Network is unreachable`。当前环境也没有 Android SDK、NDK 和预编译 libcore.aar。
- 因此没有可交付的 APK，也没有通过 Kotlin 编译、Android Lint、模拟器或真机验证。

## 编译后需要验证

1. 首次 VPN 授权的同意与拒绝；连接、连接中取消、停止中状态、失败及服务重连。
2. 前后台切换与实时速率更新，测试进行时切换节点或断开连接。
3. 四个底部入口、抽屉扩展入口、返回键、旋转及进程恢复。
4. 节点导入、订阅更新、节点选择 Activity、编辑/分享操作与运行中节点编辑限制。
5. Android 21/23/26/35 的系统栏、手势导航、深色模式、320dp 屏幕和大字体布局。

## 许可证

沿用上游 GPL-3.0 许可证；再分发时保留上游许可与声明并提供相应源码。此项目与 Shadowrocket 开发者无关联，未使用其图标或商标资产。


## ByteFlow usability update (source only)

- Home: persistent connection state, selected server name, separate test feedback,
  and secondary upload/download rates. Server names refresh after selection and edits.
- Servers: checkmark selection, bold names with two-line truncation, aligned short
  latency labels, clipped secondary text, and 48dp info/more buttons. Sharing and
  deletion live in the more menu; active-server deletion remains disabled.
- Navigation: four bottom destinations; logs, tools, dashboard and About are opened
  from Settings. Secondary pages return to Settings and keep its tab highlighted.
- Settings: connection, routing, subscription management, appearance, collapsed
  advanced options, and tools/information. Existing keys and defaults are preserved.
- Validation: XML parsing, preference attribute/key preservation, local resource
  reference checks and whitespace checks. No APK compilation, cloud build, or device
  validation performed for this update, per user instruction.


## Shadowrocket-inspired refinement (source only)

- Condensed home controls: switch/status, one-line current server, combined rates,
  and an explicit test menu. The menu separates active-connection URL testing,
  whole-group TCP latency, whole-group URL testing, and latency sorting.
- Retains tab/swipe group navigation; each group's servers now share a white list
  surface with thin dividers and a collapsible group heading. Heading displays
  visible server count and actual subscription update time when available.
- Toolbar has one + entry for QR, clipboard, subscription, file and manual adds.
  Search and existing group utilities remain accessible inside its menu.
- Add subscription opens group creation with subscription type preselected.
- Group collapse state survives recreation; list listeners are released when the
  group's view is destroyed.
- No builds or device validation were run for these changes.
