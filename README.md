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

## Google Play (release)

1. **Package name** — `applicationId` is in `app/build.gradle` (`com.ccs.javadroid`). It must be unique on Play.

2. **Upload keystore** — copy `keystore.properties.example` to `keystore.properties`, fill in passwords and `storeFile` path (project root). Create a keystore with `keytool` (see comments in the example file). Do not commit `keystore.properties` or `.jks` files.

3. **App Bundle** (what Play expects):

   ```bash
   ./gradlew :app:bundleRelease
   ```

   Output: `app/build/outputs/bundle/release/app-release.aab`  
   Without `keystore.properties`, the release may be signed with the **debug** key — only for local checks. Uploads to Play need your upload keystore (step 2).

4. **Play Console** — enable **Play App Signing**, upload the AAB, complete **Data safety** (this app declares `INTERNET`), store listing, content rating, and meet the target API policy (`targetSdk` 35 in this project).

5. **Updates** — increase `versionCode` and `versionName` in `app/build.gradle` for every upload.

### Play Store listing (English)

**Short description** (max 80 characters on Play):

`Java IDE for Android: Maven, on-device compile & run, editor, dark theme.`

**Full description** — copy into the store listing (plain text):

```
JavaDroid is a compact Java IDE on your Android device. Edit code with syntax highlighting, open Maven projects, resolve dependencies from Maven Central, compile on the phone with ECJ and D8, run your app, and fix issues in a Problems panel—without a desktop PC.

Features
• Code editor with Java highlighting, find and replace
• Maven: pom.xml, dependency resolution, run / package / test-compile
• On-device compilation; Android APIs bundled for the classpath
• Problems: compiler diagnostics and lightweight checks
• Code completion: keywords, identifiers, methods after a dot, project classes
• UI languages: English, Ukrainian, German, French — Darcula-inspired dark theme

Best for learning, experiments, and small projects. Very large builds may be limited by device memory and CPU.

Requirements: Android 8.0 (API 26) or newer.
```

## Legal / deps

Before you ship anywhere, check licenses of libraries listed in `app/build.gradle` (editor, ECJ, R8, AndroidX, etc.).

Personal / learning project.
