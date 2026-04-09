package com.ccs.javadroid;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Зведення діагностики: ECJ по тексту активного .java (як у редакторі) + легкий static по файлах на диску.
 * Для активного файлу пріоритет у повідомлень ECJ (щоб не дублювати зі застарілим вмістом на диску).
 */
public final class ProblemsWorkspaceAnalyzer {

    private ProblemsWorkspaceAnalyzer() {}

    public static List<ProblemItem> analyze(Context appContext, File projectRoot,
                                            String editorJavaSource, File activeJavaFile) {
        List<ProblemItem> out = new ArrayList<>();
        if (projectRoot == null) {
            projectRoot = new File(".");
        }

        if (activeJavaFile != null
                && activeJavaFile.getName().endsWith(".java")
                && editorJavaSource != null) {
            out.addAll(ProjectCompiler.ecjProblemsForSource(appContext, editorJavaSource, activeJavaFile));
        }

        List<File> sources = ProjectScanner.listJavaSources(projectRoot);
        List<ProblemItem> st = StaticAnalyzer.analyze(projectRoot, sources);
        String activePath = activeJavaFile != null ? activeJavaFile.getAbsolutePath() : "";
        for (ProblemItem p : st) {
            if (!activePath.isEmpty() && p.file != null
                    && activePath.equals(p.file.getAbsolutePath())) {
                continue;
            }
            out.add(p);
        }

        return dedupe(out);
    }

    private static List<ProblemItem> dedupe(List<ProblemItem> in) {
        Set<String> seen = new LinkedHashSet<>();
        List<ProblemItem> res = new ArrayList<>(in.size());
        for (ProblemItem p : in) {
            String key = (p.file != null ? p.file.getAbsolutePath() : "?")
                    + "|" + p.line + "|" + p.severity + "|" + p.message;
            if (seen.add(key)) {
                res.add(p);
            }
        }
        return res;
    }
}
