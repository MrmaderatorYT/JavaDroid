# JavaDroid

A full-featured Java IDE for Android — edit, compile, run, and debug Java/Kotlin code directly on your device.

## Features

### Code Editor
- Syntax highlighting for **12 languages**: Java, Kotlin, C/C++, JavaScript, XML/HTML/SVG, CSS, JSON, SQL, Gradle, Bash, Markdown
- **Rosemoe Sora Editor** engine with smooth scrolling and modern UI
- **Bracket pair highlighting** — auto-highlights matching `()`, `{}`, `[]`, `<>`
- **Auto-close pairs** — types `(` → `()`, `{` → `{}`, `<` → `<>`, `"` → `""`, `'` → `''`
- **Auto-indent** — automatic indentation after `{`
- **54 code snippets** — type `try`, `fori`, `sout`, `main` etc. to expand into full blocks
- **9 font families**: Monospace, Sans-serif, Serif, System default, JetBrains Mono, Fira Code, Source Code Pro, DejaVu Sans Mono, Roboto Mono
- Find & replace (file scope / project scope)
- Word wrap, line numbers, auto-save, format on save
- Split screen (dual editor panes)
- Keyboard accessory bar with quick-insert symbols
- Configurable font size (8-32sp), tab width (1-8), line spacing (1.0-3.0x)
- Undo / Redo
- Auto-import (detects and adds missing Java imports)
- Code bookmarks with navigation dialog
- **Power saving mode** (Auto / Disabled / Always Performance)

### Compiler & Runtime
- On-device compilation via **ECJ** (Eclipse Compiler for Java) + **D8 dexer**
- Java 8 target support
- Maven project support: `pom.xml`, dependencies sync, package, test-compile, test-run, clean, install
- Gradle project layout support
- JNI / native C/C++ compilation via built-in **TCC** (C) and **NDK clang++** (C++)
- NDK install/uninstall from Settings (~130MB download)
- Console output with colored error/success messages
- Process exit code display

### Debugger
- **ASM bytecode instrumentation** — breakpoints on any line
- **Unconditional breakpoints** — simple toggle on line gutter
- **Conditional breakpoints** — set conditions like `i == 5` (long-press gutter)
- **Step Over** (F6) / **Step Into** (F5) / **Step Out** (F7) / **Resume** (F9)
- **Local variables** panel (tree view with names, types, values)
- **Call stack** panel (clickable frames, jump to source)
- **Watch expressions** — add/edit/delete watches evaluated in real-time
- **Evaluate Expression** dialog — evaluate arbitrary expressions during debug
- Debug line highlighting (red overlay on current execution line)
- Debug console output panel
- Long-press expression in editor during debug → Evaluate or Add Watch

### Bytecode Viewer
- Custom bytecode viewer engine powered by **ASM 9.6**
- **Method tree** panel (left) — lists fields and methods
- **Instructions panel** (right) — shows bytecode instructions per method
- Opcode search — filter instructions by opcode
- Hex dump toggle — raw bytes view of class file
- Line numbers / comments toggles
- Jump target navigation (click label references to scroll)
- **Bytecode editor** — edit instructions, delete/insert opcodes, save back to `.class`
- Deobfuscation via ProGuard mapping files
- **Call graph analysis** — method-level callee/caller visualization

### Static Analysis
- **Live problems detection** — periodic background scanning (configurable interval)
- ECJ compilation problems — real-time compile error detection
- Static analyzer checks: empty files, long lines (>160 chars), TODO/FIXME markers
- **Problems panel** in bottom bar with clickable items (jump to file + line)

### Project Management
- **4 project types**: Maven (Java), Gradle (Java), Bytecode (pure .class/.asm), Playground (demo)
- File tree (left drawer) with expand/collapse directories
- New Java file (with package-aware placement)
- New file / new folder creation
- Import files from storage
- Export project as ZIP archive
- Copy / Paste files between folders
- Create archive from folder (ZIP)
- File context menu: Open, Rename, Delete, Copy
- Tab-based file management (horizontal tab bar, modified indicator)

### Git Integration
- **JGit 5.13.5** (pure Java, no native binary)
- Init, clone (any URL), commit, stage/unstage, pull/push
- Branch list, create, delete, checkout
- Log (up to 100 commits)
- **GitHub API** — create repository, set remote
- **GitLab API** — create project, set remote
- Personal Access Token support
- Credentials store with obfuscation

