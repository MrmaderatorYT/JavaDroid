package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Advanced JVM bytecode topics for JDK 8: class loading, initialization,
 * verifier details, compiler patterns, and bytecode design practice.
 */
final class Jdk8BytecodeAdvancedChapters {

    private Jdk8BytecodeAdvancedChapters() {
    }

    static void add(Course s) {
        Chapter ch = new Chapter(
                "JVM bytecode advanced: loading, verifier, compiler patterns",
                "JVM bytecode advanced: loading, verifier, compiler patterns");
        ch.add(materialClassLoadingLinkingInitialization());
        ch.add(materialConstructorsClinitAndFlags());
        ch.add(materialBridgeSyntheticAndErasure());
        ch.add(materialStackMapTableDeep());
        ch.add(materialTryFinallySynchronizedBytecode());
        ch.add(materialInvokedynamicDeep());
        ch.add(materialBytecodeDebugAttributes());
        ch.add(materialBytecodeCapstoneCompiler());
        s.add(ch);
    }

    private static Lesson materialClassLoadingLinkingInitialization() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Class loading: loading, linking, initialization"));
        uk.add(LessonBlock.paragraph(
                "Коли JVM бачить клас, вона не просто одразу виконує байткод. Є фази: loading "
                + "(знайти bytes класу), linking (verify, prepare, resolve) і initialization "
                + "(виконати <clinit>, тобто static initialization). Розуміння цих фаз пояснює "
                + "ClassNotFoundException, NoClassDefFoundError, ExceptionInInitializerError і дивні проблеми зі static."));
        uk.add(LessonBlock.table(
                "Фаза\tЩо відбувається\tТипові помилки",
                Arrays.asList(
                        "Loading\tClassLoader знаходить .class bytes\tClassNotFoundException",
                        "Verification\tJVM перевіряє безпечність bytecode\tVerifyError",
                        "Preparation\tвиділяється пам'ять для static fields\tрідко видно напряму",
                        "Resolution\tsymbolic refs -> direct refs\tNoSuchMethodError, NoSuchFieldError",
                        "Initialization\tвиконується <clinit>\tExceptionInInitializerError")));
        uk.add(LessonBlock.code(
                "class Config {\n"
                + "    static final int PORT = 8080;\n"
                + "    static String url = buildUrl();\n"
                + "\n"
                + "    static String buildUrl() {\n"
                + "        return \"http://localhost:\" + PORT;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# javap -c Config покаже static initializer:\n"
                + "# static {};\n"
                + "#   invokestatic Config.buildUrl:()Ljava/lang/String;\n"
                + "#   putstatic Config.url:Ljava/lang/String;"));
        uk.add(LessonBlock.note(
                "static final compile-time constants можуть бути inlined у класи-клієнти. "
                + "Якщо змінити значення константи в бібліотеці, але не перекомпілювати клієнта, "
                + "він може продовжити бачити старе значення."));
        uk.add(LessonBlock.warning(
                "ClassNotFoundException означає: клас не знайшли під час явного завантаження "
                + "(наприклад Class.forName). NoClassDefFoundError часто означає: клас був під час "
                + "компіляції, але відсутній або не ініціалізувався під час виконання."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть клас зі static field, який викликає static method.",
                "Подивіться <clinit> через javap -c -v.",
                "Створіть static block, який кидає RuntimeException, і подивіться ExceptionInInitializerError.",
                "Створіть public static final int CONST і перевірте, чи клієнт inlines значення.",
                "Поясніть різницю між loading і initialization."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Class loading: loading, linking, initialization"));
        en.add(LessonBlock.paragraph(
                "When JVM sees a class, it does not simply execute bytecode immediately. There "
                + "are phases: loading (find class bytes), linking (verify, prepare, resolve), "
                + "and initialization (execute <clinit>, static initialization). These phases explain "
                + "ClassNotFoundException, NoClassDefFoundError, ExceptionInInitializerError, and strange static issues."));
        en.add(LessonBlock.table(
                "Phase\tWhat happens\tTypical errors",
                Arrays.asList(
                        "Loading\tClassLoader finds .class bytes\tClassNotFoundException",
                        "Verification\tJVM checks bytecode safety\tVerifyError",
                        "Preparation\tmemory for static fields is allocated\trarely visible directly",
                        "Resolution\tsymbolic refs -> direct refs\tNoSuchMethodError, NoSuchFieldError",
                        "Initialization\t<clinit> executes\tExceptionInInitializerError")));
        en.add(LessonBlock.code(
                "class Config {\n"
                + "    static final int PORT = 8080;\n"
                + "    static String url = buildUrl();\n"
                + "\n"
                + "    static String buildUrl() {\n"
                + "        return \"http://localhost:\" + PORT;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# javap -c Config shows static initializer:\n"
                + "# static {};\n"
                + "#   invokestatic Config.buildUrl:()Ljava/lang/String;\n"
                + "#   putstatic Config.url:Ljava/lang/String;"));
        en.add(LessonBlock.note(
                "static final compile-time constants may be inlined into client classes. If you "
                + "change a constant value in a library but do not recompile the client, it may keep seeing the old value."));
        en.add(LessonBlock.warning(
                "ClassNotFoundException means the class was not found during explicit loading "
                + "(for example Class.forName). NoClassDefFoundError often means the class existed "
                + "during compilation but is missing or failed initialization at runtime."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create a class with static field calling static method.",
                "Inspect <clinit> through javap -c -v.",
                "Create a static block throwing RuntimeException and observe ExceptionInInitializerError.",
                "Create public static final int CONST and check whether client inlines it.",
                "Explain loading vs initialization."));

        return new Lesson("jdk8.bytecode.advanced.1", "Class loading/linking/init", "Class loading/linking/init", uk, en);
    }

    private static Lesson materialConstructorsClinitAndFlags() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("<init>, <clinit>, access flags"));
        uk.add(LessonBlock.paragraph(
                "У bytecode constructor має спеціальне ім'я <init>, а static initializer — <clinit>. "
                + "Це не звичайні Java-методи, їх не можна викликати як obj.<init>() у Java-коді. "
                + "Кожен constructor має викликати інший constructor цього класу або super constructor."));
        uk.add(LessonBlock.code(
                "class Parent {\n"
                + "    Parent() { }\n"
                + "}\n"
                + "\n"
                + "class Child extends Parent {\n"
                + "    private final int x;\n"
                + "\n"
                + "    Child(int x) {\n"
                + "        this.x = x;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# Child.<init>(I)V приблизно:\n"
                + "aload_0\n"
                + "invokespecial Parent.<init>:()V\n"
                + "aload_0\n"
                + "iload_1\n"
                + "putfield Child.x:I\n"
                + "return"));
        uk.add(LessonBlock.table(
                "Access flag\tДе зустрічається\tСенс",
                Arrays.asList(
                        "ACC_PUBLIC\tclass/method/field\tpublic",
                        "ACC_PRIVATE\tmethod/field\tprivate",
                        "ACC_PROTECTED\tmethod/field\tprotected",
                        "ACC_STATIC\tmethod/field\tstatic",
                        "ACC_FINAL\tclass/method/field\tfinal",
                        "ACC_SUPER\tclass\tсучасна семантика invokespecial",
                        "ACC_INTERFACE\tclass\tце interface",
                        "ACC_ABSTRACT\tclass/method\tabstract",
                        "ACC_SYNTHETIC\tclass/method/field\tстворено компілятором",
                        "ACC_BRIDGE\tmethod\tbridge method для generics/covariant returns")));
        uk.add(LessonBlock.note(
                "Якщо в Java-класі немає constructor, javac створює default constructor, "
                + "який просто викликає super.<init>(). У javap його видно як public ClassName();"));
        uk.add(LessonBlock.warning(
                "У bytecode не можна використовувати this як повністю ініціалізований об'єкт до "
                + "успішного invokespecial <init>. Verifier стежить за uninitializedThis."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте клас без constructor і знайдіть default <init>.",
                "Додайте static block і знайдіть <clinit>.",
                "Через javap -v знайдіть flags класу і методів.",
                "Порівняйте flags звичайного класу, abstract class та interface.",
                "Поясніть, чому constructor не має return type у Java, але descriptor має V."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("<init>, <clinit>, access flags"));
        en.add(LessonBlock.paragraph(
                "In bytecode, constructor has special name <init>, and static initializer is <clinit>. "
                + "These are not normal Java methods; you cannot call obj.<init>() in Java code. "
                + "Every constructor must call another constructor of this class or a super constructor."));
        en.add(LessonBlock.code(
                "class Parent {\n"
                + "    Parent() { }\n"
                + "}\n"
                + "\n"
                + "class Child extends Parent {\n"
                + "    private final int x;\n"
                + "\n"
                + "    Child(int x) {\n"
                + "        this.x = x;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# Child.<init>(I)V roughly:\n"
                + "aload_0\n"
                + "invokespecial Parent.<init>:()V\n"
                + "aload_0\n"
                + "iload_1\n"
                + "putfield Child.x:I\n"
                + "return"));
        en.add(LessonBlock.table(
                "Access flag\tWhere it appears\tMeaning",
                Arrays.asList(
                        "ACC_PUBLIC\tclass/method/field\tpublic",
                        "ACC_PRIVATE\tmethod/field\tprivate",
                        "ACC_PROTECTED\tmethod/field\tprotected",
                        "ACC_STATIC\tmethod/field\tstatic",
                        "ACC_FINAL\tclass/method/field\tfinal",
                        "ACC_SUPER\tclass\tmodern invokespecial semantics",
                        "ACC_INTERFACE\tclass\tthis is interface",
                        "ACC_ABSTRACT\tclass/method\tabstract",
                        "ACC_SYNTHETIC\tclass/method/field\tcreated by compiler",
                        "ACC_BRIDGE\tmethod\tbridge method for generics/covariant returns")));
        en.add(LessonBlock.note(
                "If a Java class has no constructor, javac creates a default constructor that "
                + "just calls super.<init>(). javap shows it as public ClassName();"));
        en.add(LessonBlock.warning(
                "In bytecode you cannot use this as a fully initialized object before successful "
                + "invokespecial <init>. Verifier tracks uninitializedThis."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile a class without constructor and find default <init>.",
                "Add static block and find <clinit>.",
                "Use javap -v to find class and method flags.",
                "Compare flags of normal class, abstract class, and interface.",
                "Explain why constructor has no return type in Java, but descriptor has V."));

        return new Lesson("jdk8.bytecode.advanced.2", "<init>, <clinit>, flags", "<init>, <clinit>, flags", uk, en);
    }

