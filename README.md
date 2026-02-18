# VibeForgeLauncher

A lightweight, privacy-focused Android home screen launcher with biometric app locking and a plugin system built on AIDL.

---

## Features

### App Grid
- Displays all installed apps in a scrollable 4-column grid
- Sorted alphabetically for easy navigation
- Tap to launch any app instantly

### App Locking (Biometric)
- Lock any app with a long press → "Lock App"
- Locked apps require fingerprint or face authentication before opening
- Lock state is stored locally using Room database — never leaves your device
- Unlock just as easily via the context menu

### Plugin System (AIDL)
- Supports third-party plugins via Android Interface Definition Language (AIDL)
- Each plugin exposes: name, version, memory usage, and custom actions
- Plugins dashboard shows all active plugins and their live stats
- Plugin architecture is open — developers can build and sideload their own

### System Memory Monitor
- Plugins dashboard displays real-time available system RAM
- Helps identify memory-heavy plugins instantly

### Minimal UI
- Material 3 design with DayNight theme support
- Translucent/transparent window background for a clean look
- Bottom dock with quick-access buttons
- No bloat, no ads, no tracking

---

## Download

Get the latest APK from the [Releases](https://github.com/elschuyler/New/releases) page.

> ⚠️ This is a **debug build**. You may need to enable "Install from unknown sources" in your Android settings.

---

## Requirements

- Android 8.0 (API 26) or higher
- Biometric hardware (fingerprint/face) required for app locking feature

---

## Tech Stack

- **Language:** Kotlin
- **Database:** Room (local, on-device only)
- **Biometric:** AndroidX Biometric library
- **Plugin IPC:** AIDL (Android Interface Definition Language)
- **UI:** Material 3, RecyclerView, GridLayoutManager
- **Build:** Gradle 8.4, AGP 8.1.1

---

## Security, Safety & Privacy

### ✅ What's safe
- All data (app lock states) stored **locally on-device** using Room database
- No internet permissions declared — zero network access
- No analytics, telemetry, or crash reporting
- Biometric authentication uses Android's secure hardware enclave
- Plugin communication is sandboxed via Android's AIDL IPC mechanism

### ⚠️ Known Risks & Recommendations

**1. Debug APK (High Priority)**
The released APK is a debug build, which means it is signed with a debug key and has debugging enabled. For production use, build a release APK signed with a private keystore.

**2. Plugin Trust (Medium)**
The AIDL plugin system binds to any app that implements `IVibeforgePlugin`. A malicious app could register itself as a plugin and receive `onPluginAction` calls with arbitrary data. Recommendation: add a plugin allowlist or signature verification before binding.

**3. No Input Validation on Plugin Actions (Medium)**
The `onPluginAction(String action, Bundle extras)` AIDL method passes a Bundle to plugins with no validation. A malicious plugin could exploit this. Recommendation: sanitize all action strings and bundle contents before passing.

**4. App List Access (Low)**
The launcher queries all installed apps via `queryIntentActivities`. This is standard for launchers but means the app has full visibility into what is installed on the device. This data never leaves the device.

**5. Room Database Not Encrypted (Low)**
The lock state database is unencrypted by default. On a rooted device, this data could be read. Recommendation: use SQLCipher or EncryptedSharedPreferences for sensitive lock state data if targeting high-security use cases.

**6. Biometric Bypass Risk (Low)**
If a user's device has no biometric hardware or it fails, the fallback behavior should be explicitly defined (e.g. PIN fallback). Currently, failure handling redirects away without a PIN option, which could confuse users.

---

## Project Structure

```
VibeForgeLauncher(w)/
  app/
    src/main/
      aidl/com/vibeforge/launcher/
        IVibeforgePlugin.aidl       # Plugin interface
      java/com/vibeforge/launcher/
        core/
          AppLockManager.kt         # Lock/unlock logic
          PluginManager.kt          # AIDL plugin binding
        data/
          AppLockDao.kt             # Room DAO + Database
        model/
          AppModel.kt               # App data model
        ui/
          LauncherActivity.kt       # Main launcher screen
          PluginsActivity.kt        # Plugin dashboard
          BiometricLockActivity.kt  # Biometric auth screen
      res/
        layout/                     # UI layouts
        values/                     # Themes, strings
  build.gradle
  settings.gradle
  gradle.properties
.github/workflows/
  build.yml                         # Auto-build APK on push
```

---

## Building from Source

```bash
git clone https://github.com/elschuyler/New.git
cd New/VibeForgeLauncher\(w\)
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

---

## License

This project is open source. See [LICENSE](LICENSE) for details.
