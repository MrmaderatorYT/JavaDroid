package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Курс: збірка розділів. Ідентифікатор унікальний у {@link CourseRegistry}.
 */
public final class Course {

    public final String id;
    /** [0] = uk, [1] = en. */
    public final String[] title = new String[2];
    /** [0] = uk, [1] = en. */
    public final String[] description = new String[2];
    public final List<Chapter> chapters = new ArrayList<>();

    public Course(String id, String titleUk, String titleEn,
                  String descUk, String descEn) {
        this.id = id;
        this.title[0] = titleUk;
        this.title[1] = titleEn;
        this.description[0] = descUk;
        this.description[1] = descEn;
    }

    public Course add(Chapter chapter) {
        chapters.add(chapter);
        return this;
    }

    public String title(int lang) {
        return title[lang];
    }

    public String description(int lang) {
        return description[lang];
    }

    /** Плоский список усіх уроків курсу (для навігації далі/назад). */
    public List<Lesson> allLessons() {
        List<Lesson> out = new ArrayList<>();
        for (Chapter c : chapters) out.addAll(c.lessons);
        return out;
    }
}
