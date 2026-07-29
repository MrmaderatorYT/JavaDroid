package com.ccs.javadroid.util.languages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccs.javadroid.util.JavaReflectionCompletion;
import com.ccs.javadroid.util.languages.ast.AstJavaAnalyzeManager;

import java.io.File;

import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.langs.java.JavaQuoteHandler;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

/**
 * Java support backed by the AST highlighter in
 * {@link com.ccs.javadroid.util.languages.ast} instead of the line-based
 * tokenizer.
 *
 * <p>Because the analyzer knows the scope tree, the same identifier can be
 * coloured as a type, a method, a field, a parameter or a local depending on
 * where it appears — which a per-line pattern matcher cannot do. Completion
 * behaviour is unchanged: keywords, file-local declarations and reflection-based
 * member suggestions all still apply.</p>
 */
public class AstJavaLanguage implements Language {

    private static final String[] KEYWORDS = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "record", "return", "sealed", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "var", "void",
            "volatile", "while", "yield", "true", "false", "null"
    };

    private static final SymbolPairMatch SYMBOL_PAIRS = new SymbolPairMatch() {{
        putPair('{', new SymbolPairMatch.SymbolPair("{", "}"));
        putPair('(', new SymbolPairMatch.SymbolPair("(", ")"));
        putPair('[', new SymbolPairMatch.SymbolPair("[", "]"));
        putPair('<', new SymbolPairMatch.SymbolPair("<", ">"));
        putPair('"', new SymbolPairMatch.SymbolPair("\"", "\""));
        putPair('\'', new SymbolPairMatch.SymbolPair("'", "'"));
    }};

    private final AstJavaAnalyzeManager analyzer = new AstJavaAnalyzeManager();
    private final IdentifierAutoComplete autoComplete = new IdentifierAutoComplete(KEYWORDS);
    private final JavaQuoteHandler quoteHandler = new JavaQuoteHandler();
    private final Context appContext;
    private File projectRoot;

    public AstJavaLanguage(@NonNull Context context, @Nullable File projectRoot) {
        this.appContext = context.getApplicationContext();
        this.projectRoot = projectRoot;
    }

    public void setProjectRoot(@Nullable File projectRoot) {
        this.projectRoot = projectRoot;
    }

    @NonNull
    @Override
    public AnalyzeManager getAnalyzeManager() {
        return analyzer;
    }

    @Nullable
    @Override
    public QuickQuoteHandler getQuickQuoteHandler() {
        return quoteHandler;
    }

    @Override
    public void destroy() {
    }

    @Override
    public int getInterruptionLevel() {
        return INTERRUPTION_LEVEL_STRONG;
    }

    @Override
    public void requireAutoComplete(@NonNull ContentReference content, @NonNull CharPosition position,
                                    @NonNull CompletionPublisher publisher, @NonNull Bundle extraArguments) {
        publisher.setComparator(null);
        String prefix = CompletionHelper.computePrefix(content, position, MyCharacter::isJavaIdentifierPart);
        autoComplete.requireAutoComplete(content, position, prefix, publisher, analyzer.getIdentifiers());
        try {
            JavaReflectionCompletion.contribute(appContext, projectRoot, content, position, prefix, publisher);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
        try {
            String lineText = text.getLine(line);
            int limit = Math.min(column, lineText.length());
            int depth = 0;
            boolean inString = false;
            char quote = 0;
            for (int i = 0; i < limit; i++) {
                char c = lineText.charAt(i);
                if (inString) {
                    if (c == '\\') { i++; continue; }
                    if (c == quote) inString = false;
                    continue;
                }
                if (c == '"' || c == '\'') { inString = true; quote = c; continue; }
                if (c == '{' || c == '(' || c == '[') depth++;
                else if (c == '}' || c == ')' || c == ']') depth--;
            }
            return Math.max(0, depth) * 4;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean useTab() {
        return false;
    }

    @NonNull
    @Override
    public Formatter getFormatter() {
        return EmptyLanguage.EmptyFormatter.INSTANCE;
    }

    @Override
    public SymbolPairMatch getSymbolPairs() {
        return SYMBOL_PAIRS;
    }

    @Override
    public NewlineHandler[] getNewlineHandlers() {
        return new NewlineHandler[0];
    }
}
