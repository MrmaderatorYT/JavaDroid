package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Реєстр курсів навчання. Усі курси реєструються тут при першому звертанні.
 *
 * <p>Мова — глобальна для всього навчального центру (uk/en), зберігається тут.
 * UI слухає зміну через {@link #LANG_UK}/{@link #LANG_EN}.</p>
 */
public final class CourseRegistry {

    /** Українська. */
    public static final int LANG_UK = 0;
    /** Англійська. */
    public static final int LANG_EN = 1;

    private static volatile CourseRegistry instance;

    private final List<Course> courses = new ArrayList<>();
    private volatile int language = LANG_UK;

    private CourseRegistry() {
        // Реєстрація курсів при першому створенні.
        addCourse(JavaReferenceCourse.create());
        addCourse(Jdk8DeepDiveCourse.create());
        addCourse(EssentialsCourse.create());
        addCourse(AdvancedJavaCourse.create());
        addCourse(AlgorithmsCourse.create());
        addCourse(ArchitectureCourse.create());
        addCourse(NetworkCourse.create());
        addCourse(TestingCourse.create());
        addCourse(SpringBootCourse.create());
        addCourse(DevOpsCourse.create());
        addCourse(JavaTutorials.create());
    }

    private void addCourse(Course course) {
        courses.add(BeginnerFriendlyContent.apply(course));
    }

    public static CourseRegistry getInstance() {
        if (instance == null) {
            synchronized (CourseRegistry.class) {
                if (instance == null) instance = new CourseRegistry();
            }
        }
        return instance;
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public Course getCourse(String id) {
        for (Course c : courses) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int lang) {
        if (lang == LANG_UK || lang == LANG_EN) {
            this.language = lang;
        }
    }

    public boolean isEnglish() {
        return language == LANG_EN;
    }
}
