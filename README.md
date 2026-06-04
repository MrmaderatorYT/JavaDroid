# JavaDroid

Small Java IDE for Android: edit code, open Maven projects, compile on the device (ECJ + D8), run, and see errors in a Problems panel. Dark UI similar to Darcula.

## What’s in there

- Text editor (sora-editor), Java syntax highlighting, find/replace
- Maven: `pom.xml`, dependencies, run / package / test-compile
- Compile with ECJ + D8; `android.jar` bundled in the app
- Problems: compiler messages and simple checks; refreshes in the background for the open file
- Code completion: keywords, identifiers, methods after `.`, project classes
- UI strings: English (default), Ukrainian, German, French

## Setup

- Android API 26+
- JDK 17, Gradle 8.x (same idea as a normal Android project with current AGP)

## Build the app

```bash
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/`
