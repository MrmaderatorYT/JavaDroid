package com.ccs.javadroid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Легкі перевірки «як SonarQube»: стиль, довгі рядки, порожні файли.
 */
public final class StaticAnalyzer {

    private StaticAnalyzer() {}

    public static List<ProblemItem> analyze(File projectRoot, List<File> javaFiles) {
        List<ProblemItem> out = new ArrayList<>();
        if (javaFiles == null) return out;

        for (File f : javaFiles) {
            if (!f.getName().endsWith(".java")) continue;
            try {
                String content = new String(java.nio.file.Files.readAllBytes(f.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                if (content.trim().isEmpty()) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            "Порожній файл", f, 1));
                    continue;
                }
                String[] lines = content.split("\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.length() > 160) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "Рядок довший за 160 символів (стиль коду)", f, i + 1));
                    }
                    if (line.contains("TODO") || line.contains("FIXME")) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "TODO/FIXME у коді", f, i + 1));
                    }
                }
            } catch (Exception ignored) {}
        }
        return out;
    }
}
