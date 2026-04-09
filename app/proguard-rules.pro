# Release: minify is off by default. If you turn on minifyEnabled, expand these rules.

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Eclipse JDT (ECJ)
-keep class org.eclipse.jdt.** { *; }
-dontwarn org.eclipse.jdt.**

# ASM (bytecode viewer)
-keep class org.objectweb.asm.** { *; }
-dontwarn org.objectweb.asm.**

# R8 / D8 (bundled tools)
-keep class com.android.tools.** { *; }
-dontwarn com.android.tools.**

# Editor
-keep class io.github.rosemoe.sora.** { *; }
-dontwarn io.github.rosemoe.sora.**