### AI Assistant
- **Google Gemini API** integration
- Chat interface with message history
- Markdown rendering in responses (code blocks, bold, headers)
- Quick actions: **Explain**, **Find Bugs**, **Refactor**, **Optimize**, **Document**, **Test**
- Multiple model selection: Gemini 3.5 Flash, Gemini 3.1 Flash Lite, Gemini 2.5 Flash
- Context-aware: sends current file content with prompts

### Learning Center
- **11 courses** with bilingual content (Ukrainian / English):
  1. Java Reference Course (comprehensive reference)
  2. JDK 8 Deep Dive (Lambda, Streams, Optional, etc.)
  3. Essentials Course (beginner-friendly basics)
  4. Advanced Java Course
  5. Algorithms Course (sorting, searching, data structures)
  6. Architecture Course (design patterns)
  7. Network Course (HTTP, sockets, APIs)
  8. Testing Course (JUnit, testing strategies)
  9. Spring Boot Course
  10. DevOps Course (CI/CD, Docker, deployment)
  11. Java Tutorials (practical tutorials)
- Interactive lessons with **Theory** tab (HTML) and **Practice** tab (embedded editor + console)
- Run playground code directly in lessons
- Code syntax highlighting in lessons

### Networking Tools
- **HTTP Client** (Postman-like): GET, POST, PUT, DELETE, PATCH, HEAD
- Custom headers and request body
- Response display: status code, response time, body size, headers
- Auto-format response: JSON (pretty-printed), XML, plain text
- `.http` file parser (IntelliJ format)
- **WebView Preview** — live HTML/CSS/JS preview with console forwarding

### Media Viewers
- **Image Viewer** — JPG, PNG, GIF, WebP, BMP
- **SVG Viewer** — render SVG files using androidsvg library
- **Media Player** — audio/video playback with volume/brightness control

### Database Tools
- **SQLite Database Inspector** — browse tables, run custom SQL queries
- Auto-generate `SELECT * FROM table LIMIT 100`
- Non-SELECT support (INSERT, UPDATE, DELETE, CREATE)
- Execution time display

### Class Browser
- Search all JAR files in Maven cache
- Browse available classes (Java standard library + dependencies)
- Copy import statement, open class, view bytecode

### Settings & Theming
- **Multiple preset themes** (Dark, Light, etc.) with mini-preview cards
- **Custom theme** with full color control: background, toolbar, text, accent, console, keyword, string, comment
- **RGB color picker** with seek bars, hex input, and 32-color preset palette
- Instant theme apply (recreate without animation)
- Power saving mode (Auto / Disabled / Always Performance)
- NDK install/uninstall
- Reset to defaults

### Sharing & Export
- Share file via Android share sheet
- Share to Pastebin (upload with API key, URL auto-copied)
- Export project as ZIP archive
- Copy console output

## Localization

11 languages: English, Ukrainian, German, French, Spanish, Polish, Romanian, Azerbaijani, Hindi, Igbo, Yoruba, Hausa

## Architecture

| Package | Purpose |
|---------|---------|
| `ui/` | All Activity classes and UI adapters |
| `tools/bytecode/` | Bytecode analysis, editing, formatting, deobfuscation |
| `tools/compilers/` | ECJ + D8 compilation, NDK management |
| `debug/` | Debugger controller, bridge, instrumentation, variables, watches |
| `git/` | JGit wrapper, GitHub/GitLab API, credentials |
| `ai/` | Gemini AI service, chat formatter |
| `maven/` | POM parsing/writing, dependency resolution, lifecycle, test runner |
| `analysis/` | Static analysis, live problems, workspace analysis |
| `learn/` | Course system, lessons, chapters, syntax highlighting |
| `project/` | Project management, scanning, factories |
| `util/` | Themes, preferences, formatters, languages, helpers |

## Bottom Panel Tabs

1. **Run** — Console output
2. **Problems** — Static analysis + compilation problems
3. **Bytecode** — Inline bytecode viewer
4. **Debug (Threads & Variables)** — Variables tree, call stack, watches
5. **Debug Console** — Debug session output
6. **Call Graph** — Method call graph visualization

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
