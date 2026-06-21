package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Гурток алгоритмів та структур даних.
 */
final class AlgorithmsChapters {

    static void add(Course s) {
        Chapter ch1 = new Chapter("Основи алгоритмів", "Algorithm Basics");
        ch1.add(materialBigO());
        s.add(ch1);

        Chapter ch2 = new Chapter("Сортування та Пошук", "Sorting and Searching");
        ch2.add(materialSorting());
        ch2.add(materialBinarySearch());
        s.add(ch2);

        Chapter ch3 = new Chapter("Структури даних", "Data Structures");
        ch3.add(materialTrees());
        ch3.add(materialGraphs());
        s.add(ch3);
    }

    private static Lesson materialBigO() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Оцінка складності (Big-O)"));
        uk.add(LessonBlock.paragraph(
                "Big-O нотація використовується для оцінки того, як швидко зростає час виконання "
                + "або використання пам'яті алгоритмом при збільшенні розміру вхідних даних (N)."));
        uk.add(LessonBlock.list(
                "O(1) — Константний час (доступ до елемента масиву за індексом).",
                "O(log N) — Логарифмічний час (бінарний пошук).",
                "O(N) — Лінійний час (пошук у невідсортованому масиві).",
                "O(N log N) — Лінійно-логарифмічний час (швидке сортування, сортування злиттям).",
                "O(N^2) — Квадратичний час (сортування бульбашкою).",
                "O(2^N) — Експоненціальний час (рекурсивне обчислення Фібоначчі без мемоїзації)."));
        uk.add(LessonBlock.paragraph(
                "Університетський аналіз алгоритмів завжди починається з оцінки найгіршого "
                + "(worst-case), найкращого (best-case) та середнього (average-case) сценаріїв."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Complexity Estimation (Big-O)"));
        en.add(LessonBlock.paragraph(
                "Big-O notation is used to estimate how fast an algorithm's execution time "
                + "or memory usage grows as the input size (N) increases."));
        en.add(LessonBlock.list(
                "O(1) — Constant time (array index access).",
                "O(log N) — Logarithmic time (binary search).",
                "O(N) — Linear time (searching an unsorted array).",
                "O(N log N) — Linearithmic time (quicksort, mergesort).",
                "O(N^2) — Quadratic time (bubble sort).",
                "O(2^N) — Exponential time (recursive Fibonacci without memoization)."));
        en.add(LessonBlock.paragraph(
                "University-level algorithm analysis always begins with evaluating the worst-case, "
                + "best-case, and average-case scenarios."));

        return new Lesson("alg.1", "Складність (Big-O)", "Complexity (Big-O)", uk, en);
    }

    private static Lesson materialSorting() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Алгоритми сортування"));
        uk.add(LessonBlock.paragraph(
                "Існує безліч алгоритмів сортування, але в Java стандартне сортування об'єктів "
                + "(Arrays.sort або Collections.sort) використовує TimSort (гібрид Merge Sort та "
                + "Insertion Sort), що дає складність O(N log N). Для примітивів використовується "
                + "Dual-Pivot QuickSort."));
        uk.add(LessonBlock.code(
                "// Приклад: Bubble Sort O(N^2)\n"
                + "void bubbleSort(int[] arr) {\n"
                + "    int n = arr.length;\n"
                + "    for (int i = 0; i < n - 1; i++) {\n"
                + "        for (int j = 0; j < n - i - 1; j++) {\n"
                + "            if (arr[j] > arr[j + 1]) {\n"
                + "                // swap\n"
                + "                int temp = arr[j];\n"
                + "                arr[j] = arr[j + 1];\n"
                + "                arr[j + 1] = temp;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Sorting Algorithms"));
        en.add(LessonBlock.paragraph(
                "There are many sorting algorithms, but Java's standard object sort "
                + "(Arrays.sort or Collections.sort) uses TimSort (a hybrid of Merge Sort and "
                + "Insertion Sort), providing O(N log N) complexity. For primitives, it uses "
                + "Dual-Pivot QuickSort."));
        en.add(LessonBlock.code(
                "// Example: Bubble Sort O(N^2)\n"
                + "void bubbleSort(int[] arr) {\n"
                + "    int n = arr.length;\n"
                + "    for (int i = 0; i < n - 1; i++) {\n"
                + "        for (int j = 0; j < n - i - 1; j++) {\n"
                + "            if (arr[j] > arr[j + 1]) {\n"
                + "                // swap\n"
                + "                int temp = arr[j];\n"
                + "                arr[j] = arr[j + 1];\n"
                + "                arr[j + 1] = temp;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}"));

        return new Lesson("alg.2", "Сортування", "Sorting", uk, en);
    }

