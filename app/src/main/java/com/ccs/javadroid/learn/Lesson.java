package com.ccs.javadroid.learn;

import java.util.List;

/**
 * Урок. Має ідентифікатор (унікальний у межах курсу), заголовок обома мовами
 * та список блоків контенту обома мовами.
 *
 * <p>Кожен урок несе одразу обидві мови — це дозволяє перемикати мову
 * «на льоту» без перечитування моделі.</p>
 */
public final class Lesson {

    public final String id;
    /** [0] = uk, [1] = en. */
    public final String[] title = new String[2];
    /** [0] = uk, [1] = en. */
    public final List<LessonBlock>[] content;

    @SuppressWarnings("unchecked")
    public Lesson(String id, String titleUk, String titleEn,
                  List<LessonBlock> contentUk, List<LessonBlock> contentEn) {
        this.id = id;
        this.title[0] = titleUk;
        this.title[1] = titleEn;
        this.content = new List[] { contentUk, contentEn };
    }

    public String title(int lang) {
        return title[lang];
    }

    public List<LessonBlock> content(int lang) {
        return content[lang];
    }
}
