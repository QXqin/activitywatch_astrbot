# ActivityWatch Android (AstrBot 定制版)

> 一个深度定制的 ActivityWatch 安卓客户端，移除了繁琐的抽屉式网页交互，新增真正的原生底部导航栏并特别加入了“常驻远程同步”功能。

[English](./README.md) | [中文](./README_zh-CN.md)

![Kotlin](https://img.shields.io/badge/Kotlin-1.8+-purple)
![Android API](https://img.shields.io/badge/Android-API_21+-green)
![ActivityWatch](https://img.shields.io/badge/Based_On-ActivityWatch-blue)

## ✨ 定制特性 (What's New)

相比官方原始的 `aw-android`，本定制版专门为了配套 [AstrBot](https://github.com/Soulter/AstrBot) 获取全天候的手机活动状态而进行了大幅魔改：

- 🚀 **全新构建的 Remote Sync (远程同步)**：包含后台常驻 Worker 服务，每 15 分钟稳定将本地应用使用记录（Heartbeats）自动同步推送至私有的 ActivityWatch 远端服务器（支持 Tailscale 内网等 IP 配置）。
- 👆 **真正的原生 Bottom Navigation (底栏导航)**：暴力抛弃了官方原本反人类的“左滑隐藏式抽屉”与“全局 WebView”方案，在 Android 侧注入了原生的底部栏（活动记录、原始数据、AW 设置、远程同步），切换流畅不卡顿。
- 🔄 **立即同步与状态可视**：专有的远程相关配置页（Remote Sync），支持一键“修改上报服务器地址”、显示“上一次成功同步完成期”及提供临时“立即同步”排误按钮。
- 🇨🇳 **原生中文化资源**：专门注入了对应的中文化界面与原生的 Android Strings 资源适配。

## 📦 如何安装与使用

### 安装客户端

您可以直接通过此项目构建生成的 APK 进行安装，或者克隆代码仓库使用 Android Studio 进行构建：
```bash
git clone https://github.com/QXqin/activitywatch_astrbot.git
```
*(注意：需要拉取相关的 git submodules 编译底层的 `aw-server-rust`，此处与官方构建流程保持一致。)*

### 配置远程同步

1. 安装并进入此 Android App 后，通过系统获取各种“使用情况访问权限（Usage Access）”。
2. 在底部原生导航栏点击右下角的 **【远程同步】**。
3. 输入您部署在服务器上的 ActivityWatch REST API 地址（带上 http 协议头及端口，如 `http://100.100.100.100:5600`）。
4. 勾选 **开启远程同步 (Enable Remote Sync)**。
5. （可选）点击下方新增的 **立即同步 (Sync Now)** 并观察提示气泡，如果提示成功且日志未报错，说明网络连通。自此手机将自动在后台源源不断地上报其使用数据。

## 🤖 联动 AstrBot 插件使用

如果想要在 QQ 等机器人平台上查询：
1. 请进入您的 AstrBot 机器人部署环境。
2. 安装专门的对接数据清洗插件：[astrbot_plugin_activitywatch](https://github.com/QXqin/astrbot_plugin_activitywatch)
3. 在机器人内 `/aw config <服务器地址>` 即可开始查询！

## 📄 许可证

本项目继承原版 [MPL-2.0](./LICENSE) 许可证，感谢原作者团队。
