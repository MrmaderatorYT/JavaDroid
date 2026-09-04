package com.ccs.javadroid.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Процес-локальний буфер коду, який AI хоче вставити у відкритий редактор.
 *
 * AiChatActivity працює в окремому вікні поверх MainActivity (редактора), а Cursor
 * активного редактора живе саме в MainActivity. Тому перенесення згенерованого коду
 * з чату в редактор відбувається через цей статичний буфер: пишемо сюди під час сесії
 * чату, а MainActivity.onResume дренує чергу й застосовує її до activeEditor.
 *
 * Буфер зберігає порядок вставок і підтримує кілька операцій за сесію — як кнопку
 * "Вставити", так і інструмент insertCode агента.
 */
public final class PendingEdits {

    public static final String LOCATION_CURSOR    = "cursor";
    public static final String LOCATION_APPEND    = "append";
    public static final String LOCATION_REPLACE   = "replace";

    /**
     * Replace one exact fragment, leaving the rest of the file alone.
     *
     * <p>The three modes above can only add text or overwrite everything, so an
     * agent asked to change one method had to choose between pasting a second
     * copy of it at the cursor and rewriting the whole file from memory. Both
     * look to the user like "it pasted the code instead of editing it", and the
     * second also risks losing anything the model did not bother to reproduce.</p>
     */
    public static final String LOCATION_PATCH     = "patch";

    /** Один запит на вставку коду. */
    public static final class Edit {
        public final String code;
        public final String location;
        /** For {@link #LOCATION_PATCH}: the exact text to replace. */
        public final String find;

        public Edit(String code, String location) {
            this(code, location, null);
        }

        public Edit(String code, String location, String find) {
            this.code = code == null ? "" : code;
            this.location = location == null ? LOCATION_CURSOR : location;
            this.find = find;
        }
    }

    private static final Deque<Edit> queue = new ArrayDeque<>();

    private PendingEdits() {}

    /** Додати код у чергу на вставку. Потокобезпечно через синхронізацію на черзі. */
    public static void add(String code, String location) {
        if (code == null || code.isEmpty()) return;
        synchronized (queue) {
            queue.add(new Edit(code, location));
        }
    }

    /**
     * Queues a replacement of {@code find} by {@code code}.
     *
     * <p>Empty replacement text is allowed here — deleting a fragment is a real
     * edit — so this does not share the guard above.</p>
     */
    public static void addPatch(String find, String code) {
        if (find == null || find.isEmpty()) return;
        synchronized (queue) {
            queue.add(new Edit(code, LOCATION_PATCH, find));
        }
    }

    /** Чи є хоть одна відкладена вставка. */
    public static boolean hasPending() {
        synchronized (queue) {
            return !queue.isEmpty();
        }
    }

    /**
     * Витягти всі відкладені вставки у порядку додавання й очистити чергу.
     * Повертає незмінний список (може бути порожнім).
     */
    public static List<Edit> drain() {
        synchronized (queue) {
            if (queue.isEmpty()) return Collections.emptyList();
            List<Edit> out = new ArrayList<>(queue);
            queue.clear();
            return out;
        }
    }

    /** Скасувати всі відкладені вставки (наприклад, якщо користувач закрив чат). */
    public static void clear() {
        synchronized (queue) {
            queue.clear();
        }
    }
}