    private static Lesson materialBridgeSyntheticAndErasure() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Bridge і synthetic methods"));
        uk.add(LessonBlock.paragraph(
                "Generics erasure іноді змушує javac створювати додаткові методи. Bridge method "
                + "зберігає поліморфізм після стирання типів. Synthetic означає, що елемент створений "
                + "компілятором, а не написаний прямо у source."));
        uk.add(LessonBlock.code(
                "interface Box<T> {\n"
                + "    T get();\n"
                + "}\n"
                + "\n"
                + "class StringBox implements Box<String> {\n"
                + "    public String get() {\n"
                + "        return \"value\";\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# Після erasure Box.get має Object get().\n"
                + "# Але StringBox написав String get().\n"
                + "# javac додає bridge:\n"
                + "# public Object get() { return get(); } // ідея, з cast/dispatch деталями"));
        uk.add(LessonBlock.table(
                "Ознака\tЩо означає",
                Arrays.asList(
                        "ACC_BRIDGE\tметод-мостик для erased signature",
                        "ACC_SYNTHETIC\tстворено компілятором",
                        "Signature attribute\tзберігає generic-інформацію для tools/reflection",
                        "descriptor\tте, що реально використовує JVM для виклику")));
        uk.add(LessonBlock.paragraph(
                "Bridge methods важливі для binary compatibility. Без bridge старий код, який "
                + "викликає Box.get():Object, не зміг би коректно викликати StringBox.get():String "
                + "після erasure."));
        uk.add(LessonBlock.warning(
                "Коли дивитеся javap, не панікуйте через методи, яких не писали. Якщо бачите "
                + "ACC_BRIDGE або ACC_SYNTHETIC, це часто нормальна робота javac."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть generic interface Box<T> і реалізацію Box<String>.",
                "Запустіть javap -c -v StringBox і знайдіть ACC_BRIDGE.",
                "Знайдіть descriptor і Signature для get.",
                "Створіть приклад covariant return type і подивіться, чи з'явиться bridge.",
                "Поясніть, як bridge пов'язаний з type erasure."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Bridge and synthetic methods"));
        en.add(LessonBlock.paragraph(
                "Generics erasure sometimes forces javac to create additional methods. A bridge "
                + "method preserves polymorphism after type erasure. Synthetic means an element "
                + "was created by the compiler, not written directly in source."));
        en.add(LessonBlock.code(
                "interface Box<T> {\n"
                + "    T get();\n"
                + "}\n"
                + "\n"
                + "class StringBox implements Box<String> {\n"
                + "    public String get() {\n"
                + "        return \"value\";\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "# After erasure Box.get has Object get().\n"
                + "# But StringBox wrote String get().\n"
                + "# javac adds bridge:\n"
                + "# public Object get() { return get(); } // idea, with cast/dispatch details"));
        en.add(LessonBlock.table(
                "Marker\tMeaning",
                Arrays.asList(
                        "ACC_BRIDGE\tbridge method for erased signature",
                        "ACC_SYNTHETIC\tcreated by compiler",
                        "Signature attribute\tkeeps generic information for tools/reflection",
                        "descriptor\twhat JVM actually uses for invocation")));
        en.add(LessonBlock.paragraph(
                "Bridge methods matter for binary compatibility. Without bridge, old code calling "
                + "Box.get():Object could not correctly call StringBox.get():String after erasure."));
        en.add(LessonBlock.warning(
                "When reading javap, do not panic about methods you did not write. If you see "
                + "ACC_BRIDGE or ACC_SYNTHETIC, it is often normal javac work."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create generic interface Box<T> and implementation Box<String>.",
                "Run javap -c -v StringBox and find ACC_BRIDGE.",
                "Find descriptor and Signature for get.",
                "Create covariant return type example and see if bridge appears.",
                "Explain how bridge is connected to type erasure."));

        return new Lesson("jdk8.bytecode.advanced.3", "Bridge і synthetic", "Bridge and synthetic", uk, en);
    }

    private static Lesson materialStackMapTableDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("StackMapTable: карта типів для verifier"));
        uk.add(LessonBlock.paragraph(
                "StackMapTable — атрибут, який допомагає verifier швидко перевіряти типи в "
                + "точках control flow. У старіших class file verifier міг сам виводити типи, "
                + "але з Java 7+ stack map frames стали важливою частиною коректного bytecode."));
        uk.add(LessonBlock.paragraph(
                "Коли є if/goto/try-catch, verifier має знати: які типи лежать у locals і operand "
                + "stack у target-точці переходу. Якщо дві гілки приходять у той самий label з "
                + "різними несумісними типами на stack, bytecode некоректний."));
        uk.add(LessonBlock.code(
                "public Object choose(boolean flag) {\n"
                + "    if (flag) {\n"
                + "        return \"text\";\n"
                + "    }\n"
                + "    return Integer.valueOf(10);\n"
                + "}\n"
                + "\n"
                + "# Обидві гілки повертають reference, спільний тип для verifier — Object."));
        uk.add(LessonBlock.table(
                "Frame kind\tІдея",
                Arrays.asList(
                        "same_frame\tlocals ті самі, stack empty",
                        "same_locals_1_stack_item_frame\tlocals ті самі, один item на stack",
                        "append_frame\tдодались locals",
                        "chop_frame\tзникли locals",
                        "full_frame\tповний опис locals і stack")));
        uk.add(LessonBlock.note(
                "Якщо пишете bytecode через ASM, library може обчислити frames за вас "
                + "(COMPUTE_FRAMES), але для навчання корисно розуміти, чому вони існують."));
        uk.add(LessonBlock.warning(
                "Неправильний StackMapTable часто дає VerifyError з повідомленнями про bad type "
                + "on operand stack або inconsistent stackmap frames."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте метод з if/else і відкрийте javap -v.",
                "Знайдіть StackMapTable у виводі.",
                "Скомпілюйте try/catch і подивіться frames для handler.",
                "Поясніть, чому verifier не може дозволити int на одній гілці і String на іншій перед ireturn.",
                "Запишіть locals/stack для target label у простому if."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("StackMapTable: type map for verifier"));
        en.add(LessonBlock.paragraph(
                "StackMapTable is an attribute that helps the verifier quickly check types at "
                + "control-flow points. In older class files the verifier could infer types itself, "
                + "but since Java 7+ stack map frames became an important part of correct bytecode."));
        en.add(LessonBlock.paragraph(
                "When there is if/goto/try-catch, verifier must know which types are in locals "
                + "and operand stack at the jump target. If two branches reach the same label with "
                + "incompatible stack types, bytecode is invalid."));
        en.add(LessonBlock.code(
                "public Object choose(boolean flag) {\n"
                + "    if (flag) {\n"
                + "        return \"text\";\n"
                + "    }\n"
                + "    return Integer.valueOf(10);\n"
                + "}\n"
                + "\n"
                + "# Both branches return reference, common verifier type is Object."));
        en.add(LessonBlock.table(
                "Frame kind\tIdea",
                Arrays.asList(
                        "same_frame\tsame locals, empty stack",
                        "same_locals_1_stack_item_frame\tsame locals, one item on stack",
                        "append_frame\tlocals added",
                        "chop_frame\tlocals removed",
                        "full_frame\tfull locals and stack description")));
        en.add(LessonBlock.note(
                "If you generate bytecode with ASM, the library can compute frames for you "
                + "(COMPUTE_FRAMES), but for learning it helps to understand why they exist."));
        en.add(LessonBlock.warning(
                "Wrong StackMapTable often gives VerifyError with messages about bad type on "
                + "operand stack or inconsistent stackmap frames."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile method with if/else and open javap -v.",
                "Find StackMapTable in output.",
                "Compile try/catch and inspect frames for handler.",
                "Explain why verifier cannot allow int on one branch and String on another before ireturn.",
                "Write locals/stack for target label in simple if."));

        return new Lesson("jdk8.bytecode.advanced.4", "StackMapTable глибоко", "StackMapTable deep dive", uk, en);
    }

