# JavaDroid

A full-featured Java IDE for Android — edit, compile, run, and debug Java/Kotlin code directly on your device.

**Version 1.11.3** · Android 8.0+ (API 26) · [Google Play](https://play.google.com/store/apps/details?id=com.ccs.javadroid)

## License

This project is licensed under the **GNU General Public License v3.0** — applies to all versions.

## Features

### Code Editor
- Syntax highlighting for **13 languages**: Java, Kotlin, C/C++, JavaScript, XML/HTML/SVG, CSS, JSON, SQL, Gradle, Bash, Markdown, Properties
- **Rosemoe Sora Editor 0.23.6** engine with smooth scrolling and modern UI
- **AST-based semantic highlighting** for Java — a real parser, so a field, a local and a parameter are coloured differently; falls back to lexer highlighting in power-saving mode
- **Bracket pair highlighting** — auto-highlights matching `()`, `{}`, `[]`, `<>`
- **Auto-close pairs** — types `(` → `()`, `{` → `{}`, `<` → `<>`, `"` → `""`, `'` → `''`
- **Auto-indent** — automatic indentation after `{`
- **Snippet completion** — type `sout`, `fori`, `psvm`, `try` etc. to expand into full blocks
- **9 font families**: Monospace, Sans-serif, Serif, System default, JetBrains Mono, Fira Code, Source Code Pro, DejaVu Sans Mono, Roboto Mono
- Find & replace (file scope / project scope)
- Word wrap, line numbers, auto-save, format on save
- Split screen (dual editor panes) with per-pane minimap, breakpoints, bookmarks and inlay hints
- Minimap, breadcrumb bar, git gutter, inlay hints
- Keyboard accessory bar with quick-insert symbols
- Configurable font size (8-32sp), tab width (1-8), line spacing (1.0-3.0x)
- Undo / Redo, local history
- Auto-import (detects and adds missing Java imports)
- Code bookmarks with navigation dialog
- **Power saving mode** (Auto / Disabled / Always Performance) — reacts to battery level, the system saver and device temperature

### Compiler & Runtime

Two execution backends, chosen per project at creation time:

| | ART mode (default) | Java SE 21 mode |
|---|---|---|
| Pipeline | Java → **ECJ** → **D8** → dex | Java → **ECJ** → `.class` → embedded JVM |
| Runs on | Android's ART, in-process | **HotSpot** in a separate `:javase` process |
| Libraries | Android class library | Standard Java SE 21 — `java.sql`, `java.net`, `java.nio`, `java.util.concurrent` |
| Android API | Available directly | Not available (would need a bridge) |
| Trade-off | Fast start, small footprint | Desktop-library compatibility; slower start, more RAM |

- Selectable Java target via the JDK picker; Java SE projects target 21
- `System.exit()` in your code ends the guest process, not the IDE — Java SE mode isolates the program you are running
- Maven project support: `pom.xml`, dependency sync, package, test-compile, test-run, clean, install
- Gradle project layout and script parsing
- Kotlin compilation via a repacked `kotlin-compiler-embeddable` 2.0.21
- JNI / native C and C++ compilation: built-in **TCC** for C, **NDK r29 clang++** for C++
- Console output with coloured error/success messages and exit code display
- **JShell-style REPL** panel

### On-Device Toolchains

Both payloads are extracted only when first needed, and both are pinned by SHA-256 — the bundled one at build time, the downloaded one before extraction — so a corrupt or substituted payload fails loudly rather than producing a toolchain that miscompiles.

| Toolchain | Delivery | Download | Installed size |
|---|---|---|---|
| Java SE 21 runtime (OpenJDK Mobile) | Bundled in the APK (43 MB of assets) | — | ~140 MB |
| Android NDK r29 (`clang++`, `lld`) | Downloaded on first use | 344 MB | ~1.85 GB |

The NDK download is resumable (the host answers `Range` requests, and a partial transfer survives between attempts), cancellable, and verified before a single file is extracted. Removing the NDK in Settings also discards any abandoned download. `clang-21` and `lld` themselves ship as native libraries, because Android does not let a target-SDK 36 app execute binaries from its writable files directory.

See [`app/src/main/assets/toolchains/README.txt`](app/src/main/assets/toolchains/README.txt) for provenance, upstream URLs and checksums.

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
- **Profiler** panel with live metrics

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
- **Live problems detection** — background workspace sweep, cached per file by modification time, parallelised across up to four cores
- ECJ compilation problems — real-time compile error detection on the editor buffer, not the copy on disk
- Rule sets covering structure, quality, safety, security and modernisation
- **Problems panel** with per-severity checkbox filters (Error / Security / Warning / Info), a **this file / all files** scope switch, and clickable items that move the cursor to the reported line

### Project Management
- **4 project types**: Java (Maven or Gradle), Kotlin, Bytecode (pure `.class`/`.asm`), Playground
- **Project Structure** and **Project Map** screens
- **UML class diagram** generator
- File tree (left drawer) with expand/collapse and bulk collapse
- New Java file (with package-aware placement), new file / new folder
- Import from storage, export project as ZIP
- Copy / paste files between folders, create archive from folder
- Archive support: ZIP, TAR, RAR
- **Search Everywhere** and project-wide global search
- Import an existing repository or Eclipse/Gradle/Maven layout
- Tab-based file management (horizontal tab bar, modified indicator)
- Read-only mode for files
- Multi-window support (Samsung DeX, tablets)

### Git Integration
- **JGit 5.13.5** (pure Java, no native binary)
- Init, clone (any URL), commit, stage/unstage, pull/push
- Branch list, create, delete, checkout
- Log (up to 100 commits), diff viewer, in-editor git gutter
- **GitHub API** — create repository, set remote
- **GitLab API** — create project, set remote
- Personal Access Token support, credentials store with obfuscation

### AI Assistant
- **Google Gemini API** integration
- Chat interface with message history
- Markdown rendering in responses (code blocks, bold, headers)
- Quick actions: **Explain**, **Find Bugs**, **Refactor**, **Optimize**, **Document**, **Test**
- Model selection: Gemini 3.5 Flash, Gemini 3.1 Flash Lite, Gemini 2.5 Flash
- Context-aware: sends current file content with prompts
- **Semantic code search** over the project

### Learning Center

The bundled learning center is currently unavailable while its curriculum is being rebuilt.

### Networking Tools
- **HTTP Client** (Postman-like): GET, POST, PUT, DELETE, PATCH, HEAD — via OkHttp 4.12
- Custom headers and request body
- Response display: status code, response time, body size, headers
- Auto-format response: JSON (pretty-printed), XML, plain text
- `.http` file parser (IntelliJ format)
- **SSL certificate viewer**
- **WebView Preview** — live HTML/CSS/JS preview with console forwarding
- Send files over Bluetooth

### Media & File Viewers
- **Image Viewer** — JPG, PNG, GIF, WebP, BMP
- **SVG Viewer** — render SVG files using androidsvg
- **Media Player** — audio/video playback with volume/brightness control
- **Hex editor**
- **Markdown preview**
- **TMX map viewer** and **atlas texture inspector**

### Database Tools
- **Database client** — SQLite, MySQL/MariaDB, PostgreSQL
- **SQLite Database Inspector** — browse tables, run custom SQL queries
- Auto-generate `SELECT * FROM table LIMIT 100`
- Non-SELECT support (INSERT, UPDATE, DELETE, CREATE)
- **Schema diagram** view
- Execution time display

### Other Tools
- **Class Browser** — search all JARs in the Maven cache, browse the Java standard library and dependencies, copy an import, open the class, view its bytecode
- **Library Manager**
- **Hash calculator** — MD5, SHA-1, SHA-256, SHA-512, CRC32
- **Split terminal** — several terminals in one window

### Settings & Theming
- **Multiple preset themes** (Dark, Light, etc.) with mini-preview cards
- **Custom theme** with full colour control: background, toolbar, text, accent, console, keyword, string, comment
- **RGB colour picker** with seek bars, hex input, and 32-colour preset palette
- Instant theme apply (recreate without animation)
- Power saving mode (Auto / Disabled / Always Performance)
- NDK download / removal
- Link to the source repository
- Reset to defaults

### Sharing & Export
- Share file via Android share sheet
- Share to Pastebin (upload with API key, URL auto-copied)
- Export project as ZIP archive
- Copy console output

## Localization

**15 languages**: English, Ukrainian, German, French, Italian, Spanish, Polish, Romanian, Azerbaijani, Hindi, Igbo, Yoruba, Hausa, Filipino, Brazilian Portuguese

## Architecture

| Package | Purpose |
|---------|---------|
| `ui/` | All Activity classes, panel managers and UI adapters |
| `javase/` | Embedded Java SE 21 runtime: install, launch, guest bootstrap |
| `tools/compilers/` | ECJ + D8 compilation, NDK management, dex/native runners |
| `tools/bytecode/` | Bytecode analysis, editing, formatting, deobfuscation |
| `tools/refactor/` | Refactoring helpers |
| `debug/` | Debugger controller, bridge, instrumentation, variables, watches, bookmarks |
| `profiler/` | Instrumentation-based profiling |
| `analysis/` | Static analysis rule sets, live problems, workspace sweep |
| `git/` | JGit wrapper, GitHub/GitLab API, credentials, diff, gutter |
| `ai/` | Gemini service, chat formatter, semantic search |
| `maven/` | POM parsing/writing, dependency resolution, lifecycle, test runner |
| `gradle/` | Gradle script and dependency handling |
| `project/` | Project creation, scanning, layout detection, import, runtime selection |
| `learn/` | Course system, lessons, chapters, snippet runner |
| `db/` | Database sessions (SQLite / JDBC), schema graph |
| `archive/` | ZIP / TAR / RAR extraction |
| `uml/` | Class diagram generation |
| `startup/` | App startup initialisers |
| `util/` | Themes, preferences, formatters, language definitions, AST parser, helpers |

## Bottom Panel Tabs

1. **Run** — console output
2. **Problems** — static analysis + compilation problems
3. **Bytecode** — inline bytecode viewer
4. **Debug (Threads & Variables)** — variables tree, call stack, watches
5. **Debug Console** — debug session output
6. **Call Graph** — method call graph visualization
7. **Bookmarks**
8. **Dependencies**
9. **Profiler**
10. **TODO**
11. **Console** — JShell-style REPL

## Setup

- Android **API 26+**; compile and target SDK **36**
- **JDK 17**, **Gradle 8.4**
- 16 KB page alignment for the app's own native libraries (Android 15+)
- NDK and CMake for the bundled TCC compiler and the Java SE launcher

## Build

```bash
./gradlew :app:assembleDebug
```

Release App Bundle:

```bash
./gradlew :app:bundleRelease
```

Unit tests (JVM, no device needed):

```bash
./gradlew :app:testDebugUnitTest
```

APK: `app/build/outputs/apk/debug/` · AAB: `app/build/outputs/bundle/release/`

`preBuild` runs `verifyBundledToolchains`, which checksums every bundled payload and asserts that the NDK download pin in `NdkManager.java` still matches what `README.txt` documents. A payload replaced by accident, or a version bumped without its hash, fails the build instead of failing on a user's device.

## Known Limitations

- **Java SE mode requires 4 KB memory pages.** Every shared library in the OpenJDK Mobile 21 payload is aligned for 4 KB pages, so `dlopen` will reject them on a device booted with 16 KB pages. The app's own native libraries are 16 KB-aligned; the downloaded runtime is not ours to relink.
- **The Java SE runtime is per-architecture.** A binpack is installed for one of `arm`, `arm64`, `x86`, `x86_64`, and the guest JVM is loaded into the app's own process, so the installed binpack has to match the architecture the app itself is running as — not merely one the device supports.
- **Live analysis in a Java SE project uses the Android classpath**, so standard Java SE classes may be reported as unavailable in the editor. The build or run result is the source of truth; the Problems panel says so.
- **The NDK is ARM64-only** and needs Android 9+.
- A running program cannot be stopped from the UI, and console output is delivered when the program finishes rather than streamed.
