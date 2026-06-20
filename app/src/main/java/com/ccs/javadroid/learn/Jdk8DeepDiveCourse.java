package com.ccs.javadroid.learn;

/**
 * A very detailed JDK 8-first course for students who need long-form reading,
 * careful API contracts, and hands-on drills.
 */
final class Jdk8DeepDiveCourse {

    static Course create() {
        Course c = new Course(
                "jdk8_deep_dive",
                "JDK 8 Deep Dive: колекції, ітератори, Stream",
                "JDK 8 Deep Dive: Collections, Iterators, Stream",
                "Максимально детальний JDK 8 курс у campus-стилі: контракти API, "
                + "List/Set/Map, Enumeration, Iterator, ListIterator, Comparator, Stream API "
                + "та багато практичних вправ.",
                "A very detailed JDK 8 campus-style course: API contracts, List/Set/Map, "
                + "Enumeration, Iterator, ListIterator, Comparator, Stream API, and many drills.");

        Jdk8DeepDiveChapters.add(c);
        Jdk8DeepDiveMoreChapters.add(c);
        Jdk8DeepDiveAdvancedChapters.add(c);
        Jdk8BytecodeChapters.add(c);
        Jdk8BytecodeAdvancedChapters.add(c);
        Jdk8BytecodeCookbookChapters.add(c);
        return c;
    }
}
