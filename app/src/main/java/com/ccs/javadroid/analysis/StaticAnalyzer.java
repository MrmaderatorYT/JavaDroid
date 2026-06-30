package com.ccs.javadroid.analysis;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ccs.javadroid.R;

/**
 * Повноцінний статичний аналізатор Java-коду (Checkstyle/Lint-рівень).
 * Понад 70 правил: стиль, безпека, продуктивність, архітектура, ресурси, потоки, тести, документація.
 *
 * Правила розподілені по 5 класах:
 * - StyleRules: стиль коду, форматування, неймінг, документація
 * - StructureRules: імпорти, методи, класи, поля
 * - SafetyRules: безпека, null safety, потоки, виключення, ресурси
 * - ModernRules: streams, lambda, optional, date, regex, generics, анотації
 * - QualityRules: архітектура, продуктивність, тестування, deprecated, коллекції
 */
public final class StaticAnalyzer {

    private StaticAnalyzer() {}

    /** Аналіз без контексту (fallback на hardcoded рядки). */
    public static List<ProblemItem> analyze(File projectRoot, List<File> javaFiles) {
        return analyze(null, projectRoot, javaFiles);
    }

    /** Аналіз з контекстом (використовує перекладені рядки). */
    public static List<ProblemItem> analyze(Context ctx, File projectRoot, List<File> javaFiles) {
        List<ProblemItem> out = new ArrayList<>();
        if (javaFiles == null) return out;

        for (File f : javaFiles) {
            if (!f.getName().endsWith(".java")) continue;
            try {
                String content = new String(java.nio.file.Files.readAllBytes(f.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                if (content.trim().isEmpty()) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            ctx != null ? ctx.getString(R.string.sa_empty_file) : "", f, 1));
                    continue;
                }
                String[] lines = content.split("\n", -1);

                StyleRules.analyze(ctx, lines, f, out);
                StructureRules.analyze(ctx, content, lines, f, out);
                SafetyRules.analyze(ctx, lines, f, out);
                ModernRules.analyze(ctx, lines, f, out);
                QualityRules.analyze(ctx, lines, f, out);

            } catch (Exception ignored) {}
        }
        return out;
    }
}
