# JavaDroid

A full-featured Java IDE for Android — edit, compile, run, and debug Java/Kotlin code directly on your device.

## Features

### Code Editor
- Syntax highlighting for Java, Kotlin, C++, XML, Gradle, JSON, Bash, Markdown
- Find & replace, word wrap, line numbers, auto-save, auto-import
- Code formatting, split screen, keyboard accessory bar

### Compiler & Runtime
- On-device compilation via ECJ (Eclipse Compiler for Java) + D8 dexer
- Maven project support: `pom.xml`, dependencies, package, test-compile
- Kotlin compilation via K2JVMCompiler (syntax highlighting + compile/run)
- JNI / native C/C++ compilation via built-in TCC (C) and NDK clang++ (C++)

### Debugger
- ASM bytecode instrumentation — breakpoints on any line (unconditional + conditional)
- Step over / step into / step out with stack depth tracking
- Local variables, call stack, watch expressions, expression evaluation
- Debug line highlighting (red overlay on current execution line)

### Bytecode Viewer
- Custom bytecode viewer engine powered by ASM 9.6
- Class hierarchy, fields, methods, instructions, stack map frames, bootstrap methods
- Method/field icons (public/private/protected/static)

### Learn Center
- Java reference course (15 chapters, from basics to Stream API)
- Enterprise essentials course (Date/Time, Concurrency, JVM Memory, SQL, JDBC, Web)
- Bilingual (Ukrainian / English) with code examples

### Git Integration
- JGit 5.13.5 (pure Java, no native binary)
- Init, clone, commit, stage/unstage, pull/push, branches, log

### UI & Customization
- Dark theme with customizable colors (background, toolbar, accent, syntax colors)
- Configurable font family, size, tab width, line spacing
- Library Manager (search Maven Central, add dependencies)
- JSON/XML formatter, Markdown preview

### Localization
- 11 languages: English, Ukrainian, German, French, Spanish, Polish, Romanian, Azerbaijani, Hindi, Igbo, Yoruba, Hausa

## Setup

- Android API 26+ (target SDK 35, 16KB page alignment)
- JDK 17, Gradle 8.x

## Build

```bash
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/`

## Google Play

https://play.google.com/store/apps/details?id=com.ccs.javadroid
