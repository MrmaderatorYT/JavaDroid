package com.ccs.javadroid.learn;

import java.util.List;

/**
 * Adds a short beginner-oriented study frame to every lesson.
 *
 * <p>The course files intentionally keep the core lesson content close to the
 * topic. This helper adds the repeated "how to learn this" guidance once, for
 * every course and every lesson.</p>
 */
final class BeginnerFriendlyContent {

    private static final String UK_MARKER = "Як працювати з цим уроком";
    private static final String EN_MARKER = "How to study this lesson";

    private BeginnerFriendlyContent() {
    }

    static Course apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.lessons) {
                addStudyFrame(lesson.content[0], lesson.title[0], true);
                addStudyFrame(lesson.content[1], lesson.title[1], false);
            }
        }
        return course;
    }

    private static void addStudyFrame(List<LessonBlock> blocks, String title, boolean uk) {
        if (alreadyAdded(blocks, uk ? UK_MARKER : EN_MARKER)) {
            return;
        }

        if (uk) {
            blocks.add(LessonBlock.heading(UK_MARKER));
            blocks.add(LessonBlock.paragraph(
                    "Тема «" + title + "» краще засвоюється маленькими кроками: спершу "
                    + "зрозумійте головну ідею, потім перепишіть приклад вручну, і лише після "
                    + "цього змінюйте код під власну задачу."));
            blocks.add(LessonBlock.list(
                    "Що зрозуміти: яку проблему вирішує тема і де вона трапляється у реальних Java/Android-проектах.",
                    "Що запустити: найменший приклад з уроку або його спрощену версію у JavaDroid.",
                    "Що змінити: одне значення, одну умову або один метод, щоб побачити інший результат.",
                    "Що записати собі: нові ключові слова, назви класів і типову помилку з попередження.",
                    "Міні-перевірка: поясніть приклад уголос так, ніби навчаєте людину, яка бачить Java вперше."));
            blocks.add(LessonBlock.note(
                    "Середовище курсу орієнтоване на Android SDK 26 і компілятор JDK 8. "
                    + "Тому використовуйте явні типи замість var, звичайні class замість record, "
                    + "класичний switch замість arrow/switch expression, а для HTTP — Java 8-сумісні API."));
        } else {
            blocks.add(LessonBlock.heading(EN_MARKER));
            blocks.add(LessonBlock.paragraph(
                    "The topic \"" + title + "\" is easiest to learn in small steps: first "
                    + "understand the main idea, then type the example by hand, and only after "
                    + "that adapt the code to your own task."));
            blocks.add(LessonBlock.list(
                    "What to understand: which problem this topic solves and where it appears in real Java/Android projects.",
                    "What to run: the smallest example from the lesson, or a simplified version, inside JavaDroid.",
                    "What to change: one value, one condition, or one method so you can observe a different result.",
                    "What to write down: new keywords, class names, and the common mistake from the warning block.",
                    "Mini-check: explain the example out loud as if teaching someone who sees Java for the first time."));
            blocks.add(LessonBlock.note(
                    "This course targets Android SDK 26 and a JDK 8 compiler. Use explicit types "
                    + "instead of var, regular classes instead of records, classic switch instead "
                    + "of arrow/switch expressions, and Java 8-compatible APIs for HTTP."));
        }
    }

    private static boolean alreadyAdded(List<LessonBlock> blocks, String marker) {
        for (LessonBlock block : blocks) {
            if (block.type == LessonBlock.HEADING && marker.equals(block.text)) {
                return true;
            }
        }
        return false;
    }
}