    private static Lesson materialTryFinallySynchronizedBytecode() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("try/finally і synchronized у bytecode"));
        uk.add(LessonBlock.paragraph(
                "finally — це гарантія виконати cleanup і на нормальному шляху, і на exceptional "
                + "шляху. У сучасному bytecode javac зазвичай дублює finally-код у кількох місцях "
                + "або використовує exception handler, який виконує cleanup і знову кидає exception."));
        uk.add(LessonBlock.code(
                "try {\n"
                + "    work();\n"
                + "} finally {\n"
                + "    cleanup();\n"
                + "}\n"
                + "\n"
                + "# Ідея bytecode:\n"
                + "call work\n"
                + "call cleanup\n"
                + "goto end\n"
                + "handler:\n"
                + "  astore ex\n"
                + "  call cleanup\n"
                + "  aload ex\n"
                + "  athrow\n"
                + "end:"));
        uk.add(LessonBlock.paragraph(
                "synchronized block компілюється в monitorenter/monitorexit. JVM monitor належить "
                + "об'єкту. Важливо: monitorexit має виконатись і при exception, тому javac генерує "
                + "try/finally-подібний bytecode."));
        uk.add(LessonBlock.code(
                "synchronized (lock) {\n"
                + "    work();\n"
                + "}\n"
                + "\n"
                + "# Ідея bytecode:\n"
                + "aload lock\n"
                + "dup\n"
                + "astore monitorLocal\n"
                + "monitorenter\n"
                + "call work\n"
                + "aload monitorLocal\n"
                + "monitorexit\n"
                + "goto end\n"
                + "handler:\n"
                + "  aload monitorLocal\n"
                + "  monitorexit\n"
                + "  athrow"));
        uk.add(LessonBlock.warning(
                "Непарний monitorenter/monitorexit або exception path без monitorexit може зламати "
                + "коректність синхронізації. Javac генерує захисний handler саме тому."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте try/finally і знайдіть exception table.",
                "Скомпілюйте synchronized block і знайдіть monitorenter/monitorexit.",
                "Скомпілюйте synchronized method і порівняйте: там буде ACC_SYNCHRONIZED flag.",
                "Поясніть, чому synchronized block складніший за synchronized method у bytecode.",
                "Знайдіть athrow у bytecode finally-сценарію."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("try/finally and synchronized in bytecode"));
        en.add(LessonBlock.paragraph(
                "finally guarantees cleanup on both normal and exceptional paths. In modern bytecode, "
                + "javac usually duplicates finally code in several places or uses an exception handler "
                + "that runs cleanup and rethrows the exception."));
        en.add(LessonBlock.code(
                "try {\n"
                + "    work();\n"
                + "} finally {\n"
                + "    cleanup();\n"
                + "}\n"
                + "\n"
                + "# Bytecode idea:\n"
                + "call work\n"
                + "call cleanup\n"
                + "goto end\n"
                + "handler:\n"
                + "  astore ex\n"
                + "  call cleanup\n"
                + "  aload ex\n"
                + "  athrow\n"
                + "end:"));
        en.add(LessonBlock.paragraph(
                "A synchronized block compiles to monitorenter/monitorexit. JVM monitor belongs "
                + "to an object. Important: monitorexit must run even on exception, so javac generates "
                + "try/finally-like bytecode."));
        en.add(LessonBlock.code(
                "synchronized (lock) {\n"
                + "    work();\n"
                + "}\n"
                + "\n"
                + "# Bytecode idea:\n"
                + "aload lock\n"
                + "dup\n"
                + "astore monitorLocal\n"
                + "monitorenter\n"
                + "call work\n"
                + "aload monitorLocal\n"
                + "monitorexit\n"
                + "goto end\n"
                + "handler:\n"
                + "  aload monitorLocal\n"
                + "  monitorexit\n"
                + "  athrow"));
        en.add(LessonBlock.warning(
                "Unpaired monitorenter/monitorexit or exception path without monitorexit can break "
                + "synchronization correctness. javac generates protective handler for this reason."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile try/finally and find exception table.",
                "Compile synchronized block and find monitorenter/monitorexit.",
                "Compile synchronized method and compare: it has ACC_SYNCHRONIZED flag.",
                "Explain why synchronized block is more complex than synchronized method in bytecode.",
                "Find athrow in bytecode of finally scenario."));

        return new Lesson("jdk8.bytecode.advanced.5", "try/finally і synchronized", "try/finally and synchronized", uk, en);
    }

    private static Lesson materialInvokedynamicDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("invokedynamic і LambdaMetafactory"));
        uk.add(LessonBlock.paragraph(
                "invokedynamic з'явився в Java 7 для динамічних мов на JVM, а в Java 8 став "
                + "основою lambdas. На відміну від invokevirtual, де owner/method відомі напряму, "
                + "invokedynamic використовує bootstrap method. Для lambdas це зазвичай "
                + "java/lang/invoke/LambdaMetafactory.metafactory."));
        uk.add(LessonBlock.code(
                "Runnable r = () -> System.out.println(\"run\");\n"
                + "r.run();\n"
                + "\n"
                + "# javap -c може показати:\n"
                + "invokedynamic #2,  0 // InvokeDynamic #0:run:()Ljava/lang/Runnable;\n"
                + "\n"
                + "# javap -v покаже BootstrapMethods:\n"
                + "# 0: invokestatic java/lang/invoke/LambdaMetafactory.metafactory ..."));
        uk.add(LessonBlock.table(
                "Частина invokedynamic\tСенс",
                Arrays.asList(
                        "call site name\tнаприклад run, apply, test",
                        "call site descriptor\tякий functional interface створити",
                        "bootstrap method\tкод, який створює call site",
                        "method handle\tпосилання на реальну lambda body",
                        "captured args\tзначення, захоплені lambda")));
        uk.add(LessonBlock.paragraph(
                "Non-capturing lambda може бути створена один раз і перевикористана. Capturing lambda "
                + "захоплює значення з локального контексту, тому зазвичай потребує об'єкт із полями "
                + "для цих captured values."));
        uk.add(LessonBlock.warning(
                "Lambda не обов'язково означає anonymous inner class. У Java 8 це зазвичай "
                + "invokedynamic. Тому bytecode lambda дуже відрізняється від старого new Runnable(){...}."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте non-capturing Runnable lambda і знайдіть BootstrapMethods.",
                "Скомпілюйте capturing lambda, яка використовує локальну змінну.",
                "Порівняйте bytecode lambda і anonymous inner class.",
                "Знайдіть synthetic method lambda$... у javap -p.",
                "Поясніть, що таке bootstrap method."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("invokedynamic and LambdaMetafactory"));
        en.add(LessonBlock.paragraph(
                "invokedynamic appeared in Java 7 for dynamic languages on JVM, and in Java 8 it "
                + "became the foundation for lambdas. Unlike invokevirtual, where owner/method are "
                + "known directly, invokedynamic uses a bootstrap method. For lambdas it is usually "
                + "java/lang/invoke/LambdaMetafactory.metafactory."));
        en.add(LessonBlock.code(
                "Runnable r = () -> System.out.println(\"run\");\n"
                + "r.run();\n"
                + "\n"
                + "# javap -c may show:\n"
                + "invokedynamic #2,  0 // InvokeDynamic #0:run:()Ljava/lang/Runnable;\n"
                + "\n"
                + "# javap -v shows BootstrapMethods:\n"
                + "# 0: invokestatic java/lang/invoke/LambdaMetafactory.metafactory ..."));
        en.add(LessonBlock.table(
                "invokedynamic part\tMeaning",
                Arrays.asList(
                        "call site name\tfor example run, apply, test",
                        "call site descriptor\twhich functional interface to create",
                        "bootstrap method\tcode that creates call site",
                        "method handle\treference to real lambda body",
                        "captured args\tvalues captured by lambda")));
        en.add(LessonBlock.paragraph(
                "A non-capturing lambda may be created once and reused. A capturing lambda captures "
                + "values from local context, so it usually needs an object with fields for captured values."));
        en.add(LessonBlock.warning(
                "Lambda does not necessarily mean anonymous inner class. In Java 8 it is usually "
                + "invokedynamic. Therefore lambda bytecode is very different from old new Runnable(){...}."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile non-capturing Runnable lambda and find BootstrapMethods.",
                "Compile capturing lambda using a local variable.",
                "Compare lambda bytecode and anonymous inner class.",
                "Find synthetic method lambda$... in javap -p.",
                "Explain what bootstrap method is."));

        return new Lesson("jdk8.bytecode.advanced.6", "invokedynamic глибоко", "invokedynamic deep dive", uk, en);
    }

    private static Lesson materialBytecodeDebugAttributes() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Debug attributes: LineNumberTable, LocalVariableTable"));
        uk.add(LessonBlock.paragraph(
                "Stack trace показує файл і рядок не тому, що JVM магічно пам'ятає source. "
                + "Це береться з debug attributes у class file: SourceFile, LineNumberTable, "
                + "LocalVariableTable. Якщо компілювати без debug info, stack traces і debugger "
                + "будуть менш інформативними."));
        uk.add(LessonBlock.table(
                "Attribute\tЩо дає",
                Arrays.asList(
                        "SourceFile\tназва .java файлу",
                        "LineNumberTable\tзв'язок bytecode offset -> source line",
                        "LocalVariableTable\tімена і scope локальних змінних",
                        "LocalVariableTypeTable\tgeneric signatures локальних змінних",
                        "Deprecated / RuntimeVisibleAnnotations\tметадані для tools/runtime")));
        uk.add(LessonBlock.code(
                "# З debug info за замовчуванням:\n"
                + "javac Demo.java\n"
                + "javap -v Demo\n"
                + "\n"
                + "# Можна керувати debug info:\n"
                + "javac -g Demo.java\n"
                + "javac -g:none Demo.java"));
        uk.add(LessonBlock.paragraph(
                "LineNumberTable не впливає на логіку програми, але впливає на якість stack trace. "
                + "LocalVariableTable не потрібна JVM для виконання, але потрібна debugger-у, щоб "
                + "показувати назви локальних змінних."));
        uk.add(LessonBlock.warning(
                "Obfuscation може перейменувати класи, методи і поля, а також змінити debug attributes. "
                + "Для Android це особливо актуально при ProGuard/R8: stack trace без mapping.txt важко читати."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Скомпілюйте клас з -g і знайдіть LineNumberTable.",
                "Скомпілюйте з -g:none і порівняйте javap -v.",
                "Киньте RuntimeException і подивіться, чи є номер рядка.",
                "Знайдіть LocalVariableTable для методу з кількома локальними змінними.",
                "Поясніть, чому debug attributes не змінюють результат програми."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Debug attributes: LineNumberTable, LocalVariableTable"));
        en.add(LessonBlock.paragraph(
                "Stack trace shows file and line not because JVM magically remembers source. "
                + "It comes from debug attributes in class file: SourceFile, LineNumberTable, "
                + "LocalVariableTable. If compiled without debug info, stack traces and debugger "
                + "become less informative."));
        en.add(LessonBlock.table(
                "Attribute\tWhat it provides",
                Arrays.asList(
                        "SourceFile\t.java file name",
                        "LineNumberTable\tbytecode offset -> source line mapping",
                        "LocalVariableTable\tnames and scopes of local variables",
                        "LocalVariableTypeTable\tgeneric signatures of local variables",
                        "Deprecated / RuntimeVisibleAnnotations\tmetadata for tools/runtime")));
        en.add(LessonBlock.code(
                "# With debug info by default:\n"
                + "javac Demo.java\n"
                + "javap -v Demo\n"
                + "\n"
                + "# You can control debug info:\n"
                + "javac -g Demo.java\n"
                + "javac -g:none Demo.java"));
        en.add(LessonBlock.paragraph(
                "LineNumberTable does not affect program logic, but affects stack trace quality. "
                + "LocalVariableTable is not needed by JVM for execution, but the debugger needs it "
                + "to show local variable names."));
        en.add(LessonBlock.warning(
                "Obfuscation may rename classes, methods, and fields, and change debug attributes. "
                + "For Android this is especially relevant with ProGuard/R8: stack trace without mapping.txt is hard to read."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Compile a class with -g and find LineNumberTable.",
                "Compile with -g:none and compare javap -v.",
                "Throw RuntimeException and check whether line number exists.",
                "Find LocalVariableTable for a method with several local variables.",
                "Explain why debug attributes do not change program result."));

        return new Lesson("jdk8.bytecode.advanced.7", "Debug attributes", "Debug attributes", uk, en);
    }

    private static Lesson materialBytecodeCapstoneCompiler() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Capstone: маленький compiler у bytecode"));
        uk.add(LessonBlock.paragraph(
                "Щоб справді зрозуміти bytecode, корисно написати маленький compiler з простих "
                + "виразів у stack-machine інструкції. Не обов'язково одразу генерувати .class. "
                + "Спершу зробіть власний навчальний instruction list, який схожий на JVM: iconst, "
                + "iload, istore, iadd, imul, ireturn."));
        uk.add(LessonBlock.code(
                "Вираз:\n"
                + "  (a + 2) * b\n"
                + "\n"
                + "Навчальні інструкції:\n"
                + "  iload a\n"
                + "  iconst 2\n"
                + "  iadd\n"
                + "  iload b\n"
                + "  imul\n"
                + "  ireturn\n"
                + "\n"
                + "Stack trace виконання:\n"
                + "  [] -> [a] -> [a,2] -> [a+2] -> [a+2,b] -> [(a+2)*b]"));
        uk.add(LessonBlock.heading("Етапи проєкту"));
        uk.add(LessonBlock.list(
                "1. Опишіть AST: NumberExpr, VarExpr, AddExpr, MulExpr.",
                "2. Напишіть compiler: Expr -> List<String> інструкцій.",
                "3. Напишіть interpreter для цих інструкцій зі stack.",
                "4. Додайте local variables map: a=3, b=4.",
                "5. Додайте if expression: cond ? left : right через labels.",
                "6. Порівняйте ваші інструкції з javap для аналогічного Java-коду.",
                "7. Опишіть maxStack для кожного виразу.",
                "8. Додайте помилки verifier-style: stack underflow, unknown local, wrong return type."));
        uk.add(LessonBlock.table(
                "Компонент\tЩо тренує",
                Arrays.asList(
                        "AST\tмислення компілятора",
                        "Instruction list\tзв'язок high-level -> bytecode",
                        "Stack interpreter\toperand stack",
                        "Labels\tcontrol flow",
                        "maxStack\tліміти методу",
                        "Verifier-style checks\tбезпека bytecode")));
        uk.add(LessonBlock.note(
                "Після такого навчального compiler легше переходити до справжніх інструментів "
                + "на кшталт ASM: ви вже розумієте, які інструкції треба генерувати і чому verifier свариться."));
        uk.add(LessonBlock.warning(
                "Не починайте bytecode-розробку з генерації складних класів. Спершу навчіться "
                + "генерувати і перевіряти маленькі методи: add, max, loop, array access, try/catch."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Зробіть AST для арифметики int.",
                "Згенеруйте instruction list для 5 різних виразів.",
                "Напишіть interpreter і перевірте результати.",
                "Порахуйте maxStack вручну і автоматично.",
                "Додайте labels для if/else.",
                "Порівняйте з javap реального Java-методу."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Capstone: tiny compiler to bytecode"));
        en.add(LessonBlock.paragraph(
                "To really understand bytecode, write a tiny compiler from simple expressions "
                + "to stack-machine instructions. You do not need to generate .class immediately. "
                + "First create your own educational instruction list resembling JVM: iconst, "
                + "iload, istore, iadd, imul, ireturn."));
        en.add(LessonBlock.code(
                "Expression:\n"
                + "  (a + 2) * b\n"
                + "\n"
                + "Educational instructions:\n"
                + "  iload a\n"
                + "  iconst 2\n"
                + "  iadd\n"
                + "  iload b\n"
                + "  imul\n"
                + "  ireturn\n"
                + "\n"
                + "Execution stack trace:\n"
                + "  [] -> [a] -> [a,2] -> [a+2] -> [a+2,b] -> [(a+2)*b]"));
        en.add(LessonBlock.heading("Project stages"));
        en.add(LessonBlock.list(
                "1. Define AST: NumberExpr, VarExpr, AddExpr, MulExpr.",
                "2. Write compiler: Expr -> List<String> instructions.",
                "3. Write interpreter for these instructions with stack.",
                "4. Add local variables map: a=3, b=4.",
                "5. Add if expression: cond ? left : right through labels.",
                "6. Compare your instructions with javap for equivalent Java code.",
                "7. Describe maxStack for every expression.",
                "8. Add verifier-style errors: stack underflow, unknown local, wrong return type."));
        en.add(LessonBlock.table(
                "Component\tWhat it trains",
                Arrays.asList(
                        "AST\tcompiler thinking",
                        "Instruction list\thigh-level -> bytecode connection",
                        "Stack interpreter\toperand stack",
                        "Labels\tcontrol flow",
                        "maxStack\tmethod limits",
                        "Verifier-style checks\tbytecode safety")));
        en.add(LessonBlock.note(
                "After this educational compiler, moving to real tools such as ASM is easier: "
                + "you already understand which instructions to generate and why verifier complains."));
        en.add(LessonBlock.warning(
                "Do not start bytecode development by generating complex classes. First learn "
                + "to generate and verify tiny methods: add, max, loop, array access, try/catch."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create AST for int arithmetic.",
                "Generate instruction list for 5 different expressions.",
                "Write interpreter and verify results.",
                "Calculate maxStack manually and automatically.",
                "Add labels for if/else.",
                "Compare with javap of real Java method."));

        return new Lesson("jdk8.bytecode.advanced.8", "Bytecode capstone compiler", "Bytecode capstone compiler", uk, en);
    }
}
