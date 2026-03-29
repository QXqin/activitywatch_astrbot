# ActivityWatch Android (AstrBot Edition)

> A heavily customized ActivityWatch Android client featuring a native Bottom Navigation Bar, built-in Remote Sycnhoronization Worker, and tailored exclusively for AstrBot monitoring integrations.

[English](./README.md) | [中文](./README_zh-CN.md)

![Kotlin](https://img.shields.io/badge/Kotlin-1.8+-purple)
![Android API](https://img.shields.io/badge/Android-API_21+-green)
![ActivityWatch](https://img.shields.io/badge/Based_On-ActivityWatch-blue)

## ✨ Custom Features

Compared to the official `aw-android`, this custom fork is specially adapted to connect closely with a standalone [AstrBot](https://github.com/Soulter/AstrBot) for 24/7 mobile app usage state monitoring:

- 🚀 **Background Remote Sync Worker**: Contains a WorkManager task to regularly sync collected raw heartbeats to a private ActivityWatch remote server (even internal Tailscale IP configurations).
- 👆 **Native Bottom Navigation Bar**: Brutally abandoned the original anti-human "swipeable drawer WebView" and instead baked an interactive, native Bottom Navigation component for instantaneous switching (Activity, Raw Data, AW Settings, Remote Sync).
- 🔄 **One-Tap Manual Sync & Status Tracker**: A dedicated settings Fragment for testing URL remote connections, observing "Last successful sync time", and a manual "Sync Now" button for instant debug payloads.
- 🇨🇳 **Bilingual Native Locale Support**: Features proper Android-native string extractions and Chinese localized string values `values-zh-rCN`.

## 📦 Installation & Usage

### Installing the Client 

You can simply deploy the APK from our CI builds, or you can compile it using Android Studio directly:
```bash
git clone https://github.com/QXqin/activitywatch_astrbot.git
```
*(Note: As with the official build, this requires recursive fetch of git submodules to compile the underlying `aw-server-rust` components.)*

### Remote Sync Configuration

1. Install this customized Android App and grant it required "Usage Access" system permissions.
2. Tap the bottom-right **[Remote Sync]** section on the Bottom Navigation Bar.
3. Provide your server's ActivityWatch REST API address (make sure to include `http://` and the port, e.g. `http://100.100.100.100:5600`).
4. Toggle **Enable Remote Sync**.
5. (Optional) Tap **Sync Now** manually to test output logs. If success, your Android device will periodically push App-Usage heartbeats flawlessly into your custom remote server.

## 🤖 Automate with AstrBot

To leverage this fork inside IM platforms (QQ, Discord, etc):
1. Navigate to your AstrBot Bot server configuration.
2. Install the data-cleanup proxy plugin: [astrbot_plugin_activitywatch](https://github.com/QXqin/astrbot_plugin_activitywatch)
3. Use `/aw config <address>` to securely query your real-time phone usage logs!

## 📄 License

Inherited under the original [MPL-2.0](./LICENSE) from the creators of the ActivityWatch project.
