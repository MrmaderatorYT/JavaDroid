package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Detailed JDK 8 course chapters. The focus is intentionally narrow and deep:
 * contracts, behavior, edge cases, and practice.
 */
final class Jdk8DeepDiveChapters {

    private Jdk8DeepDiveChapters() {
    }

    static void add(Course s) {
        addOrientation(s);
        addCollectionsCore(s);
        addSetsAndMaps(s);
        addAlgorithmsAndStreams(s);
        addPracticeMarathon(s);
    }

    // ── Orientation ────────────────────────────────────────────────────────

    private static void addOrientation(Course s) {
        Chapter ch = new Chapter(
                "JDK 8: як вчитися глибоко",
                "JDK 8: how to study deeply");
        ch.add(materialJdk8ContractMindset());
        s.add(ch);
    }

    private static Lesson materialJdk8ContractMindset() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("JDK 8 як набір контрактів"));
        uk.add(LessonBlock.paragraph(
                "JDK 8 треба вчити не як список класів, а як систему контрактів. "
                + "Контракт відповідає на питання: що метод обіцяє, що він НЕ обіцяє, "
                + "які помилки можливі, які операції можуть бути повільними, і що станеться "
                + "при зміні колекції під час обходу."));
        uk.add(LessonBlock.paragraph(
                "Наприклад, List обіцяє порядок елементів і доступ за індексом. Але List "
                + "не обіцяє, що get(i) завжди швидкий: в ArrayList це майже O(1), а в "
                + "LinkedList пошук за індексом проходить вузли один за одним. Саме тому "
                + "важливо читати не тільки інтерфейс, а й реалізацію."));
        uk.add(LessonBlock.table(
                "Рівень\tЩо вивчати\tПитання для самоперевірки",
                Arrays.asList(
                        "Інтерфейс\tList, Set, Map, Iterator\tЯку поведінку обіцяє контракт?",
                        "Реалізація\tArrayList, LinkedList, HashMap\tЯка структура даних всередині?",
                        "Алгоритм\tsort, binarySearch, removeIf\tЯка складність і передумови?",
                        "Помилки\tConcurrentModificationException, ClassCastException\tКоли саме це падає?",
                        "Практика\tмаленькі задачі й переписування коду\tЧи можу пояснити приклад без підглядання?")));
        uk.add(LessonBlock.note(
                "У цьому курсі весь код орієнтований на JDK 8. Якщо ви бачите новішу Java "
                + "в інтернеті: var, List.of, records, switch ->, text blocks, sealed classes, "
                + "java.net.http.HttpClient — це не для вашого компілятора."));
        uk.add(LessonBlock.heading("Як тренуватися"));
        uk.add(LessonBlock.list(
                "1. Прочитайте контракт класу або інтерфейсу своїми словами.",
                "2. Перепишіть приклад вручну, не копіюйте.",
                "3. Додайте 2-3 System.out.println, щоб побачити стан колекції після кожної операції.",
                "4. Навмисно зламайте приклад: неправильний індекс, null, видалення під час циклу.",
                "5. Запишіть висновок: коли цей API зручний, а коли небезпечний або повільний."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("JDK 8 as a set of contracts"));
        en.add(LessonBlock.paragraph(
                "Study JDK 8 not as a list of classes, but as a system of contracts. "
                + "A contract answers: what a method promises, what it does NOT promise, "
                + "which errors are possible, which operations may be slow, and what happens "
                + "when a collection changes during iteration."));
        en.add(LessonBlock.paragraph(
                "For example, List promises element order and indexed access. But List does "
                + "not promise that get(i) is always fast: in ArrayList it is close to O(1), "
                + "while LinkedList walks nodes one by one. That is why you must study both "
                + "the interface and the implementation."));
        en.add(LessonBlock.table(
                "Level\tWhat to study\tSelf-check question",
                Arrays.asList(
                        "Interface\tList, Set, Map, Iterator\tWhich behavior does the contract promise?",
                        "Implementation\tArrayList, LinkedList, HashMap\tWhich data structure is inside?",
                        "Algorithm\tsort, binarySearch, removeIf\tWhat is the complexity and precondition?",
                        "Errors\tConcurrentModificationException, ClassCastException\tWhen exactly does it fail?",
                        "Practice\tsmall tasks and rewriting code\tCan I explain the example without looking?")));
        en.add(LessonBlock.note(
                "All code in this course targets JDK 8. If you see newer Java online: var, "
                + "List.of, records, switch ->, text blocks, sealed classes, "
                + "java.net.http.HttpClient — that is not for your compiler."));
        en.add(LessonBlock.heading("How to practice"));
        en.add(LessonBlock.list(
                "1. Read the class or interface contract in your own words.",
                "2. Type the example by hand; do not copy it.",
                "3. Add 2-3 System.out.println calls to observe collection state after each operation.",
                "4. Intentionally break the example: wrong index, null, removal during a loop.",
                "5. Write a conclusion: when this API is useful, dangerous, or slow."));

        return new Lesson("jdk8.1", "JDK 8: контракти і практика", "JDK 8: contracts and practice", uk, en);
    }

    // ── Collections Core ───────────────────────────────────────────────────

    private static void addCollectionsCore(Course s) {
        Chapter ch = new Chapter(
                "Collections Framework: List, Iterator, Enumeration",
                "Collections Framework: List, Iterator, Enumeration");
        ch.add(materialCollectionHierarchy());
        ch.add(materialListContract());
        ch.add(materialArrayListDeep());
        ch.add(materialLinkedListVectorStack());
        ch.add(materialEnumerationIteratorListIterator());
        s.add(ch);
    }

    private static Lesson materialCollectionHierarchy() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Ієрархія Collections Framework"));
        uk.add(LessonBlock.paragraph(
                "Collections Framework у JDK 8 — це набір інтерфейсів, реалізацій і алгоритмів "
                + "для роботи з групами об'єктів. Головна ідея: писати код проти інтерфейсу "
                + "(List, Set, Map), а конкретну реалізацію вибирати під задачу."));
        uk.add(LessonBlock.table(
                "Тип\tЩо означає\tКлючові реалізації",
                Arrays.asList(
                        "Iterable\tможна обходити у for-each\tмайже всі колекції",
                        "Collection\tгрупа елементів без ключів\tList, Set, Queue",
                        "List\tпорядок + індекси + дублікати\tArrayList, LinkedList, Vector",
                        "Set\tунікальні елементи\tHashSet, LinkedHashSet, TreeSet",
                        "Queue/Deque\tчерга або двостороння черга\tLinkedList, ArrayDeque, PriorityQueue",
                        "Map\tключ -> значення, не extends Collection\tHashMap, LinkedHashMap, TreeMap, Hashtable")));
        uk.add(LessonBlock.code(
                "List<String> names = new ArrayList<String>();\n"
                + "names.add(\"Ira\");\n"
                + "names.add(\"Oleh\");\n"
                + "names.add(\"Ira\");       // List дозволяє дублікати\n"
                + "\n"
                + "Set<String> unique = new HashSet<String>();\n"
                + "unique.add(\"Ira\");\n"
                + "unique.add(\"Ira\");      // Set залишить один елемент\n"
                + "\n"
                + "Map<String, Integer> ages = new HashMap<String, Integer>();\n"
                + "ages.put(\"Ira\", 20);    // ключ Ira -> значення 20"));
        uk.add(LessonBlock.warning(
                "Map не наслідує Collection. Це окрема гілка API: у Map немає add(), "
                + "зате є put(key, value), get(key), containsKey(key), entrySet()."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть List з 5 іменами, де одне ім'я повторюється.",
                "Перекладіть цей List у HashSet і подивіться, що сталося з дублем.",
                "Створіть Map ім'я -> кількість появ у списку.",
                "Поясніть, чому List і Set схожі, але Map має інший контракт."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Collections Framework hierarchy"));
        en.add(LessonBlock.paragraph(
                "The JDK 8 Collections Framework is a set of interfaces, implementations, "
                + "and algorithms for groups of objects. Main idea: code against an interface "
                + "(List, Set, Map), then choose an implementation for the task."));
        en.add(LessonBlock.table(
                "Type\tMeaning\tKey implementations",
                Arrays.asList(
                        "Iterable\tcan be traversed in for-each\talmost all collections",
                        "Collection\tgroup of elements without keys\tList, Set, Queue",
                        "List\torder + indexes + duplicates\tArrayList, LinkedList, Vector",
                        "Set\tunique elements\tHashSet, LinkedHashSet, TreeSet",
                        "Queue/Deque\tqueue or double-ended queue\tLinkedList, ArrayDeque, PriorityQueue",
                        "Map\tkey -> value, does not extend Collection\tHashMap, LinkedHashMap, TreeMap, Hashtable")));
        en.add(LessonBlock.code(
                "List<String> names = new ArrayList<String>();\n"
                + "names.add(\"Ira\");\n"
                + "names.add(\"Oleh\");\n"
                + "names.add(\"Ira\");       // List allows duplicates\n"
                + "\n"
                + "Set<String> unique = new HashSet<String>();\n"
                + "unique.add(\"Ira\");\n"
                + "unique.add(\"Ira\");      // Set keeps one element\n"
                + "\n"
                + "Map<String, Integer> ages = new HashMap<String, Integer>();\n"
                + "ages.put(\"Ira\", 20);    // key Ira -> value 20"));
        en.add(LessonBlock.warning(
                "Map does not extend Collection. It is a separate API branch: Map has no add(), "
                + "but has put(key, value), get(key), containsKey(key), entrySet()."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create a List with 5 names, where one name repeats.",
                "Convert this List into a HashSet and observe what happened to the duplicate.",
                "Create a Map name -> number of occurrences in the list.",
                "Explain why List and Set are similar, but Map has a different contract."));

        return new Lesson("jdk8.collections.1", "Ієрархія колекцій", "Collections hierarchy", uk, en);
    }

    private static Lesson materialListContract() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("List: контракт, індекси, дублікати"));
        uk.add(LessonBlock.paragraph(
                "List — впорядкована колекція. Кожен елемент має позицію: 0, 1, 2... "
                + "List дозволяє дублікати, зазвичай дозволяє null, має методи get(index), "
                + "set(index, value), add(index, value), remove(index)."));
        uk.add(LessonBlock.code(
                "List<String> list = new ArrayList<String>();\n"
                + "list.add(\"A\");            // [A]\n"
                + "list.add(\"B\");            // [A, B]\n"
                + "list.add(\"A\");            // [A, B, A] дублікати дозволені\n"
                + "list.add(1, \"X\");         // [A, X, B, A]\n"
                + "\n"
                + "String first = list.get(0); // A\n"
                + "list.set(2, \"C\");         // [A, X, C, A]\n"
                + "String removed = list.remove(1); // X, тепер [A, C, A]"));
        uk.add(LessonBlock.table(
                "Операція\tЩо робить\tТипова помилка",
                Arrays.asList(
                        "get(i)\tчитає елемент за індексом\tIndexOutOfBoundsException",
                        "set(i, x)\tзамінює елемент\tіндекс має вже існувати",
                        "add(i, x)\tвставляє перед позицією i\ti може бути size(), але не більше",
                        "remove(i)\tвидаляє за індексом\tпісля видалення індекси зсуваються",
                        "contains(x)\tшукає через equals()\tбез equals у власному класі результат дивний")));
        uk.add(LessonBlock.warning(
                "Arrays.asList повертає фіксований за розміром список, пов'язаний з масивом. "
                + "set() працює, а add()/remove() кинуть UnsupportedOperationException."));
        uk.add(LessonBlock.code(
                "List<String> fixed = Arrays.asList(\"A\", \"B\", \"C\");\n"
                + "fixed.set(0, \"X\");        // OK: [X, B, C]\n"
                + "// fixed.add(\"D\");        // UnsupportedOperationException\n"
                + "\n"
                + "List<String> mutable = new ArrayList<String>(fixed);\n"
                + "mutable.add(\"D\");          // OK"));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть метод swap(List<String> list, int i, int j).",
                "Напишіть метод removeEverySecond(List<String> list), використовуючи Iterator.",
                "Створіть власний клас Student і перевірте contains до та після equals/hashCode.",
                "Перевірте різницю між list.remove(1) і list.remove(Integer.valueOf(1)) для List<Integer>."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("List: contract, indexes, duplicates"));
        en.add(LessonBlock.paragraph(
                "List is an ordered collection. Each element has a position: 0, 1, 2... "
                + "List allows duplicates, usually allows null, and has get(index), set(index, value), "
                + "add(index, value), remove(index)."));
        en.add(LessonBlock.code(
                "List<String> list = new ArrayList<String>();\n"
                + "list.add(\"A\");            // [A]\n"
                + "list.add(\"B\");            // [A, B]\n"
                + "list.add(\"A\");            // [A, B, A] duplicates allowed\n"
                + "list.add(1, \"X\");         // [A, X, B, A]\n"
                + "\n"
                + "String first = list.get(0); // A\n"
                + "list.set(2, \"C\");         // [A, X, C, A]\n"
                + "String removed = list.remove(1); // X, now [A, C, A]"));
        en.add(LessonBlock.table(
                "Operation\tWhat it does\tCommon mistake",
                Arrays.asList(
                        "get(i)\treads element by index\tIndexOutOfBoundsException",
                        "set(i, x)\treplaces an element\tindex must already exist",
                        "add(i, x)\tinserts before position i\ti may be size(), but not bigger",
                        "remove(i)\tremoves by index\tindexes shift after removal",
                        "contains(x)\tsearches using equals()\tcustom classes need equals")));
        en.add(LessonBlock.warning(
                "Arrays.asList returns a fixed-size list backed by the array. set() works, "
                + "but add()/remove() throw UnsupportedOperationException."));
        en.add(LessonBlock.code(
                "List<String> fixed = Arrays.asList(\"A\", \"B\", \"C\");\n"
                + "fixed.set(0, \"X\");        // OK: [X, B, C]\n"
                + "// fixed.add(\"D\");        // UnsupportedOperationException\n"
                + "\n"
                + "List<String> mutable = new ArrayList<String>(fixed);\n"
                + "mutable.add(\"D\");          // OK"));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write swap(List<String> list, int i, int j).",
                "Write removeEverySecond(List<String> list) using Iterator.",
                "Create a Student class and test contains before and after equals/hashCode.",
                "Check the difference between list.remove(1) and list.remove(Integer.valueOf(1)) for List<Integer>."));

        return new Lesson("jdk8.collections.2", "List глибоко", "List deep dive", uk, en);
    }

    private static Lesson materialArrayListDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("ArrayList: масив всередині"));
        uk.add(LessonBlock.paragraph(
                "ArrayList зберігає елементи у внутрішньому Object[]. Поле size показує, "
                + "скільки елементів реально лежить у списку, а capacity — скільки місця "
                + "виділено у внутрішньому масиві. Capacity не видно напряму через API, але "
                + "саме вона пояснює швидкість add у кінець."));
        uk.add(LessonBlock.table(
                "Операція ArrayList\tСкладність\tПояснення",
                Arrays.asList(
                        "get(i)\tO(1)\tпрямий доступ до elementData[i]",
                        "set(i, x)\tO(1)\tзамінює посилання у масиві",
                        "add(x) в кінець\tамортизовано O(1)\tіноді масив розширюється",
                        "add(i, x)\tO(n)\tтреба зсунути елементи праворуч",
                        "remove(i)\tO(n)\tтреба зсунути елементи ліворуч",
                        "contains(x)\tO(n)\tпослідовний пошук через equals")));
        uk.add(LessonBlock.code(
                "List<Integer> numbers = new ArrayList<Integer>();\n"
                + "for (int i = 0; i < 5; i++) {\n"
                + "    numbers.add(i);          // швидко додаємо в кінець\n"
                + "}\n"
                + "numbers.add(0, 100);         // дорого: всі елементи зсуваються\n"
                + "numbers.remove(2);           // дорого: хвіст зсувається назад"));
        uk.add(LessonBlock.note(
                "У OpenJDK 8 ArrayList починає з порожнього масиву, а при першому додаванні "
                + "зазвичай отримує capacity 10. При розширенні capacity росте приблизно у 1.5 раза. "
                + "Це деталь реалізації, але вона допомагає зрозуміти продуктивність."));
        uk.add(LessonBlock.heading("Коли обирати ArrayList"));
        uk.add(LessonBlock.list(
                "Часто читаєте за індексом: list.get(i).",
                "Часто додаєте в кінець списку.",
                "Рідко вставляєте або видаляєте з початку/середини.",
                "Потрібна компактна структура з хорошою локальністю пам'яті.",
                "Початківцю майже завжди варто починати саме з ArrayList, доки немає причини обрати інше."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть ArrayList на 1000 чисел і виміряйте час додавання в кінець.",
                "Повторіть, але додавайте кожне число в позицію 0.",
                "Напишіть метод findMax(List<Integer> list), не використовуючи Collections.max.",
                "Напишіть метод compact(List<String> list), який видаляє null та порожні рядки."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("ArrayList: array inside"));
        en.add(LessonBlock.paragraph(
                "ArrayList stores elements in an internal Object[]. The size field tells how "
                + "many elements are actually in the list, while capacity tells how much space "
                + "the internal array has. Capacity is not visible directly through the API, "
                + "but it explains why appending is fast."));
        en.add(LessonBlock.table(
                "ArrayList operation\tComplexity\tExplanation",
                Arrays.asList(
                        "get(i)\tO(1)\tdirect access to elementData[i]",
                        "set(i, x)\tO(1)\treplaces a reference in the array",
                        "add(x) at end\tamortized O(1)\tsometimes the array grows",
                        "add(i, x)\tO(n)\telements shift right",
                        "remove(i)\tO(n)\telements shift left",
                        "contains(x)\tO(n)\tlinear search using equals")));
        en.add(LessonBlock.code(
                "List<Integer> numbers = new ArrayList<Integer>();\n"
                + "for (int i = 0; i < 5; i++) {\n"
                + "    numbers.add(i);          // fast append\n"
                + "}\n"
                + "numbers.add(0, 100);         // expensive: all elements shift\n"
                + "numbers.remove(2);           // expensive: tail shifts back"));
        en.add(LessonBlock.note(
                "In OpenJDK 8, ArrayList starts with an empty array and usually gets capacity "
                + "10 on the first add. When it grows, capacity increases by about 1.5x. "
                + "This is an implementation detail, but it helps explain performance."));
        en.add(LessonBlock.heading("When to choose ArrayList"));
        en.add(LessonBlock.list(
                "You often read by index: list.get(i).",
                "You often append to the end.",
                "You rarely insert or remove from the beginning/middle.",
                "You need a compact structure with good memory locality.",
                "Beginners should usually start with ArrayList until there is a reason to choose something else."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create an ArrayList of 1000 numbers and measure append time.",
                "Repeat, but insert every number at position 0.",
                "Write findMax(List<Integer> list) without Collections.max.",
                "Write compact(List<String> list), removing null and empty strings."));

        return new Lesson("jdk8.collections.3", "ArrayList детально", "ArrayList in detail", uk, en);
    }

    private static Lesson materialLinkedListVectorStack() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("LinkedList, Vector, Stack"));
        uk.add(LessonBlock.paragraph(
                "LinkedList у JDK 8 — двозв'язний список: кожен вузол знає попередній і "
                + "наступний вузол. Він реалізує List і Deque. Це означає, що LinkedList "
                + "може бути списком, чергою або стеком, але не означає, що він завжди швидший."));
        uk.add(LessonBlock.table(
                "Клас\tЩо всередині\tКоли використовувати",
                Arrays.asList(
                        "LinkedList\tланцюжок вузлів\tчерга/Deque, часті addFirst/removeFirst",
                        "Vector\tсинхронізований динамічний масив\tlegacy-код, майже не новий код",
                        "Stack\tнащадок Vector\tlegacy stack; краще ArrayDeque",
                        "ArrayDeque\tмасив-кільце\tстек або черга в новому JDK 8 коді")));
        uk.add(LessonBlock.code(
                "Deque<String> deque = new ArrayDeque<String>();\n"
                + "deque.addFirst(\"middle\");\n"
                + "deque.addFirst(\"first\");\n"
                + "deque.addLast(\"last\");\n"
                + "\n"
                + "System.out.println(deque.removeFirst()); // first\n"
                + "System.out.println(deque.removeLast());  // last"));
        uk.add(LessonBlock.warning(
                "Не використовуйте LinkedList тільки тому, що там 'швидке видалення'. "
                + "Щоб видалити елемент за індексом, його спершу треба знайти, а це O(n). "
                + "У реальних задачах ArrayList часто швидший через кеш процесора і менше об'єктів."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Реалізуйте стек через ArrayDeque: push, pop, peek.",
                "Реалізуйте чергу через ArrayDeque: offer, poll, peek.",
                "Створіть LinkedList і спробуйте addFirst/removeFirst.",
                "Поясніть, чому Stack вважається legacy, хоча він досі працює."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("LinkedList, Vector, Stack"));
        en.add(LessonBlock.paragraph(
                "LinkedList in JDK 8 is a doubly-linked list: each node knows previous and "
                + "next nodes. It implements List and Deque. This means LinkedList can be a "
                + "list, queue, or stack, but it does not mean it is always faster."));
        en.add(LessonBlock.table(
                "Class\tInside\tWhen to use",
                Arrays.asList(
                        "LinkedList\tchain of nodes\tqueue/Deque, frequent addFirst/removeFirst",
                        "Vector\tsynchronized dynamic array\tlegacy code, rarely new code",
                        "Stack\tsubclass of Vector\tlegacy stack; prefer ArrayDeque",
                        "ArrayDeque\tring array\tstack or queue in new JDK 8 code")));
        en.add(LessonBlock.code(
                "Deque<String> deque = new ArrayDeque<String>();\n"
                + "deque.addFirst(\"middle\");\n"
                + "deque.addFirst(\"first\");\n"
                + "deque.addLast(\"last\");\n"
                + "\n"
                + "System.out.println(deque.removeFirst()); // first\n"
                + "System.out.println(deque.removeLast());  // last"));
        en.add(LessonBlock.warning(
                "Do not use LinkedList just because you heard 'removal is fast'. To remove "
                + "by index, the node must be found first, which is O(n). In real tasks, "
                + "ArrayList is often faster because of CPU cache and fewer objects."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Implement a stack with ArrayDeque: push, pop, peek.",
                "Implement a queue with ArrayDeque: offer, poll, peek.",
                "Create a LinkedList and try addFirst/removeFirst.",
                "Explain why Stack is considered legacy even though it still works."));

        return new Lesson("jdk8.collections.4", "LinkedList, Vector, Stack", "LinkedList, Vector, Stack", uk, en);
    }

    private static Lesson materialEnumerationIteratorListIterator() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Enumeration, Iterator, ListIterator"));
        uk.add(LessonBlock.paragraph(
                "Enumeration — старий API з Java 1.0. Він використовується у legacy-класах "
                + "Vector, Hashtable, Properties. Iterator — сучасніший базовий спосіб обходу "
                + "колекцій. ListIterator — спеціальний ітератор для List: може рухатися назад, "
                + "додавати, замінювати і показувати індекси."));
        uk.add(LessonBlock.table(
                "API\tМетоди\tЩо вміє",
                Arrays.asList(
                        "Enumeration<E>\thasMoreElements(), nextElement()\tтільки читання вперед",
                        "Iterator<E>\thasNext(), next(), remove()\tчитання вперед + безпечне remove",
                        "ListIterator<E>\thasNext(), next(), hasPrevious(), previous(), add(), set()\tрух у дві сторони + зміни List",
                        "Iterable<E>\titerator()\tдозволяє for-each")));
        uk.add(LessonBlock.code(
                "Vector<String> vector = new Vector<String>();\n"
                + "vector.add(\"A\");\n"
                + "vector.add(\"B\");\n"
                + "\n"
                + "Enumeration<String> e = vector.elements();\n"
                + "while (e.hasMoreElements()) {\n"
                + "    String value = e.nextElement();\n"
                + "    System.out.println(value);\n"
                + "}"));
        uk.add(LessonBlock.code(
                "List<String> list = new ArrayList<String>(Arrays.asList(\"A\", \"B\", \"C\"));\n"
                + "Iterator<String> it = list.iterator();\n"
                + "while (it.hasNext()) {\n"
                + "    String value = it.next();\n"
                + "    if (\"B\".equals(value)) {\n"
                + "        it.remove();       // правильно: видалення через Iterator\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(list);  // [A, C]"));
        uk.add(LessonBlock.warning(
                "Не робіть list.remove(value) всередині for-each по тому самому list. "
                + "Більшість стандартних ітераторів fail-fast і кинуть ConcurrentModificationException. "
                + "Правильний шлях у JDK 8: Iterator.remove() або removeIf(predicate)."));
        uk.add(LessonBlock.code(
                "List<String> letters = new ArrayList<String>(Arrays.asList(\"A\", \"C\"));\n"
                + "ListIterator<String> li = letters.listIterator();\n"
                + "while (li.hasNext()) {\n"
                + "    String value = li.next();\n"
                + "    if (\"A\".equals(value)) {\n"
                + "        li.add(\"B\");      // вставить після A, перед C\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(letters); // [A, B, C]"));
        uk.add(LessonBlock.heading("Курсор ListIterator"));
        uk.add(LessonBlock.paragraph(
                "ListIterator має курсор між елементами, а не 'на елементі'. Після next() "
                + "метод remove() або set() працює з щойно повернутим елементом. Метод add(x) "
                + "вставляє x перед елементом, який повернув би next(), і після елемента, який "
                + "повернув би previous(). Це звучить складно, тому треба тренувати руками."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Обійдіть Vector через Enumeration і перепишіть той самий код через Iterator.",
                "Видаліть усі парні числа з List<Integer> через Iterator.remove().",
                "Через ListIterator вставте \"middle\" між \"left\" і \"right\".",
                "Спробуйте викликати iterator.remove() до next() і зафіксуйте виняток.",
                "Поясніть різницю між fail-fast і потокобезпечністю."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Enumeration, Iterator, ListIterator"));
        en.add(LessonBlock.paragraph(
                "Enumeration is an old Java 1.0 API. It is used in legacy classes like Vector, "
                + "Hashtable, Properties. Iterator is the modern basic traversal API. "
                + "ListIterator is special for List: it can move backward, add, replace, and show indexes."));
        en.add(LessonBlock.table(
                "API\tMethods\tCapability",
                Arrays.asList(
                        "Enumeration<E>\thasMoreElements(), nextElement()\tread-only forward traversal",
                        "Iterator<E>\thasNext(), next(), remove()\tforward traversal + safe remove",
                        "ListIterator<E>\thasNext(), next(), hasPrevious(), previous(), add(), set()\ttwo-way traversal + List changes",
                        "Iterable<E>\titerator()\tenables for-each")));
        en.add(LessonBlock.code(
                "Vector<String> vector = new Vector<String>();\n"
                + "vector.add(\"A\");\n"
                + "vector.add(\"B\");\n"
                + "\n"
                + "Enumeration<String> e = vector.elements();\n"
                + "while (e.hasMoreElements()) {\n"
                + "    String value = e.nextElement();\n"
                + "    System.out.println(value);\n"
                + "}"));
        en.add(LessonBlock.code(
                "List<String> list = new ArrayList<String>(Arrays.asList(\"A\", \"B\", \"C\"));\n"
                + "Iterator<String> it = list.iterator();\n"
                + "while (it.hasNext()) {\n"
                + "    String value = it.next();\n"
                + "    if (\"B\".equals(value)) {\n"
                + "        it.remove();       // correct: remove through Iterator\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(list);  // [A, C]"));
        en.add(LessonBlock.warning(
                "Do not call list.remove(value) inside a for-each over the same list. Most "
                + "standard iterators are fail-fast and will throw ConcurrentModificationException. "
                + "The correct JDK 8 way: Iterator.remove() or removeIf(predicate)."));
        en.add(LessonBlock.code(
                "List<String> letters = new ArrayList<String>(Arrays.asList(\"A\", \"C\"));\n"
                + "ListIterator<String> li = letters.listIterator();\n"
                + "while (li.hasNext()) {\n"
                + "    String value = li.next();\n"
                + "    if (\"A\".equals(value)) {\n"
                + "        li.add(\"B\");      // inserts after A, before C\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(letters); // [A, B, C]"));
        en.add(LessonBlock.heading("ListIterator cursor"));
        en.add(LessonBlock.paragraph(
                "ListIterator has a cursor between elements, not 'on an element'. After next(), "
                + "remove() or set() affects the element just returned. add(x) inserts x before "
                + "the element next() would return and after the element previous() would return. "
                + "It sounds abstract, so practice it by hand."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Traverse Vector with Enumeration, then rewrite the same code with Iterator.",
                "Remove all even numbers from List<Integer> using Iterator.remove().",
                "Use ListIterator to insert \"middle\" between \"left\" and \"right\".",
                "Try calling iterator.remove() before next() and record the exception.",
                "Explain the difference between fail-fast and thread-safety."));

        return new Lesson("jdk8.collections.5", "Enumeration, Iterator, ListIterator", "Enumeration, Iterator, ListIterator", uk, en);
    }

    // ── Sets and Maps ──────────────────────────────────────────────────────

    private static void addSetsAndMaps(Course s) {
        Chapter ch = new Chapter(
                "Set і Map у JDK 8: equals, hashCode, порядок",
                "Set and Map in JDK 8: equals, hashCode, order");
        ch.add(materialSetDeepDive());
        ch.add(materialMapHashMapDeep());
        ch.add(materialLinkedTreeHashtableProperties());
        s.add(ch);
    }

    private static Lesson materialSetDeepDive() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Set: унікальність через equals/hashCode"));
        uk.add(LessonBlock.paragraph(
                "Set — колекція без дублікатів. Але 'дублікат' у Java означає не 'схожий на око', "
                + "а equals() повертає true. Для HashSet також критично важливий hashCode()."));
        uk.add(LessonBlock.table(
                "Реалізація\tПорядок\tОсновна структура",
                Arrays.asList(
                        "HashSet\tне гарантується\tHashMap всередині",
                        "LinkedHashSet\tпорядок вставки\tHashMap + linked list",
                        "TreeSet\tсортування\tTreeMap / червоно-чорне дерево")));
        uk.add(LessonBlock.code(
                "Set<String> hash = new HashSet<String>();\n"
                + "hash.add(\"B\"); hash.add(\"A\"); hash.add(\"B\");\n"
                + "System.out.println(hash); // порядок не обіцяний, дубль зник\n"
                + "\n"
                + "Set<String> linked = new LinkedHashSet<String>();\n"
                + "linked.add(\"B\"); linked.add(\"A\"); linked.add(\"C\");\n"
                + "System.out.println(linked); // [B, A, C]\n"
                + "\n"
                + "Set<String> tree = new TreeSet<String>();\n"
                + "tree.add(\"B\"); tree.add(\"A\"); tree.add(\"C\");\n"
                + "System.out.println(tree); // [A, B, C]"));
        uk.add(LessonBlock.warning(
                "Якщо об'єкт змінюється після додавання в HashSet так, що змінюється hashCode, "
                + "елемент може 'загубитися': він є всередині, але contains/remove більше не знаходять його."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть клас Student(id, name). Додайте двох студентів з однаковим id у HashSet.",
                "Спочатку не перевизначайте equals/hashCode і подивіться розмір Set.",
                "Потім перевизначте equals/hashCode тільки за id і повторіть.",
                "Перевірте HashSet, LinkedHashSet і TreeSet на одному наборі рядків.",
                "Поясніть, чому TreeSet потребує Comparable або Comparator."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Set: uniqueness through equals/hashCode"));
        en.add(LessonBlock.paragraph(
                "Set is a collection without duplicates. But a 'duplicate' in Java does not "
                + "mean 'looks similar'; it means equals() returns true. For HashSet, hashCode() "
                + "is critical as well."));
        en.add(LessonBlock.table(
                "Implementation\tOrder\tMain structure",
                Arrays.asList(
                        "HashSet\tnot guaranteed\tHashMap inside",
                        "LinkedHashSet\tinsertion order\tHashMap + linked list",
                        "TreeSet\tsorted\tTreeMap / red-black tree")));
        en.add(LessonBlock.code(
                "Set<String> hash = new HashSet<String>();\n"
                + "hash.add(\"B\"); hash.add(\"A\"); hash.add(\"B\");\n"
                + "System.out.println(hash); // order not promised, duplicate removed\n"
                + "\n"
                + "Set<String> linked = new LinkedHashSet<String>();\n"
                + "linked.add(\"B\"); linked.add(\"A\"); linked.add(\"C\");\n"
                + "System.out.println(linked); // [B, A, C]\n"
                + "\n"
                + "Set<String> tree = new TreeSet<String>();\n"
                + "tree.add(\"B\"); tree.add(\"A\"); tree.add(\"C\");\n"
                + "System.out.println(tree); // [A, B, C]"));
        en.add(LessonBlock.warning(
                "If an object changes after being added to HashSet so that hashCode changes, "
                + "the element may become 'lost': it is inside, but contains/remove no longer find it."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create Student(id, name). Add two students with the same id to HashSet.",
                "First do not override equals/hashCode and observe Set size.",
                "Then override equals/hashCode only by id and repeat.",
                "Compare HashSet, LinkedHashSet, and TreeSet on the same strings.",
                "Explain why TreeSet needs Comparable or Comparator."));

        return new Lesson("jdk8.setmap.1", "Set детально", "Set deep dive", uk, en);
    }

    private static Lesson materialMapHashMapDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("HashMap: ключі, buckets, hash"));
        uk.add(LessonBlock.paragraph(
                "HashMap зберігає пари key -> value. Ключ має бути стабільним: equals/hashCode "
                + "не повинні змінюватися, поки ключ лежить у Map. У JDK 8 HashMap має масив "
                + "bucket-ів; кожен bucket містить вузли з однаковою або схожою hash-позицією."));
        uk.add(LessonBlock.table(
                "Метод\tЩо робить\tПовертає",
                Arrays.asList(
                        "put(k, v)\tдодає або замінює значення\tпопереднє значення або null",
                        "get(k)\tшукає за ключем\tзначення або null",
                        "containsKey(k)\tперевіряє наявність ключа\tboolean",
                        "containsValue(v)\tшукає значення повним обходом\tboolean, O(n)",
                        "remove(k)\tвидаляє пару\tстаре значення або null",
                        "entrySet()\tнабір пар Map.Entry\tзручний для обходу")));
        uk.add(LessonBlock.code(
                "Map<String, Integer> count = new HashMap<String, Integer>();\n"
                + "String[] words = {\"java\", \"list\", \"java\", \"map\"};\n"
                + "\n"
                + "for (String word : words) {\n"
                + "    Integer old = count.get(word);\n"
                + "    if (old == null) {\n"
                + "        count.put(word, 1);\n"
                + "    } else {\n"
                + "        count.put(word, old + 1);\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(count); // {java=2, list=1, map=1}"));
        uk.add(LessonBlock.code(
                "// JDK 8 має зручний merge\n"
                + "Map<String, Integer> count2 = new HashMap<String, Integer>();\n"
                + "for (String word : words) {\n"
                + "    count2.merge(word, 1, new java.util.function.BiFunction<Integer, Integer, Integer>() {\n"
                + "        public Integer apply(Integer a, Integer b) { return a + b; }\n"
                + "    });\n"
                + "}\n"
                + "// Або коротко через lambda: count2.merge(word, 1, (a, b) -> a + b);"));
        uk.add(LessonBlock.note(
                "У JDK 8 при великій кількості колізій bucket може перетворитися з linked list "
                + "на tree bin. Це захищає від дуже поганого O(n), але нормальний equals/hashCode "
                + "все одно обов'язковий."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Порахуйте частоти слів у реченні через HashMap.",
                "Виведіть Map через entrySet(), keySet() і values().",
                "Перевірте різницю між get(k) == null і containsKey(k), якщо значення null.",
                "Створіть клас User як ключ і перевірте поведінку без equals/hashCode."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("HashMap: keys, buckets, hash"));
        en.add(LessonBlock.paragraph(
                "HashMap stores key -> value pairs. A key must be stable: equals/hashCode "
                + "must not change while the key is inside the Map. In JDK 8, HashMap has an "
                + "array of buckets; each bucket holds nodes with the same or similar hash position."));
        en.add(LessonBlock.table(
                "Method\tWhat it does\tReturns",
                Arrays.asList(
                        "put(k, v)\tadds or replaces value\tprevious value or null",
                        "get(k)\tsearches by key\tvalue or null",
                        "containsKey(k)\tchecks key presence\tboolean",
                        "containsValue(v)\tfull scan for value\tboolean, O(n)",
                        "remove(k)\tremoves pair\told value or null",
                        "entrySet()\tset of Map.Entry pairs\tconvenient for traversal")));
        en.add(LessonBlock.code(
                "Map<String, Integer> count = new HashMap<String, Integer>();\n"
                + "String[] words = {\"java\", \"list\", \"java\", \"map\"};\n"
                + "\n"
                + "for (String word : words) {\n"
                + "    Integer old = count.get(word);\n"
                + "    if (old == null) {\n"
                + "        count.put(word, 1);\n"
                + "    } else {\n"
                + "        count.put(word, old + 1);\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(count); // {java=2, list=1, map=1}"));
        en.add(LessonBlock.code(
                "// JDK 8 has convenient merge\n"
                + "Map<String, Integer> count2 = new HashMap<String, Integer>();\n"
                + "for (String word : words) {\n"
                + "    count2.merge(word, 1, new java.util.function.BiFunction<Integer, Integer, Integer>() {\n"
                + "        public Integer apply(Integer a, Integer b) { return a + b; }\n"
                + "    });\n"
                + "}\n"
                + "// Or short lambda: count2.merge(word, 1, (a, b) -> a + b);"));
        en.add(LessonBlock.note(
                "In JDK 8, when a bucket has many collisions it can turn from a linked list "
                + "into a tree bin. This protects from very bad O(n), but proper equals/hashCode "
                + "is still mandatory."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Count word frequencies in a sentence using HashMap.",
                "Print a Map through entrySet(), keySet(), and values().",
                "Check the difference between get(k) == null and containsKey(k) when value is null.",
                "Create a User class as key and test behavior without equals/hashCode."));

        return new Lesson("jdk8.setmap.2", "HashMap детально", "HashMap deep dive", uk, en);
    }

    private static Lesson materialLinkedTreeHashtableProperties() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("LinkedHashMap, TreeMap, Hashtable, Properties"));
        uk.add(LessonBlock.paragraph(
                "Map-реалізації відрізняються не тільки швидкістю, а й порядком обходу, "
                + "підтримкою null, потокобезпечністю і типовими сценаріями."));
        uk.add(LessonBlock.table(
                "Клас\tПорядок\tnull\tКоментар",
                Arrays.asList(
                        "HashMap\tне гарантується\t1 null key, багато null values\tосновний вибір",
                        "LinkedHashMap\tпорядок вставки або access-order\tтак\tзручно для LRU cache",
                        "TreeMap\tсортування ключів\tnull key ні\tпотребує Comparable/Comparator",
                        "Hashtable\tне гарантується\tnull ні\tlegacy synchronized",
                        "Properties\tрядкові налаштування\tкраще String\tlegacy config API")));
        uk.add(LessonBlock.code(
                "Map<String, Integer> linked = new LinkedHashMap<String, Integer>();\n"
                + "linked.put(\"first\", 1);\n"
                + "linked.put(\"second\", 2);\n"
                + "linked.put(\"third\", 3);\n"
                + "System.out.println(linked.keySet()); // [first, second, third]\n"
                + "\n"
                + "Map<String, Integer> tree = new TreeMap<String, Integer>();\n"
                + "tree.put(\"b\", 2);\n"
                + "tree.put(\"a\", 1);\n"
                + "tree.put(\"c\", 3);\n"
                + "System.out.println(tree.keySet());   // [a, b, c]"));
        uk.add(LessonBlock.code(
                "Properties props = new Properties();\n"
                + "props.setProperty(\"db.url\", \"jdbc:h2:mem:test\");\n"
                + "props.setProperty(\"db.user\", \"sa\");\n"
                + "\n"
                + "String url = props.getProperty(\"db.url\");\n"
                + "String timeout = props.getProperty(\"timeout\", \"30\"); // default"));
        uk.add(LessonBlock.warning(
                "Hashtable синхронізує окремі методи, але це не робить складні сценарії автоматично безпечними. "
                + "Для нового коду частіше використовуйте ConcurrentHashMap або зовнішню синхронізацію."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть LinkedHashMap і покажіть, що порядок вставки зберігається.",
                "Створіть TreeMap з ключами Integer і виведіть firstKey/lastKey.",
                "Спробуйте покласти null key у TreeMap і зафіксуйте виняток.",
                "Зробіть Properties з трьома налаштуваннями і прочитайте значення з default."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("LinkedHashMap, TreeMap, Hashtable, Properties"));
        en.add(LessonBlock.paragraph(
                "Map implementations differ not only by speed, but also by iteration order, "
                + "null support, thread-safety, and typical use cases."));
        en.add(LessonBlock.table(
                "Class\tOrder\tnull\tComment",
                Arrays.asList(
                        "HashMap\tnot guaranteed\t1 null key, many null values\tmain choice",
                        "LinkedHashMap\tinsertion or access-order\tyes\tuseful for LRU cache",
                        "TreeMap\tsorted keys\tno null key\tneeds Comparable/Comparator",
                        "Hashtable\tnot guaranteed\tno null\tlegacy synchronized",
                        "Properties\tstring settings\tprefer String\tlegacy config API")));
        en.add(LessonBlock.code(
                "Map<String, Integer> linked = new LinkedHashMap<String, Integer>();\n"
                + "linked.put(\"first\", 1);\n"
                + "linked.put(\"second\", 2);\n"
                + "linked.put(\"third\", 3);\n"
                + "System.out.println(linked.keySet()); // [first, second, third]\n"
                + "\n"
                + "Map<String, Integer> tree = new TreeMap<String, Integer>();\n"
                + "tree.put(\"b\", 2);\n"
                + "tree.put(\"a\", 1);\n"
                + "tree.put(\"c\", 3);\n"
                + "System.out.println(tree.keySet());   // [a, b, c]"));
        en.add(LessonBlock.code(
                "Properties props = new Properties();\n"
                + "props.setProperty(\"db.url\", \"jdbc:h2:mem:test\");\n"
                + "props.setProperty(\"db.user\", \"sa\");\n"
                + "\n"
                + "String url = props.getProperty(\"db.url\");\n"
                + "String timeout = props.getProperty(\"timeout\", \"30\"); // default"));
        en.add(LessonBlock.warning(
                "Hashtable synchronizes individual methods, but that does not make complex "
                + "scenarios automatically safe. For new code, usually use ConcurrentHashMap "
                + "or external synchronization."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create a LinkedHashMap and show that insertion order is preserved.",
                "Create a TreeMap with Integer keys and print firstKey/lastKey.",
                "Try putting null key into TreeMap and record the exception.",
                "Create Properties with three settings and read a value with default."));

        return new Lesson("jdk8.setmap.3", "Map-реалізації", "Map implementations", uk, en);
    }

    // ── Algorithms and Streams ─────────────────────────────────────────────

    private static void addAlgorithmsAndStreams(Course s) {
        Chapter ch = new Chapter(
                "Алгоритми Collections і Stream API у JDK 8",
                "Collections algorithms and Stream API in JDK 8");
        ch.add(materialComparatorCollectionsAlgorithms());
        ch.add(materialStreamsFromCollections());
        ch.add(materialConcurrentCollections());
        s.add(ch);
    }

    private static Lesson materialComparatorCollectionsAlgorithms() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Collections algorithms"));
        uk.add(LessonBlock.paragraph(
                "Клас java.util.Collections містить готові алгоритми для List: sort, binarySearch, "
                + "reverse, shuffle, rotate, min, max, frequency, disjoint. Це не структура даних, "
                + "а утилітний клас зі static методами."));
        uk.add(LessonBlock.code(
                "List<String> names = new ArrayList<String>(Arrays.asList(\"Oleh\", \"Ira\", \"Anna\"));\n"
                + "Collections.sort(names);          // [Anna, Ira, Oleh]\n"
                + "Collections.reverse(names);       // [Oleh, Ira, Anna]\n"
                + "Collections.shuffle(names);       // випадковий порядок\n"
                + "\n"
                + "int count = Collections.frequency(names, \"Ira\");\n"
                + "String min = Collections.min(names);"));
        uk.add(LessonBlock.heading("Comparator у JDK 8"));
        uk.add(LessonBlock.code(
                "class Student {\n"
                + "    final String name;\n"
                + "    final int age;\n"
                + "    final double grade;\n"
                + "\n"
                + "    Student(String name, int age, double grade) {\n"
                + "        this.name = name;\n"
                + "        this.age = age;\n"
                + "        this.grade = grade;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Comparator<Student> byName = new Comparator<Student>() {\n"
                + "    public int compare(Student a, Student b) {\n"
                + "        return a.name.compareTo(b.name);\n"
                + "    }\n"
                + "};\n"
                + "\n"
                + "// JDK 8 lambda + helper methods\n"
                + "Comparator<Student> byGradeDesc = Comparator\n"
                + "        .comparingDouble((Student s) -> s.grade)\n"
                + "        .reversed();"));
        uk.add(LessonBlock.warning(
                "binarySearch працює правильно тільки на відсортованому списку і з тим самим "
                + "Comparator, яким ви сортували. Інакше результат не має сенсу."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Відсортуйте студентів за name, age, grade.",
                "Зробіть comparator: grade desc, потім name asc.",
                "Перевірте binarySearch до сортування і після сортування.",
                "Напишіть метод top3(List<Student>) без Stream API, тільки Collections.sort."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Collections algorithms"));
        en.add(LessonBlock.paragraph(
                "java.util.Collections contains ready algorithms for List: sort, binarySearch, "
                + "reverse, shuffle, rotate, min, max, frequency, disjoint. It is not a data "
                + "structure, but a utility class with static methods."));
        en.add(LessonBlock.code(
                "List<String> names = new ArrayList<String>(Arrays.asList(\"Oleh\", \"Ira\", \"Anna\"));\n"
                + "Collections.sort(names);          // [Anna, Ira, Oleh]\n"
                + "Collections.reverse(names);       // [Oleh, Ira, Anna]\n"
                + "Collections.shuffle(names);       // random order\n"
                + "\n"
                + "int count = Collections.frequency(names, \"Ira\");\n"
                + "String min = Collections.min(names);"));
        en.add(LessonBlock.heading("Comparator in JDK 8"));
        en.add(LessonBlock.code(
                "class Student {\n"
                + "    final String name;\n"
                + "    final int age;\n"
                + "    final double grade;\n"
                + "\n"
                + "    Student(String name, int age, double grade) {\n"
                + "        this.name = name;\n"
                + "        this.age = age;\n"
                + "        this.grade = grade;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Comparator<Student> byName = new Comparator<Student>() {\n"
                + "    public int compare(Student a, Student b) {\n"
                + "        return a.name.compareTo(b.name);\n"
                + "    }\n"
                + "};\n"
                + "\n"
                + "// JDK 8 lambda + helper methods\n"
                + "Comparator<Student> byGradeDesc = Comparator\n"
                + "        .comparingDouble((Student s) -> s.grade)\n"
                + "        .reversed();"));
        en.add(LessonBlock.warning(
                "binarySearch works correctly only on a sorted list and with the same Comparator "
                + "used for sorting. Otherwise the result is meaningless."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Sort students by name, age, grade.",
                "Build comparator: grade desc, then name asc.",
                "Check binarySearch before sorting and after sorting.",
                "Write top3(List<Student>) without Stream API, only Collections.sort."));

        return new Lesson("jdk8.algorithms.1", "Collections algorithms", "Collections algorithms", uk, en);
    }

    private static Lesson materialStreamsFromCollections() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Stream API: pipeline над колекцією"));
        uk.add(LessonBlock.paragraph(
                "Stream API з'явився у Java 8. Stream не зберігає дані, а описує pipeline "
                + "операцій над джерелом: Collection, масив, файл, генератор. Є проміжні "
                + "операції (filter, map, sorted) і термінальні (collect, count, forEach)."));
        uk.add(LessonBlock.code(
                "import java.util.stream.Collectors;\n"
                + "\n"
                + "List<String> names = Arrays.asList(\"Ira\", \"Oleh\", \"Anna\", \"Ivan\");\n"
                + "\n"
                + "List<String> result = names.stream()\n"
                + "        .filter(s -> s.length() >= 4)     // проміжна\n"
                + "        .map(String::toUpperCase)         // проміжна\n"
                + "        .sorted()                         // проміжна\n"
                + "        .collect(Collectors.toList());    // термінальна\n"
                + "\n"
                + "System.out.println(result); // [ANNA, IVAN, OLEH]"));
        uk.add(LessonBlock.table(
                "Операція\tТип\tЩо повертає",
                Arrays.asList(
                        "filter\tпроміжна\tStream<T>",
                        "map\tпроміжна\tStream<R>",
                        "flatMap\tпроміжна\tStream<R> з вкладених джерел",
                        "sorted\tпроміжна\tStream<T>",
                        "collect\tтермінальна\tколекцію або інший результат",
                        "count\tтермінальна\tlong",
                        "anyMatch/allMatch\tтермінальна\tboolean")));
        uk.add(LessonBlock.warning(
                "Stream можна використати тільки один раз. Після термінальної операції pipeline "
                + "закритий. Якщо треба повторити обробку, створіть новий stream з колекції."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "З List<Integer> отримайте квадрати тільки парних чисел.",
                "З List<String> знайдіть перше слово довше 6 символів через findFirst.",
                "З List<Student> згрупуйте студентів за віком через Collectors.groupingBy.",
                "Перепишіть один Stream pipeline у звичайний for-loop і порівняйте читабельність."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Stream API: pipeline over a collection"));
        en.add(LessonBlock.paragraph(
                "Stream API appeared in Java 8. A Stream does not store data; it describes a "
                + "pipeline of operations over a source: Collection, array, file, generator. "
                + "There are intermediate operations (filter, map, sorted) and terminal ones "
                + "(collect, count, forEach)."));
        en.add(LessonBlock.code(
                "import java.util.stream.Collectors;\n"
                + "\n"
                + "List<String> names = Arrays.asList(\"Ira\", \"Oleh\", \"Anna\", \"Ivan\");\n"
                + "\n"
                + "List<String> result = names.stream()\n"
                + "        .filter(s -> s.length() >= 4)     // intermediate\n"
                + "        .map(String::toUpperCase)         // intermediate\n"
                + "        .sorted()                         // intermediate\n"
                + "        .collect(Collectors.toList());    // terminal\n"
                + "\n"
                + "System.out.println(result); // [ANNA, IVAN, OLEH]"));
        en.add(LessonBlock.table(
                "Operation\tType\tReturns",
                Arrays.asList(
                        "filter\tintermediate\tStream<T>",
                        "map\tintermediate\tStream<R>",
                        "flatMap\tintermediate\tStream<R> from nested sources",
                        "sorted\tintermediate\tStream<T>",
                        "collect\tterminal\tcollection or another result",
                        "count\tterminal\tlong",
                        "anyMatch/allMatch\tterminal\tboolean")));
        en.add(LessonBlock.warning(
                "A Stream can be used only once. After a terminal operation, the pipeline is "
                + "closed. If you need to process again, create a new stream from the collection."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "From List<Integer>, get squares of even numbers only.",
                "From List<String>, find the first word longer than 6 characters using findFirst.",
                "From List<Student>, group students by age using Collectors.groupingBy.",
                "Rewrite one Stream pipeline as a normal for-loop and compare readability."));

        return new Lesson("jdk8.algorithms.2", "Stream API у JDK 8", "Stream API in JDK 8", uk, en);
    }

    private static Lesson materialConcurrentCollections() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Fail-fast і concurrent collections"));
        uk.add(LessonBlock.paragraph(
                "Більшість звичайних колекцій JDK 8 не потокобезпечні. Якщо один потік змінює "
                + "ArrayList, а інший обходить його, результат непередбачуваний. Fail-fast "
                + "ітератор намагається швидко помітити структурну зміну і кинути "
                + "ConcurrentModificationException, але це не механізм безпеки потоків."));
        uk.add(LessonBlock.table(
                "Клас\tДля чого\tОсобливість",
                Arrays.asList(
                        "Collections.synchronizedList\tобгортка з synchronized методами\tобхід треба синхронізувати вручну",
                        "CopyOnWriteArrayList\tчасто читаємо, рідко пишемо\tкожна зміна копіює масив",
                        "ConcurrentHashMap\tпотокобезпечна Map\tкраще за Hashtable для нового коду",
                        "BlockingQueue\tчерга між потоками\tput/take можуть блокуватися")));
        uk.add(LessonBlock.code(
                "List<String> list = Collections.synchronizedList(new ArrayList<String>());\n"
                + "list.add(\"A\");\n"
                + "list.add(\"B\");\n"
                + "\n"
                + "synchronized (list) {\n"
                + "    Iterator<String> it = list.iterator();\n"
                + "    while (it.hasNext()) {\n"
                + "        System.out.println(it.next());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.code(
                "ConcurrentMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();\n"
                + "map.put(\"java\", 1);\n"
                + "map.putIfAbsent(\"java\", 2);      // не перезапише\n"
                + "map.replace(\"java\", 1, 3);       // замінить тільки якщо старе 1\n"
                + "System.out.println(map.get(\"java\")); // 3"));
        uk.add(LessonBlock.warning(
                "Collections.synchronizedList синхронізує окремі методи, але ітерація — це "
                + "послідовність багатьох дій. Тому обхід треба обгорнути synchronized(list)."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Навмисно отримайте ConcurrentModificationException у ArrayList.",
                "Виправте код через Iterator.remove().",
                "Створіть synchronizedList і правильно обійдіть його у synchronized-блоці.",
                "Створіть ConcurrentHashMap і порахуйте частоти слів через merge.",
                "Поясніть, коли CopyOnWriteArrayList може бути доречним."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Fail-fast and concurrent collections"));
        en.add(LessonBlock.paragraph(
                "Most regular JDK 8 collections are not thread-safe. If one thread changes "
                + "ArrayList while another iterates it, the result is unpredictable. A fail-fast "
                + "iterator tries to detect structural changes quickly and throw "
                + "ConcurrentModificationException, but this is not a thread-safety mechanism."));
        en.add(LessonBlock.table(
                "Class\tPurpose\tSpecial point",
                Arrays.asList(
                        "Collections.synchronizedList\twrapper with synchronized methods\titeration must be manually synchronized",
                        "CopyOnWriteArrayList\tmany reads, rare writes\teach change copies the array",
                        "ConcurrentHashMap\tthread-safe Map\tbetter than Hashtable for new code",
                        "BlockingQueue\tqueue between threads\tput/take may block")));
        en.add(LessonBlock.code(
                "List<String> list = Collections.synchronizedList(new ArrayList<String>());\n"
                + "list.add(\"A\");\n"
                + "list.add(\"B\");\n"
                + "\n"
                + "synchronized (list) {\n"
                + "    Iterator<String> it = list.iterator();\n"
                + "    while (it.hasNext()) {\n"
                + "        System.out.println(it.next());\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.code(
                "ConcurrentMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();\n"
                + "map.put(\"java\", 1);\n"
                + "map.putIfAbsent(\"java\", 2);      // will not overwrite\n"
                + "map.replace(\"java\", 1, 3);       // replaces only if old value is 1\n"
                + "System.out.println(map.get(\"java\")); // 3"));
        en.add(LessonBlock.warning(
                "Collections.synchronizedList synchronizes individual methods, but iteration "
                + "is a sequence of many actions. Wrap traversal in synchronized(list)."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Intentionally get ConcurrentModificationException in ArrayList.",
                "Fix the code through Iterator.remove().",
                "Create synchronizedList and iterate it correctly inside synchronized block.",
                "Create ConcurrentHashMap and count word frequencies through merge.",
                "Explain when CopyOnWriteArrayList may be appropriate."));

        return new Lesson("jdk8.algorithms.3", "Concurrent collections", "Concurrent collections", uk, en);
    }

    // ── Practice Marathon ──────────────────────────────────────────────────

    private static void addPracticeMarathon(Course s) {
        Chapter ch = new Chapter(
                "Практичний марафон JDK 8",
                "JDK 8 practice marathon");
        ch.add(materialCollectionsMarathon());
        s.add(ch);
    }

    private static Lesson materialCollectionsMarathon() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Марафон: 30 вправ на колекції"));
        uk.add(LessonBlock.paragraph(
                "Цей урок не для читання за 5 хвилин. Його треба проходити як тренажер: "
                + "відкрити редактор, створити маленький клас Main, і виконувати вправи одну "
                + "за одною. Якщо вправа здається легкою — додайте edge cases: порожня колекція, "
                + "null, дублікати, один елемент, тисяча елементів."));
        uk.add(LessonBlock.list(
                "1. Реалізуйте reverse(List<String>) без Collections.reverse.",
                "2. Реалізуйте uniquePreserveOrder(List<String>) через LinkedHashSet.",
                "3. Реалізуйте countWords(String text) через HashMap.",
                "4. Реалізуйте topWords(String text, int n) через Map + sort.",
                "5. Видаліть з List<Integer> всі числа менші за 10 через Iterator.",
                "6. Вставте елемент після кожного входження \"A\" через ListIterator.",
                "7. Порівняйте HashSet і TreeSet для рядків різного регістру.",
                "8. Зробіть Comparator<Person>: age asc, name asc.",
                "9. Перевірте binarySearch з правильним і неправильним Comparator.",
                "10. Перетворіть List<Student> у Map<Integer, Student> за id.",
                "11. Знайдіть дублікати у List<String> через Set.",
                "12. Знайдіть перший неповторний символ у рядку через LinkedHashMap.",
                "13. Реалізуйте простий LRU cache через LinkedHashMap.",
                "14. Згрупуйте студентів за groupName через Map<String, List<Student>>.",
                "15. Напишіть метод safeGet(List<T>, int index, T defaultValue).",
                "16. Перевірте різницю між ArrayList і LinkedList для get(5000).",
                "17. Реалізуйте queue через ArrayDeque.",
                "18. Реалізуйте stack через ArrayDeque.",
                "19. Перепишіть Enumeration-приклад на Iterator.",
                "20. Напишіть власний Iterable, який повертає числа від 1 до n.",
                "21. Зробіть Stream pipeline: filter + map + collect.",
                "22. Перепишіть pipeline у for-loop.",
                "23. Згрупуйте слова за довжиною через Collectors.groupingBy.",
                "24. Знайдіть max/min через Collections і через Stream.",
                "25. Перевірте, що Arrays.asList не підтримує add/remove.",
                "26. Створіть unmodifiableList і спробуйте змінити.",
                "27. Створіть synchronizedList і правильно обійдіть його.",
                "28. Створіть ConcurrentHashMap і використайте putIfAbsent.",
                "29. Поясніть equals/hashCode на власному класі.",
                "30. Напишіть конспект: яку колекцію обирати у 10 типових задачах."));
        uk.add(LessonBlock.note(
                "Правило марафону: кожна вправа має мати щонайменше 3 тести руками через "
                + "System.out.println. Для початківця це нормально: спершу бачимо поведінку, "
                + "потім формалізуємо її у тести."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Marathon: 30 collection exercises"));
        en.add(LessonBlock.paragraph(
                "This lesson is not meant to be read in 5 minutes. Use it as a training set: "
                + "open the editor, create a tiny Main class, and solve exercises one by one. "
                + "If an exercise feels easy, add edge cases: empty collection, null, duplicates, "
                + "one element, one thousand elements."));
        en.add(LessonBlock.list(
                "1. Implement reverse(List<String>) without Collections.reverse.",
                "2. Implement uniquePreserveOrder(List<String>) using LinkedHashSet.",
                "3. Implement countWords(String text) using HashMap.",
                "4. Implement topWords(String text, int n) using Map + sort.",
                "5. Remove all numbers below 10 from List<Integer> using Iterator.",
                "6. Insert an element after each \"A\" using ListIterator.",
                "7. Compare HashSet and TreeSet for strings with different case.",
                "8. Build Comparator<Person>: age asc, name asc.",
                "9. Test binarySearch with correct and incorrect Comparator.",
                "10. Convert List<Student> into Map<Integer, Student> by id.",
                "11. Find duplicates in List<String> using Set.",
                "12. Find first non-repeating character using LinkedHashMap.",
                "13. Implement a simple LRU cache using LinkedHashMap.",
                "14. Group students by groupName using Map<String, List<Student>>.",
                "15. Write safeGet(List<T>, int index, T defaultValue).",
                "16. Compare ArrayList and LinkedList for get(5000).",
                "17. Implement queue using ArrayDeque.",
                "18. Implement stack using ArrayDeque.",
                "19. Rewrite an Enumeration example as Iterator.",
                "20. Write your own Iterable returning numbers from 1 to n.",
                "21. Build Stream pipeline: filter + map + collect.",
                "22. Rewrite the pipeline as a for-loop.",
                "23. Group words by length using Collectors.groupingBy.",
                "24. Find max/min through Collections and through Stream.",
                "25. Verify that Arrays.asList does not support add/remove.",
                "26. Create unmodifiableList and try to modify it.",
                "27. Create synchronizedList and iterate it correctly.",
                "28. Create ConcurrentHashMap and use putIfAbsent.",
                "29. Explain equals/hashCode on your own class.",
                "30. Write notes: which collection to choose in 10 typical tasks."));
        en.add(LessonBlock.note(
                "Marathon rule: every exercise must have at least 3 manual checks through "
                + "System.out.println. For a beginner this is normal: first observe behavior, "
                + "then formalize it into tests."));

        return new Lesson("jdk8.practice.1", "Марафон колекцій", "Collections marathon", uk, en);
    }
}
