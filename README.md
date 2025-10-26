# مِـسك (Mesk) - Islamic Prayer Times App

<div dir="rtl">
مِـسك هو تطبيق إسلامي ذكي لتواقيت الصلاة يعرض أوقات الصلاة اليومية بناءً على موقعك الجغرافي. التطبيق يوفر ميزات متقدمة مثل تحديد القبلة، الأذكار، وأذان مخصص.
</div>

Mesk is an intelligent Islamic prayer times application that displays daily prayer times based on your geographical location. The app features advanced capabilities including Qibla compass, Athkar (remembrances), and customizable Athan (call to prayer).

## 🕌 Features

- **Prayer Times**: Accurate prayer times based on your location
- **Qibla Compass**: Built-in compass for finding the Qibla direction
- **Athkar Section**: Morning, evening, and miscellaneous remembrances
- **Prayer Notification**: Persistent notification showing time until next prayer
- **Customizable Athan**: Upload your own audio files or use default sounds
- **Settings**: Flexible configuration for prayers and notifications
- **Compass Calibration**: Interactive figure-8 motion calibration for accurate Qibla direction

## 📋 Requirements

- **Android API Level**: 24 (Android 7.0) or higher
- **Location Permission**: Required for accurate prayer time calculation
- **Internet**: Required for initial data download and location services

## 🏗️ Setup Instructions

### Prerequisites

1. **Android Studio**: Download and install [Android Studio](https://developer.android.com/studio)
2. **JDK**: Java Development Kit 11 or higher
3. **Android SDK**: Minimum SDK 24
4. **Gradle**: Included via wrapper (`gradlew`)

### Building the App

#### Method 1: Using Gradle Wrapper (Recommended)

```bash
# Navigate to project directory
cd Masjd2

# Build release APK
./gradlew assembleRelease

# Or build debug APK
./gradlew assembleDebug

# Find the APK at:
# app/build/outputs/apk/release/app-release-unsigned.apk
# app/build/outputs/apk/debug/app-debug.apk
```

#### Method 2: Using Android Studio

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Go to **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
4. Wait for build to complete
5. The APK will be in `app/build/outputs/apk/`

### Signing the APK (For Release)

> **Note**: Create your own keystore for production releases. The project does NOT include a signing key.

1. Generate a keystore:
```bash
keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

2. Configure signing in `app/build.gradle` (see Android documentation)

## 📱 Running on Device

### Via Android Studio

1. Connect your Android device via USB
2. Enable **Developer Options** and **USB Debugging** on your device
3. Click **Run** ▶️ in Android Studio
4. Select your device from the list
5. The app will install and launch automatically

### Via ADB

```bash
# Install the debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or install the release APK (unsigned)
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

## 🧪 Testing Checklist

- [ ] **Location Services**: Verify accurate location detection
- [ ] **Prayer Times**: Confirm times are correct for your location
- [ ] **Compass Calibration**: Test figure-8 motion and Qibla accuracy
- [ ] **Notifications**: Check persistent prayer notification displays correctly
- [ ] **Athkar**: Navigate through all Azkar sections
- [ ] **Settings**: Test Athan toggle, volume control, audio upload
- [ ] **Athan Service**: Verify Athan plays at correct prayer times
- [ ] **Location Widget**: Test prayer widget expansion and collapse
- [ ] **Permissions**: Confirm all required permissions are granted
- [ ] **Offline Mode**: Test app behavior without internet connection

## 🔒 Security Note

This repository **excludes** sensitive information for security:

- ❌ API keys and credentials
- ❌ Keystore files and signing keys
- ❌ Google Services configuration files
- ❌ Local properties (SDK paths)
- ❌ User-specific data

**Before deploying:**
1. Add your `google-services.json` (if using Google services)
2. Create and configure your signing keystore
3. Add any necessary API keys securely

## 🗂️ Project Structure

```
Masjd2/
├── app/                           # Main Android application
│   ├── src/main/
│   │   ├── java/                  # Kotlin source code
│   │   │   ├── MainActivity.kt     # Main activity with prayer widgets
│   │   │   ├── CompassActivity.kt # Qibla compass
│   │   │   ├── services/          # Background services
│   │   │   ├── ui/                # UI activities
│   │   │   ├── data/              # Data models and API
│   │   │   └── repository/        # Data repository
│   │   ├── res/                   # Resources (layouts, images, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle                # App-level Gradle config
├── flutter_ui/                    # Flutter module (if used)
├── gradle/                        # Gradle wrapper
├── build.gradle                   # Project-level Gradle config
└── README.md                      # This file
```

## 🛠️ Technologies

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room
- **Location Services**: Google Play Services
- **Notifications**: Android NotificationManager
- **Animations**: Lottie

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For issues, questions, or contributions, please open an issue in the GitHub repository.

---

<div dir="rtl">
تم بناء هذا التطبيق بعناية ليوفر تجربة موثوقة ومريحة للمستخدمين المسلمين حول العالم.
</div>

Built with care to provide a reliable and comfortable experience for Muslim users worldwide.