    private static Lesson materialBinarySearch() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Бінарний пошук"));
        uk.add(LessonBlock.paragraph(
                "Бінарний пошук дозволяє знайти елемент у ВІДСОРТОВАНОМУ масиві за O(log N) часу "
                + "шляхом постійного поділу області пошуку навпіл."));
        uk.add(LessonBlock.code(
                "int binarySearch(int[] arr, int target) {\n"
                + "    int left = 0;\n"
                + "    int right = arr.length - 1;\n"
                + "\n"
                + "    while (left <= right) {\n"
                + "        int mid = left + (right - left) / 2;\n"
                + "        \n"
                + "        if (arr[mid] == target) return mid;\n"
                + "        if (arr[mid] < target) left = mid + 1;\n"
                + "        else right = mid - 1;\n"
                + "    }\n"
                + "    return -1; // не знайдено\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Binary Search"));
        en.add(LessonBlock.paragraph(
                "Binary search allows finding an element in a SORTED array in O(log N) time "
                + "by continuously halving the search area."));
        en.add(LessonBlock.code(
                "int binarySearch(int[] arr, int target) {\n"
                + "    int left = 0;\n"
                + "    int right = arr.length - 1;\n"
                + "\n"
                + "    while (left <= right) {\n"
                + "        int mid = left + (right - left) / 2;\n"
                + "        \n"
                + "        if (arr[mid] == target) return mid;\n"
                + "        if (arr[mid] < target) left = mid + 1;\n"
                + "        else right = mid - 1;\n"
                + "    }\n"
                + "    return -1; // not found\n"
                + "}"));

        return new Lesson("alg.3", "Бінарний пошук", "Binary Search", uk, en);
    }

    private static Lesson materialTrees() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Дерева (Trees)"));
        uk.add(LessonBlock.paragraph(
                "Дерево — це ієрархічна структура даних. Найбільш відомим є Бінарне Дерево Пошуку "
                + "(BST). В збалансованому BST (наприклад, Red-Black Tree, що лежить в основі TreeMap) "
                + "операції пошуку, вставки та видалення виконуються за O(log N)."));
        uk.add(LessonBlock.code(
                "class TreeNode {\n"
                + "    int val;\n"
                + "    TreeNode left, right;\n"
                + "    TreeNode(int x) { val = x; }\n"
                + "}\n"
                + "\n"
                + "// Обхід In-Order (відсортований порядок для BST)\n"
                + "void inOrder(TreeNode node) {\n"
                + "    if (node != null) {\n"
                + "        inOrder(node.left);\n"
                + "        System.out.println(node.val);\n"
                + "        inOrder(node.right);\n"
                + "    }\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Trees"));
        en.add(LessonBlock.paragraph(
                "A tree is a hierarchical data structure. The most famous is the Binary Search Tree "
                + "(BST). In a balanced BST (like the Red-Black Tree underpinning TreeMap), "
                + "search, insertion, and deletion operate in O(log N) time."));
        en.add(LessonBlock.code(
                "class TreeNode {\n"
                + "    int val;\n"
                + "    TreeNode left, right;\n"
                + "    TreeNode(int x) { val = x; }\n"
                + "}\n"
                + "\n"
                + "// In-Order traversal (sorted order for BST)\n"
                + "void inOrder(TreeNode node) {\n"
                + "    if (node != null) {\n"
                + "        inOrder(node.left);\n"
                + "        System.out.println(node.val);\n"
                + "        inOrder(node.right);\n"
                + "    }\n"
                + "}"));

        return new Lesson("alg.4", "Дерева (Trees)", "Trees", uk, en);
    }

    private static Lesson materialGraphs() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Графи (Graphs): BFS та DFS"));
        uk.add(LessonBlock.paragraph(
                "Граф складається з вершин (вузлів) та ребер (зв'язків). Два основні алгоритми "
                + "обходу: Пошук у ширину (BFS, використовує чергу) та Пошук у глибину "
                + "(DFS, використовує стек або рекурсію)."));
        uk.add(LessonBlock.code(
                "// Обхід у ширину (BFS) з використанням черги\n"
                + "void bfs(int startNode, Map<Integer, List<Integer>> graph) {\n"
                + "    Set<Integer> visited = new HashSet<>();\n"
                + "    Queue<Integer> queue = new LinkedList<>();\n"
                + "    \n"
                + "    visited.add(startNode);\n"
                + "    queue.offer(startNode);\n"
                + "    \n"
                + "    while (!queue.isEmpty()) {\n"
                + "        int node = queue.poll();\n"
                + "        System.out.println(node);\n"
                + "        \n"
                + "        for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {\n"
                + "            if (!visited.contains(neighbor)) {\n"
                + "                visited.add(neighbor);\n"
                + "                queue.offer(neighbor);\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Graphs: BFS and DFS"));
        en.add(LessonBlock.paragraph(
                "A graph consists of vertices (nodes) and edges (connections). The two main traversal "
                + "algorithms are Breadth-First Search (BFS, uses a queue) and Depth-First Search "
                + "(DFS, uses a stack or recursion)."));
        en.add(LessonBlock.code(
                "// Breadth-First Search (BFS) using a queue\n"
                + "void bfs(int startNode, Map<Integer, List<Integer>> graph) {\n"
                + "    Set<Integer> visited = new HashSet<>();\n"
                + "    Queue<Integer> queue = new LinkedList<>();\n"
                + "    \n"
                + "    visited.add(startNode);\n"
                + "    queue.offer(startNode);\n"
                + "    \n"
                + "    while (!queue.isEmpty()) {\n"
                + "        int node = queue.poll();\n"
                + "        System.out.println(node);\n"
                + "        \n"
                + "        for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {\n"
                + "            if (!visited.contains(neighbor)) {\n"
                + "                visited.add(neighbor);\n"
                + "                queue.offer(neighbor);\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}"));

        return new Lesson("alg.5", "Графи: BFS і DFS", "Graphs: BFS & DFS", uk, en);
    }
}
