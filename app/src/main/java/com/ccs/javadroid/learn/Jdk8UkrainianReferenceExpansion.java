package com.ccs.javadroid.learn;

import java.util.Arrays;
import java.util.List;

/**
 * Ukrainian-only reference additions for every Java-side JDK 8 Deep Dive lesson.
 *
 * <p>The base lessons stay readable as a course. These blocks turn the same material into a
 * practical reference: exact contracts, runnable examples, edge cases and complexity notes.
 * English content is deliberately not touched until a dedicated translation pass.</p>
 */
final class Jdk8UkrainianReferenceExpansion {

    private static final String MARKER = "Детальний довідник JDK 8";

    private Jdk8UkrainianReferenceExpansion() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                if (!lesson.id.startsWith("jdk8.") || lesson.id.startsWith("jdk8.bytecode.")) {
                    continue;
                }
                List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
                if (containsMarker(uk)) {
                    continue;
                }
                addReference(lesson.id, uk);
            }
        }
    }

    private static boolean containsMarker(List<LessonBlock> blocks) {
        for (LessonBlock block : blocks) {
            if (block.type == LessonBlock.HEADING && MARKER.equals(block.text)) {
                return true;
            }
        }
        return false;
    }

    private static void begin(List<LessonBlock> uk, String focus) {
        uk.add(LessonBlock.heading(MARKER));
        uk.add(LessonBlock.paragraph(focus));
    }

    private static void addReference(String id, List<LessonBlock> uk) {
        switch (id) {
            case "jdk8.1":
                orientation(uk);
                break;
            case "jdk8.collections.1":
                collectionHierarchy(uk);
                break;
            case "jdk8.collections.2":
                listContract(uk);
                break;
            case "jdk8.collections.3":
                arrayList(uk);
                break;
            case "jdk8.collections.4":
                legacyLists(uk);
                break;
            case "jdk8.collections.5":
                iterators(uk);
                break;
            case "jdk8.setmap.1":
                sets(uk);
                break;
            case "jdk8.setmap.2":
                hashMap(uk);
                break;
            case "jdk8.setmap.3":
                mapImplementations(uk);
                break;
            case "jdk8.algorithms.1":
                collectionAlgorithms(uk);
                break;
            case "jdk8.algorithms.2":
                streams(uk);
                break;
            case "jdk8.algorithms.3":
                concurrentCollections(uk);
                break;
            case "jdk8.practice.1":
                collectionPractice(uk);
                break;
            case "jdk8.generics.1":
                generics(uk);
                break;
            case "jdk8.generics.2":
                erasure(uk);
                break;
            case "jdk8.generics.3":
                wildcards(uk);
                break;
            case "jdk8.oop.1":
                objectContract(uk);
                break;
            case "jdk8.oop.2":
                inheritance(uk);
                break;
            case "jdk8.exceptions.1":
                exceptions(uk);
                break;
            case "jdk8.exceptions.2":
                resources(uk);
                break;
            case "jdk8.io.1":
                ioNio(uk);
                break;
            case "jdk8.datetime.1":
                dateTime(uk);
                break;
            case "jdk8.concurrent.1":
                memoryModel(uk);
                break;
            case "jdk8.concurrent.2":
                executors(uk);
                break;
            case "jdk8.capstone.1":
                capstone(uk);
                break;
            case "jdk8.functional.1":
                functionalInterfaces(uk);
                break;
            case "jdk8.functional.2":
                methodReferences(uk);
                break;
            case "jdk8.functional.3":
                optional(uk);
                break;
            case "jdk8.functional.4":
                regex(uk);
                break;
            case "jdk8.runtime.1":
                reflection(uk);
                break;
            case "jdk8.runtime.2":
                serialization(uk);
                break;
            case "jdk8.platform.1":
                classpath(uk);
                break;
            case "jdk8.platform.2":
                diagnostics(uk);
                break;
            default:
                // A new lesson must receive a deliberate reference expansion, not filler.
                break;
        }
    }

    private static void orientation(List<LessonBlock> uk) {
        begin(uk, "Для будь-якого API фіксуйте п'ять речей: контракт, допустимий null, мутабельність, "
                + "складність і потокобезпечність. Назва методу сама по собі цього не гарантує.");
        uk.add(LessonBlock.table("Запитання\tДе шукати відповідь\tПриклад",
                Arrays.asList(
                        "Чи можна null?\tJavadoc параметра і реалізації\tTreeMap з natural ordering не приймає null-ключ",
                        "Хто володіє даними?\tcopy/view формулювання\tArrays.asList створює view над масивом",
                        "Чи є порядок?\tконтракт інтерфейсу\tHashMap порядку не гарантує",
                        "Чи безпечно між потоками?\tопис synchronization\tArrayList не thread-safe",
                        "Коли падає?\tThrows у Javadoc\tList.get може кинути IndexOutOfBoundsException")));
        uk.add(LessonBlock.code("static void inspect(List<String> list) {\n"
                + "    System.out.println(list.getClass().getName());\n"
                + "    System.out.println(\"size=\" + list.size());\n"
                + "    System.out.println(\"allows duplicate: \" + list.add(list.get(0)));\n"
                + "}\n"
                + "\n"
                + "inspect(new ArrayList<String>(Arrays.asList(\"A\", \"B\")));"));
        uk.add(LessonBlock.note("Оголошуйте змінну через інтерфейс, але документуйте властивості, "
                + "на які реально спирається код: порядок, унікальність, сортування або O(1)-доступ."));
    }

    private static void collectionHierarchy(List<LessonBlock> uk) {
        begin(uk, "Колекції розрізняються не лише методом add або put. Важливі порядок ітерації, "
                + "політика дублікатів, null, асимптотика та тип ітератора.");
        uk.add(LessonBlock.table("Абстракція\tКлючова гарантія\tНе гарантує",
                Arrays.asList(
                        "List<E>\tпозиційний порядок, дублікати\tшвидкий доступ за індексом",
                        "Set<E>\tне більше одного рівного елемента\tпорядок обходу",
                        "SortedSet<E>\tсортування і range-view\tO(1) пошук",
                        "Queue<E>\tполітика видачі елементів\tFIFO для PriorityQueue",
                        "Map<K,V>\tунікальні ключі\tнезалежність view keySet/values")));
        uk.add(LessonBlock.code("Collection<String> values = new ArrayList<String>();\n"
                + "boolean changed1 = values.add(\"A\");       // true\n"
                + "boolean changed2 = values.addAll(Arrays.asList(\"B\", \"C\"));\n"
                + "boolean changed3 = values.removeIf(s -> s.compareTo(\"B\") >= 0);\n"
                + "System.out.println(changed1 + \" \" + changed2 + \" \" + changed3);"));
        uk.add(LessonBlock.warning("Методи, що повертають boolean, зазвичай повідомляють, чи змінилася "
                + "колекція. Ігнорування результату add для Set приховує дублікати."));
    }

    private static void listContract(List<LessonBlock> uk) {
        begin(uk, "List має дві групи операцій: позиційні та пошукові. Позиційна вставка зсуває хвіст, "
                + "а indexOf використовує equals і повертає -1, якщо збігу немає.");
        uk.add(LessonBlock.table("Операція\tArrayList\tLinkedList",
                Arrays.asList(
                        "get/set за індексом\tO(1)\tO(n)",
                        "add у кінець\tамортизовано O(1)\tO(1)",
                        "add/remove на початку\tO(n)\tO(1)",
                        "contains/indexOf\tO(n)\tO(n)",
                        "ітерація\tcache-friendly\tперехід між вузлами")));
        uk.add(LessonBlock.code("List<Integer> xs = new ArrayList<Integer>(Arrays.asList(10, 20, 10));\n"
                + "xs.remove(1);                         // remove(int): видаляє 20\n"
                + "xs.remove(Integer.valueOf(10));       // remove(Object): першу 10\n"
                + "List<Integer> view = xs.subList(0, xs.size());\n"
                + "view.clear();                         // змінює також xs\n"
                + "System.out.println(xs);               // []"));
        uk.add(LessonBlock.warning("subList — backed view, а не незалежна копія. Структурна зміна "
                + "батьківського списку поза view робить подальшу роботу з view невизначеною і часто "
                + "завершується ConcurrentModificationException."));
    }

    private static void arrayList(List<LessonBlock> uk) {
        begin(uk, "ArrayList зберігає елементи у масиві Object[]. capacity — довжина внутрішнього "
                + "масиву, size — кількість логічних елементів; це різні величини.");
        uk.add(LessonBlock.code("ArrayList<String> buffer = new ArrayList<String>(1000);\n"
                + "System.out.println(buffer.size()); // 0, а не 1000\n"
                + "for (int i = 0; i < 900; i++) {\n"
                + "    buffer.add(\"row-\" + i);\n"
                + "}\n"
                + "buffer.ensureCapacity(2000);       // мінімізує майбутні reallocations\n"
                + "buffer.trimToSize();               // звільняє зайву capacity"));
        uk.add(LessonBlock.list(
                "set(index, value) замінює наявний елемент і не збільшує size.",
                "add(index, value) вставляє елемент і зсуває праву частину.",
                "toArray(new String[0]) повертає типізований String[], звичайний toArray() — Object[].",
                "clone() копіює структуру списку поверхнево: самі елементи лишаються спільними."));
        uk.add(LessonBlock.note("Задавайте початкову capacity, коли приблизний розмір відомий. Це "
                + "оптимізація allocations, а не спосіб створити заповнені null комірки."));
    }

    private static void legacyLists(List<LessonBlock> uk) {
        begin(uk, "LinkedList одночасно реалізує List і Deque. Vector синхронізує окремі методи, "
                + "але складена операція з кількох викликів усе одно не стає атомарною.");
        uk.add(LessonBlock.code("Deque<String> tasks = new ArrayDeque<String>();\n"
                + "tasks.addLast(\"compile\");\n"
                + "tasks.addLast(\"test\");\n"
                + "while (!tasks.isEmpty()) {\n"
                + "    System.out.println(tasks.removeFirst());\n"
                + "}\n"
                + "\n"
                + "// Stack зазвичай краще замінити на Deque:\n"
                + "Deque<Integer> stack = new ArrayDeque<Integer>();\n"
                + "stack.push(10); stack.push(20);\n"
                + "System.out.println(stack.pop()); // 20"));
        uk.add(LessonBlock.table("Потреба\tРекомендація\tПричина",
                Arrays.asList(
                        "стек\tArrayDeque\tчистіший API без спадщини Vector",
                        "черга\tArrayDeque\tшвидкі операції на обох кінцях",
                        "частий get(i)\tArrayList\tLinkedList має O(n)",
                        "вставка через Iterator усередині\tLinkedList\tне треба зсувати масив",
                        "legacy API\tVector/Stack\tлише коли вимагає старий контракт")));
        uk.add(LessonBlock.warning("ArrayDeque не дозволяє null: null використовується як сигнал "
                + "відсутності для peek/poll. LinkedList технічно дозволяє null, але для черги це двозначно."));
    }

    private static void iterators(List<LessonBlock> uk) {
        begin(uk, "Ітератор має стан між елементами. remove() дозволений лише один раз після успішного "
                + "next(); ListIterator додатково підтримує рух назад, set та add.");
        uk.add(LessonBlock.code("List<String> names = new ArrayList<String>(\n"
                + "        Arrays.asList(\"Ann\", \"\", \"Bob\"));\n"
                + "ListIterator<String> it = names.listIterator();\n"
                + "while (it.hasNext()) {\n"
                + "    String value = it.next();\n"
                + "    if (value.isEmpty()) it.remove();\n"
                + "    else it.set(value.toUpperCase());\n"
                + "}\n"
                + "System.out.println(names); // [ANN, BOB]"));
        uk.add(LessonBlock.list(
                "for-each компілюється в Iterator для Iterable, але в індексний цикл для масиву.",
                "Fail-fast — best effort діагностика, а не гарантія виявити кожну конкурентну зміну.",
                "Iterator weakly consistent у concurrent-колекціях не кидає CME і може бачити частину змін.",
                "Enumeration — legacy read-only обхід; remove у ньому немає."));
        uk.add(LessonBlock.warning("Не викликайте list.remove(...) під час обходу звичайним Iterator. "
                + "Видаляйте через iterator.remove() або застосовуйте removeIf у JDK 8."));
    }

    private static void sets(List<LessonBlock> uk) {
        begin(uk, "Set визначає дубль через equals, а hash-реалізації спочатку використовують hashCode. "
                + "TreeSet визначає еквівалентність через compare/compareTo == 0.");
        uk.add(LessonBlock.code("final class User {\n"
                + "    final int id;\n"
                + "    User(int id) { this.id = id; }\n"
                + "    public boolean equals(Object o) {\n"
                + "        return o instanceof User && ((User) o).id == id;\n"
                + "    }\n"
                + "    public int hashCode() { return Integer.hashCode(id); }\n"
                + "}\n"
                + "Set<User> users = new HashSet<User>();\n"
                + "users.add(new User(7));\n"
                + "users.add(new User(7));\n"
                + "System.out.println(users.size()); // 1"));
        uk.add(LessonBlock.table("Реалізація\tПорядок\tТипова ціна",
                Arrays.asList(
                        "HashSet\tне визначений\tO(1) average",
                        "LinkedHashSet\tпорядок вставки\tO(1) + зв'язки",
                        "TreeSet\tвідсортований\tO(log n)",
                        "EnumSet\tпорядок enum-констант\tкомпактна bit vector")));
        uk.add(LessonBlock.warning("Не змінюйте поля, що беруть участь у equals/hashCode, поки об'єкт "
                + "лежить у HashSet. Елемент опиниться не у своєму bucket і його може бути неможливо знайти."));
    }

    private static void hashMap(List<LessonBlock> uk) {
        begin(uk, "HashMap у JDK 8 індексує bucket за змішаним hash. Колізії зберігаються у списку, "
                + "а довгі bucket за достатньої capacity можуть перетворюватися на red-black tree.");
        uk.add(LessonBlock.code("Map<String, Integer> counts = new HashMap<String, Integer>();\n"
                + "for (String word : Arrays.asList(\"java\", \"map\", \"java\")) {\n"
                + "    counts.merge(word, 1, Integer::sum);\n"
                + "}\n"
                + "counts.computeIfAbsent(\"jdk\", k -> k.length());\n"
                + "System.out.println(counts); // java=2, map=1, jdk=3; порядок довільний"));
        uk.add(LessonBlock.table("Метод\tВажливий нюанс",
                Arrays.asList(
                        "put\tповертає попереднє значення або null",
                        "putIfAbsent\tвважає null-значення відсутнім",
                        "computeIfAbsent\tне записує mapping, якщо функція повернула null",
                        "compute\tвидаляє mapping, якщо remapping повернула null",
                        "merge\tзручно для лічильників; null-результат видаляє ключ")));
        uk.add(LessonBlock.warning("containsKey потрібен, щоб відрізнити «ключ відсутній» від "
                + "«ключ явно відображається у null». Один get цього не розрізняє."));
    }

    private static void mapImplementations(List<LessonBlock> uk) {
        begin(uk, "Вибір Map — це вибір порядку, способу порівняння, політики null і конкурентної поведінки.");
        uk.add(LessonBlock.table("Map\tПорядок\tnull\tОсобливість",
                Arrays.asList(
                        "HashMap\tнемає\t1 key, багато values\tшвидкий загальний випадок",
                        "LinkedHashMap\tinsertion/access\tяк HashMap\tLRU-подібні кеші",
                        "TreeMap\tsorted\tnull key зазвичай ні\trange operations",
                        "EnumMap\tenum order\tkey ні\tдуже компактний для enum",
                        "IdentityHashMap\tнемає\tтак\tпорівнює ключі через ==",
                        "WeakHashMap\tнемає\tтак\tключ не утримується strong reference")));
        uk.add(LessonBlock.code("Map<Integer, String> byAccess = new LinkedHashMap<Integer, String>(\n"
                + "        16, 0.75f, true) {\n"
                + "    protected boolean removeEldestEntry(Map.Entry<Integer, String> e) {\n"
                + "        return size() > 3;\n"
                + "    }\n"
                + "};\n"
                + "byAccess.put(1, \"A\"); byAccess.put(2, \"B\");\n"
                + "byAccess.get(1); // 1 стає найновішим за access-order"));
        uk.add(LessonBlock.note("keySet(), values() та entrySet() — живі views. Видалення через них "
                + "змінює Map; add зазвичай не підтримується, бо для нього бракує ключа або значення."));
    }

    private static void collectionAlgorithms(List<LessonBlock> uk) {
        begin(uk, "Алгоритми Collections змінюють переданий список, якщо документація не каже інакше. "
                + "binarySearch коректний лише для того самого порядку, яким список відсортовано.");
        uk.add(LessonBlock.code("List<String> names = new ArrayList<String>(\n"
                + "        Arrays.asList(\"Bob\", \"ann\", \"Clara\"));\n"
                + "Comparator<String> byText = String.CASE_INSENSITIVE_ORDER;\n"
                + "Collections.sort(names, byText);\n"
                + "int index = Collections.binarySearch(names, \"ANN\", byText);\n"
                + "if (index < 0) {\n"
                + "    int insertionPoint = -index - 1;\n"
                + "    names.add(insertionPoint, \"ANN\");\n"
                + "}"));
        uk.add(LessonBlock.list(
                "sort у Java 8 для об'єктів стабільний: рівні елементи зберігають взаємний порядок.",
                "shuffle використовує Random; передайте seeded Random для відтворюваного тесту.",
                "unmodifiableList — read-only view, але зміни вихідного списку в ньому видно.",
                "frequency і disjoint використовують equals та можуть бути O(n*m) залежно від колекцій."));
        uk.add(LessonBlock.warning("Comparator має бути транзитивним і узгодженим із самим собою. "
                + "Порушення загального порядку може дати 'Comparison method violates its general contract'."));
    }

    private static void streams(List<LessonBlock> uk) {
        begin(uk, "Stream — одноразовий конвеєр, а не контейнер. Проміжні операції lazy; обхід джерела "
                + "починає terminal operation. Side effects у map/filter ускладнюють паралелізм і тести.");
        uk.add(LessonBlock.code("List<String> result = Arrays.asList(\" java \", \"\", \"JDK\", \"java\")\n"
                + "        .stream()\n"
                + "        .map(String::trim)\n"
                + "        .filter(s -> !s.isEmpty())\n"
                + "        .map(String::toLowerCase)\n"
                + "        .distinct()\n"
                + "        .sorted()\n"
                + "        .collect(Collectors.toList());\n"
                + "System.out.println(result); // [java, jdk]"));
        uk.add(LessonBlock.table("Операція\tТип\tНюанс",
                Arrays.asList(
                        "filter/map\tstateless intermediate\tlazy",
                        "distinct/sorted\tstateful intermediate\tможе буферизувати дані",
                        "findFirst\tshort-circuit terminal\tповажає encounter order",
                        "findAny\tshort-circuit terminal\tкращий кандидат для parallel",
                        "reduce/collect\tterminal\tcombiner критичний у parallel")));
        uk.add(LessonBlock.warning("Не використовуйте один Stream двічі: після terminal operation він "
                + "закритий логічно й повторний виклик кидає IllegalStateException."));
    }

    private static void concurrentCollections(List<LessonBlock> uk) {
        begin(uk, "Concurrent-колекція дає визначені гарантії окремих операцій, але послідовність "
                + "containsKey + put не стає атомарною. Для складеної дії потрібні compute/merge або lock.");
        uk.add(LessonBlock.code("ConcurrentMap<String, LongAdder> counters =\n"
                + "        new ConcurrentHashMap<String, LongAdder>();\n"
                + "counters.computeIfAbsent(\"requests\", k -> new LongAdder()).increment();\n"
                + "long current = counters.get(\"requests\").sum();\n"
                + "System.out.println(current);"));
        uk.add(LessonBlock.table("Тип\tКоли використовувати",
                Arrays.asList(
                        "ConcurrentHashMap\tчасті паралельні read/write за ключем",
                        "CopyOnWriteArrayList\tдуже багато читань, дуже мало записів",
                        "BlockingQueue\tproducer-consumer з backpressure",
                        "ConcurrentLinkedQueue\tnon-blocking FIFO без очікування",
                        "ConcurrentSkipListMap\tпаралельна sorted map")));
        uk.add(LessonBlock.warning("size() у колекції, яку активно змінюють інші потоки, — моментальний "
                + "діагностичний знімок, а не база для логіки 'if size then act'."));
    }

    private static void collectionPractice(List<LessonBlock> uk) {
        begin(uk, "Практичне завдання має перевіряти контракт, а не лише happy path. Для кожної структури "
                + "додайте дублікати, null (де дозволено), порожній ввід і великий набір даних.");
        uk.add(LessonBlock.code("static <T> Map<T, Integer> histogram(Iterable<T> source) {\n"
                + "    Map<T, Integer> result = new LinkedHashMap<T, Integer>();\n"
                + "    for (T value : source) {\n"
                + "        result.merge(value, 1, Integer::sum);\n"
                + "    }\n"
                + "    return result;\n"
                + "}\n"
                + "System.out.println(histogram(Arrays.asList(\"a\", \"b\", \"a\")));"));
        uk.add(LessonBlock.list(
                "Перевірте, що порядок першої появи збережено завдяки LinkedHashMap.",
                "Замініть LinkedHashMap на TreeMap і зафіксуйте вимоги до T.",
                "Додайте null та поясніть поведінку кожної реалізації.",
                "Оцініть time O(n) і space O(k), де k — кількість унікальних значень.",
                "Напишіть тести для порожнього, одноелементного і повторюваного вводу."));
        uk.add(LessonBlock.note("Хороша відповідь до вправи містить не тільки код, а й обґрунтування "
                + "вибору реалізації, складність та інваріант результату."));
    }

    private static void generics(List<LessonBlock> uk) {
        begin(uk, "Generic-параметр може мати кілька bounds, але class-bound записується першим. "
                + "Generic-метод оголошує <T> перед типом результату; параметр класу і методу незалежні.");
        uk.add(LessonBlock.code("static <T extends Number & Comparable<T>> T max(T a, T b) {\n"
                + "    return a.compareTo(b) >= 0 ? a : b;\n"
                + "}\n"
                + "\n"
                + "static <T> T requireFirst(List<T> values) {\n"
                + "    if (values.isEmpty()) throw new NoSuchElementException();\n"
                + "    return values.get(0);\n"
                + "}\n"
                + "Integer answer = max(10, 20);"));
        uk.add(LessonBlock.table("Правило\tНаслідок",
                Arrays.asList(
                        "Generics invariant\tList<Integer> не є List<Number>",
                        "Diamond у Java 8\tnew ArrayList<>() виводить аргументи з target type",
                        "Примітиви заборонені\tвикористовуйте Integer, Long тощо",
                        "Static не бачить T класу\tоголосіть власний static <T>",
                        "Generic exception заборонений\tclass Problem<T> extends Exception не компілюється")));
        uk.add(LessonBlock.note("Публічний API краще приймає найзагальніший коректний тип, наприклад "
                + "List<? extends Number>, але повертає конкретний зручний тип без wildcard, якщо можливо."));
    }

    private static void erasure(List<LessonBlock> uk) {
        begin(uk, "Erasure замінює необмежений T на Object, а T extends Number — на Number. Компілятор "
                + "може створити bridge method, щоб зберегти поліморфізм після стирання сигнатур.");
        uk.add(LessonBlock.code("class Parent<T> { T value() { return null; } }\n"
                + "class Child extends Parent<String> {\n"
                + "    @Override String value() { return \"ok\"; }\n"
                + "}\n"
                + "// Компілятор додає synthetic bridge приблизно такого змісту:\n"
                + "// Object value() { return value(); }"));
        uk.add(LessonBlock.list(
                "List<String>.class не існує: є лише List.class.",
                "Перевантаження m(List<String>) і m(List<Integer>) неможливе через однаковий erasure.",
                "@SafeVarargs дозволений для final/static/constructor у Java 8, коли тіло справді type-safe.",
                "Generic signature зберігається у metadata для reflection, хоча JVM-інструкції працюють зі стертим типом."));
        uk.add(LessonBlock.warning("Масив generic-varargs доступний як масив runtime. Не записуйте в нього "
                + "значення іншого параметризованого типу й не передавайте посилання назовні."));
    }

    private static void wildcards(List<LessonBlock> uk) {
        begin(uk, "Wildcard описує сімейство типів, а не новий named type. Capture conversion дозволяє "
                + "компілятору тимчасово назвати невідомий тип; інколи для цього потрібен helper-метод.");
        uk.add(LessonBlock.code("static double sum(List<? extends Number> values) {\n"
                + "    double total = 0.0;\n"
                + "    for (Number n : values) total += n.doubleValue();\n"
                + "    return total;\n"
                + "}\n"
                + "static void addDefaults(List<? super Integer> out) {\n"
                + "    out.add(0); out.add(1);\n"
                + "}\n"
                + "static void swapFirstTwo(List<?> list) { swap(list, 0, 1); }\n"
                + "private static <T> void swap(List<T> list, int a, int b) {\n"
                + "    list.set(a, list.set(b, list.get(a)));\n"
                + "}"));
        uk.add(LessonBlock.table("Тип\tМожна читати як\tМожна додати",
                Arrays.asList(
                        "List<?>\tObject\tлише null",
                        "List<? extends Number>\tNumber\tлише null",
                        "List<? super Integer>\tObject\tInteger і підтипи",
                        "List<Integer>\tInteger\tInteger")));
        uk.add(LessonBlock.warning("PECS стосується параметрів-джерел і параметрів-приймачів. Не "
                + "замінюйте кожен List<T> на wildcard: якщо метод і читає, і записує T, зазвичай потрібен List<T>."));
    }

    private static void objectContract(List<LessonBlock> uk) {
        begin(uk, "equals має бути рефлексивним, симетричним, транзитивним, консистентним і повертати "
                + "false для null. Рівні об'єкти зобов'язані мати однаковий hashCode.");
        uk.add(LessonBlock.code("final class Point {\n"
                + "    private final int x, y;\n"
                + "    Point(int x, int y) { this.x = x; this.y = y; }\n"
                + "    @Override public boolean equals(Object obj) {\n"
                + "        if (this == obj) return true;\n"
                + "        if (!(obj instanceof Point)) return false;\n"
                + "        Point other = (Point) obj;\n"
                + "        return x == other.x && y == other.y;\n"
                + "    }\n"
                + "    @Override public int hashCode() { return 31 * x + y; }\n"
                + "    @Override public String toString() { return \"Point(\" + x + \", \" + y + \")\"; }\n"
                + "}"));
        uk.add(LessonBlock.list(
                "== порівнює identity посилань; equals — логічну рівність за контрактом класу.",
                "getClass() у equals забороняє рівність із підкласом; instanceof дозволяє її, але ускладнює симетрію.",
                "toString не повинен розкривати паролі, токени чи персональні дані.",
                "finalize у Java 8 непередбачуваний і не підходить для закриття ресурсів."));
        uk.add(LessonBlock.warning("Поле, яке входить до hashCode ключа HashMap, має бути фактично "
                + "незмінним протягом перебування ключа в map."));
    }

    private static void inheritance(List<LessonBlock> uk) {
        begin(uk, "Override вибирається динамічно за runtime-класом об'єкта. Поля, static і private "
                + "методи не поліморфні: вони приховуються або зв'язуються за compile-time типом.");
        uk.add(LessonBlock.code("class Base {\n"
                + "    String field = \"base\";\n"
                + "    String value() { return \"base\"; }\n"
                + "    static String kind() { return \"Base\"; }\n"
                + "}\n"
                + "class Derived extends Base {\n"
                + "    String field = \"derived\";\n"
                + "    @Override String value() { return \"derived\"; }\n"
                + "    static String kind() { return \"Derived\"; }\n"
                + "}\n"
                + "Base x = new Derived();\n"
                + "System.out.println(x.field + \" / \" + x.value() + \" / \" + x.kind());\n"
                + "// base / derived / Base"));
        uk.add(LessonBlock.table("Елемент\tПравило",
                Arrays.asList(
                        "return type\tможе бути covariant",
                        "checked exceptions\tне можна розширити в override",
                        "visibility\tне можна звузити",
                        "constructor\tне успадковується; super(...) має бути першим",
                        "final method\tне перевизначається")));
        uk.add(LessonBlock.note("Надавайте перевагу композиції, коли немає справжнього is-a контракту "
                + "або підклас мусив би порушувати інваріанти базового класу."));
    }

    private static void exceptions(List<LessonBlock> uk) {
        begin(uk, "Checked exception є частиною compile-time контракту. RuntimeException зазвичай "
                + "сигналізує порушення передумови або програмну помилку, яку локально не виправити.");
        uk.add(LessonBlock.code("static int parsePort(String text) {\n"
                + "    try {\n"
                + "        int port = Integer.parseInt(text);\n"
                + "        if (port < 1 || port > 65535) {\n"
                + "            throw new IllegalArgumentException(\"port out of range: \" + port);\n"
                + "        }\n"
                + "        return port;\n"
                + "    } catch (NumberFormatException e) {\n"
                + "        throw new IllegalArgumentException(\"not a decimal port: \" + text, e);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.list(
                "Ловіть найвужчий тип, який справді можете обробити.",
                "Зберігайте cause при перекладі винятку між шарами.",
                "Не використовуйте exception для звичайного керування циклом.",
                "Multi-catch IOException | SQLException e забороняє альтернативи, де одна є підтипом іншої.",
                "finally виконується і після return, але System.exit або аварія JVM є винятками."));
        uk.add(LessonBlock.warning("return або throw з finally маскує результат і первинний виняток. "
                + "У finally робіть лише коротке очищення, яке не перебиває основну помилку."));
    }

    private static void resources(List<LessonBlock> uk) {
        begin(uk, "try-with-resources закриває ресурси у зворотному порядку. Якщо тіло і close кидають "
                + "винятки, виняток тіла головний, а помилки close доступні через getSuppressed().");
        uk.add(LessonBlock.code("try (InputStream in = Files.newInputStream(path);\n"
                + "     BufferedInputStream buffered = new BufferedInputStream(in)) {\n"
                + "    return buffered.read();\n"
                + "} catch (IOException e) {\n"
                + "    for (Throwable suppressed : e.getSuppressed()) {\n"
                + "        System.err.println(\"close failed: \" + suppressed);\n"
                + "    }\n"
                + "    throw e;\n"
                + "}"));
        uk.add(LessonBlock.table("Ситуація\tРезультат",
                Arrays.asList(
                        "тіло успішне, close успішний\tзвичайний результат",
                        "тіло успішне, close падає\tвиняток close",
                        "тіло падає, close успішний\tвиняток тіла",
                        "тіло і close падають\tтіло primary, close suppressed",
                        "два close падають\tобидва suppressed у порядку закриття")));
        uk.add(LessonBlock.warning("У Java 8 ресурс треба оголосити всередині дужок try. Синтаксис "
                + "try (existingVariable) з'явився пізніше, у Java 9."));
    }

    private static void ioNio(List<LessonBlock> uk) {
        begin(uk, "InputStream/OutputStream працюють із байтами, Reader/Writer — із символами. "
                + "Перетворення між ними завжди потребує явного Charset.");
        uk.add(LessonBlock.code("Charset utf8 = StandardCharsets.UTF_8;\n"
                + "Path target = Paths.get(\"notes.txt\");\n"
                + "try (BufferedWriter out = Files.newBufferedWriter(target, utf8,\n"
                + "        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {\n"
                + "    out.write(\"Привіт\");\n"
                + "    out.newLine();\n"
                + "}\n"
                + "try (Stream<String> lines = Files.lines(target, utf8)) {\n"
                + "    lines.forEach(System.out::println);\n"
                + "}"));
        uk.add(LessonBlock.table("API\tПоведінка",
                Arrays.asList(
                        "Files.readAllBytes\tвесь файл у heap; лише для контрольованого розміру",
                        "Files.lines\tlazy Stream; його треба закрити",
                        "BufferedReader.readLine\tне повертає символ переносу рядка",
                        "Path.normalize\tлексично прибирає . і .., не читає файлову систему",
                        "Path.toRealPath\tпотребує існування і розв'язує symbolic links")));
        uk.add(LessonBlock.warning("Не покладайтеся на default charset: він залежить від пристрою/системи. "
                + "Для формату файлу або протоколу завжди задавайте UTF-8 чи інше визначене кодування."));
    }

    private static void dateTime(List<LessonBlock> uk) {
        begin(uk, "Instant — точка на UTC timeline; LocalDateTime — локальні поля без zone; "
                + "ZonedDateTime — локальні поля плюс ZoneId і правила переходів.");
        uk.add(LessonBlock.code("ZoneId kyiv = ZoneId.of(\"Europe/Kiev\"); // сумісно зі старою tzdb JDK 8\n"
                + "Instant now = Instant.now();\n"
                + "ZonedDateTime local = now.atZone(kyiv);\n"
                + "String text = local.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);\n"
                + "Instant restored = ZonedDateTime.parse(text).toInstant();\n"
                + "System.out.println(now.equals(restored));\n"
                + "\n"
                + "LocalDate due = LocalDate.now(kyiv).plusDays(30);\n"
                + "long days = ChronoUnit.DAYS.between(LocalDate.now(kyiv), due);"));
        uk.add(LessonBlock.table("Тип\tДля чого",
                Arrays.asList(
                        "Duration\tчасова тривалість у seconds/nanos",
                        "Period\tкалендарна різниця years/months/days",
                        "ZoneOffset\tфіксоване зміщення, наприклад +03:00",
                        "ZoneId\tрегіон із історичними DST-правилами",
                        "Clock\tін'єкція часу для відтворюваних тестів")));
        uk.add(LessonBlock.warning("Не зберігайте майбутній локальний розклад лише як Instant, якщо "
                + "важливе правило 'о 09:00 за Києвом': правила zone можуть змінитися. Зберігайте local time + ZoneId."));
    }

    private static void memoryModel(List<LessonBlock> uk) {
        begin(uk, "Java Memory Model описує happens-before. Без такого зв'язку потік може не побачити "
                + "запис іншого потоку, навіть якщо на конкретному запуску код 'працює'.");
        uk.add(LessonBlock.code("final class StopFlag {\n"
                + "    private volatile boolean stopped;\n"
                + "    void stop() { stopped = true; }\n"
                + "    void runLoop() {\n"
                + "        while (!stopped) {\n"
                + "            // робота; volatile read бачить stop()\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "AtomicInteger seq = new AtomicInteger();\n"
                + "int id = seq.incrementAndGet(); // атомарний read-modify-write"));
        uk.add(LessonBlock.table("Дія A\tHappens-before B",
                Arrays.asList(
                        "unlock monitor\tнаступний lock того самого monitor",
                        "write volatile\tнаступний read тієї самої volatile",
                        "Thread.start\tдії нового потоку",
                        "дії потоку\tуспішне повернення join",
                        "запис final у constructor\tкоректне читання після safe publication")));
        uk.add(LessonBlock.warning("volatile не робить count++ атомарним: це read, add, write. Для "
                + "лічильника використовуйте AtomicInteger/LongAdder або synchronized."));
    }

    private static void executors(List<LessonBlock> uk) {
        begin(uk, "Executor відділяє подання задачі від політики потоків. Потрібно явно визначити "
                + "розмір pool, тип queue, rejection policy та життєвий цикл shutdown.");
        uk.add(LessonBlock.code("ExecutorService pool = Executors.newFixedThreadPool(2);\n"
                + "List<Future<Integer>> futures = new ArrayList<Future<Integer>>();\n"
                + "try {\n"
                + "    for (int i = 1; i <= 3; i++) {\n"
                + "        final int value = i;\n"
                + "        futures.add(pool.submit(() -> value * value));\n"
                + "    }\n"
                + "    for (Future<Integer> f : futures) System.out.println(f.get());\n"
                + "} finally {\n"
                + "    pool.shutdown();\n"
                + "    if (!pool.awaitTermination(5, TimeUnit.SECONDS)) pool.shutdownNow();\n"
                + "}"));
        uk.add(LessonBlock.list(
                "execute(Runnable) передає uncaught exception механізму потоку; submit ховає його у Future.",
                "Future.get блокує і загортає помилку задачі в ExecutionException.",
                "cancel(true) лише просить перервати; задача має реагувати на interrupt.",
                "CachedThreadPool може створити багато потоків; не використовуйте його для неконтрольованого навантаження.",
                "CompletableFuture без executor використовує common ForkJoinPool."));
        uk.add(LessonBlock.warning("Завжди закривайте ExecutorService. Інакше non-daemon worker threads "
                + "можуть утримувати процес і ресурси після завершення корисної роботи."));
    }

    private static void capstone(List<LessonBlock> uk) {
        begin(uk, "Підсумковий проєкт повинен мати явні інваріанти та межі: формат вводу, модель помилок, "
                + "ownership ресурсів, потокобезпечність і верхні межі пам'яті.");
        uk.add(LessonBlock.code("interface Repository<K, V> {\n"
                + "    Optional<V> find(K key) throws IOException;\n"
                + "    void save(K key, V value) throws IOException;\n"
                + "}\n"
                + "\n"
                + "final class Service<K, V> {\n"
                + "    private final Repository<K, V> repository;\n"
                + "    Service(Repository<K, V> repository) {\n"
                + "        this.repository = Objects.requireNonNull(repository);\n"
                + "    }\n"
                + "    Optional<V> find(K key) throws IOException {\n"
                + "        return repository.find(Objects.requireNonNull(key));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.list(
                "Опишіть контракти публічних методів до реалізації.",
                "Відокремте domain logic від файлів, мережі та console I/O.",
                "Додайте deterministic Clock/Random через constructor injection.",
                "Тестуйте corrupted input, duplicate keys і partial write.",
                "Виміряйте складність та не завантажуйте необмежений файл цілком у пам'ять."));
        uk.add(LessonBlock.note("Критерій готовності: проєкт можна запустити з нуля, помилки мають "
                + "зрозумілі повідомлення, ресурси закриваються, а ключові контракти перевірені тестами."));
    }

    private static void functionalInterfaces(List<LessonBlock> uk) {
        begin(uk, "SAM-інтерфейс може успадковувати методи, якщо після врахування override лишається один "
                + "абстрактний контракт. Методи Object не рахуються як функціональний метод.");
        uk.add(LessonBlock.code("Predicate<String> nonEmpty = s -> s != null && !s.isEmpty();\n"
                + "Predicate<String> shortText = s -> s.length() <= 10;\n"
                + "Predicate<String> valid = nonEmpty.and(shortText);\n"
                + "\n"
                + "Function<String, String> trim = String::trim;\n"
                + "Function<String, Integer> length = String::length;\n"
                + "Function<String, Integer> normalizedLength = trim.andThen(length);\n"
                + "System.out.println(valid.test(\" Java \"));\n"
                + "System.out.println(normalizedLength.apply(\" Java \"));"));
        uk.add(LessonBlock.table("Спеціалізація\tНавіщо",
                Arrays.asList(
                        "IntPredicate\tбез boxing int -> Integer",
                        "ToIntFunction<T>\tрезультат primitive int",
                        "ObjIntConsumer<T>\tT + primitive int",
                        "IntUnaryOperator\tint -> int без allocations",
                        "BiFunction<T,U,R>\tдва аргументи різних типів")));
        uk.add(LessonBlock.warning("Перевантаження методів різними functional interfaces може зробити "
                + "lambda неоднозначною. Допоможе явний cast або унікальна назва методу."));
    }

    private static void methodReferences(List<LessonBlock> uk) {
        begin(uk, "Reference виду Type::instanceMethod має прихований перший параметр-receiver. Тому "
                + "String::compareToIgnoreCase сумісний із Comparator<String>, який має два аргументи.");
        uk.add(LessonBlock.code("Comparator<String> cmp = String::compareToIgnoreCase;\n"
                + "// еквівалент: (left, right) -> left.compareToIgnoreCase(right)\n"
                + "\n"
                + "Supplier<List<String>> empty = ArrayList<String>::new;\n"
                + "IntFunction<String[]> arrayFactory = String[]::new;\n"
                + "String[] names = Stream.of(\"A\", \"B\").toArray(arrayFactory);"));
        uk.add(LessonBlock.list(
                "InterfaceName.super.method() явно обирає default method батьківського інтерфейсу.",
                "Метод класу завжди має пріоритет над default method інтерфейсу.",
                "Два непов'язані однакові default methods треба розв'язати власним override.",
                "Static method інтерфейсу не успадковується реалізацією; виклик лише Interface.method()."));
        uk.add(LessonBlock.note("Method reference обирайте, коли він коротший і не приховує важливу "
                + "адаптацію аргументів. Звичайна lambda часто читабельніша для перевірок і кількох кроків."));
    }

    private static void optional(List<LessonBlock> uk) {
        begin(uk, "Optional призначений насамперед для return type. Це value-based class: не "
                + "синхронізуйтеся на Optional і не покладайтеся на identity.");
        uk.add(LessonBlock.code("String label = users.stream()\n"
                + "        .filter(u -> u.getId() == wantedId)\n"
                + "        .map(User::getName)       // null перетвориться на empty\n"
                + "        .filter(s -> !s.trim().isEmpty())\n"
                + "        .map(String::trim)\n"
                + "        .orElseGet(() -> \"user-\" + wantedId);\n"
                + "\n"
                + "User user = findUser(id).orElseThrow(\n"
                + "        () -> new NoSuchElementException(\"user \" + id));"));
        uk.add(LessonBlock.table("Метод\tНюанс",
                Arrays.asList(
                        "of(value)\tкидає NPE для null",
                        "ofNullable(value)\tnull -> empty",
                        "map(f)\tnull result -> empty",
                        "flatMap(f)\tf має повернути Optional, не null",
                        "orElse(x)\tx обчислюється завжди",
                        "orElseGet(s)\tsupplier лише для empty")));
        uk.add(LessonBlock.warning("Не використовуйте Optional як поле DTO/entity, параметр методу або "
                + "елемент кожної колекції без чіткої причини. Це ускладнює серіалізацію та API."));
    }

    private static void regex(List<LessonBlock> uk) {
        begin(uk, "Pattern компілюється і є immutable/thread-safe; Matcher містить mutable state і не "
                + "thread-safe. Для повторюваного шаблону кешуйте Pattern, а Matcher створюйте на виклик.");
        uk.add(LessonBlock.code("private static final Pattern PAIR = Pattern.compile(\n"
                + "        \"(?<key>[A-Za-z][A-Za-z0-9_]*)\\\\s*=\\\\s*(?<value>[^,]+)\");\n"
                + "Matcher matcher = PAIR.matcher(\"host=localhost, port=8080\");\n"
                + "while (matcher.find()) {\n"
                + "    System.out.println(matcher.group(\"key\") + \" -> \"\n"
                + "            + matcher.group(\"value\").trim());\n"
                + "}"));
        uk.add(LessonBlock.table("Метод\tОбласть перевірки",
                Arrays.asList(
                        "matches()\tвесь input",
                        "lookingAt()\tпочаток input",
                        "find()\tнаступний збіг будь-де",
                        "group(0)\tповний збіг",
                        "group(n/name)\tзахоплена група",
                        "quoteReplacement\tекранує $ і \\ у replacement")));
        uk.add(LessonBlock.warning("Уникайте вкладених неоднозначних квантифікаторів на недовіреному "
                + "вводі, наприклад (a+)+: catastrophic backtracking може зайняти надто багато CPU."));
    }

    private static void reflection(List<LessonBlock> uk) {
        begin(uk, "getMethods/getFields бачать public успадковані члени; getDeclared* бачить усі члени "
                + "лише поточного класу. Generic-типи читаються через Type, не завжди через Class.");
        uk.add(LessonBlock.code("for (Field field : Config.class.getDeclaredFields()) {\n"
                + "    if (!field.isAnnotationPresent(Setting.class)) continue;\n"
                + "    if (Modifier.isStatic(field.getModifiers())) continue;\n"
                + "    field.setAccessible(true);\n"
                + "    Object value = field.get(config);\n"
                + "    System.out.println(field.getName() + \"=\" + value);\n"
                + "}\n"
                + "Method method = Service.class.getDeclaredMethod(\"run\", String.class);\n"
                + "try { method.invoke(service, \"job\"); }\n"
                + "catch (InvocationTargetException e) { throw e.getCause(); }"));
        uk.add(LessonBlock.list(
                "RetentionPolicy.RUNTIME потрібен для читання annotation через reflection.",
                "@Inherited працює лише для annotation на класах, не на методах/полях.",
                "getAnnotation враховує @Inherited; getDeclaredAnnotation — ні.",
                "Method.invoke загортає виняток цільового методу в InvocationTargetException.",
                "setAccessible може порушувати інкапсуляцію і залежить від середовища виконання."));
        uk.add(LessonBlock.warning("Reflection переносить частину помилок із compile time у runtime. "
                + "Перевіряйте сигнатури на старті й кешуйте валідовані Field/Method, якщо шлях гарячий."));
    }

    private static void serialization(List<LessonBlock> uk) {
        begin(uk, "Java serialization записує граф об'єктів із identity та циклами. serialVersionUID "
                + "контролює сумісність версій класу; transient і static поля не входять у звичайний стан.");
        uk.add(LessonBlock.code("final class Session implements Serializable {\n"
                + "    private static final long serialVersionUID = 1L;\n"
                + "    private final String user;\n"
                + "    private transient String token;\n"
                + "    Session(String user, String token) { this.user = user; this.token = token; }\n"
                + "    private void readObject(ObjectInputStream in)\n"
                + "            throws IOException, ClassNotFoundException {\n"
                + "        in.defaultReadObject();\n"
                + "        if (user == null || user.isEmpty())\n"
                + "            throw new InvalidObjectException(\"empty user\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.list(
                "Конструктор першого non-Serializable superclass викликається при deserialization.",
                "readResolve може повернути канонічний об'єкт, наприклад singleton.",
                "writeReplace може підмінити представлення перед записом.",
                "serialPersistentFields дає явний контроль serialized form.",
                "Зміна non-transient полів потребує продуманої versioning policy."));
        uk.add(LessonBlock.warning("Не десеріалізуйте недовірені байти через ObjectInputStream: у JDK 8 "
                + "це може активувати небезпечний object graph. Для зовнішніх даних оберіть явний формат і валідацію."));
    }

    private static void classpath(List<LessonBlock> uk) {
        begin(uk, "Classpath — впорядкований список коренів пакетів і JAR. Для com.acme.Main class file "
                + "має лежати як com/acme/Main.class відносно одного з цих коренів.");
        uk.add(LessonBlock.code("# Компіляція у каталог out\n"
                + "javac -source 8 -target 8 -d out src/com/acme/Main.java\n"
                + "\n"
                + "# Запуск: ім'я класу, а не шлях до .class\n"
                + "java -cp out com.acme.Main\n"
                + "\n"
                + "# JAR із Main-Class у MANIFEST.MF\n"
                + "jar cfm app.jar MANIFEST.MF -C out .\n"
                + "java -jar app.jar"));
        uk.add(LessonBlock.table("Manifest attribute\tЗначення",
                Arrays.asList(
                        "Main-Class\tповне binary name без .class",
                        "Class-Path\tspace-separated відносні URL залежностей",
                        "Manifest-Version\tзазвичай 1.0",
                        "Sealed\tобмеження package одним JAR")));
        uk.add(LessonBlock.warning("Порядок classpath важливий: перший знайдений клас перемагає. Дві "
                + "версії одного класу можуть дати NoSuchMethodError, хоча компіляція іншою версією пройшла."));
    }

    private static void diagnostics(List<LessonBlock> uk) {
        begin(uk, "Stack trace читайте від верхнього exception до першого кадру власного коду, а для cause — "
                + "від найглибшого 'Caused by'. '... N more' означає спільний хвіст кадрів.");
        uk.add(LessonBlock.code("Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {\n"
                + "    System.err.println(\"Uncaught in \" + thread.getName());\n"
                + "    error.printStackTrace(System.err);\n"
                + "});\n"
                + "\n"
                + "Runtime runtime = Runtime.getRuntime();\n"
                + "long used = runtime.totalMemory() - runtime.freeMemory();\n"
                + "System.out.println(\"heap used MiB=\" + used / 1024 / 1024);"));
        uk.add(LessonBlock.table("Симптом\tПерша перевірка",
                Arrays.asList(
                        "OutOfMemoryError: Java heap space\theap dump, утримувачі великих графів",
                        "StackOverflowError\tнескінченна/глибока рекурсія",
                        "NoClassDefFoundError\tclasspath або помилка static initialization",
                        "NoSuchMethodError\tнесумісні compile/runtime версії JAR",
                        "deadlock\tthread dump і цикли очікування monitor")));
        uk.add(LessonBlock.note("Діагностика має бути відтворюваною: запишіть версію runtime, аргументи, "
                + "точний stack trace, thread names, розмір вводу і мінімальний сценарій повторення."));
    }
}
