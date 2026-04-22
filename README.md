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

Best for learning, experiments, and small projects. Very large builds may be limited by device memory and CPU.

