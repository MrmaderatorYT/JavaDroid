package com.ccs.javadroid.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Шаблони файлів для створення нових файлів у проєкті.
 */
public final class FileTemplates {

    public static final String KEY_CLASS = "class";
    public static final String KEY_INTERFACE = "interface";
    public static final String KEY_ENUM = "enum";
    public static final String KEY_ABSTRACT = "abstract";
    public static final String KEY_MAIN = "main";
    public static final String KEY_RECORD = "record";
    public static final String KEY_ANNOTATION = "annotation";
    public static final String KEY_TEST = "test";
    public static final String KEY_BUILDER = "builder";
    public static final String KEY_SINGLETON = "singleton";
    public static final String KEY_FACTORY = "factory";
    public static final String KEY_OBSERVER = "observer";

    private static final Map<String, String[]> templates = new LinkedHashMap<>();

    static {
        templates.put(KEY_CLASS, new String[]{
                "Class",
                "public class %s {\n\n}\n"
        });
        templates.put(KEY_INTERFACE, new String[]{
                "Interface",
                "public interface %s {\n\n}\n"
        });
        templates.put(KEY_ENUM, new String[]{
                "Enum",
                "public enum %s {\n\n}\n"
        });
        templates.put(KEY_ABSTRACT, new String[]{
                "Abstract Class",
                "public abstract class %s {\n\n}\n"
        });
        templates.put(KEY_MAIN, new String[]{
                "Main Class (Hello World)",
                "public class %s {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}\n"
        });
        templates.put(KEY_RECORD, new String[]{
                "Record (Java 16+)",
                "public record %s() {\n\n}\n"
        });
        templates.put(KEY_ANNOTATION, new String[]{
                "Annotation",
                "import java.lang.annotation.*;\n\n" +
                "@Retention(RetentionPolicy.RUNTIME)\n" +
                "@Target(ElementType.METHOD)\n" +
                "public @interface %s {\n\n}\n"
        });
        templates.put(KEY_TEST, new String[]{
                "JUnit Test",
                "import org.junit.jupiter.api.Test;\n" +
                "import static org.junit.jupiter.api.Assertions.*;\n\n" +
                "public class %s {\n\n" +
                "    @Test\n" +
                "    void testMethod() {\n" +
                "        // TODO: write test\n" +
                "    }\n" +
                "}\n"
        });
        templates.put(KEY_BUILDER, new String[]{
                "Builder Pattern",
                "public class %s {\n\n" +
                "    private %s() {}\n\n" +
                "    public static Builder builder() {\n" +
                "        return new Builder();\n" +
                "    }\n\n" +
                "    public static class Builder {\n\n" +
                "        public Builder build() {\n" +
                "            return this;\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        });
        templates.put(KEY_SINGLETON, new String[]{
                "Singleton",
                "public class %s {\n\n" +
                "    private static volatile %s instance;\n\n" +
                "    private %s() {}\n\n" +
                "    public static %s getInstance() {\n" +
                "        if (instance == null) {\n" +
                "            synchronized (%s.class) {\n" +
                "                if (instance == null) {\n" +
                "                    instance = new %s();\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        return instance;\n" +
                "    }\n" +
                "}\n"
        });
        templates.put(KEY_FACTORY, new String[]{
                "Factory Method",
                "public class %s {\n\n" +
                "    public static %s create(String type) {\n" +
                "        switch (type) {\n" +
                "            default:\n" +
                "                throw new IllegalArgumentException(\"Unknown type: \" + type);\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        });
        templates.put(KEY_OBSERVER, new String[]{
                "Observer Pattern",
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n\n" +
                "public class %s {\n\n" +
                "    public interface Listener {\n" +
                "        void onEvent(String event);\n" +
                "    }\n\n" +
                "    private final List<Listener> listeners = new ArrayList<>();\n\n" +
                "    public void addListener(Listener l) { listeners.add(l); }\n" +
                "    public void removeListener(Listener l) { listeners.remove(l); }\n\n" +
                "    public void notify(String event) {\n" +
                "        for (Listener l : listeners) l.onEvent(event);\n" +
                "    }\n" +
                "}\n"
        });
    }

    private FileTemplates() {}

    /** Повертає всі шаблони: key → [displayName, templateCode]. */
    public static Map<String, String[]> getAll() {
        return new LinkedHashMap<>(templates);
    }

    /** Повертає шаблон за ключем або null. */
    public static String[] get(String key) {
        return templates.get(key);
    }

    /** Повертає список назв шаблонів. */
    public static String[] getDisplayNames() {
        String[] names = new String[templates.size()];
        int i = 0;
        for (String[] v : templates.values()) {
            names[i++] = v[0];
        }
        return names;
    }

    /** Повертає ключі шаблонів у порядку. */
    public static String[] getKeys() {
        return templates.keySet().toArray(new String[0]);
    }

    /** Форматує шаблон під ім'я класу. */
    public static String format(String templateCode, String className) {
        if (templateCode == null) return "";
        // For singleton/builder/factory — replace %s with className multiple times
        String result = templateCode;
        while (result.contains("%s")) {
            result = result.replaceFirst("%s", className);
        }
        return result;
    }
}
