package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JDK 8 bytecode-only cookbook: instruction families, lowering patterns,
 * and drills based on javap output.
 */
final class Jdk8BytecodeCookbookChapters {

    private Jdk8BytecodeCookbookChapters() {
    }

    static void add(Course s) {
        Chapter ch = new Chapter(
                "JDK 8 bytecode cookbook: інструкції, lowering, drills",
                "JDK 8 bytecode cookbook: instructions, lowering, drills");
        ch.add(materialConstantsLoadsStoresStack());
        ch.add(materialNumericConversionsAndComparisons());
        ch.add(materialBranchesSwitchCookbook());
        ch.add(materialFieldsVolatileFinalTransient());
        ch.add(materialInnerAnonymousEnumBytecode());
        ch.add(materialMethodHandleInvokePackage());
        ch.add(materialAnnotationAttributesBytecode());
        ch.add(materialRawClassFileReading());
        ch.add(materialJavapDrills());
        s.add(ch);
    }

    private static Lesson materialConstantsLoadsStoresStack() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Constants, loads, stores, stack manipulation"));
        uk.add(LessonBlock.paragraph(
                "Перший рівень чистого bytecode — автоматично читати, як значення потрапляють "
                + "на operand stack і назад у local variables. У JVM майже все відбувається через "
                + "push на stack, операцію, store або return."));
        uk.add(LessonBlock.table(
                "Група\tІнструкції\tЩо роблять",
                Arrays.asList(
                        "int constants\ticonst_m1..iconst_5, bipush, sipush, ldc\tпокласти int на stack",
                        "long/float/double constants\tlconst_0, fconst_1, dconst_0, ldc2_w\tпокласти wide/value constants",
                        "reference constants\taconst_null, ldc \"text\"\tnull або String/Class constant",
                        "loads\tiload, lload, fload, dload, aload\tlocal -> stack",
                        "stores\tistore, lstore, fstore, dstore, astore\tstack -> local",
                        "stack ops\tpop, pop2, dup, dup_x1, dup2, swap\tпереставити або дублювати stack values")));
        uk.add(LessonBlock.code(
                "public int demo(int x) {\n"
                + "    int y = 10;\n"
                + "    return x + y;\n"
                + "}\n"
                + "\n"
                + "# javap -c ідея:\n"
                + "0: bipush 10     // push constant 10\n"
                + "2: istore_2      // y = 10\n"
                + "3: iload_1       // push x\n"
                + "4: iload_2       // push y\n"
                + "5: iadd          // x + y\n"
                + "6: ireturn"));
        uk.add(LessonBlock.heading("dup у реальному коді"));
        uk.add(LessonBlock.code(
                "Object o = new Object();\n"
                + "\n"
                + "# bytecode pattern:\n"
                + "new java/lang/Object\n"
                + "dup\n"
                + "invokespecial java/lang/Object/<init>()V\n"
                + "astore_1"));
        uk.add(LessonBlock.note(
                "dup копіює reference на top of stack. Один reference піде в constructor "
                + "<init>, другий лишиться, щоб зберегти об'єкт у local variable."));
        uk.add(LessonBlock.warning(
                "Stack category matters: int/reference/float займають category 1, long/double — "
                + "category 2. Тому pop2/dup2 існують не випадково."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте int y = 5, 100, 1000 і порівняйте iconst/bipush/sipush/ldc.",
                "Скомпілюйте String s = \"abc\" і знайдіть ldc.",
                "Скомпілюйте long x = 1L і знайдіть lconst_1 або ldc2_w.",
                "Розпишіть stack state для new Object().",
                "Поясніть, чому long/double займають два local slots."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Constants, loads, stores, stack manipulation"));
        en.add(LessonBlock.paragraph(
                "The first level of pure bytecode is reading how values move onto the operand "
                + "stack and back into local variables. In JVM, almost everything happens through "
                + "push to stack, operation, store, or return."));
        en.add(LessonBlock.table(
                "Group\tInstructions\tPurpose",
                Arrays.asList(
                        "int constants\ticonst_m1..iconst_5, bipush, sipush, ldc\tpush int to stack",
                        "long/float/double constants\tlconst_0, fconst_1, dconst_0, ldc2_w\tpush wide/value constants",
                        "reference constants\taconst_null, ldc \"text\"\tnull or String/Class constant",
                        "loads\tiload, lload, fload, dload, aload\tlocal -> stack",
                        "stores\tistore, lstore, fstore, dstore, astore\tstack -> local",
                        "stack ops\tpop, pop2, dup, dup_x1, dup2, swap\trearrange or duplicate stack values")));
        en.add(LessonBlock.code(
                "public int demo(int x) {\n"
                + "    int y = 10;\n"
                + "    return x + y;\n"
                + "}\n"
                + "\n"
                + "# javap -c idea:\n"
                + "0: bipush 10     // push constant 10\n"
                + "2: istore_2      // y = 10\n"
                + "3: iload_1       // push x\n"
                + "4: iload_2       // push y\n"
                + "5: iadd          // x + y\n"
                + "6: ireturn"));
        en.add(LessonBlock.heading("dup in real code"));
        en.add(LessonBlock.code(
                "Object o = new Object();\n"
                + "\n"
                + "# bytecode pattern:\n"
                + "new java/lang/Object\n"
                + "dup\n"
                + "invokespecial java/lang/Object/<init>()V\n"
                + "astore_1"));
        en.add(LessonBlock.note(
                "dup copies the reference on top of stack. One reference goes into constructor "
                + "<init>, the other remains to store the object in local variable."));
        en.add(LessonBlock.warning(
                "Stack category matters: int/reference/float are category 1, long/double are "
                + "category 2. That is why pop2/dup2 exist."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile int y = 5, 100, 1000 and compare iconst/bipush/sipush/ldc.",
                "Compile String s = \"abc\" and find ldc.",
                "Compile long x = 1L and find lconst_1 or ldc2_w.",
                "Write stack state for new Object().",
                "Explain why long/double use two local slots."));

        return new Lesson("jdk8.bytecode.cookbook.1", "Constants/load/store/stack", "Constants/load/store/stack", uk, en);
    }

    private static Lesson materialNumericConversionsAndComparisons() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Numeric conversions і comparisons"));
        uk.add(LessonBlock.paragraph(
                "Bytecode має окремі інструкції для перетворення числових типів. Java source "
                + "виглядає просто, але javac явно вставляє i2l, i2d, d2i тощо. Порівняння long, "
                + "float, double також не завжди працює як int: використовуються lcmp, fcmpl/fcmpg, dcmpl/dcmpg."));
        uk.add(LessonBlock.table(
                "Інструкція\tСенс",
                Arrays.asList(
                        "i2l\tint -> long",
                        "i2f\tint -> float",
                        "i2d\tint -> double",
                        "l2i\tlong -> int",
                        "f2i\tfloat -> int",
                        "d2i\tdouble -> int",
                        "i2b / i2c / i2s\tint -> byte/char/short",
                        "lcmp\tпорівняти long, результат int -1/0/1",
                        "fcmpl/fcmpg\tпорівняти float з різною поведінкою для NaN",
                        "dcmpl/dcmpg\tпорівняти double з різною поведінкою для NaN")));
        uk.add(LessonBlock.code(
                "public long widen(int x) {\n"
                + "    return x;\n"
                + "}\n"
                + "\n"
                + "# javap -c:\n"
                + "0: iload_1\n"
                + "1: i2l\n"
                + "2: lreturn"));
        uk.add(LessonBlock.code(
                "public boolean greater(long a, long b) {\n"
                + "    return a > b;\n"
                + "}\n"
                + "\n"
                + "# ідея:\n"
                + "lload_1\n"
                + "lload_3\n"
                + "lcmp\n"
                + "ifle falseLabel"));
        uk.add(LessonBlock.warning(
                "byte, short і char в арифметиці зазвичай піднімаються до int. Тому bytecode "
                + "часто працює з int навіть тоді, коли source має byte або short."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте int -> long, int -> double, double -> int і знайдіть conversion інструкції.",
                "Скомпілюйте порівняння long і знайдіть lcmp.",
                "Скомпілюйте порівняння double і знайдіть dcmpg або dcmpl.",
                "Скомпілюйте byte a=1,b=2; byte c=(byte)(a+b); і поясніть casts.",
                "Поясніть, чому для int немає icmp інструкції, а є if_icmp*."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Numeric conversions and comparisons"));
        en.add(LessonBlock.paragraph(
                "Bytecode has separate instructions for numeric conversions. Java source looks "
                + "simple, but javac explicitly inserts i2l, i2d, d2i, and similar. Comparing "
                + "long, float, double is also not the same as int: lcmp, fcmpl/fcmpg, dcmpl/dcmpg are used."));
        en.add(LessonBlock.table(
                "Instruction\tMeaning",
                Arrays.asList(
                        "i2l\tint -> long",
                        "i2f\tint -> float",
                        "i2d\tint -> double",
                        "l2i\tlong -> int",
                        "f2i\tfloat -> int",
                        "d2i\tdouble -> int",
                        "i2b / i2c / i2s\tint -> byte/char/short",
                        "lcmp\tcompare long, result int -1/0/1",
                        "fcmpl/fcmpg\tcompare float with different NaN behavior",
                        "dcmpl/dcmpg\tcompare double with different NaN behavior")));
        en.add(LessonBlock.code(
                "public long widen(int x) {\n"
                + "    return x;\n"
                + "}\n"
                + "\n"
                + "# javap -c:\n"
                + "0: iload_1\n"
                + "1: i2l\n"
                + "2: lreturn"));
        en.add(LessonBlock.code(
                "public boolean greater(long a, long b) {\n"
                + "    return a > b;\n"
                + "}\n"
                + "\n"
                + "# idea:\n"
                + "lload_1\n"
                + "lload_3\n"
                + "lcmp\n"
                + "ifle falseLabel"));
        en.add(LessonBlock.warning(
                "byte, short, and char are usually promoted to int in arithmetic. Therefore "
                + "bytecode often works with int even when source has byte or short."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile int -> long, int -> double, double -> int and find conversion instructions.",
                "Compile long comparison and find lcmp.",
                "Compile double comparison and find dcmpg or dcmpl.",
                "Compile byte a=1,b=2; byte c=(byte)(a+b); and explain casts.",
                "Explain why there is no icmp instruction for int, but if_icmp* exists."));

        return new Lesson("jdk8.bytecode.cookbook.2", "Numeric conversions", "Numeric conversions", uk, en);
    }

    private static Lesson materialBranchesSwitchCookbook() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Branches, tableswitch, lookupswitch, String switch"));
        uk.add(LessonBlock.paragraph(
                "У JVM немає if/else/switch як source-конструкцій. Є conditional jumps і switch "
                + "інструкції. javac обирає tableswitch, якщо int-ключі щільні, і lookupswitch, "
                + "якщо ключі розріджені. String switch у Java 8 компілюється через hashCode + equals + switch."));
        uk.add(LessonBlock.table(
                "Source\tТиповий bytecode",
                Arrays.asList(
                        "if (x == 0)\tiload x; ifne label",
                        "if (a < b)\tiload a; iload b; if_icmpge label",
                        "switch 1,2,3\ttableswitch",
                        "switch 10,100,1000\tlookupswitch",
                        "switch String\thashCode switch + equals перевірки")));
        uk.add(LessonBlock.code(
                "switch (x) {\n"
                + "    case 1: return \"one\";\n"
                + "    case 2: return \"two\";\n"
                + "    case 3: return \"three\";\n"
                + "    default: return \"other\";\n"
                + "}\n"
                + "\n"
                + "# щільні ключі -> tableswitch"));
        uk.add(LessonBlock.code(
                "switch (s) {\n"
                + "    case \"A\": return 1;\n"
                + "    case \"B\": return 2;\n"
                + "    default: return 0;\n"
                + "}\n"
                + "\n"
                + "# Java 8 ідея:\n"
                + "# 1) s.hashCode()\n"
                + "# 2) switch по hash\n"
                + "# 3) equals для захисту від collisions\n"
                + "# 4) switch/branch до результату"));
        uk.add(LessonBlock.warning(
                "String switch завжди має враховувати hash collisions. Тому javap-вивід може "
                + "виглядати значно складніше, ніж source."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте switch з case 1,2,3,4 і знайдіть tableswitch.",
                "Скомпілюйте switch з case 1,100,1000 і знайдіть lookupswitch.",
                "Скомпілюйте String switch і знайдіть hashCode та equals.",
                "Зробіть if/else chain і порівняйте з switch.",
                "Поясніть, чому default потрібен на bytecode-рівні."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Branches, tableswitch, lookupswitch, String switch"));
        en.add(LessonBlock.paragraph(
                "JVM has no if/else/switch as source constructs. It has conditional jumps and "
                + "switch instructions. javac chooses tableswitch when int keys are dense and "
                + "lookupswitch when keys are sparse. String switch in Java 8 compiles through hashCode + equals + switch."));
        en.add(LessonBlock.table(
                "Source\tTypical bytecode",
                Arrays.asList(
                        "if (x == 0)\tiload x; ifne label",
                        "if (a < b)\tiload a; iload b; if_icmpge label",
                        "switch 1,2,3\ttableswitch",
                        "switch 10,100,1000\tlookupswitch",
                        "switch String\thashCode switch + equals checks")));
        en.add(LessonBlock.code(
                "switch (x) {\n"
                + "    case 1: return \"one\";\n"
                + "    case 2: return \"two\";\n"
                + "    case 3: return \"three\";\n"
                + "    default: return \"other\";\n"
                + "}\n"
                + "\n"
                + "# dense keys -> tableswitch"));
        en.add(LessonBlock.code(
                "switch (s) {\n"
                + "    case \"A\": return 1;\n"
                + "    case \"B\": return 2;\n"
                + "    default: return 0;\n"
                + "}\n"
                + "\n"
                + "# Java 8 idea:\n"
                + "# 1) s.hashCode()\n"
                + "# 2) switch by hash\n"
                + "# 3) equals to protect from collisions\n"
                + "# 4) switch/branch to result"));
        en.add(LessonBlock.warning(
                "String switch must always account for hash collisions. Therefore javap output "
                + "may look much more complex than source."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile switch with case 1,2,3,4 and find tableswitch.",
                "Compile switch with case 1,100,1000 and find lookupswitch.",
                "Compile String switch and find hashCode and equals.",
                "Create if/else chain and compare with switch.",
                "Explain why default is needed at bytecode level."));

        return new Lesson("jdk8.bytecode.cookbook.3", "Branches і switch", "Branches and switch", uk, en);
    }

    private static Lesson materialFieldsVolatileFinalTransient() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Fields: static, final, volatile, transient"));
        uk.add(LessonBlock.paragraph(
                "Поля в class file мають descriptors, access flags і attributes. На bytecode-рівні "
                + "читання/запис поля — це getfield/putfield або getstatic/putstatic. Модифікатори "
                + "final, volatile, transient представлені flags."));
        uk.add(LessonBlock.table(
                "Java field\tBytecode flag / pattern",
                Arrays.asList(
                        "static\tACC_STATIC + getstatic/putstatic",
                        "final\tACC_FINAL; для compile-time constants може бути ConstantValue",
                        "volatile\tACC_VOLATILE; JVM memory semantics",
                        "transient\tACC_TRANSIENT; serialization ігнорує поле",
                        "private\tACC_PRIVATE; access перевіряється verifier/runtime")));
        uk.add(LessonBlock.code(
                "class Flags {\n"
                + "    static int counter;\n"
                + "    final int id = 10;\n"
                + "    volatile boolean running;\n"
                + "    transient String cache;\n"
                + "}\n"
                + "\n"
                + "# javap -v Flags покаже flags полів."));
        uk.add(LessonBlock.paragraph(
                "final instance field зазвичай записується у constructor через putfield. "
                + "static final compile-time constant може мати ConstantValue attribute і бути "
                + "inlined у клієнтів."));
        uk.add(LessonBlock.warning(
                "volatile не змінює opcode getfield/putfield на щось інше у javap-виводі. "
                + "Семантика volatile закладена у field access rules JVM і memory model."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть поля static/final/volatile/transient і відкрийте javap -v.",
                "Знайдіть ConstantValue для static final int.",
                "Знайдіть putfield final поля у constructor.",
                "Поясніть, чому transient видно у class file, хоча це про serialization.",
                "Поясніть, чому volatile важливий, навіть якщо opcode виглядає як звичайний getfield."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Fields: static, final, volatile, transient"));
        en.add(LessonBlock.paragraph(
                "Fields in class file have descriptors, access flags, and attributes. At bytecode "
                + "level, field read/write is getfield/putfield or getstatic/putstatic. Modifiers "
                + "final, volatile, transient are represented as flags."));
        en.add(LessonBlock.table(
                "Java field\tBytecode flag / pattern",
                Arrays.asList(
                        "static\tACC_STATIC + getstatic/putstatic",
                        "final\tACC_FINAL; compile-time constants may have ConstantValue",
                        "volatile\tACC_VOLATILE; JVM memory semantics",
                        "transient\tACC_TRANSIENT; serialization ignores field",
                        "private\tACC_PRIVATE; access checked by verifier/runtime")));
        en.add(LessonBlock.code(
                "class Flags {\n"
                + "    static int counter;\n"
                + "    final int id = 10;\n"
                + "    volatile boolean running;\n"
                + "    transient String cache;\n"
                + "}\n"
                + "\n"
                + "# javap -v Flags shows field flags."));
        en.add(LessonBlock.paragraph(
                "A final instance field is usually written in constructor through putfield. "
                + "A static final compile-time constant may have ConstantValue attribute and be inlined into clients."));
        en.add(LessonBlock.warning(
                "volatile does not change getfield/putfield opcode into something else in javap output. "
                + "Volatile semantics are in JVM field access rules and memory model."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create static/final/volatile/transient fields and open javap -v.",
                "Find ConstantValue for static final int.",
                "Find putfield of final field in constructor.",
                "Explain why transient is visible in class file, although it is about serialization.",
                "Explain why volatile matters even if opcode looks like normal getfield."));

        return new Lesson("jdk8.bytecode.cookbook.4", "Field flags", "Field flags", uk, en);
    }

    private static Lesson materialInnerAnonymousEnumBytecode() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Inner classes, anonymous classes, enum bytecode"));
        uk.add(LessonBlock.paragraph(
                "Java source приховує багато деталей. Inner class отримує окремий .class файл. "
                + "Non-static inner class має synthetic reference на outer instance. Anonymous class "
                + "теж стає окремим .class. enum компілюється у final class, який extends java.lang.Enum."));
        uk.add(LessonBlock.table(
                "Source construct\tBytecode/class files",
                Arrays.asList(
                        "static nested class\tOuter$Nested.class без outer this",
                        "non-static inner class\tOuter$Inner.class + synthetic this$0",
                        "anonymous class\tOuter$1.class, Outer$2.class...",
                        "enum\tclass extends java/lang/Enum + values() + valueOf()",
                        "private access між nested/outer\tможливі synthetic accessor methods")));
        uk.add(LessonBlock.code(
                "class Outer {\n"
                + "    private int x = 10;\n"
                + "\n"
                + "    class Inner {\n"
                + "        int read() { return x; }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# javac створить Outer.class і Outer$Inner.class."));
        uk.add(LessonBlock.code(
                "enum Color { RED, GREEN }\n"
                + "\n"
                + "# Ідея після компіляції:\n"
                + "# final class Color extends java.lang.Enum<Color>\n"
                + "# static final Color RED;\n"
                + "# static final Color GREEN;\n"
                + "# static Color[] values();\n"
                + "# static Color valueOf(String name);"));
        uk.add(LessonBlock.warning(
                "Не дивуйтеся файлам Outer$1.class або synthetic access$000 методам у старому Java 8 bytecode. "
                + "Це normal compiler lowering для inner/anonymous доступу."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть static nested і non-static inner class, порівняйте .class файли.",
                "Через javap -v знайдіть InnerClasses attribute.",
                "Створіть anonymous Runnable і знайдіть Outer$1.class.",
                "Скомпілюйте enum і знайдіть values/valueOf.",
                "Знайдіть synthetic поле this$0 у non-static inner class."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Inner classes, anonymous classes, enum bytecode"));
        en.add(LessonBlock.paragraph(
                "Java source hides many details. An inner class gets a separate .class file. "
                + "A non-static inner class has a synthetic reference to outer instance. Anonymous "
                + "class also becomes a separate .class. enum compiles into final class extending java.lang.Enum."));
        en.add(LessonBlock.table(
                "Source construct\tBytecode/class files",
                Arrays.asList(
                        "static nested class\tOuter$Nested.class without outer this",
                        "non-static inner class\tOuter$Inner.class + synthetic this$0",
                        "anonymous class\tOuter$1.class, Outer$2.class...",
                        "enum\tclass extends java/lang/Enum + values() + valueOf()",
                        "private access between nested/outer\tsynthetic accessor methods may appear")));
        en.add(LessonBlock.code(
                "class Outer {\n"
                + "    private int x = 10;\n"
                + "\n"
                + "    class Inner {\n"
                + "        int read() { return x; }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# javac creates Outer.class and Outer$Inner.class."));
        en.add(LessonBlock.code(
                "enum Color { RED, GREEN }\n"
                + "\n"
                + "# Idea after compilation:\n"
                + "# final class Color extends java.lang.Enum<Color>\n"
                + "# static final Color RED;\n"
                + "# static final Color GREEN;\n"
                + "# static Color[] values();\n"
                + "# static Color valueOf(String name);"));
        en.add(LessonBlock.warning(
                "Do not be surprised by Outer$1.class files or synthetic access$000 methods in old Java 8 bytecode. "
                + "This is normal compiler lowering for inner/anonymous access."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create static nested and non-static inner class, compare .class files.",
                "Find InnerClasses attribute through javap -v.",
                "Create anonymous Runnable and find Outer$1.class.",
                "Compile enum and find values/valueOf.",
                "Find synthetic field this$0 in non-static inner class."));

        return new Lesson("jdk8.bytecode.cookbook.5", "Inner/anonymous/enum bytecode", "Inner/anonymous/enum bytecode", uk, en);
    }

    private static Lesson materialMethodHandleInvokePackage() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("java.lang.invoke у Java 8"));
        uk.add(LessonBlock.paragraph(
                "Пакет java.lang.invoke — низькорівневий механізм динамічних викликів. Він важливий "
                + "для розуміння invokedynamic і lambdas. Основні типи: MethodHandle, MethodType, "
                + "MethodHandles.Lookup. Це не те саме, що reflection, хоча задачі іноді схожі."));
        uk.add(LessonBlock.table(
                "Клас\tСенс",
                Arrays.asList(
                        "MethodHandle\ttyped, directly executable reference to method/field/constructor",
                        "MethodType\treturn type + parameter types",
                        "MethodHandles.Lookup\tоб'єкт доступу для пошуку handles",
                        "CallSite\tточка виклику для invokedynamic",
                        "LambdaMetafactory\tbootstrap factory для Java 8 lambdas")));
        uk.add(LessonBlock.code(
                "MethodHandles.Lookup lookup = MethodHandles.lookup();\n"
                + "MethodType type = MethodType.methodType(int.class, int.class, int.class);\n"
                + "MethodHandle mh = lookup.findStatic(Math.class, \"max\", type);\n"
                + "\n"
                + "int result = (int) mh.invokeExact(3, 7);\n"
                + "System.out.println(result); // 7"));
        uk.add(LessonBlock.paragraph(
                "invokeExact вимагає точного збігу типів. invoke більш гнучкий і може робити "
                + "деякі адаптації. MethodHandle зазвичай швидший і більш JVM-friendly, ніж reflection, "
                + "але складніший для початківця."));
        uk.add(LessonBlock.warning(
                "MethodHandle throws Throwable, не тільки Exception. У навчальних прикладах це часто "
                + "означає, що main доведеться оголосити throws Throwable."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Знайдіть MethodHandle для static Math.abs(int).",
                "Знайдіть MethodHandle для instance String.substring(int).",
                "Порівняйте invokeExact і invoke.",
                "Навмисно передайте неправильний тип і прочитайте WrongMethodTypeException.",
                "Поясніть, як MethodHandle пов'язаний з invokedynamic."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("java.lang.invoke in Java 8"));
        en.add(LessonBlock.paragraph(
                "java.lang.invoke is a low-level dynamic invocation mechanism. It matters for "
                + "understanding invokedynamic and lambdas. Main types: MethodHandle, MethodType, "
                + "MethodHandles.Lookup. It is not the same as reflection, although tasks may look similar."));
        en.add(LessonBlock.table(
                "Class\tMeaning",
                Arrays.asList(
                        "MethodHandle\ttyped, directly executable reference to method/field/constructor",
                        "MethodType\treturn type + parameter types",
                        "MethodHandles.Lookup\taccess object for finding handles",
                        "CallSite\tinvocation point for invokedynamic",
                        "LambdaMetafactory\tbootstrap factory for Java 8 lambdas")));
        en.add(LessonBlock.code(
                "MethodHandles.Lookup lookup = MethodHandles.lookup();\n"
                + "MethodType type = MethodType.methodType(int.class, int.class, int.class);\n"
                + "MethodHandle mh = lookup.findStatic(Math.class, \"max\", type);\n"
                + "\n"
                + "int result = (int) mh.invokeExact(3, 7);\n"
                + "System.out.println(result); // 7"));
        en.add(LessonBlock.paragraph(
                "invokeExact requires exact type match. invoke is more flexible and may do some "
                + "adaptation. MethodHandle is usually faster and more JVM-friendly than reflection, "
                + "but harder for beginners."));
        en.add(LessonBlock.warning(
                "MethodHandle throws Throwable, not only Exception. In learning examples, this often "
                + "means main must declare throws Throwable."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Find MethodHandle for static Math.abs(int).",
                "Find MethodHandle for instance String.substring(int).",
                "Compare invokeExact and invoke.",
                "Pass a wrong type intentionally and read WrongMethodTypeException.",
                "Explain how MethodHandle is connected to invokedynamic."));

        return new Lesson("jdk8.bytecode.cookbook.6", "java.lang.invoke", "java.lang.invoke", uk, en);
    }

    private static Lesson materialAnnotationAttributesBytecode() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Annotations як class file attributes"));
        uk.add(LessonBlock.paragraph(
                "Анотації у bytecode зберігаються як attributes. Retention визначає, де вони "
                + "залишаються: SOURCE зникає, CLASS лишається у .class, RUNTIME доступна reflection. "
                + "У javap -v шукайте RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, "
                + "RuntimeVisibleParameterAnnotations."));
        uk.add(LessonBlock.table(
                "Java annotation retention\tClass file effect",
                Arrays.asList(
                        "SOURCE\tнемає в .class",
                        "CLASS\tє в .class як RuntimeInvisibleAnnotations",
                        "RUNTIME\tє в .class і доступна reflection як RuntimeVisibleAnnotations",
                        "PARAMETER\tможе бути parameter annotation attribute",
                        "TYPE_USE\tможе з'являтися в type annotation attributes")));
        uk.add(LessonBlock.code(
                "@Retention(RetentionPolicy.RUNTIME)\n"
                + "@interface Marker {\n"
                + "    String value();\n"
                + "}\n"
                + "\n"
                + "@Marker(\"demo\")\n"
                + "class Annotated { }\n"
                + "\n"
                + "# javap -v Annotated:\n"
                + "# RuntimeVisibleAnnotations:\n"
                + "#   0: #...(#...=s#...)"));
        uk.add(LessonBlock.warning(
                "Annotation default values зберігаються в annotation interface class file, а не "
                + "дублюються всюди. Reflection підставляє default, якщо значення не задане."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть SOURCE, CLASS і RUNTIME анотації та порівняйте javap -v.",
                "Додайте annotation field value() і default.",
                "Додайте annotation на method parameter і знайдіть parameter annotations.",
                "Через reflection прочитайте тільки RUNTIME annotation.",
                "Поясніть різницю між visible і invisible annotation attributes."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Annotations as class file attributes"));
        en.add(LessonBlock.paragraph(
                "Annotations in bytecode are stored as attributes. Retention decides where they "
                + "remain: SOURCE disappears, CLASS stays in .class, RUNTIME is available to reflection. "
                + "In javap -v look for RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, "
                + "RuntimeVisibleParameterAnnotations."));
        en.add(LessonBlock.table(
                "Java annotation retention\tClass file effect",
                Arrays.asList(
                        "SOURCE\tnot in .class",
                        "CLASS\tin .class as RuntimeInvisibleAnnotations",
                        "RUNTIME\tin .class and reflection-visible as RuntimeVisibleAnnotations",
                        "PARAMETER\tmay be parameter annotation attribute",
                        "TYPE_USE\tmay appear in type annotation attributes")));
        en.add(LessonBlock.code(
                "@Retention(RetentionPolicy.RUNTIME)\n"
                + "@interface Marker {\n"
                + "    String value();\n"
                + "}\n"
                + "\n"
                + "@Marker(\"demo\")\n"
                + "class Annotated { }\n"
                + "\n"
                + "# javap -v Annotated:\n"
                + "# RuntimeVisibleAnnotations:\n"
                + "#   0: #...(#...=s#...)"));
        en.add(LessonBlock.warning(
                "Annotation default values are stored in the annotation interface class file, "
                + "not duplicated everywhere. Reflection supplies default if value is not set."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create SOURCE, CLASS, and RUNTIME annotations and compare javap -v.",
                "Add annotation field value() and default.",
                "Add annotation on method parameter and find parameter annotations.",
                "Read only RUNTIME annotation through reflection.",
                "Explain visible vs invisible annotation attributes."));

        return new Lesson("jdk8.bytecode.cookbook.7", "Annotation attributes", "Annotation attributes", uk, en);
    }

    private static Lesson materialRawClassFileReading() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Читання raw .class bytes"));
        uk.add(LessonBlock.paragraph(
                "Для глибокого bytecode корисно хоча б один раз прочитати .class як bytes. "
                + "Не треба писати повний parser одразу. Почніть з magic, minor_version, major_version "
                + "і constant_pool_count. Усі багатобайтові числа в class file — big-endian."));
        uk.add(LessonBlock.table(
                "Offset\tПоле\tРозмір",
                Arrays.asList(
                        "0\tmagic 0xCAFEBABE\t4 bytes",
                        "4\tminor_version\t2 bytes",
                        "6\tmajor_version\t2 bytes",
                        "8\tconstant_pool_count\t2 bytes",
                        "10+\tconstant_pool entries\tvariable")));
        uk.add(LessonBlock.code(
                "static int u1(byte[] data, int off) {\n"
                + "    return data[off] & 0xFF;\n"
                + "}\n"
                + "\n"
                + "static int u2(byte[] data, int off) {\n"
                + "    return ((data[off] & 0xFF) << 8)\n"
                + "         |  (data[off + 1] & 0xFF);\n"
                + "}\n"
                + "\n"
                + "static long u4(byte[] data, int off) {\n"
                + "    return ((long) u2(data, off) << 16) | u2(data, off + 2);\n"
                + "}"));
        uk.add(LessonBlock.code(
                "byte[] bytes = Files.readAllBytes(Paths.get(\"Hello.class\"));\n"
                + "long magic = u4(bytes, 0);\n"
                + "int minor = u2(bytes, 4);\n"
                + "int major = u2(bytes, 6);\n"
                + "int cpCount = u2(bytes, 8);\n"
                + "\n"
                + "System.out.println(Long.toHexString(magic)); // cafebabe\n"
                + "System.out.println(major); // Java 8 -> 52"));
        uk.add(LessonBlock.warning(
                "constant_pool індексується з 1, не з 0. Long і Double займають два entries. "
                + "Це класична пастка при написанні власного class parser."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Прочитайте magic/major/minor з власного .class.",
                "Перевірте, що Java 8 class має major 52.",
                "Прочитайте constant_pool_count.",
                "Додайте парсинг тільки CONSTANT_Utf8 entries.",
                "Поясніть, чому byte треба маскувати через & 0xFF."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Reading raw .class bytes"));
        en.add(LessonBlock.paragraph(
                "For deep bytecode, it helps to read .class as bytes at least once. You do not "
                + "need a full parser immediately. Start with magic, minor_version, major_version, "
                + "and constant_pool_count. All multi-byte numbers in class file are big-endian."));
        en.add(LessonBlock.table(
                "Offset\tField\tSize",
                Arrays.asList(
                        "0\tmagic 0xCAFEBABE\t4 bytes",
                        "4\tminor_version\t2 bytes",
                        "6\tmajor_version\t2 bytes",
                        "8\tconstant_pool_count\t2 bytes",
                        "10+\tconstant_pool entries\tvariable")));
        en.add(LessonBlock.code(
                "static int u1(byte[] data, int off) {\n"
                + "    return data[off] & 0xFF;\n"
                + "}\n"
                + "\n"
                + "static int u2(byte[] data, int off) {\n"
                + "    return ((data[off] & 0xFF) << 8)\n"
                + "         |  (data[off + 1] & 0xFF);\n"
                + "}\n"
                + "\n"
                + "static long u4(byte[] data, int off) {\n"
                + "    return ((long) u2(data, off) << 16) | u2(data, off + 2);\n"
                + "}"));
        en.add(LessonBlock.code(
                "byte[] bytes = Files.readAllBytes(Paths.get(\"Hello.class\"));\n"
                + "long magic = u4(bytes, 0);\n"
                + "int minor = u2(bytes, 4);\n"
                + "int major = u2(bytes, 6);\n"
                + "int cpCount = u2(bytes, 8);\n"
                + "\n"
                + "System.out.println(Long.toHexString(magic)); // cafebabe\n"
                + "System.out.println(major); // Java 8 -> 52"));
        en.add(LessonBlock.warning(
                "constant_pool is indexed from 1, not 0. Long and Double occupy two entries. "
                + "This is a classic trap when writing your own class parser."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Read magic/major/minor from your own .class.",
                "Verify Java 8 class has major 52.",
                "Read constant_pool_count.",
                "Add parsing for CONSTANT_Utf8 entries only.",
                "Explain why byte must be masked with & 0xFF."));

        return new Lesson("jdk8.bytecode.cookbook.8", "Raw .class bytes", "Raw .class bytes", uk, en);
    }

    private static Lesson materialJavapDrills() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Javap drills: 40 bytecode задач"));
        uk.add(LessonBlock.paragraph(
                "Цей урок — тренажер. Для кожної задачі: напишіть маленький Java 8 метод, "
                + "скомпілюйте, відкрийте javap -c -v, знайдіть потрібний bytecode-патерн і "
                + "коротко поясніть stack/local state."));
        uk.add(LessonBlock.list(
                "1. int add(int a, int b): знайти iload/iadd/ireturn.",
                "2. long add(long a, long b): знайти lload/ladd/lreturn і local slots.",
                "3. double avg(int a, int b): знайти i2d/dadd/ddiv.",
                "4. int abs(int x): знайти conditional branch.",
                "5. int max(int a, int b): знайти if_icmp*.",
                "6. int sumTo(int n): знайти loop і goto назад.",
                "7. boolean isEven(int x): знайти irem.",
                "8. String concat(String a, String b): знайти StringBuilder.",
                "9. Object make(): знайти new/dup/invokespecial.",
                "10. int[] makeArray(int n): знайти newarray.",
                "11. String[] makeStrings(): знайти anewarray/aastore.",
                "12. int first(int[] a): знайти iaload.",
                "13. void set(int[] a, int v): знайти iastore.",
                "14. int len(Object[] a): знайти arraylength.",
                "15. void print(String s): знайти getstatic/invokevirtual.",
                "16. static call Math.max: знайти invokestatic.",
                "17. interface call List.size(): знайти invokeinterface.",
                "18. private method call: знайти invokespecial.",
                "19. constructor chaining: знайти this(...) або super(...).",
                "20. static initializer: знайти <clinit>.",
                "21. final static int constant: знайти ConstantValue.",
                "22. volatile field: знайти ACC_VOLATILE.",
                "23. transient field: знайти ACC_TRANSIENT.",
                "24. synchronized method: знайти ACC_SYNCHRONIZED.",
                "25. synchronized block: знайти monitorenter/monitorexit.",
                "26. try/catch: знайти Exception table.",
                "27. try/finally: знайти handler + athrow.",
                "28. try-with-resources: знайти close і suppressed.",
                "29. enhanced for List: знайти Iterator.",
                "30. enhanced for array: порівняти без Iterator.",
                "31. switch dense ints: знайти tableswitch.",
                "32. switch sparse ints: знайти lookupswitch.",
                "33. switch String: знайти hashCode/equals.",
                "34. lambda Runnable: знайти invokedynamic.",
                "35. method reference: знайти invokedynamic.",
                "36. generic get List<String>: знайти checkcast.",
                "37. generic override: знайти bridge method.",
                "38. inner class: знайти InnerClasses і this$0.",
                "39. enum: знайти values/valueOf і <clinit>.",
                "40. annotation runtime: знайти RuntimeVisibleAnnotations."));
        uk.add(LessonBlock.note(
                "Мета drills — не запам'ятати всі opcodes за вечір. Мета — навчитися дивитися "
                + "на Java 8 source і прогнозувати, який bytecode приблизно згенерує javac."));
        uk.add(LessonBlock.heading("Формат звіту"));
        uk.add(LessonBlock.list(
                "Java snippet.",
                "Команда javac і javap.",
                "3-10 ключових рядків bytecode.",
                "Пояснення stack/local state.",
                "Один висновок: який Java 8 патерн я тепер бачу в bytecode."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Javap drills: 40 bytecode tasks"));
        en.add(LessonBlock.paragraph(
                "This lesson is a trainer. For each task: write a tiny Java 8 method, compile, "
                + "open javap -c -v, find the needed bytecode pattern, and briefly explain stack/local state."));
        en.add(LessonBlock.list(
                "1. int add(int a, int b): find iload/iadd/ireturn.",
                "2. long add(long a, long b): find lload/ladd/lreturn and local slots.",
                "3. double avg(int a, int b): find i2d/dadd/ddiv.",
                "4. int abs(int x): find conditional branch.",
                "5. int max(int a, int b): find if_icmp*.",
                "6. int sumTo(int n): find loop and backward goto.",
                "7. boolean isEven(int x): find irem.",
                "8. String concat(String a, String b): find StringBuilder.",
                "9. Object make(): find new/dup/invokespecial.",
                "10. int[] makeArray(int n): find newarray.",
                "11. String[] makeStrings(): find anewarray/aastore.",
                "12. int first(int[] a): find iaload.",
                "13. void set(int[] a, int v): find iastore.",
                "14. int len(Object[] a): find arraylength.",
                "15. void print(String s): find getstatic/invokevirtual.",
                "16. static call Math.max: find invokestatic.",
                "17. interface call List.size(): find invokeinterface.",
                "18. private method call: find invokespecial.",
                "19. constructor chaining: find this(...) or super(...).",
                "20. static initializer: find <clinit>.",
                "21. final static int constant: find ConstantValue.",
                "22. volatile field: find ACC_VOLATILE.",
                "23. transient field: find ACC_TRANSIENT.",
                "24. synchronized method: find ACC_SYNCHRONIZED.",
                "25. synchronized block: find monitorenter/monitorexit.",
                "26. try/catch: find Exception table.",
                "27. try/finally: find handler + athrow.",
                "28. try-with-resources: find close and suppressed.",
                "29. enhanced for List: find Iterator.",
                "30. enhanced for array: compare without Iterator.",
                "31. switch dense ints: find tableswitch.",
                "32. switch sparse ints: find lookupswitch.",
                "33. switch String: find hashCode/equals.",
                "34. lambda Runnable: find invokedynamic.",
                "35. method reference: find invokedynamic.",
                "36. generic get List<String>: find checkcast.",
                "37. generic override: find bridge method.",
                "38. inner class: find InnerClasses and this$0.",
                "39. enum: find values/valueOf and <clinit>.",
                "40. runtime annotation: find RuntimeVisibleAnnotations."));
        en.add(LessonBlock.note(
                "The goal of drills is not memorizing all opcodes in one evening. The goal is "
                + "learning to look at Java 8 source and predict roughly which bytecode javac generates."));
        en.add(LessonBlock.heading("Report format"));
        en.add(LessonBlock.list(
                "Java snippet.",
                "javac and javap command.",
                "3-10 key bytecode lines.",
                "Explanation of stack/local state.",
                "One conclusion: which Java 8 pattern I now see in bytecode."));

        return new Lesson("jdk8.bytecode.cookbook.9", "Javap drills", "Javap drills", uk, en);
    }
}
