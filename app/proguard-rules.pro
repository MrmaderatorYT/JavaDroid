# ── Release obfuscation ───────────────────────────────────────────────────────
#
# Play measures the complete DEX, including bundled libraries. Enable renaming
# for statically linked tools while protecting reflection, JNI and resources.
# Shrinking and optimization need separate compiler validation before enabling
# them to address the other Play thresholds.
-dontshrink
-dontoptimize

# ── What R8 must not rename ──────────────────────────────────────────────────
#
# minifyEnabled is on for release. Keep reflection and resource lookup names,
# while allowing statically linked bytecode tools to be renamed.

# Keep metadata used by compiler reflection, including annotations, generic
# signatures and nested classes. Preserve line numbers for retraced crashes.
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions,
                *Annotation*,RuntimeVisible*,RuntimeInvisible*,
                AnnotationDefault,MethodParameters,
                SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── JNI ──────────────────────────────────────────────────────────────────────
#
# The C symbols in java_se_launcher.c and native_compiler_jni.c spell out the
# package, class and method name of the Java side. Renaming either class breaks
# the symbol lookup at load time, with no compile-time warning.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep class com.ccs.javadroid.tools.compilers.NativeCompiler { *; }
-keep class com.ccs.javadroid.javase.JavaSeNativeLauncher { *; }

# ── Names the app looks up as strings ────────────────────────────────────────
#
# BuildConfig is read by name in a few places and costs nothing to keep.
-keep class com.ccs.javadroid.BuildConfig { *; }

# ── Bundled compilers and tools ──────────────────────────────────────────────
#
# Kotlin uses reflection and ServiceLoader. ECJ and D8 are called directly, so
# their internals can be renamed as long as resource lookup names survive.

# Eclipse JDT (ECJ) — the Java compiler
-keep,allowobfuscation class org.eclipse.jdt.** { *; }
# These classes read parser tables/messages relative to their class package.
-keepnames class org.eclipse.jdt.internal.compiler.parser.Parser
-keepnames class org.eclipse.jdt.internal.compiler.batch.Main
-keepnames class org.eclipse.jdt.internal.compiler.util.Messages
-keepclassmembers class org.eclipse.jdt.internal.compiler.util.Messages {
    static java.lang.String *;
}
-dontwarn org.eclipse.jdt.**

# Kotlin compiler (embeddable) and its runtime
-keep class org.jetbrains.kotlin.** { *; }
-keep class org.jetbrains.kotlinx.** { *; }
-keep class org.jetbrains.org.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class org.jetbrains.annotations.** { *; }
-keep class gnu.trove.** { *; }
-keep class com.intellij.** { *; }
# SLF4J resolves its StaticLoggerBinder by name.
-keep class org.slf4j.** { *; }
-dontwarn org.jetbrains.kotlin.**
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn com.intellij.**

# R8 / D8 — the dexer the app runs on user code
-keep,allowobfuscation class com.android.tools.** { *; }
# D8 selects these providers with Class.forName using a configurable name.
-keep class com.android.tools.r8.threading.providers.** { *; }
-dontwarn com.android.tools.**

# ASM — bytecode viewer and class decompiler
# ASM's experimental API whitelist checks these class names. Renaming them
# makes it try to read a .class resource, which does not exist in an APK.
-keepnames class org.objectweb.asm.util.Trace**Visitor*
-keepnames class org.objectweb.asm.util.Check**Adapter*
-dontwarn org.objectweb.asm.**

# JGit — pure-Java git, heavy on ServiceLoader
-keep class org.eclipse.jgit.** { *; }
-dontwarn org.eclipse.jgit.**
-dontwarn org.slf4j.**
-dontwarn javax.servlet.**
-dontwarn org.apache.http.**

# Archive readers
-keepnames interface org.apache.commons.compress.archivers.ArchiveStreamProvider
-keepnames class * implements org.apache.commons.compress.archivers.ArchiveStreamProvider
-keepnames interface org.apache.commons.compress.compressors.CompressorStreamProvider
-keepnames class * implements org.apache.commons.compress.compressors.CompressorStreamProvider
-dontwarn org.apache.commons.compress.**
-dontwarn org.tukaani.xz.**
-dontwarn com.github.junrar.**

# XML pull parser — the pom reader binds to the implementation by name
-keep class org.xmlpull.** { *; }
-keep class org.kxml2.** { *; }
-dontwarn org.xmlpull.**

# The editor's languages and colour schemes are referenced directly by the app.
-dontwarn io.github.rosemoe.sora.**

# Vosk/JNA call interface methods by name through native dispatch.
-keep class org.vosk.** { *; }
-keep class com.sun.jna.** { *; }

# JDBC drivers and StAX factories are instantiated via names/resources.
-keep class org.postgresql.** { *; }
-keep class org.mariadb.jdbc.** { *; }
-keep class javax.xml.stream.** { *; }
-keep class org.codehaus.stax2.** { *; }

# ── Service registrations ────────────────────────────────────────────────────
#
# ServiceLoader finds implementations by the class name written in
# META-INF/services; a renamed implementation is no longer findable.
-keep,allowobfuscation @interface com.google.auto.service.AutoService
-keepnames class * implements java.nio.file.spi.FileSystemProvider

# ── Classes that do not exist on Android ─────────────────────────────────────
#
# R8 fails a build that references a class it cannot find, and these are all
# reached from code paths the app never takes: JGit carries transports and
# authentication backends for desktop and server environments, and the
# annotation packages are compile-time only. Every one of them is a Java SE or
# Windows API that is simply not part of Android.

# JGit: S3 transport, Windows credentials, LDAP and JMX
-dontwarn software.amazon.awssdk.**
-dontwarn com.sun.jna.**
-dontwarn waffle.**
-dontwarn javax.naming.**
-dontwarn javax.management.**
-dontwarn javax.security.auth.**
-dontwarn javax.sql.**
-dontwarn java.sql.**
-dontwarn java.lang.management.**
-dontwarn org.ietf.jgss.**

# Desktop-only APIs referenced by the bundled compilers
-dontwarn java.awt.**
-dontwarn javax.xml.transform.stax.**

# Compile-time annotations with no runtime presence
-dontwarn org.checkerframework.**
-dontwarn org.jetbrains.annotations.**

# ── More Java SE APIs absent from Android ────────────────────────────────────
#
# These appear once shrinking is off: with nothing discarded, R8 has to resolve
# references inside code the app never reaches — annotation processors shipped
# inside libraries, OSGi and JTA hooks, and the javax.tools compiler interface
# that ECJ implements for desktop use.
-dontwarn javax.lang.model.**
-dontwarn javax.tools.**
-dontwarn javax.annotation.processing.**
-dontwarn javax.transaction.**
-dontwarn org.osgi.**
-dontwarn java.lang.invoke.MethodHandleProxies
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn javaslang.**
-dontwarn com.google.errorprone.**
-dontwarn org.apache.commons.lang3.**
