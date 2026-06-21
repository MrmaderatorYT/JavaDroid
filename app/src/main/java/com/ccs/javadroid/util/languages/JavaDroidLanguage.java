package com.ccs.javadroid.util.languages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccs.javadroid.util.JavaReflectionCompletion;
import java.io.File;

import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

/**
 * Java з Rosemoe + рефлексія методів після "." та класи з проєкту / import.
 */
public class JavaDroidLanguage extends JavaLanguage {

    private final Context appContext;
    private File projectRoot;

    private static final SymbolPairMatch SYMBOL_PAIRS = new SymbolPairMatch() {{
        putPair('{', new SymbolPairMatch.SymbolPair("{", "}"));
        putPair('(', new SymbolPairMatch.SymbolPair("(", ")"));
        putPair('[', new SymbolPairMatch.SymbolPair("[", "]"));
        putPair('<', new SymbolPairMatch.SymbolPair("<", ">"));
        putPair('"', new SymbolPairMatch.SymbolPair("\"", "\""));
        putPair('\'', new SymbolPairMatch.SymbolPair("'", "'"));
    }};

    public JavaDroidLanguage(@NonNull Context context, @Nullable File projectRoot) {
        this.appContext = context.getApplicationContext();
        this.projectRoot = projectRoot;
    }

    public void setProjectRoot(@Nullable File projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public SymbolPairMatch getSymbolPairs() {
        return SYMBOL_PAIRS;
    }

    @Override
    public void requireAutoComplete(@NonNull ContentReference content, @NonNull CharPosition position,
                                    @NonNull CompletionPublisher publisher, @NonNull Bundle extraArguments) {
        publisher.setComparator(null);
        super.requireAutoComplete(content, position, publisher, extraArguments);
        try {
            String prefix = CompletionHelper.computePrefix(content, position, MyCharacter::isJavaIdentifierPart);
            JavaReflectionCompletion.contribute(appContext, projectRoot, content, position, prefix, publisher);
        } catch (Throwable ignored) {
        }
    }
}
