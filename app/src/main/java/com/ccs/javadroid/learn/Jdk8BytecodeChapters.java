package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JVM bytecode section for the JDK 8 deep-dive course.
 */
final class Jdk8BytecodeChapters {

    private Jdk8BytecodeChapters() {
    }

    static void add(Course c) {
        Chapter ch = new Chapter(
                "Розробка на чистому JVM bytecode",
                "Pure JVM bytecode development");
        ch.add(lessonBytecodeMindset());
        ch.add(lessonClassFileAndConstantPool());
        ch.add(lessonDescriptorsAndSignatures());
        ch.add(lessonOperandStackAndLocals());
        ch.add(lessonInstructionsArithmeticAndFlow());
        ch.add(lessonObjectsFieldsAndInvoke());
        ch.add(lessonArraysExceptionsVerifier());
        ch.add(lessonJasminStylePractice());
        ch.add(lessonJava8BytecodePatterns());
        c.add(ch);
    }

    private static Lesson lessonBytecodeMindset() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Що означає \"писати bytecode\""));
        uk.add(LessonBlock.paragraph(
                "JVM bytecode — це інструкції для Java Virtual Machine. Java-файл компілюється "
                + "у .class, а .class містить constant pool, поля, методи, атрибути та байткод "
                + "методів. Розробка на чистому bytecode означає, що ви думаєте не класами "
                + "високого рівня, а стеком операндів, локальними змінними, дескрипторами і "
                + "інструкціями на кшталт iload, iadd, invokevirtual."));
        uk.add(LessonBlock.note(
                "Для Android важливо: JVM .class bytecode потім перетворюється Android toolchain "
                + "у DEX bytecode для ART/Dalvik. Але розуміння JVM bytecode все одно пояснює "
                + "javac, generics erasure, lambdas, stack traces і багато помилок компіляції."));
        uk.add(LessonBlock.table(
                "Рівень\tЩо бачите\tІнструмент",
                Arrays.asList(
                        "Java source\tHello.java\tредактор, javac",
                        "JVM bytecode\tHello.class\tjavap -c -v",
                        "Android bytecode\tclasses.dex\tdex tools / Android build tools",
                        "Runtime\tstack trace, VerifyError, ClassNotFoundException\tJVM/ART logs")));
        uk.add(LessonBlock.code(
                "public class Hello {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hi\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# Компіляція JDK 8 стилем\n"
                + "javac Hello.java\n"
                + "\n"
                + "# Дивимось bytecode\n"
                + "javap -c -v Hello"));
        uk.add(LessonBlock.paragraph(
                "JVM — стекова машина. Більшість інструкцій бере значення зі stack, кладе "
                + "результат назад на stack або читає/пише local variables. Наприклад, iadd "
                + "знімає два int зі stack і кладе результат."));
        uk.add(LessonBlock.warning(
                "JDK сам по собі не має офіційного текстового assembler для .class. Для навчання "
                + "часто використовують Jasmin-style синтаксис або бібліотеки на кшталт ASM. "
                + "У цьому розділі приклади bytecode даються як навчальна assembly-форма і як javap-вивід."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть Hello.java і скомпілюйте через javac.",
                "Запустіть javap -c Hello і знайдіть getstatic, ldc, invokevirtual, return.",
                "Запустіть javap -v Hello і знайдіть major version. Для Java 8 це 52.",
                "Поясніть, чому System.out.println у bytecode — це не одна інструкція."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("What \"writing bytecode\" means"));
        en.add(LessonBlock.paragraph(
                "JVM bytecode is the instruction set for the Java Virtual Machine. A Java file "
                + "is compiled into .class; a .class file contains constant pool, fields, methods, "
                + "attributes, and method bytecode. Pure bytecode development means thinking not "
                + "in high-level classes, but in operand stack, local variables, descriptors, and "
                + "instructions such as iload, iadd, invokevirtual."));
        en.add(LessonBlock.note(
                "For Android: JVM .class bytecode is later converted by the Android toolchain "
                + "into DEX bytecode for ART/Dalvik. Still, JVM bytecode explains javac, generics "
                + "erasure, lambdas, stack traces, and many compilation/runtime errors."));
        en.add(LessonBlock.table(
                "Level\tWhat you see\tTool",
                Arrays.asList(
                        "Java source\tHello.java\teditor, javac",
                        "JVM bytecode\tHello.class\tjavap -c -v",
                        "Android bytecode\tclasses.dex\tdex tools / Android build tools",
                        "Runtime\tstack trace, VerifyError, ClassNotFoundException\tJVM/ART logs")));
        en.add(LessonBlock.code(
                "public class Hello {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hi\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# Compile in JDK 8 style\n"
                + "javac Hello.java\n"
                + "\n"
                + "# Inspect bytecode\n"
                + "javap -c -v Hello"));
        en.add(LessonBlock.paragraph(
                "The JVM is a stack machine. Most instructions take values from the operand stack, "
                + "put results back on the stack, or read/write local variables. For example, "
                + "iadd pops two int values and pushes the result."));
        en.add(LessonBlock.warning(
                "The JDK itself has no official textual assembler for .class files. For learning, "
                + "people often use Jasmin-style syntax or libraries such as ASM. In this section, "
                + "bytecode examples are shown as educational assembly and javap output."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create Hello.java and compile it with javac.",
                "Run javap -c Hello and find getstatic, ldc, invokevirtual, return.",
                "Run javap -v Hello and find major version. For Java 8 it is 52.",
                "Explain why System.out.println in bytecode is not one instruction."));

        return new Lesson("jdk8.bytecode.1", "Bytecode mindset", "Bytecode mindset", uk, en);
    }

    private static Lesson lessonClassFileAndConstantPool() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading(".class файл і constant pool"));
        uk.add(LessonBlock.paragraph(
                ".class файл починається з magic number 0xCAFEBABE. Далі йде версія class file, "
                + "constant pool, flags класу, this_class, super_class, interfaces, fields, methods "
                + "та attributes. Constant pool — таблиця констант і посилань: рядки, імена класів, "
                + "імена методів, дескриптори, Methodref, Fieldref."));
        uk.add(LessonBlock.table(
                "Частина class file\tДля чого",
                Arrays.asList(
                        "magic\tперевірка, що це .class файл: CAFEBABE",
                        "minor/major version\tверсія bytecode; Java 8 = major 52",
                        "constant_pool\tрядки, class refs, method refs, descriptors",
                        "access_flags\tpublic, final, interface, abstract",
                        "this_class / super_class\tпоточний клас і батьківський клас",
                        "fields\tполя класу",
                        "methods\tметоди і їх Code attribute",
                        "attributes\tSourceFile, LineNumberTable, StackMapTable тощо")));
        uk.add(LessonBlock.code(
                "public class Tiny {\n"
                + "    public int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# javap -v Tiny покаже Constant pool:\n"
                + "# #1 = Methodref java/lang/Object.\"<init>\":()V\n"
                + "# #2 = Class     java/lang/Object\n"
                + "# #3 = NameAndType \"<init>\":()V\n"
                + "# #4 = Utf8      Tiny\n"
                + "# #5 = Utf8      add\n"
                + "# #6 = Utf8      (II)I"));
        uk.add(LessonBlock.paragraph(
                "Bytecode не зберігає повні Java-імена так, як ми їх пишемо. Наприклад, "
                + "java.lang.String у class file виглядає як java/lang/String. Метод add(int,int) "
                + "має descriptor (II)I: два int аргументи, int результат."));
        uk.add(LessonBlock.note(
                "Constant pool економить місце і робить bytecode посилальним: інструкція "
                + "invokevirtual не містить повний текст методу, а посилається на індекс у constant pool."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте Tiny.java і відкрийте javap -v Tiny.",
                "Знайдіть у constant pool ім'я класу Tiny.",
                "Знайдіть descriptor методу add.",
                "Знайдіть Methodref для java/lang/Object.<init>.",
                "Поясніть, навіщо constant pool потрібен замість дублювання рядків у кожній інструкції."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading(".class file and constant pool"));
        en.add(LessonBlock.paragraph(
                "A .class file starts with magic number 0xCAFEBABE. Then come class file version, "
                + "constant pool, class flags, this_class, super_class, interfaces, fields, methods, "
                + "and attributes. Constant pool is a table of constants and references: strings, "
                + "class names, method names, descriptors, Methodref, Fieldref."));
        en.add(LessonBlock.table(
                "Class file part\tPurpose",
                Arrays.asList(
                        "magic\tcheck that this is a .class file: CAFEBABE",
                        "minor/major version\tbytecode version; Java 8 = major 52",
                        "constant_pool\tstrings, class refs, method refs, descriptors",
                        "access_flags\tpublic, final, interface, abstract",
                        "this_class / super_class\tcurrent class and parent class",
                        "fields\tclass fields",
                        "methods\tmethods and their Code attribute",
                        "attributes\tSourceFile, LineNumberTable, StackMapTable, etc.")));
        en.add(LessonBlock.code(
                "public class Tiny {\n"
                + "    public int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# javap -v Tiny shows Constant pool:\n"
                + "# #1 = Methodref java/lang/Object.\"<init>\":()V\n"
                + "# #2 = Class     java/lang/Object\n"
                + "# #3 = NameAndType \"<init>\":()V\n"
                + "# #4 = Utf8      Tiny\n"
                + "# #5 = Utf8      add\n"
                + "# #6 = Utf8      (II)I"));
        en.add(LessonBlock.paragraph(
                "Bytecode does not store full Java names exactly as we write them. For example, "
                + "java.lang.String in a class file is java/lang/String. Method add(int,int) "
                + "has descriptor (II)I: two int arguments, int result."));
        en.add(LessonBlock.note(
                "Constant pool saves space and makes bytecode reference-based: invokevirtual "
                + "does not contain full method text, but points to an index in constant pool."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile Tiny.java and open javap -v Tiny.",
                "Find class name Tiny in constant pool.",
                "Find descriptor of method add.",
                "Find Methodref for java/lang/Object.<init>.",
                "Explain why constant pool is better than duplicating strings in each instruction."));

        return new Lesson("jdk8.bytecode.2", "Class file і constant pool", "Class file and constant pool", uk, en);
    }

    private static Lesson lessonDescriptorsAndSignatures() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Descriptors: мова типів bytecode"));
        uk.add(LessonBlock.paragraph(
                "Descriptor — компактний запис типу або сигнатури методу. JVM не пише int, "
                + "boolean або java.lang.String словами. Вона використовує символи: I для int, "
                + "Z для boolean, V для void, Ljava/lang/String; для String, [I для int[]."));
        uk.add(LessonBlock.table(
                "Java тип\tDescriptor",
                Arrays.asList(
                        "byte\tB",
                        "char\tC",
                        "double\tD",
                        "float\tF",
                        "int\tI",
                        "long\tJ",
                        "short\tS",
                        "boolean\tZ",
                        "void\tV",
                        "java.lang.String\tLjava/lang/String;",
                        "int[]\t[I",
                        "String[][]\t[[Ljava/lang/String;")));
        uk.add(LessonBlock.code(
                "int add(int a, int b)        -> (II)I\n"
                + "void print(String s)       -> (Ljava/lang/String;)V\n"
                + "String[] split(String s)   -> (Ljava/lang/String;)[Ljava/lang/String;\n"
                + "long mix(int x, double d)  -> (ID)J"));
        uk.add(LessonBlock.heading("Generic signature vs descriptor"));
        uk.add(LessonBlock.paragraph(
                "Через type erasure descriptor для List<String> і List<Integer> однаковий: "
                + "Ljava/util/List;. Generic-інформація може бути збережена в атрибуті Signature, "
                + "але JVM verifier працює з erased descriptor."));
        uk.add(LessonBlock.code(
                "List<String> names;\n"
                + "\n"
                + "# descriptor:\n"
                + "Ljava/util/List;\n"
                + "\n"
                + "# Signature attribute може містити:\n"
                + "Ljava/util/List<Ljava/lang/String;>;"));
        uk.add(LessonBlock.warning(
                "Помилка в descriptor майже завжди означає, що JVM не знайде метод: "
                + "NoSuchMethodError або VerifyError. Для bytecode-розробки descriptors треба читати автоматично."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Запишіть descriptor для void main(String[] args).",
                "Запишіть descriptor для int[][] make(int size).",
                "Запишіть descriptor для boolean contains(String s, char c).",
                "Через javap -s подивіться descriptors методів свого класу.",
                "Поясніть, чому long має символ J, а не L."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Descriptors: bytecode type language"));
        en.add(LessonBlock.paragraph(
                "A descriptor is a compact notation for a type or method signature. JVM does "
                + "not write int, boolean, or java.lang.String as words. It uses symbols: I for int, "
                + "Z for boolean, V for void, Ljava/lang/String; for String, [I for int[]."));
        en.add(LessonBlock.table(
                "Java type\tDescriptor",
                Arrays.asList(
                        "byte\tB",
                        "char\tC",
                        "double\tD",
                        "float\tF",
                        "int\tI",
                        "long\tJ",
                        "short\tS",
                        "boolean\tZ",
                        "void\tV",
                        "java.lang.String\tLjava/lang/String;",
                        "int[]\t[I",
                        "String[][]\t[[Ljava/lang/String;")));
        en.add(LessonBlock.code(
                "int add(int a, int b)        -> (II)I\n"
                + "void print(String s)       -> (Ljava/lang/String;)V\n"
                + "String[] split(String s)   -> (Ljava/lang/String;)[Ljava/lang/String;\n"
                + "long mix(int x, double d)  -> (ID)J"));
        en.add(LessonBlock.heading("Generic signature vs descriptor"));
        en.add(LessonBlock.paragraph(
                "Because of type erasure, descriptor for List<String> and List<Integer> is the same: "
                + "Ljava/util/List;. Generic information may be stored in Signature attribute, "
                + "but JVM verifier works with erased descriptor."));
        en.add(LessonBlock.code(
                "List<String> names;\n"
                + "\n"
                + "# descriptor:\n"
                + "Ljava/util/List;\n"
                + "\n"
                + "# Signature attribute may contain:\n"
                + "Ljava/util/List<Ljava/lang/String;>;"));
        en.add(LessonBlock.warning(
                "A wrong descriptor almost always means JVM cannot find a method: "
                + "NoSuchMethodError or VerifyError. For bytecode development, descriptors must become automatic."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write descriptor for void main(String[] args).",
                "Write descriptor for int[][] make(int size).",
                "Write descriptor for boolean contains(String s, char c).",
                "Use javap -s to inspect descriptors in your own class.",
                "Explain why long uses J, not L."));

        return new Lesson("jdk8.bytecode.3", "Descriptors і signatures", "Descriptors and signatures", uk, en);
    }

    private static Lesson lessonOperandStackAndLocals() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Operand stack і local variables"));
        uk.add(LessonBlock.paragraph(
                "Кожен виклик методу має frame. Frame містить local variables array і operand stack. "
                + "Local variables — слоти для this, параметрів і локальних змінних. Operand stack — "
                + "робочий стек, через який інструкції передають значення одна одній."));
        uk.add(LessonBlock.table(
                "Категорія\tСлоти",
                Arrays.asList(
                        "int, float, reference, returnAddress\t1 slot",
                        "long, double\t2 slots",
                        "this у non-static методі\tlocal 0",
                        "перший параметр static методу\tlocal 0",
                        "перший параметр instance методу\tlocal 1, бо local 0 = this")));
        uk.add(LessonBlock.code(
                "public int add(int a, int b) {\n"
                + "    return a + b;\n"
                + "}\n"
                + "\n"
                + "# javap -c приблизно:\n"
                + "0: iload_1   // push local 1: a\n"
                + "1: iload_2   // push local 2: b\n"
                + "2: iadd      // pop a,b; push a+b\n"
                + "3: ireturn   // return int from stack"));
        uk.add(LessonBlock.heading("Стан stack покроково"));
        uk.add(LessonBlock.table(
                "Після інструкції\tOperand stack",
                Arrays.asList(
                        "start\t[]",
                        "iload_1\t[a]",
                        "iload_2\t[a, b]",
                        "iadd\t[a+b]",
                        "ireturn\t[] метод завершено")));
        uk.add(LessonBlock.warning(
                "Тип return має відповідати значенню на stack. Якщо метод має descriptor ()I, "
                + "перед ireturn на stack має лежати int. Для reference використовується areturn, "
                + "для void — return без значення."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте метод int square(int x) і розпишіть stack після кожної інструкції.",
                "Скомпілюйте static int add(int a, int b) і порівняйте local indexes з instance методом.",
                "Скомпілюйте метод long sum(long a, long b) і подивіться, як long займає слоти.",
                "Поясніть різницю між iload_1 і aload_1."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Operand stack and local variables"));
        en.add(LessonBlock.paragraph(
                "Every method invocation has a frame. The frame contains local variables array "
                + "and operand stack. Local variables are slots for this, parameters, and locals. "
                + "Operand stack is the working stack through which instructions pass values."));
        en.add(LessonBlock.table(
                "Category\tSlots",
                Arrays.asList(
                        "int, float, reference, returnAddress\t1 slot",
                        "long, double\t2 slots",
                        "this in non-static method\tlocal 0",
                        "first parameter of static method\tlocal 0",
                        "first parameter of instance method\tlocal 1 because local 0 = this")));
        en.add(LessonBlock.code(
                "public int add(int a, int b) {\n"
                + "    return a + b;\n"
                + "}\n"
                + "\n"
                + "# javap -c roughly:\n"
                + "0: iload_1   // push local 1: a\n"
                + "1: iload_2   // push local 2: b\n"
                + "2: iadd      // pop a,b; push a+b\n"
                + "3: ireturn   // return int from stack"));
        en.add(LessonBlock.heading("Stack state step by step"));
        en.add(LessonBlock.table(
                "After instruction\tOperand stack",
                Arrays.asList(
                        "start\t[]",
                        "iload_1\t[a]",
                        "iload_2\t[a, b]",
                        "iadd\t[a+b]",
                        "ireturn\t[] method finished")));
        en.add(LessonBlock.warning(
                "Return type must match the value on stack. If method descriptor is ()I, an int "
                + "must be on stack before ireturn. For reference use areturn, for void use return with no value."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile int square(int x) and write stack after every instruction.",
                "Compile static int add(int a, int b) and compare local indexes with instance method.",
                "Compile long sum(long a, long b) and inspect how long occupies slots.",
                "Explain iload_1 vs aload_1."));

        return new Lesson("jdk8.bytecode.4", "Operand stack і locals", "Operand stack and locals", uk, en);
    }

    private static Lesson lessonInstructionsArithmeticAndFlow() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Інструкції: arithmetic і control flow"));
        uk.add(LessonBlock.paragraph(
                "Bytecode має сімейства інструкцій за типами: i* для int, l* для long, f* для float, "
                + "d* для double, a* для references. Control flow будується через labels і переходи: "
                + "ifeq, ifne, if_icmpge, goto, tableswitch, lookupswitch."));
        uk.add(LessonBlock.table(
                "Інструкція\tЩо робить",
                Arrays.asList(
                        "iconst_0..iconst_5\tpush маленького int",
                        "bipush n\tpush byte-sized int",
                        "iload_n / istore_n\tчитати/писати int local",
                        "iadd, isub, imul, idiv\tарифметика int",
                        "if_icmpge label\tпорівняти два int зі stack",
                        "ifeq label\tперейти, якщо int == 0",
                        "goto label\tбезумовний перехід",
                        "ireturn\tповернути int")));
        uk.add(LessonBlock.code(
                "public int max(int a, int b) {\n"
                + "    return a >= b ? a : b;\n"
                + "}\n"
                + "\n"
                + "# javap -c приблизно:\n"
                + "0: iload_1\n"
                + "1: iload_2\n"
                + "2: if_icmplt 7\n"
                + "5: iload_1\n"
                + "6: ireturn\n"
                + "7: iload_2\n"
                + "8: ireturn"));
        uk.add(LessonBlock.code(
                "public int sumTo(int n) {\n"
                + "    int sum = 0;\n"
                + "    for (int i = 1; i <= n; i++) sum += i;\n"
                + "    return sum;\n"
                + "}\n"
                + "\n"
                + "# У bytecode цикл — це labels + if + goto."));
        uk.add(LessonBlock.warning(
                "У bytecode немає 'for' або 'while' як окремих високорівневих конструкцій. "
                + "Є переходи. Тому нескінченний цикл — це просто goto назад без правильної умови виходу."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте max(int,int) і знайдіть if_icmp* інструкцію.",
                "Скомпілюйте abs(int x) і розпишіть гілки.",
                "Скомпілюйте for-loop і знайдіть goto назад.",
                "Скомпілюйте switch над int і порівняйте tableswitch/lookupswitch."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Instructions: arithmetic and control flow"));
        en.add(LessonBlock.paragraph(
                "Bytecode has instruction families by type: i* for int, l* for long, f* for float, "
                + "d* for double, a* for references. Control flow is built through labels and jumps: "
                + "ifeq, ifne, if_icmpge, goto, tableswitch, lookupswitch."));
        en.add(LessonBlock.table(
                "Instruction\tWhat it does",
                Arrays.asList(
                        "iconst_0..iconst_5\tpush small int",
                        "bipush n\tpush byte-sized int",
                        "iload_n / istore_n\tread/write int local",
                        "iadd, isub, imul, idiv\tint arithmetic",
                        "if_icmpge label\tcompare two ints from stack",
                        "ifeq label\tjump if int == 0",
                        "goto label\tunconditional jump",
                        "ireturn\treturn int")));
        en.add(LessonBlock.code(
                "public int max(int a, int b) {\n"
                + "    return a >= b ? a : b;\n"
                + "}\n"
                + "\n"
                + "# javap -c roughly:\n"
                + "0: iload_1\n"
                + "1: iload_2\n"
                + "2: if_icmplt 7\n"
                + "5: iload_1\n"
                + "6: ireturn\n"
                + "7: iload_2\n"
                + "8: ireturn"));
        en.add(LessonBlock.code(
                "public int sumTo(int n) {\n"
                + "    int sum = 0;\n"
                + "    for (int i = 1; i <= n; i++) sum += i;\n"
                + "    return sum;\n"
                + "}\n"
                + "\n"
                + "# In bytecode a loop is labels + if + goto."));
        en.add(LessonBlock.warning(
                "Bytecode has no 'for' or 'while' as high-level constructs. It has jumps. "
                + "An infinite loop is just a goto back without a correct exit condition."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile max(int,int) and find the if_icmp* instruction.",
                "Compile abs(int x) and write branches.",
                "Compile a for-loop and find backward goto.",
                "Compile switch over int and compare tableswitch/lookupswitch."));

        return new Lesson("jdk8.bytecode.5", "Instructions і control flow", "Instructions and control flow", uk, en);
    }

    private static Lesson lessonObjectsFieldsAndInvoke() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Objects, fields, invoke*"));
        uk.add(LessonBlock.paragraph(
                "Створення об'єкта у bytecode — це не одна дія. Зазвичай йде new, dup, "
                + "invokespecial <init>. Поля читаються getfield/getstatic, записуються "
                + "putfield/putstatic. Методи викликаються різними invoke-інструкціями."));
        uk.add(LessonBlock.table(
                "Інструкція\tДля чого",
                Arrays.asList(
                        "new\tстворити uninitialized object",
                        "dup\tскопіювати top stack value",
                        "invokespecial\tconstructor, private method, super call",
                        "invokevirtual\tзвичайний virtual instance method",
                        "invokestatic\tstatic method",
                        "invokeinterface\tметод через interface reference",
                        "invokedynamic\tdynamic call site; lambdas у Java 8",
                        "getfield / putfield\tinstance field",
                        "getstatic / putstatic\tstatic field")));
        uk.add(LessonBlock.code(
                "StringBuilder sb = new StringBuilder();\n"
                + "sb.append(\"A\");\n"
                + "String s = sb.toString();\n"
                + "\n"
                + "# javap -c ідея:\n"
                + "new java/lang/StringBuilder\n"
                + "dup\n"
                + "invokespecial java/lang/StringBuilder.<init>:()V\n"
                + "astore_1\n"
                + "aload_1\n"
                + "ldc \"A\"\n"
                + "invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;\n"
                + "pop\n"
                + "aload_1\n"
                + "invokevirtual java/lang/StringBuilder.toString:()Ljava/lang/String;\n"
                + "astore_2"));
        uk.add(LessonBlock.note(
                "dup потрібен перед constructor, бо invokespecial <init> споживає reference зі stack. "
                + "Але reference ще потрібен, щоб зберегти створений об'єкт у local variable."));
        uk.add(LessonBlock.warning(
                "Найчастіші bytecode-помилки при викликах: неправильний descriptor, неправильний owner "
                + "класу, забутий object reference на stack, або використання invokevirtual там, де треба invokestatic."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте new ArrayList<String>() і знайдіть new/dup/invokespecial.",
                "Скомпілюйте Math.max(a,b) і знайдіть invokestatic.",
                "Скомпілюйте list.size() через List reference і знайдіть invokeinterface.",
                "Скомпілюйте private метод і подивіться invokespecial."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Objects, fields, invoke*"));
        en.add(LessonBlock.paragraph(
                "Object creation in bytecode is not one action. Usually it is new, dup, "
                + "invokespecial <init>. Fields are read with getfield/getstatic and written "
                + "with putfield/putstatic. Methods are called by different invoke instructions."));
        en.add(LessonBlock.table(
                "Instruction\tPurpose",
                Arrays.asList(
                        "new\tcreate uninitialized object",
                        "dup\tcopy top stack value",
                        "invokespecial\tconstructor, private method, super call",
                        "invokevirtual\tnormal virtual instance method",
                        "invokestatic\tstatic method",
                        "invokeinterface\tmethod through interface reference",
                        "invokedynamic\tdynamic call site; lambdas in Java 8",
                        "getfield / putfield\tinstance field",
                        "getstatic / putstatic\tstatic field")));
        en.add(LessonBlock.code(
                "StringBuilder sb = new StringBuilder();\n"
                + "sb.append(\"A\");\n"
                + "String s = sb.toString();\n"
                + "\n"
                + "# javap -c idea:\n"
                + "new java/lang/StringBuilder\n"
                + "dup\n"
                + "invokespecial java/lang/StringBuilder.<init>:()V\n"
                + "astore_1\n"
                + "aload_1\n"
                + "ldc \"A\"\n"
                + "invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;\n"
                + "pop\n"
                + "aload_1\n"
                + "invokevirtual java/lang/StringBuilder.toString:()Ljava/lang/String;\n"
                + "astore_2"));
        en.add(LessonBlock.note(
                "dup is needed before constructor because invokespecial <init> consumes the reference "
                + "from stack. But the reference is still needed to store the created object in a local variable."));
        en.add(LessonBlock.warning(
                "Common bytecode call mistakes: wrong descriptor, wrong owner class, missing object "
                + "reference on stack, or using invokevirtual where invokestatic is required."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile new ArrayList<String>() and find new/dup/invokespecial.",
                "Compile Math.max(a,b) and find invokestatic.",
                "Compile list.size() through List reference and find invokeinterface.",
                "Compile a private method and inspect invokespecial."));

        return new Lesson("jdk8.bytecode.6", "Objects і invoke*", "Objects and invoke*", uk, en);
    }

    private static Lesson lessonArraysExceptionsVerifier() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Arrays, exceptions, verifier"));
        uk.add(LessonBlock.paragraph(
                "Масиви мають окремі інструкції: newarray для primitive arrays, anewarray для "
                + "reference arrays, iaload/aastore тощо для читання/запису. Винятки в bytecode "
                + "описуються exception table у Code attribute: from, to, target, type."));
        uk.add(LessonBlock.table(
                "Інструкція\tЩо робить",
                Arrays.asList(
                        "newarray int\tстворити int[]",
                        "anewarray java/lang/String\tстворити String[]",
                        "arraylength\tдовжина масиву",
                        "iaload / iastore\tчитати/писати int[]",
                        "aaload / aastore\tчитати/писати reference[]",
                        "athrow\tкинути exception object")));
        uk.add(LessonBlock.code(
                "public int first(int[] a) {\n"
                + "    return a[0];\n"
                + "}\n"
                + "\n"
                + "# javap -c приблизно:\n"
                + "0: aload_1\n"
                + "1: iconst_0\n"
                + "2: iaload\n"
                + "3: ireturn"));
        uk.add(LessonBlock.heading("Verifier"));
        uk.add(LessonBlock.paragraph(
                "Перед виконанням JVM перевіряє bytecode: типи на stack мають збігатися, "
                + "переходи мають вести в коректні місця, метод не може повернути не той тип, "
                + "uninitialized object не можна використовувати як готовий об'єкт. У Java 7+ "
                + "важливу роль має StackMapTable."));
        uk.add(LessonBlock.warning(
                "VerifyError означає: .class формально завантажився, але bytecode не пройшов перевірку. "
                + "Часто причина — неправильний stack state на різних гілках control flow."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте int[] create(int n) і знайдіть newarray.",
                "Скомпілюйте String[] і знайдіть anewarray.",
                "Скомпілюйте try/catch і подивіться Exception table у javap -v.",
                "Поясніть, чому verifier повинен знати типи на stack у кожній точці переходу."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Arrays, exceptions, verifier"));
        en.add(LessonBlock.paragraph(
                "Arrays have separate instructions: newarray for primitive arrays, anewarray for "
                + "reference arrays, iaload/aastore and similar for reading/writing. Exceptions in "
                + "bytecode are described by exception table in Code attribute: from, to, target, type."));
        en.add(LessonBlock.table(
                "Instruction\tWhat it does",
                Arrays.asList(
                        "newarray int\tcreate int[]",
                        "anewarray java/lang/String\tcreate String[]",
                        "arraylength\tarray length",
                        "iaload / iastore\tread/write int[]",
                        "aaload / aastore\tread/write reference[]",
                        "athrow\tthrow exception object")));
        en.add(LessonBlock.code(
                "public int first(int[] a) {\n"
                + "    return a[0];\n"
                + "}\n"
                + "\n"
                + "# javap -c roughly:\n"
                + "0: aload_1\n"
                + "1: iconst_0\n"
                + "2: iaload\n"
                + "3: ireturn"));
        en.add(LessonBlock.heading("Verifier"));
        en.add(LessonBlock.paragraph(
                "Before execution, JVM verifies bytecode: stack types must match, jumps must go "
                + "to valid places, a method cannot return the wrong type, and an uninitialized "
                + "object cannot be used as a ready object. In Java 7+, StackMapTable is important."));
        en.add(LessonBlock.warning(
                "VerifyError means the .class was loaded structurally, but bytecode failed verification. "
                + "A common reason is wrong stack state across control-flow branches."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile int[] create(int n) and find newarray.",
                "Compile String[] and find anewarray.",
                "Compile try/catch and inspect Exception table in javap -v.",
                "Explain why verifier must know stack types at every jump target."));

        return new Lesson("jdk8.bytecode.7", "Arrays, exceptions, verifier", "Arrays, exceptions, verifier", uk, en);
    }

    private static Lesson lessonJasminStylePractice() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Jasmin-style bytecode: навчальна assembly-форма"));
        uk.add(LessonBlock.paragraph(
                "Щоб писати bytecode руками, зручно використовувати assembly-подібний синтаксис. "
                + "Jasmin — класичний навчальний формат, де ви пишете .class, .super, .method, "
                + ".limit stack, .limit locals та інструкції. Навіть якщо у вашому середовищі "
                + "немає Jasmin assembler, такий формат добре тренує мислення bytecode."));
        uk.add(LessonBlock.code(
                ".class public HelloBytecode\n"
                + ".super java/lang/Object\n"
                + "\n"
                + ".method public <init>()V\n"
                + "  aload_0\n"
                + "  invokespecial java/lang/Object/<init>()V\n"
                + "  return\n"
                + ".end method\n"
                + "\n"
                + ".method public static main([Ljava/lang/String;)V\n"
                + "  .limit stack 2\n"
                + "  .limit locals 1\n"
                + "  getstatic java/lang/System/out Ljava/io/PrintStream;\n"
                + "  ldc \"Hello from bytecode\"\n"
                + "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n"
                + "  return\n"
                + ".end method"));
        uk.add(LessonBlock.table(
                "Директива\tСенс",
                Arrays.asList(
                        ".class\tоголошення класу",
                        ".super\tбатьківський клас",
                        ".method\tпочаток методу з descriptor",
                        ".limit stack\tмаксимальна глибина operand stack",
                        ".limit locals\tкількість local variable slots",
                        ".end method\tкінець методу")));
        uk.add(LessonBlock.code(
                ".method public static add(II)I\n"
                + "  .limit stack 2\n"
                + "  .limit locals 2\n"
                + "  iload_0\n"
                + "  iload_1\n"
                + "  iadd\n"
                + "  ireturn\n"
                + ".end method"));
        uk.add(LessonBlock.warning(
                ".limit stack і .limit locals мають відповідати реальному коду. Якщо stack limit "
                + "замалий або locals не вистачає, class не пройде складання або verification."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Перепишіть Java метод int add(int,int) у Jasmin-style форму.",
                "Напишіть Jasmin-style метод static int square(int x).",
                "Для HelloBytecode порахуйте максимальну stack depth вручну.",
                "Змініть println(String) на println(int) і виправте descriptor.",
                "Поясніть, чому main має descriptor ([Ljava/lang/String;)V."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Jasmin-style bytecode: educational assembly form"));
        en.add(LessonBlock.paragraph(
                "To write bytecode by hand, an assembly-like syntax is convenient. Jasmin is a "
                + "classic educational format where you write .class, .super, .method, "
                + ".limit stack, .limit locals, and instructions. Even if your environment has "
                + "no Jasmin assembler, this format trains bytecode thinking well."));
        en.add(LessonBlock.code(
                ".class public HelloBytecode\n"
                + ".super java/lang/Object\n"
                + "\n"
                + ".method public <init>()V\n"
                + "  aload_0\n"
                + "  invokespecial java/lang/Object/<init>()V\n"
                + "  return\n"
                + ".end method\n"
                + "\n"
                + ".method public static main([Ljava/lang/String;)V\n"
                + "  .limit stack 2\n"
                + "  .limit locals 1\n"
                + "  getstatic java/lang/System/out Ljava/io/PrintStream;\n"
                + "  ldc \"Hello from bytecode\"\n"
                + "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n"
                + "  return\n"
                + ".end method"));
        en.add(LessonBlock.table(
                "Directive\tMeaning",
                Arrays.asList(
                        ".class\tclass declaration",
                        ".super\tparent class",
                        ".method\tmethod start with descriptor",
                        ".limit stack\tmaximum operand stack depth",
                        ".limit locals\tnumber of local variable slots",
                        ".end method\tmethod end")));
        en.add(LessonBlock.code(
                ".method public static add(II)I\n"
                + "  .limit stack 2\n"
                + "  .limit locals 2\n"
                + "  iload_0\n"
                + "  iload_1\n"
                + "  iadd\n"
                + "  ireturn\n"
                + ".end method"));
        en.add(LessonBlock.warning(
                ".limit stack and .limit locals must match real code. If stack limit is too small "
                + "or locals are missing, the class will not assemble or verify."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Rewrite Java method int add(int,int) into Jasmin-style form.",
                "Write Jasmin-style method static int square(int x).",
                "For HelloBytecode, calculate maximum stack depth by hand.",
                "Change println(String) to println(int) and fix descriptor.",
                "Explain why main has descriptor ([Ljava/lang/String;)V."));

        return new Lesson("jdk8.bytecode.8", "Jasmin-style практика", "Jasmin-style practice", uk, en);
    }

    private static Lesson lessonJava8BytecodePatterns() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Java 8 patterns у bytecode"));
        uk.add(LessonBlock.paragraph(
                "Java 8 має кілька особливих bytecode-патернів: lambdas часто компілюються через "
                + "invokedynamic і LambdaMetafactory; generics стираються до raw descriptors; "
                + "try-with-resources генерує код із suppressed exceptions; enhanced for над Iterable "
                + "перетворюється на Iterator."));
        uk.add(LessonBlock.table(
                "Java конструкція\tBytecode-патерн",
                Arrays.asList(
                        "lambda\tinvokedynamic + synthetic/static helper method або method handle",
                        "method reference\tinvokedynamic call site",
                        "generics\terased descriptors + Signature attribute",
                        "enhanced for над Iterable\titerator(), hasNext(), next()",
                        "try-with-resources\ttry/finally + close + suppressed exceptions",
                        "String concat у JDK 8\tStringBuilder chain",
                        "switch String\thashCode + equals + tableswitch/lookupswitch")));
        uk.add(LessonBlock.code(
                "Runnable r = () -> System.out.println(\"Hi\");\n"
                + "\n"
                + "# У Java 8 javap -c часто покаже:\n"
                + "invokedynamic #... // run:()Ljava/lang/Runnable;\n"
                + "\n"
                + "# А javap -v покаже BootstrapMethods з LambdaMetafactory."));
        uk.add(LessonBlock.code(
                "for (String s : list) {\n"
                + "    System.out.println(s);\n"
                + "}\n"
                + "\n"
                + "# Ідея bytecode:\n"
                + "aload list\n"
                + "invokeinterface java/util/List.iterator:()Ljava/util/Iterator;\n"
                + "astore iterator\n"
                + "loop:\n"
                + "  aload iterator\n"
                + "  invokeinterface java/util/Iterator.hasNext:()Z\n"
                + "  ifeq end\n"
                + "  aload iterator\n"
                + "  invokeinterface java/util/Iterator.next:()Ljava/lang/Object;\n"
                + "  checkcast java/lang/String"));
        uk.add(LessonBlock.note(
                "checkcast після Iterator.next з'являється через generics erasure: Iterator у runtime "
                + "повертає Object, а компілятор вставляє cast до String."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте lambda Runnable і знайдіть invokedynamic.",
                "Скомпілюйте enhanced for по List<String> і знайдіть Iterator calls.",
                "Скомпілюйте String concat у JDK 8 і знайдіть StringBuilder.",
                "Скомпілюйте try-with-resources і знайдіть close та suppressed handling.",
                "Скомпілюйте List<String>.get(0) і знайдіть checkcast."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Java 8 patterns in bytecode"));
        en.add(LessonBlock.paragraph(
                "Java 8 has several special bytecode patterns: lambdas often compile through "
                + "invokedynamic and LambdaMetafactory; generics erase to raw descriptors; "
                + "try-with-resources generates code with suppressed exceptions; enhanced for "
                + "over Iterable becomes Iterator."));
        en.add(LessonBlock.table(
                "Java construct\tBytecode pattern",
                Arrays.asList(
                        "lambda\tinvokedynamic + synthetic/static helper method or method handle",
                        "method reference\tinvokedynamic call site",
                        "generics\terased descriptors + Signature attribute",
                        "enhanced for over Iterable\titerator(), hasNext(), next()",
                        "try-with-resources\ttry/finally + close + suppressed exceptions",
                        "String concat in JDK 8\tStringBuilder chain",
                        "switch String\thashCode + equals + tableswitch/lookupswitch")));
        en.add(LessonBlock.code(
                "Runnable r = () -> System.out.println(\"Hi\");\n"
                + "\n"
                + "# In Java 8 javap -c often shows:\n"
                + "invokedynamic #... // run:()Ljava/lang/Runnable;\n"
                + "\n"
                + "# javap -v shows BootstrapMethods with LambdaMetafactory."));
        en.add(LessonBlock.code(
                "for (String s : list) {\n"
                + "    System.out.println(s);\n"
                + "}\n"
                + "\n"
                + "# Bytecode idea:\n"
                + "aload list\n"
                + "invokeinterface java/util/List.iterator:()Ljava/util/Iterator;\n"
                + "astore iterator\n"
                + "loop:\n"
                + "  aload iterator\n"
                + "  invokeinterface java/util/Iterator.hasNext:()Z\n"
                + "  ifeq end\n"
                + "  aload iterator\n"
                + "  invokeinterface java/util/Iterator.next:()Ljava/lang/Object;\n"
                + "  checkcast java/lang/String"));
        en.add(LessonBlock.note(
                "checkcast after Iterator.next appears because of generics erasure: Iterator at runtime "
                + "returns Object, and the compiler inserts a cast to String."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile lambda Runnable and find invokedynamic.",
                "Compile enhanced for over List<String> and find Iterator calls.",
                "Compile String concat in JDK 8 and find StringBuilder.",
                "Compile try-with-resources and find close and suppressed handling.",
                "Compile List<String>.get(0) and find checkcast."));

        return new Lesson("jdk8.bytecode.9", "Java 8 bytecode patterns", "Java 8 bytecode patterns", uk, en);
    }
}
