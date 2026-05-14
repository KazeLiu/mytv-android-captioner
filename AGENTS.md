# Local Build Notes

This Windows workspace uses Android Studio's bundled JDK and a local Android SDK.

Before building, use these paths:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\code\android-sdk'
$env:ANDROID_SDK_ROOT='D:\code\android-sdk'
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:PATH"
.\gradlew.bat :tv:assembleDebug
```

Or run:

```powershell
.\build-tv-debug.ps1
```

APK output:

```text
D:\code\mytv-android-captioner\tv\build\outputs\apk\debug\mytv-android-tv-3.1-all-sdk21.apk
```
