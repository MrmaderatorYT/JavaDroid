package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Гурток з тестування ПЗ.
 */
final class TestingChapters {

    static void add(Course s) {
        Chapter ch1 = new Chapter("Основи модульного тестування", "Unit Testing Basics");
        ch1.add(materialJUnit5());
        ch1.add(materialTdd());
        s.add(ch1);

        Chapter ch2 = new Chapter("Мокування та Ізоляція", "Mocking and Isolation");
        ch2.add(materialMockito());
        ch2.add(materialIntegrationTesting());
        s.add(ch2);
    }

    private static Lesson materialJUnit5() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("JUnit 5"));
        uk.add(LessonBlock.paragraph(
                "JUnit 5 — стандартний фреймворк для написання модульних (Unit) тестів у Java. "
                + "Модульний тест перевіряє найменшу частину коду (наприклад, один метод) в ізоляції."));
        uk.add(LessonBlock.code(
                "import static org.junit.jupiter.api.Assertions.*;\n"
                + "import org.junit.jupiter.api.Test;\n"
                + "import org.junit.jupiter.api.BeforeEach;\n"
                + "\n"
                + "class CalculatorTest {\n"
                + "    private Calculator calc;\n"
                + "\n"
                + "    @BeforeEach // Виконується перед КОЖНИМ тестом\n"
                + "    void setUp() { calc = new Calculator(); }\n"
                + "\n"
                + "    @Test\n"
                + "    void testAddition() {\n"
                + "        int result = calc.add(2, 3);\n"
                + "        assertEquals(5, result, \"2 + 3 має дорівнювати 5\");\n"
                + "    }\n"
                + "\n"
                + "    @Test\n"
                + "    void testDivisionByZero() {\n"
                + "        assertThrows(ArithmeticException.class, () -> calc.divide(10, 0));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "В JUnit 5 також існують анотації @ParameterizedTest для запуску одного тесту "
                + "з різними вхідними даними, та @Disabled для тимчасового вимкнення тесту."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("JUnit 5"));
        en.add(LessonBlock.paragraph(
                "JUnit 5 is the standard framework for writing Unit tests in Java. "
                + "A unit test verifies the smallest piece of code (like a single method) in isolation."));
        en.add(LessonBlock.code(
                "import static org.junit.jupiter.api.Assertions.*;\n"
                + "import org.junit.jupiter.api.Test;\n"
                + "import org.junit.jupiter.api.BeforeEach;\n"
                + "\n"
                + "class CalculatorTest {\n"
                + "    private Calculator calc;\n"
                + "\n"
                + "    @BeforeEach // Runs before EVERY test\n"
                + "    void setUp() { calc = new Calculator(); }\n"
                + "\n"
                + "    @Test\n"
                + "    void testAddition() {\n"
                + "        int result = calc.add(2, 3);\n"
                + "        assertEquals(5, result, \"2 + 3 should equal 5\");\n"
                + "    }\n"
                + "\n"
                + "    @Test\n"
                + "    void testDivisionByZero() {\n"
                + "        assertThrows(ArithmeticException.class, () -> calc.divide(10, 0));\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.note(
                "JUnit 5 also features @ParameterizedTest for running a single test with "
                + "multiple inputs, and @Disabled to temporarily disable a test."));

        return new Lesson("test.1", "JUnit 5", "JUnit 5", uk, en);
    }

    private static Lesson materialTdd() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Test-Driven Development (TDD)"));
        uk.add(LessonBlock.paragraph(
                "TDD (Розробка через тестування) — методологія, де тести пишуться ПЕРЕД написанням коду. "
                + "Життєвий цикл TDD відомий як Red-Green-Refactor."));
        uk.add(LessonBlock.list(
                "1. Red (Червоний) — Напишіть тест для нової функції. Він не пройде (буде червоним), бо функції ще немає.",
                "2. Green (Зелений) — Напишіть мінімальний код, потрібний для проходження тесту.",
                "3. Refactor (Рефакторинг) — Покращте написаний код, не змінюючи його поведінку (тести гарантують, що ви нічого не зламали)."));
        uk.add(LessonBlock.paragraph(
                "TDD змушує думати про архітектуру ДО реалізації та гарантує високе покриття коду тестами (Code Coverage)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Test-Driven Development (TDD)"));
        en.add(LessonBlock.paragraph(
                "TDD is a methodology where tests are written BEFORE writing the actual code. "
                + "The TDD lifecycle is known as Red-Green-Refactor."));
        en.add(LessonBlock.list(
                "1. Red — Write a test for a new feature. It will fail (be red) because the feature doesn't exist yet.",
                "2. Green — Write the minimal amount of code required to make the test pass.",
                "3. Refactor — Improve the code without changing its behavior (tests ensure you haven't broken anything)."));
        en.add(LessonBlock.paragraph(
                "TDD forces you to think about architecture BEFORE implementation and guarantees "
                + "high code coverage."));

        return new Lesson("test.2", "TDD", "TDD", uk, en);
    }

    private static Lesson materialMockito() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Mockito: Заглушки (Mocks & Stubs)"));
        uk.add(LessonBlock.paragraph(
                "Коли клас залежить від інших класів (наприклад, від бази даних), важко тестувати його "
                + "в ізоляції. Mockito дозволяє створювати фейкові об'єкти (mocks/stubs), які імітують "
                + "справжню поведінку."));
        uk.add(LessonBlock.code(
                "import static org.mockito.Mockito.*;\n"
                + "\n"
                + "class OrderServiceTest {\n"
                + "    @Test\n"
                + "    void testPlaceOrder() {\n"
                + "        // 1. Створюємо mock (заглушку)\n"
                + "        Database dbMock = mock(Database.class);\n"
                + "        \n"
                + "        // 2. Налаштовуємо поведінку (Stubbing)\n"
                + "        when(dbMock.saveOrder(any())).thenReturn(true);\n"
                + "\n"
                + "        // 3. Інжектуємо mock у сервіс\n"
                + "        OrderService service = new OrderService(dbMock);\n"
                + "        boolean result = service.placeOrder(new Order());\n"
                + "\n"
                + "        // 4. Перевіряємо (Verify)\n"
                + "        assertTrue(result);\n"
                + "        verify(dbMock, times(1)).saveOrder(any());\n"
                + "    }\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Mockito: Mocks & Stubs"));
        en.add(LessonBlock.paragraph(
                "When a class depends on other classes (like a database), it is hard to test it in isolation. "
                + "Mockito allows creating fake objects (mocks/stubs) that simulate real behavior."));
        en.add(LessonBlock.code(
                "import static org.mockito.Mockito.*;\n"
                + "\n"
                + "class OrderServiceTest {\n"
                + "    @Test\n"
                + "    void testPlaceOrder() {\n"
                + "        // 1. Create a mock\n"
                + "        Database dbMock = mock(Database.class);\n"
                + "        \n"
                + "        // 2. Stub the behavior\n"
                + "        when(dbMock.saveOrder(any())).thenReturn(true);\n"
                + "\n"
                + "        // 3. Inject mock into the service\n"
                + "        OrderService service = new OrderService(dbMock);\n"
                + "        boolean result = service.placeOrder(new Order());\n"
                + "\n"
                + "        // 4. Verify\n"
                + "        assertTrue(result);\n"
                + "        verify(dbMock, times(1)).saveOrder(any());\n"
                + "    }\n"
                + "}"));

        return new Lesson("test.3", "Mockito (Mocks)", "Mockito (Mocks)", uk, en);
    }

    private static Lesson materialIntegrationTesting() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Інтеграційне тестування"));
        uk.add(LessonBlock.paragraph(
                "Якщо модульні тести (Unit tests) перевіряють компоненти в ізоляції (часто з використанням Mockito), "
                + "інтеграційні тести перевіряють, чи працюють ці компоненти РАЗОМ."));
        uk.add(LessonBlock.paragraph(
                "Наприклад: чи може UserRepository зберегти дані у СПРАВЖНЮ базу даних? "
                + "Для цього часто використовують Testcontainers — бібліотеку, яка автоматично "
                + "піднімає Docker-контейнер з MySQL або PostgreSQL на час виконання тесту, "
                + "а потім знищує його."));
        uk.add(LessonBlock.warning(
                "Інтеграційні тести значно повільніші за модульні. Тому в класичній 'Піраміді Тестування' "
                + "(Testing Pyramid) модульних тестів має бути багато, інтеграційних — менше, "
                + "а E2E (End-to-End, UI тестів) — ще менше."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Integration Testing"));
        en.add(LessonBlock.paragraph(
                "While unit tests check components in isolation (often using Mockito), "
                + "integration tests verify whether these components work TOGETHER."));
        en.add(LessonBlock.paragraph(
                "For example: can the UserRepository save data to a REAL database? "
                + "To achieve this, developers often use Testcontainers — a library that automatically "
                + "spins up a Docker container with MySQL or PostgreSQL during the test run, "
                + "and destroys it afterward."));
        en.add(LessonBlock.warning(
                "Integration tests are much slower than unit tests. Therefore, in the classic "
                + "'Testing Pyramid', you should have many unit tests, fewer integration tests, "
                + "and even fewer E2E (End-to-End, UI) tests."));

        return new Lesson("test.4", "Інтеграційні тести", "Integration tests", uk, en);
    }
}
