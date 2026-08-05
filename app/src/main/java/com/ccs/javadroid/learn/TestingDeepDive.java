package com.ccs.javadroid.learn;

import java.util.Arrays;
import java.util.List;

/**
 * Ukrainian-only narrative walkthrough for the Testing lessons (test.1 … test.4).
 *
 * <p>Style contract: teach the way the Rust Book teaches. Start from a problem the reader can
 * feel, write the naive solution first, let it fail, and only then introduce the real technique.
 * Every code block is small, is introduced before it appears, and is walked through afterwards —
 * never a 40-line dump followed by one sentence. English content is deliberately untouched until
 * a dedicated translation pass.</p>
 */
final class TestingDeepDive {

    static final String MARKER = "Розбір крок за кроком";

    private TestingDeepDive() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
                if (containsMarker(uk)) {
                    continue;
                }
                switch (lesson.id) {
                    case "test.1": junit5(uk); break;
                    case "test.2": tdd(uk); break;
                    case "test.3": mockito(uk); break;
                    case "test.4": integration(uk); break;
                    default: break;
                }
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

    // ══════════════════════════════════════════════════════════════════════
    // test.1 — JUnit 5
    // ══════════════════════════════════════════════════════════════════════

    private static void junit5(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Почнемо не з визначень, а з ситуації, у яку рано чи пізно потрапляє кожен. "
                + "Ви написали клас кошика покупок і два тести до нього. Запускаєте перший — "
                + "зелений. Запускаєте другий окремо — теж зелений. Запускаєте обидва разом — "
                + "другий падає. Код ви не чіпали. Змінився лише порядок запуску."));

        uk.add(LessonBlock.heading("Тест, який ламається від сусіда"));
        uk.add(LessonBlock.paragraph(
                "Ось той самий код. Прочитайте його уважно і спробуйте самі здогадатися, де "
                + "пастка — вона в одному-єдиному слові:"));
        uk.add(LessonBlock.code(
                "class CartTest {\n"
                + "    // Одне поле на ВЕСЬ клас тестів\n"
                + "    private Cart cart = new Cart();\n"
                + "\n"
                + "    @Test\n"
                + "    void addingItemIncreasesSize() {\n"
                + "        cart.add(new Item(\"Кава\", 120));\n"
                + "        assertEquals(1, cart.size());\n"
                + "    }\n"
                + "\n"
                + "    @Test\n"
                + "    void emptyCartHasZeroTotal() {\n"
                + "        assertEquals(0, cart.total());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Пастка — у слові «одне». Здається природним створити cart один раз у полі: "
                + "навіщо плодити об'єкти? Але JUnit створює НОВИЙ екземпляр тестового класу "
                + "перед кожним тестовим методом саме для того, щоб такого поля-спільника не "
                + "існувало. Проблема тут не в JUnit — проблема виникає тоді, коли ви обходите "
                + "цей механізм: робите поле static, кладете стан у синглтон, пишете у файл або "
                + "в базу. Тоді другий тест бачить сміття від першого."));
        uk.add(LessonBlock.paragraph(
                "Уявімо, що поле оголошено як static Cart cart = new Cart(). Простежмо, що "
                + "фактично відбувається, коли обидва тести виконуються один за одним:"));
        uk.add(LessonBlock.table(
                "Крок\tЩо виконується\tСтан cart після кроку\tРезультат",
                Arrays.asList(
                        "1\tstatic-ініціалізація\tпорожній, total = 0\t—",
                        "2\taddingItemIncreasesSize\t1 товар, total = 120\tзелений",
                        "3\temptyCartHasZeroTotal\t1 товар, total = 120\tЧЕРВОНИЙ: чекали 0, отримали 120")));
        uk.add(LessonBlock.paragraph(
                "Другий тест не бреше — він чесно каже, що кошик не порожній. Він просто "
                + "перевіряє не те, що ви думали: замість «новий кошик має нульову суму» він "
                + "перевіряє «кошик після всього, що зробив попередній тест, має нульову суму». "
                + "І найгірше: якщо запустити його ОКРЕМО, він зелений. Такі тести називають "
                + "«тестами з ефектом сусіда», і вони отруюють довіру до всього набору — коли "
                + "збірка іноді червона, а іноді зелена без змін у коді, люди перестають читати "
                + "звіт тестів узагалі."));

        uk.add(LessonBlock.heading("@BeforeEach: свіжий об'єкт на кожен тест"));
        uk.add(LessonBlock.paragraph(
                "Лікування просте: перед кожним тестом складати сцену заново. Саме для цього "
                + "існує @BeforeEach — метод, який JUnit викликає перед КОЖНИМ @Test:"));
        uk.add(LessonBlock.code(
                "class CartTest {\n"
                + "    private Cart cart;\n"
                + "\n"
                + "    @BeforeEach\n"
                + "    void createEmptyCart() {\n"
                + "        cart = new Cart();\n"
                + "    }\n"
                + "\n"
                + "    @Test\n"
                + "    void addingItemIncreasesSize() {\n"
                + "        cart.add(new Item(\"Кава\", 120));\n"
                + "        assertEquals(1, cart.size());\n"
                + "    }\n"
                + "\n"
                + "    @Test\n"
                + "    void emptyCartHasZeroTotal() {\n"
                + "        assertEquals(0, cart.total());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер послідовність така: createEmptyCart → перший тест → createEmptyCart → "
                + "другий тест. Другий тест отримує кошик, якого ще ніхто не торкався, і його "
                + "результат більше не залежить від того, що робив сусід і в якому порядку "
                + "JUnit вирішив їх запустити. Це і є справжня причина існування @BeforeEach: "
                + "не «зменшити дублювання коду» (це приємний бонус), а гарантувати ІЗОЛЯЦІЮ."));
        uk.add(LessonBlock.note(
                "Порядок виконання тестів у JUnit 5 навмисно недетермінований і може змінитися "
                + "між версіями. Це зроблено спеціально: якщо ваші тести залежать від порядку, "
                + "розробники хочуть, щоб ви дізналися про це якнайраніше, а не за півроку на "
                + "сервері збірки."));

        uk.add(LessonBlock.heading("Повний життєвий цикл: коли що виконується"));
        uk.add(LessonBlock.paragraph(
                "Крім @BeforeEach є ще три анотації життєвого циклу. Найпростіше запам'ятати "
                + "їх, простеживши реальний запуск. Візьмімо клас із двома тестами і всіма "
                + "чотирма гачками:"));
        uk.add(LessonBlock.code(
                "class LifecycleDemoTest {\n"
                + "    @BeforeAll  static void openConnectionPool() { }\n"
                + "    @BeforeEach void freshFixture()  { }\n"
                + "    @Test       void testA()          { }\n"
                + "    @Test       void testB()          { }\n"
                + "    @AfterEach  void cleanupFixture() { }\n"
                + "    @AfterAll   static void closePool() { }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "А ось порядок, у якому JUnit це виконає. Прочитайте таблицю зверху вниз — це "
                + "буквально хронологія:"));
        uk.add(LessonBlock.table(
                "№\tЩо виконується\tСкільки разів за весь клас",
                Arrays.asList(
                        "1\t@BeforeAll openConnectionPool\t1 (перед усіма тестами)",
                        "2\t@BeforeEach freshFixture\tперед testA",
                        "3\t@Test testA\t1",
                        "4\t@AfterEach cleanupFixture\tпісля testA",
                        "5\t@BeforeEach freshFixture\tперед testB",
                        "6\t@Test testB\t1",
                        "7\t@AfterEach cleanupFixture\tпісля testB",
                        "8\t@AfterAll closePool\t1 (після всіх тестів)")));
        uk.add(LessonBlock.paragraph(
                "Головне, що видно з таблиці: BeforeEach/AfterEach виконуються стільки разів, "
                + "скільки у вас тестів, а BeforeAll/AfterAll — рівно по одному разу на клас. "
                + "Звідси випливає правило вибору. Усе, що дешево створити й що має бути "
                + "чистим, — у @BeforeEach. Усе, що ДОРОГО створити й що тест не псує (пул "
                + "з'єднань, запущений вбудований сервер, завантажений великий файл), — у "
                + "@BeforeAll."));
        uk.add(LessonBlock.paragraph(
                "Тепер найчастіше питання новачків: чому @BeforeAll мусить бути static? "
                + "Відповідь випливає з першого розділу цього уроку. JUnit створює новий "
                + "екземпляр тестового класу перед кожним тестом. Отже, у момент, коли треба "
                + "виконати @BeforeAll («перед усіма»), жодного екземпляра ще не існує — "
                + "викликати нестатичний метод просто немає на чому. Static-метод належить "
                + "класу, а не екземпляру, тому його викликати можна. Те саме дзеркально "
                + "стосується @AfterAll: усі екземпляри вже відпрацювали."));
        uk.add(LessonBlock.note(
                "Якщо static заважає (наприклад, поле треба інжектити фреймворком), можна "
                + "позначити клас @TestInstance(Lifecycle.PER_CLASS). Тоді JUnit створює один "
                + "екземпляр на весь клас, і @BeforeAll стає нестатичним. Ціна — ви власноруч "
                + "повертаєте ту саму спільну мутабельну «сцену», з якої почався цей урок, тож "
                + "робіть це свідомо."));

        uk.add(LessonBlock.heading("Каталог перевірок: що саме брати"));
        uk.add(LessonBlock.paragraph(
                "assertEquals — не єдина перевірка, і вибір правильної важливий: від нього "
                + "залежить, наскільки зрозумілим буде повідомлення про помилку о третій ночі. "
                + "Ось ті, що покривають 95% реальних потреб:"));
        uk.add(LessonBlock.table(
                "Перевірка\tКоли брати\tЩо покаже при падінні",
                Arrays.asList(
                        "assertEquals(exp, act)\tзначення збігається\texpected: <5> but was: <4>",
                        "assertTrue(cond)\tумова істинна\tлише «expected true» — без деталей",
                        "assertNull / assertNotNull\tперевірка на null\tповідомляє фактичне значення",
                        "assertThrows(Ex.class, …)\tмає впасти виняток\tякий виняток стався замість очікуваного",
                        "assertSame(a, b)\tтой самий об'єкт у пам'яті\tрозрізняє рівність і тотожність",
                        "assertIterableEquals(a, b)\tколекції поелементно\tіндекс першої розбіжності",
                        "assertAll(…)\tкілька незалежних перевірок\tУСІ, що впали, разом")));
        uk.add(LessonBlock.warning(
                "Найпоширеніша помилка — писати assertTrue(a.equals(b)) замість "
                + "assertEquals(a, b). Обидва перевіряють те саме, але перший при падінні "
                + "скаже лише «expected: <true> but was: <false>», і ви не знатимете НІ "
                + "очікуваного, ні фактичного значення. Другий покаже обидва. Різниця — "
                + "п'ять хвилин або година налагодження."));

        uk.add(LessonBlock.heading("Чому існує assertAll"));
        uk.add(LessonBlock.paragraph(
                "Уявіть, що ви перевіряєте розібраний з JSON профіль користувача. Природно "
                + "написати так:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void parsesUserProfile() {\n"
                + "    User u = parser.parse(json);\n"
                + "    assertEquals(\"Оксана\", u.getName());\n"
                + "    assertEquals(31, u.getAge());\n"
                + "    assertEquals(\"oksana@example.com\", u.getEmail());\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер уявіть, що парсер зламався і всі три поля порожні. Що ви побачите у "
                + "звіті? Лише одне: «expected: <Оксана> but was: <null>». Бо кожен assert, "
                + "який не проходить, кидає AssertionFailedError — і виконання методу негайно "
                + "припиняється. Другий і третій рядки просто ніколи не виконаються. Ви "
                + "полагодите ім'я, перезапустите тест — і побачите падіння на віці. "
                + "Полагодите вік — падіння на email. Три цикли замість одного."));
        uk.add(LessonBlock.paragraph(
                "assertAll саме для цього і зробили: він приймає набір перевірок як лямбди, "
                + "виконує ВСІ до кінця, збирає всі падіння і показує їх одним списком:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void parsesUserProfile() {\n"
                + "    User u = parser.parse(json);\n"
                + "    assertAll(\"профіль користувача\",\n"
                + "        () -> assertEquals(\"Оксана\", u.getName()),\n"
                + "        () -> assertEquals(31, u.getAge()),\n"
                + "        () -> assertEquals(\"oksana@example.com\", u.getEmail())\n"
                + "    );\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Ключова деталь — кожна перевірка загорнута в лямбду () -> …. Без цього "
                + "обгортання assertEquals виконався б ще ДО того, як assertAll отримав "
                + "керування, і впав би звично. Лямбда відкладає виконання: assertAll сам "
                + "вирішує, коли й у якому порядку її запустити, і сам ловить помилки."));
        uk.add(LessonBlock.warning(
                "assertAll годиться лише для НЕЗАЛЕЖНИХ перевірок. Якщо друга перевірка не має "
                + "сенсу без першої — наприклад, assertNotNull(list) і потім "
                + "assertEquals(3, list.size()) — не кладіть їх в assertAll: коли list виявиться "
                + "null, друга лямбда впаде з NullPointerException, і у звіті буде дві помилки "
                + "замість однієї зрозумілої."));

        uk.add(LessonBlock.heading("@ParameterizedTest: один тест, багато даних"));
        uk.add(LessonBlock.paragraph(
                "Припустимо, ви перевіряєте метод isValidPassword. Правил кілька, і хочеться "
                + "перевірити купу варіантів. Наївний підхід — писати окремий @Test на кожен, "
                + "і швидко виходить стіна майже однакових методів. Спокуса — злити їх в один "
                + "тест із циклом. Обидва варіанти погані, і ось чому: у першому ви "
                + "копіпастите, у другому — при падінні бачите один червоний тест і не знаєте, "
                + "на якому саме вході він упав."));
        uk.add(LessonBlock.paragraph(
                "@ParameterizedTest вирішує обидві проблеми: ви пишете тіло один раз, а JUnit "
                + "звітує про кожен вхід ОКРЕМИМ рядком у звіті:"));
        uk.add(LessonBlock.code(
                "@ParameterizedTest\n"
                + "@ValueSource(strings = {\"\", \"   \", \"abc\", \"12345\"})\n"
                + "void shortOrBlankPasswordsAreRejected(String candidate) {\n"
                + "    assertFalse(Validator.isValidPassword(candidate));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Це чотири окремі запуски. У звіті вони будуть чотирма рядками з підписами "
                + "[1] \"\", [2] \"   \", [3] abc, [4] 12345 — тож коли впаде третій, ви одразу "
                + "бачитимете, що проблема саме з \"abc\", і не гратимете в вгадайку."));
        uk.add(LessonBlock.paragraph(
                "@ValueSource працює, поки на вхід потрібне одне значення. Щойно вам треба "
                + "пара «вхід → очікуваний результат», беріть @CsvSource: кожен рядок — це "
                + "колонки, розділені комою, які лягають у параметри методу по порядку:"));
        uk.add(LessonBlock.code(
                "@ParameterizedTest(name = \"{0} грн зі знижкою {1}% = {2} грн\")\n"
                + "@CsvSource({\n"
                + "    \"100, 0,  100\",\n"
                + "    \"100, 10,  90\",\n"
                + "    \"100, 100,  0\",\n"
                + "    \"99,  33,  66\"\n"
                + "})\n"
                + "void appliesDiscount(int price, int percent, int expected) {\n"
                + "    assertEquals(expected, Pricing.applyDiscount(price, percent));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Рядок \"100, 10, 90\" означає: price = 100, percent = 10, expected = 90. "
                + "Атрибут name задає підпис у звіті, а {0}, {1}, {2} — це підстановка "
                + "параметрів за індексом. Замість безликого «appliesDiscount[2]» ви побачите "
                + "«100 грн зі знижкою 10% = 90 грн». Це той рідкісний випадок, коли трохи "
                + "зусиль на форматування економить реальний час пізніше."));
        uk.add(LessonBlock.note(
                "Зверніть увагу на четвертий рядок: 99 зі знижкою 33% — це 66.33, а не 66. Такі "
                + "граничні випадки з округленням найлегше додати саме в @CsvSource: один рядок "
                + "тексту замість цілого нового методу. Дешевизна додавання випадку — головна "
                + "практична причина любити параметризовані тести."));

        uk.add(LessonBlock.heading("@Nested: щоб звіт читався як речення"));
        uk.add(LessonBlock.paragraph(
                "Коли тестів у класі стає тридцять, звіт перетворюється на плаский список "
                + "довгих імен на кшталт withdrawFailsWhenBalanceTooLowAndAccountIsFrozen. "
                + "@Nested дозволяє згрупувати тести за контекстом — вкладений клас стає "
                + "заголовком групи:"));
        uk.add(LessonBlock.code(
                "class AccountTest {\n"
                + "    Account account;\n"
                + "\n"
                + "    @Nested\n"
                + "    class WhenBalanceIsZero {\n"
                + "        @BeforeEach void setUp() { account = new Account(0); }\n"
                + "\n"
                + "        @Test void withdrawIsRejected() {\n"
                + "            assertThrows(InsufficientFunds.class, () -> account.withdraw(10));\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "У звіті це виглядатиме як AccountTest › WhenBalanceIsZero › withdrawIsRejected "
                + "— майже речення. Але цінність не лише в читабельності: кожен вкладений клас "
                + "має ВЛАСНИЙ @BeforeEach, який виконується ПІСЛЯ зовнішнього. Тобто зовнішній "
                + "готує спільне тло, а вкладений доналаштовує його під свій контекст. Це "
                + "прибирає нескінченні if-и в налаштуванні."));
        uk.add(LessonBlock.warning(
                "Вкладений клас має бути НЕстатичним внутрішнім класом (без static) — інакше він "
                + "не матиме доступу до полів зовнішнього і JUnit не зв'яже їхні життєві цикли. "
                + "Це прямо протилежно вимозі до @BeforeAll, і плутанина тут трапляється часто."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть параметризований тест за допомогою @CsvSource, який перевіряє метод обчислення знижки для кількох різних сум."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@ParameterizedTest\n"
                + "@CsvSource({\"100, 10, 90\", \"200, 20, 160\"})\n"
                + "void discountCalculation(int price, int percent, int expected) {\n"
                + "    assertEquals(expected, Calculator.discount(price, percent));\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // test.2 — TDD
    // ══════════════════════════════════════════════════════════════════════

    private static void tdd(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Про TDD легко прочитати три слова «Red-Green-Refactor» і не зрозуміти нічого. "
                + "Тому в цьому уроці ми не будемо описувати цикл — ми його ПРОЙДЕМО. Разом, "
                + "крок за кроком, на одному маленькому класі. Ви побачите, як я навмисно пишу "
                + "код, який очевидно неправильний, — і чому це не дурість, а метод."));
        uk.add(LessonBlock.paragraph(
                "Завдання: клас RomanNumerals із методом toRoman(int), який перетворює число "
                + "на римський запис. 1 → I, 4 → IV, 9 → IX, 14 → XIV. Задача достатньо "
                + "хитра, щоб «просто сісти й написати» вийшло криво, і достатньо маленька, "
                + "щоб пройти її повністю в одному уроці."));

        uk.add(LessonBlock.heading("Крок 1 (Red): тест, який навіть не компілюється"));
        uk.add(LessonBlock.paragraph(
                "Перше, що ми пишемо, — не клас. Тест. Найпростіший випадок, який тільки "
                + "спадає на думку:"));
        uk.add(LessonBlock.code(
                "class RomanNumeralsTest {\n"
                + "    @Test\n"
                + "    void one() {\n"
                + "        assertEquals(\"I\", RomanNumerals.toRoman(1));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Запускаємо — і отримуємо помилку компіляції: класу RomanNumerals не існує. "
                + "Це і є перший «червоний». Здається безглуздим: звісно, не існує, ми ж його "
                + "не написали. Але цей крок уже дав нам дві важливі речі, і обидві — "
                + "проєктні, а не тестові. Ми щойно вирішили, що клас називається "
                + "RomanNumerals, метод — toRoman, він статичний, приймає int і повертає "
                + "String. Ми спроєктували API з позиції ТОГО, ХТО НИМ КОРИСТУЄТЬСЯ, ще не "
                + "написавши жодного рядка реалізації."));
        uk.add(LessonBlock.note(
                "Це і є перша справжня причина писати тест першим, і вона не про тести. Коли "
                + "ви пишете клас, а потім тест до нього, ви підлаштовуєте виклик під те, що "
                + "вийшло. Коли навпаки — ви спершу описуєте зручний виклик, і реалізація "
                + "мусить під нього підлаштуватися. Незручні API так просто не народжуються."));

        uk.add(LessonBlock.heading("Крок 2 (Green): найдурніша реалізація, яка проходить"));
        uk.add(LessonBlock.paragraph(
                "Правило зеленої фази звучить дивно: напишіть МІНІМУМ коду, щоб тест став "
                + "зеленим. Не «правильний код». Мінімум. Ось він:"));
        uk.add(LessonBlock.code(
                "class RomanNumerals {\n"
                + "    static String toRoman(int number) {\n"
                + "        return \"I\";\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тест зелений. І ви цілком справедливо думаєте: це ж шахрайство, метод "
                + "повертає \"I\" на будь-яке число. Так. І це нормально. Тому що зараз єдине "
                + "твердження, яке ми зробили про поведінку системи, — «одиниця це I». Код "
                + "рівно настільки складний, наскільки складні наші вимоги. Ані на рядок "
                + "більше."));
        uk.add(LessonBlock.paragraph(
                "У цьому й полягає найважча для прийняття ідея TDD. Спокуса просто зараз "
                + "написати повний алгоритм із таблицею символів величезна. Але подумайте, що "
                + "станеться, якщо ви піддастеся: ви напишете 30 рядків, які проходять один "
                + "тест на одиницю. Решта 29 рядків не перевірена НІЧИМ. Ви не знатимете, "
                + "працюють вони чи ні, доки не напишете тести — а тести після коду мають "
                + "неприємну властивість підтверджувати те, що код робить, а не те, що він "
                + "мав би робити."));

        uk.add(LessonBlock.heading("Крок 3 (Red): тест, який змушує код подорослішати"));
        uk.add(LessonBlock.paragraph(
                "Наше «шахрайство» тримається лише тому, що ми не поставили жодного "
                + "незручного питання. Ставимо:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void two() {\n"
                + "    assertEquals(\"II\", RomanNumerals.toRoman(2));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Червоний: expected \"II\" but was \"I\". Зверніть увагу, що сталося. Не ми "
                + "вирішили, що час ускладнити код, — це ТЕСТ змусив нас. Кожне ускладнення в "
                + "TDD має конкретну причину у вигляді конкретного червоного тесту. Це "
                + "радикально відрізняється від звички «а раптом знадобиться» і природним "
                + "чином не дає розростатися коду, який ніхто не замовляв."));

        uk.add(LessonBlock.heading("Крок 4 (Green): все ще мінімум"));
        uk.add(LessonBlock.paragraph(
                "Робимо обидва тести зеленими найдешевшим способом. Знову без алгоритму — "
                + "просто повторюємо \"I\" потрібну кількість разів:"));
        uk.add(LessonBlock.code(
                "static String toRoman(int number) {\n"
                + "    StringBuilder sb = new StringBuilder();\n"
                + "    for (int i = 0; i < number; i++) {\n"
                + "        sb.append(\"I\");\n"
                + "    }\n"
                + "    return sb.toString();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Обидва тести зелені. Код усе ще «неправильний» — toRoman(9) поверне "
                + "\"IIIIIIIII\". Але зверніть увагу, що він уже НЕ шахрайський: він відображає "
                + "справжнє, хай і неповне, правило римської системи. Кожен цикл робить код "
                + "трохи чеснішим. Це називають «загальнішанням через приклади»: ви не "
                + "вгадуєте загальний алгоритм, ви до нього дорощуєте."));

        uk.add(LessonBlock.heading("Крок 5 (Red): перший справжній виклик — четвірка"));
        uk.add(LessonBlock.paragraph(
                "Тепер додамо випадок, який ламає всю нашу модель. У римській системі 4 — це "
                + "не IIII, а IV: менший символ перед більшим означає віднімання."));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void four() {\n"
                + "    assertEquals(\"IV\", RomanNumerals.toRoman(4));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Червоний: отримали \"IIII\". Цикл із одиничок далі не тягне — потрібна ідея "
                + "інша за природою. І ось тут TDD дає ще одну перевагу, про яку рідко "
                + "говорять: ви підходите до складного місця, маючи два зелені тести за "
                + "спиною. Якщо нова ідея зламає одиницю чи двійку, ви дізнаєтеся про це за "
                + "секунду, а не через тиждень."));

        uk.add(LessonBlock.heading("Крок 6 (Green): жадібний алгоритм із таблицею"));
        uk.add(LessonBlock.paragraph(
                "Ідея: тримати символи від найбільшого до найменшого, включно з «відніманнями» "
                + "IV та IX як окремими записами, і жадібно відкушувати найбільший, що "
                + "вміщається:"));
        uk.add(LessonBlock.code(
                "private static final int[]    VALUES  = {10, 9, 5, 4, 1};\n"
                + "private static final String[] SYMBOLS = {\"X\", \"IX\", \"V\", \"IV\", \"I\"};\n"
                + "\n"
                + "static String toRoman(int number) {\n"
                + "    StringBuilder sb = new StringBuilder();\n"
                + "    for (int i = 0; i < VALUES.length; i++) {\n"
                + "        while (number >= VALUES[i]) {\n"
                + "            sb.append(SYMBOLS[i]);\n"
                + "            number -= VALUES[i];\n"
                + "        }\n"
                + "    }\n"
                + "    return sb.toString();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Простежмо його руками на числі 14, щоб переконатися, що це справді працює, а "
                + "не «здається, що працює»:"));
        uk.add(LessonBlock.table(
                "Крок\tПоточне число\tЩо вміщається\tДодали\tЗалишок",
                Arrays.asList(
                        "1\t14\t10 (X)\tX\t4",
                        "2\t4\t10 не вміщається, 9 ні, 5 ні, 4 так (IV)\tIV\t0",
                        "3\t0\tнічого\t—\t0")));
        uk.add(LessonBlock.paragraph(
                "Результат \"XIV\" — правильно. Ключова деталь алгоритму, яку легко проґавити: "
                + "IV стоїть у таблиці МІЖ 5 і 1. Якби ми поклали його після одиниці, до "
                + "четвірки цикл дійшов би раніше через 1 і видав би \"IIII\". Порядок у "
                + "таблиці — не оформлення, а частина алгоритму."));

        uk.add(LessonBlock.heading("Крок 7 (Refactor): тепер можна прибирати"));
        uk.add(LessonBlock.paragraph(
                "Зелено — час на третю фазу, про яку найчастіше забувають. Рефакторинг у TDD "
                + "означає одне: змінити ФОРМУ коду, не змінюючи його ПОВЕДІНКИ. Тести — це "
                + "ваша страховка: якщо після зміни всі три лишилися зелені, поведінка не "
                + "поїхала. Два паралельні масиви — типовий кандидат на прибирання, бо їх "
                + "легко розсинхронізувати:"));
        uk.add(LessonBlock.code(
                "private enum Numeral {\n"
                + "    X(10, \"X\"), IX(9, \"IX\"), V(5, \"V\"), IV(4, \"IV\"), I(1, \"I\");\n"
                + "\n"
                + "    final int value;\n"
                + "    final String symbol;\n"
                + "    Numeral(int value, String symbol) {\n"
                + "        this.value = value;\n"
                + "        this.symbol = symbol;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "static String toRoman(int number) {\n"
                + "    StringBuilder sb = new StringBuilder();\n"
                + "    for (Numeral n : Numeral.values()) {\n"
                + "        while (number >= n.value) {\n"
                + "            sb.append(n.symbol);\n"
                + "            number -= n.value;\n"
                + "        }\n"
                + "    }\n"
                + "    return sb.toString();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Значення і символ тепер фізично не можуть розійтися — вони в одному "
                + "оголошенні. Порядок констант enum задає порядок перебору, тож правило «від "
                + "більшого до меншого» так само зашите, але тепер його видно з одного рядка. "
                + "Запускаємо тести: усі три зелені. Ми змінили внутрішній устрій і за секунду "
                + "переконалися, що зовні нічого не змінилося. Без тестів це був би ризик, "
                + "який більшість розробників просто не взяла б на себе — і код так і лишився "
                + "б із двома масивами назавжди."));
        uk.add(LessonBlock.note(
                "Рефакторити можна і ТЕСТИ. Три майже однакові методи one/two/four напрошуються "
                + "на @CsvSource з попереднього уроку. Тестовий код живе стільки ж, скільки "
                + "робочий, і потребує такої ж охайності."));

        uk.add(LessonBlock.heading("Що ми насправді отримали"));
        uk.add(LessonBlock.paragraph(
                "Озирніться на сім кроків. Жодного разу ми не сідали «проєктувати алгоритм» — "
                + "він проступив сам, під тиском конкретних прикладів. Жодного разу ми не мали "
                + "коду, який не працює: між будь-якими двома кроками система була в робочому "
                + "стані. І в кожен момент ми знали, ЩО саме зараз не працює, — рівно один "
                + "червоний тест, який ми щойно написали."));
        uk.add(LessonBlock.paragraph(
                "Порівняйте з класичним підходом: написати 60 рядків, запустити, побачити "
                + "неправильний результат і не знати, у якому з шести місць помилка. TDD не "
                + "робить вас розумнішим — він робить проміжок між «усе працювало» і «щось "
                + "зламалося» настільки коротким, що місце поломки очевидне."));
        uk.add(LessonBlock.warning(
                "TDD — не універсальний закон. Коли ви ще не знаєте, ЩО будувати (досліджуєте "
                + "чужий API, малюєте прототип екрана, підбираєте параметри), тест першим лише "
                + "заважає: ви пишете перевірки для поведінки, яку викинете за годину. У таких "
                + "випадках чесніше зробити начерк, викинути його і почати з TDD, коли форма "
                + "рішення вже зрозуміла."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть перший тест для функції перевірки високосного року, який змушує реалізувати базову перевірку (ділиться на 4)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void leapYearIsDivisibleByFour() {\n"
                + "    assertTrue(Year.isLeap(2024));\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // test.3 — Mockito
    // ══════════════════════════════════════════════════════════════════════

    private static void mockito(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Ви летите в літаку і вирішуєте попрацювати. Відкриваєте проєкт, запускаєте "
                + "тести — і половина падає з ConnectException. Не тому, що ви щось зламали, а "
                + "тому, що на висоті десять кілометрів немає бази даних. Ті самі тести на "
                + "робочому місці виконуються півтори хвилини й іноді падають без причини, бо "
                + "колега саме почистив тестову базу."));

        uk.add(LessonBlock.heading("Тест, який залежить від усього світу"));
        uk.add(LessonBlock.paragraph(
                "Ось винуватець. Сервіс оформлення замовлення і тест до нього — рівно так, як "
                + "це пишуть, коли ще не знають про моки:"));
        uk.add(LessonBlock.code(
                "class OrderService {\n"
                + "    private final OrderRepository repo;\n"
                + "    private final EmailSender mailer;\n"
                + "\n"
                + "    OrderService(OrderRepository repo, EmailSender mailer) {\n"
                + "        this.repo = repo;\n"
                + "        this.mailer = mailer;\n"
                + "    }\n"
                + "\n"
                + "    boolean placeOrder(Order order) {\n"
                + "        if (order.getTotal() <= 0) {\n"
                + "            return false;\n"
                + "        }\n"
                + "        repo.save(order);\n"
                + "        mailer.send(order.getEmail(), \"Замовлення прийнято\");\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Логіка тут крихітна: перевірити суму, зберегти, надіслати лист. Але щоб "
                + "перевірити цю крихітну логіку «в лоб», доведеться підняти базу і поштовий "
                + "сервер:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void placesOrder() {\n"
                + "    OrderRepository repo = new PostgresOrderRepository(\"jdbc:postgresql://...\");\n"
                + "    EmailSender mailer = new SmtpEmailSender(\"smtp.gmail.com\", 587);\n"
                + "    OrderService service = new OrderService(repo, mailer);\n"
                + "\n"
                + "    assertTrue(service.placeOrder(new Order(500, \"user@example.com\")));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Порахуймо, що з цим не так. По-перше, тест повільний: підключення до бази й "
                + "SMTP — це сотні мілісекунд, а таких тестів у проєкті сотні. По-друге, він "
                + "нестабільний: мережа моргнула — червоно. По-третє, він має ПОБІЧНІ ЕФЕКТИ: "
                + "у базі з'явився зайвий рядок, а на реальну адресу пішов реальний лист. "
                + "По-четверте, і це найважливіше: він не вміє перевірити те, що нас цікавить "
                + "найбільше."));
        uk.add(LessonBlock.paragraph(
                "Спробуйте, наприклад, перевірити, що при відмові бази лист НЕ надсилається. "
                + "Як змусити справжній Postgres впасти саме в цьому тесті? Вимкнути його з "
                + "розетки посеред збірки? Цілі класи сценаріїв — таймаути, помилки диска, "
                + "порожні відповіді — просто недосяжні, поки ви працюєте зі справжніми "
                + "залежностями."));

        uk.add(LessonBlock.heading("Мок: підставний виконавець"));
        uk.add(LessonBlock.paragraph(
                "Мок — це об'єкт, який на вигляд має той самий тип, що й справжня залежність, "
                + "але всередині порожній: усі методи нічого не роблять і повертають "
                + "«нульове» значення. Mockito генерує такий об'єкт на льоту:"));
        uk.add(LessonBlock.code(
                "OrderRepository repo = mock(OrderRepository.class);\n"
                + "EmailSender mailer = mock(EmailSender.class);\n"
                + "OrderService service = new OrderService(repo, mailer);\n"
                + "\n"
                + "assertTrue(service.placeOrder(new Order(500, \"user@example.com\")));"));
        uk.add(LessonBlock.paragraph(
                "Цей тест виконується за частки мілісекунди, працює в літаку і не залишає "
                + "слідів: repo.save() просто нічого не робить, mailer.send() теж. Зверніть "
                + "увагу, чому це взагалі можливо — тому що OrderService приймає залежності "
                + "через конструктор. Якби він створював new PostgresOrderRepository() "
                + "всередині себе, підставити нічого не вийшло б. Тестованість — це не "
                + "властивість тесту, це властивість дизайну класу."));
        uk.add(LessonBlock.paragraph(
                "Що саме повертають незаданi методи мока — важливо знати напам'ять, бо це "
                + "джерело половини непорозумінь:"));
        uk.add(LessonBlock.table(
                "Тип повернення\tЩо поверне мок за замовчуванням",
                Arrays.asList(
                        "int, long, double\t0",
                        "boolean\tfalse",
                        "об'єкт (String, Order, …)\tnull",
                        "List, Set, Map\tпорожня колекція, не null",
                        "Optional\tOptional.empty() (Mockito 2+)",
                        "void\tнічого не робить")));
        uk.add(LessonBlock.warning(
                "Рядок про об'єкти пояснює найчастіший NullPointerException у тестах: ви "
                + "викликаєте service.findUser(1), усередині виконується repo.findById(1), мок "
                + "повертає null, і код падає на user.getName(). Це не баг Mockito — це "
                + "нагадування, що ви забули задати поведінку."));

        uk.add(LessonBlock.heading("Stubbing: коли моку треба щось відповісти"));
        uk.add(LessonBlock.paragraph(
                "Порожній мок годиться, лише поки результат виклику коду не цікавить. Щойно "
                + "клас під тестом ВИКОРИСТОВУЄ відповідь залежності, її треба задати. Це "
                + "називають stubbing, і робиться через when(...).thenReturn(...):"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void appliesDiscountForLoyalCustomer() {\n"
                + "    CustomerRepository customers = mock(CustomerRepository.class);\n"
                + "    when(customers.findById(42))\n"
                + "        .thenReturn(new Customer(42, /* loyaltyYears */ 5));\n"
                + "\n"
                + "    PricingService pricing = new PricingService(customers);\n"
                + "    assertEquals(90, pricing.priceFor(42, 100));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Читається це так: «коли хтось викличе customers.findById(42), поверни ось "
                + "цього клієнта». Важлива деталь — прив'язка до конкретного аргументу 42. "
                + "Якщо код під тестом викличе findById(43), заглушка не спрацює і мок поверне "
                + "null. Це не прикрість, а корисна властивість: тест непрямо перевіряє, що "
                + "сервіс шукає саме того клієнта, якого просили."));
        uk.add(LessonBlock.paragraph(
                "Крім thenReturn є ще два корисні варіанти. Перший — змусити мок кинути виняток; "
                + "саме так ми нарешті перевіримо той сценарій «база впала», недосяжний зі "
                + "справжнім Postgres:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void doesNotSendEmailWhenSaveFails() {\n"
                + "    OrderRepository repo = mock(OrderRepository.class);\n"
                + "    EmailSender mailer = mock(EmailSender.class);\n"
                + "    doThrow(new DataAccessException(\"з'єднання втрачено\"))\n"
                + "        .when(repo).save(any());\n"
                + "\n"
                + "    OrderService service = new OrderService(repo, mailer);\n"
                + "\n"
                + "    assertThrows(DataAccessException.class,\n"
                + "                 () -> service.placeOrder(new Order(500, \"u@e.com\")));\n"
                + "    verifyNoInteractions(mailer);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Останній рядок — суть тесту: лист про успішне замовлення не має піти, якщо "
                + "замовлення не збереглося. Зі справжньою базою цей тест написати практично "
                + "неможливо; з моком він займає п'ять рядків і виконується миттєво. Ось за що "
                + "насправді люблять моки — не за швидкість, а за доступ до сценаріїв відмов."));
        uk.add(LessonBlock.note(
                "Синтаксис doThrow(...).when(mock).method() виглядає вивернутим порівняно з "
                + "when(mock.method()).thenThrow(...). Причина технічна: для void-методів "
                + "when(mock.method()) не скомпілюється, бо void не можна передати аргументом. "
                + "Тому для void-методів завжди беріть форму do*(...).when(mock)."));

        uk.add(LessonBlock.heading("verify: перевірка того, що не видно в результаті"));
        uk.add(LessonBlock.paragraph(
                "Іноді правильність визначається не поверненим значенням, а тим, що метод "
                + "ЗРОБИВ. placeOrder повертає true — але ж головне, щоб замовлення справді "
                + "збереглося і лист пішов рівно один раз, а не двічі. Для цього є verify:"));
        uk.add(LessonBlock.code(
                "service.placeOrder(new Order(500, \"user@example.com\"));\n"
                + "\n"
                + "verify(repo).save(any(Order.class));            // рівно 1 раз\n"
                + "verify(mailer, times(1)).send(eq(\"user@example.com\"), anyString());\n"
                + "verify(repo, never()).delete(any());"));
        uk.add(LessonBlock.paragraph(
                "Перший рядок читається «repo.save було викликано рівно один раз із будь-яким "
                + "Order» — verify без кількості означає times(1). Другий перевіряє адресу "
                + "точно, а текст листа — байдуже яким. Третій стверджує, що видалення не "
                + "відбувалося: never() ловить помилки, яких не видно в результаті взагалі."));
        uk.add(LessonBlock.warning(
                "Не перевіряйте verify на все підряд. Кожен verify фіксує ВНУТРІШНЮ будову "
                + "методу — те, кого саме він викликає. Якщо ви завтра заміните два виклики "
                + "save() на один batchSave(), поведінка для користувача не зміниться, а тест "
                + "стане червоним. Такі тести називають крихкими: вони заважають рефакторингу "
                + "замість того, щоб його страхувати. Правило: перевіряйте verify лише те, що "
                + "є САМОЮ метою методу (лист відправлено, гроші списано), а не побічні кроки."));

        uk.add(LessonBlock.heading("Матчери і правило «все або нічого»"));
        uk.add(LessonBlock.paragraph(
                "any(), eq(), anyString() з прикладу вище називають матчерами (argument "
                + "matchers). Вони описують не конкретне значення, а множину прийнятних. "
                + "Найкорисніші:"));
        uk.add(LessonBlock.list(
                "any() / any(Order.class) — будь-яке значення, включно з null.",
                "anyString(), anyInt(), anyList() — будь-яке значення саме цього типу, але НЕ null.",
                "eq(value) — точно це значення; потрібен, коли в тому ж виклику є інші матчери.",
                "argThat(o -> o.getTotal() > 100) — власна умова, коли готового матчера мало."));
        uk.add(LessonBlock.paragraph(
                "А тепер правило, на якому спотикаються всі без винятку. У ОДНОМУ виклику "
                + "не можна змішувати матчери й звичайні значення. Ось код, який виглядає "
                + "цілком розумно і при цьому падає:"));
        uk.add(LessonBlock.code(
                "// НЕ ПРАЦЮЄ: InvalidUseOfMatchersException\n"
                + "verify(mailer).send(\"user@example.com\", anyString());\n"
                + "\n"
                + "// Правильно: обидва аргументи — матчери\n"
                + "verify(mailer).send(eq(\"user@example.com\"), anyString());"));
        uk.add(LessonBlock.paragraph(
                "Причина в тому, як матчери влаштовані всередині. anyString() насправді не "
                + "повертає «будь-який рядок» — він КЛАДЕ опис умови у внутрішній стек Mockito "
                + "і повертає порожнє значення-заглушку. Коли виклик завершується, Mockito "
                + "бачить два аргументи, але лише один запис у стеку — і не може зрозуміти, до "
                + "якої позиції той запис належить. Тому вимога жорстка: або всі аргументи "
                + "звичайні, або всі — матчери. eq(\"user@example.com\") — це спосіб сказати "
                + "«точне значення» мовою матчерів."));

        uk.add(LessonBlock.heading("Мок чи шпигун: у чому різниця"));
        uk.add(LessonBlock.paragraph(
                "Іноді потрібен об'єкт, який здебільшого поводиться як справжній, але один "
                + "метод має бути підмінений. Для цього є spy — обгортка навколо реального "
                + "екземпляра. Різниця з моком принципова, і плутанина тут дорого коштує:"));
        uk.add(LessonBlock.table(
                "Питання\tmock(Foo.class)\tspy(new Foo())",
                Arrays.asList(
                        "Чи виконується справжній код методу?\tНі, ніколи\tТак, якщо метод не підмінено",
                        "Що повертає незаданий метод?\t0/false/null\tте, що поверне справжня реалізація",
                        "Чи є побічні ефекти (запис у файл)?\tНі\tТак — справжній код виконується",
                        "Типове застосування\tзовнішні залежності\tчастковa підміна свого ж класу",
                        "Безпечний спосіб stubbing\twhen(m.f()).thenReturn(x)\tdoReturn(x).when(s).f()")));
        uk.add(LessonBlock.paragraph(
                "Останній рядок таблиці — не педантизм, а справжня пастка. Подивіться на "
                + "різницю між двома рядками:"));
        uk.add(LessonBlock.code(
                "List<String> spy = spy(new ArrayList<>());\n"
                + "\n"
                + "// ПАСТКА: spy.get(0) РЕАЛЬНО виконається до stubbing\n"
                + "// і впаде з IndexOutOfBoundsException на порожньому списку\n"
                + "when(spy.get(0)).thenReturn(\"привіт\");\n"
                + "\n"
                + "// Безпечно: справжній get(0) не викликається взагалі\n"
                + "doReturn(\"привіт\").when(spy).get(0);"));
        uk.add(LessonBlock.paragraph(
                "Річ у тім, що when(spy.get(0)) — це звичайний Java-вираз: щоб передати "
                + "результат у when, JVM мусить СПОЧАТКУ обчислити spy.get(0). Для мока це "
                + "нешкідливо (порожній метод поверне null), а для шпигуна це реальний виклик "
                + "реального ArrayList — з реальним винятком. Форма doReturn(...).when(spy) "
                + "цього не робить: у ній виклик get(0) відбувається вже в «режимі "
                + "налаштування»."));
        uk.add(LessonBlock.note(
                "Практична порада: якщо вам захотілося взяти spy, спершу спитайте себе, чи не "
                + "простіше виділити ту частину класу, яку ви хочете підмінити, в окрему "
                + "залежність. Потреба у шпигуні майже завжди — сигнал, що клас робить дві "
                + "різні речі одночасно."));

        uk.add(LessonBlock.heading("@Mock, @InjectMocks і менше рутини"));
        uk.add(LessonBlock.paragraph(
                "Коли залежностей три-чотири, ручні виклики mock(...) у кожному тесті "
                + "набридають. Анотації Mockito прибирають цю рутину:"));
        uk.add(LessonBlock.code(
                "@ExtendWith(MockitoExtension.class)\n"
                + "class OrderServiceTest {\n"
                + "\n"
                + "    @Mock  OrderRepository repo;\n"
                + "    @Mock  EmailSender mailer;\n"
                + "    @InjectMocks OrderService service;\n"
                + "\n"
                + "    @Test\n"
                + "    void savesAndNotifies() {\n"
                + "        service.placeOrder(new Order(500, \"u@e.com\"));\n"
                + "\n"
                + "        verify(repo).save(any());\n"
                + "        verify(mailer).send(eq(\"u@e.com\"), anyString());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Розберімо по ролях. @ExtendWith(MockitoExtension.class) підключає Mockito до "
                + "життєвого циклу JUnit 5 — саме він перед кожним тестом створює свіжі моки "
                + "(так, знову ізоляція з першого уроку). @Mock каже «зроби мок цього типу й "
                + "поклади в поле». @InjectMocks створює справжній OrderService і намагається "
                + "передати наявні моки в його конструктор, зіставляючи їх за типом."));
        uk.add(LessonBlock.warning(
                "@InjectMocks мовчить, коли не може щось підставити: замість помилки поле "
                + "просто лишається null, і тест падає з NPE десь глибоко всередині. Особливо "
                + "підступний випадок — дві залежності однакового типу: Mockito зіставляє їх за "
                + "іменем поля, і при найменшій розбіжності підставить не те. Явний конструктор "
                + "new OrderService(repo, mailer) багатослівніший, але ніколи не бреше."));
        uk.add(LessonBlock.paragraph(
                "І остання, найважливіша думка уроку. Якщо для одного тесту вам потрібно шість "
                + "моків, проблема не в тесті. Шість залежностей означає, що клас знає про "
                + "шість різних частин системи, тобто має щонайменше шість причин змінитися — "
                + "пряме порушення принципу єдиної відповідальності. Складний тест — це "
                + "найчесніший відгук про дизайн, який ви коли-небудь отримаєте: він скаржиться "
                + "ще до того, як на клас почнуть скаржитися колеги."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Використайте Mockito, щоб створити заглушку для UserRepository, яка повертає конкретного користувача при пошуку за ідентифікатором 1."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void findsUserById() {\n"
                + "    UserRepository repo = mock(UserRepository.class);\n"
                + "    when(repo.findById(1L)).thenReturn(Optional.of(new User(\"Test\")));\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // test.4 — Integration testing
    // ══════════════════════════════════════════════════════════════════════

    private static void integration(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Уявіть релізний день. Усі 1 200 модульних тестів зелені, покриття 87%, збірка "
                + "проходить за сорок секунд. Ви викочуєте версію — і за десять хвилин "
                + "прилітає перша скарга: реєстрація не працює. У логах — помилка бази про "
                + "занадто довге значення в колонці. Жоден тест цього не спіймав, і жоден не "
                + "збрехав."));

        uk.add(LessonBlock.heading("Сліпа зона між зеленими тестами"));
        uk.add(LessonBlock.paragraph(
                "Щоб зрозуміти, як так вийшло, подивімося на тест, який «перевіряв» "
                + "реєстрацію. Це класичний модульний тест із моком, рівно такий, як ми писали "
                + "в попередньому уроці:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void registersUser() {\n"
                + "    UserRepository repo = mock(UserRepository.class);\n"
                + "    RegistrationService service = new RegistrationService(repo);\n"
                + "\n"
                + "    service.register(\"дуже-довге-імя-на-сорок-символів\", \"pass\");\n"
                + "\n"
                + "    verify(repo).save(any(User.class));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Цей тест абсолютно правдивий у межах своєї компетенції: сервіс справді "
                + "викликає save. Але мок save приймає ЩО ЗАВГОДНО. У справжній таблиці колонка "
                + "оголошена як VARCHAR(20), і сорок символів туди не влізуть. Мок про це не "
                + "знає — він не має жодної уяви про схему бази."));
        uk.add(LessonBlock.paragraph(
                "Це не вада конкретного тесту, а структурна властивість модульного тестування. "
                + "Мок фіксує ваше УЯВЛЕННЯ про те, як поводиться залежність. Якщо уявлення "
                + "хибне — тест підтверджує хибне уявлення й лишається зеленим. Ось де саме "
                + "виникають такі сліпі зони:"));
        uk.add(LessonBlock.list(
                "Обмеження схеми БД: довжина колонки, NOT NULL, унікальні індекси, зовнішні ключі.",
                "SQL, згенерований ORM: чи є взагалі така колонка, чи правильно названо таблицю.",
                "Транзакції: чи справді відкат скасовує зміни в реальному двигуні БД.",
                "Серіалізація JSON: чи збігаються імена полів у відповіді з тим, що чекає клієнт.",
                "Конфігурація: чи підхопився потрібний профіль, чи створився потрібний бін.",
                "HTTP-шар: маршрути, коди статусів, заголовки, валідація вхідних даних."));
        uk.add(LessonBlock.paragraph(
                "Спільне в цьому списку одне: усе це — стики між вашим кодом і чужим. "
                + "Модульний тест за визначенням стики не перевіряє, бо він навмисно їх "
                + "відрізав. Саме цю прогалину закривають інтеграційні тести."));

        uk.add(LessonBlock.heading("Скільки чого писати: піраміда"));
        uk.add(LessonBlock.paragraph(
                "Природна реакція — «тоді пишімо всі тести інтеграційними, вони ж чесніші». "
                + "Подивіться на цифри, і стане зрозуміло, чому так не роблять:"));
        uk.add(LessonBlock.table(
                "Рівень\tЧас на 1 тест\tЩо ловить\tЧого не ловить\tЧастка",
                Arrays.asList(
                        "Модульні\t1-20 мс\tлогіка, гілки, граничні значення\tвсе, що на стиках\t~70%",
                        "Інтеграційні\t0,1-5 с\tсхема БД, SQL, JSON, HTTP, конфіг\tповний шлях користувача\t~20%",
                        "E2E (UI)\t5-60 с\tчи працює сценарій цілком\tрідкісні гілки логіки\t~10%")));
        uk.add(LessonBlock.paragraph(
                "Порахуймо на реальних числах. 1 200 модульних тестів по 5 мс — це шість "
                + "секунд, і ви можете запускати їх після кожного збереження файлу. Ті самі "
                + "1 200 як інтеграційні по 2 секунди — це сорок хвилин. Такий набір ніхто не "
                + "запускатиме локально; його запускатимуть уночі, помилку побачать наступного "
                + "дня, і весь сенс швидкого зворотного зв'язку зникне."));
        uk.add(LessonBlock.paragraph(
                "Тому правило таке: КОЖНУ гілку логіки перевіряйте модульно, а інтеграційно — "
                + "лише те, що модульно перевірити неможливо в принципі. Не «зареєструвати "
                + "користувача з порожнім паролем, з коротким паролем, з паролем без цифр» "
                + "(це три модульні тести), а один інтеграційний «користувач справді "
                + "зберігається в базу і його можна прочитати назад»."));
        uk.add(LessonBlock.warning(
                "Перевернута піраміда — коли інтеграційних і E2E-тестів більше, ніж модульних — "
                + "має свою назву: «ріжок морозива». Симптоми впізнаються з першого погляду: "
                + "збірка йде 40 хвилин, тести періодично червоніють без змін у коді, і в "
                + "команді з'являється звичка «перезапустити, зазвичай минає». Саме з цієї "
                + "звички починається смерть довіри до тестів."));

        uk.add(LessonBlock.heading("Перший інтеграційний тест: @SpringBootTest"));
        uk.add(LessonBlock.paragraph(
                "У Spring інтеграційний тест починається з анотації @SpringBootTest — вона "
                + "піднімає справжній контекст застосунку з усіма бінами, конфігурацією та "
                + "з'єднанням із базою. Ось перевірка того самого репозиторію, але вже "
                + "по-справжньому:"));
        uk.add(LessonBlock.code(
                "@SpringBootTest\n"
                + "@Transactional\n"
                + "class UserRepositoryIT {\n"
                + "\n"
                + "    @Autowired UserRepository repo;\n"
                + "\n"
                + "    @Test\n"
                + "    void savesAndReadsBack() {\n"
                + "        User saved = repo.save(new User(\"oksana\", \"o@e.com\"));\n"
                + "\n"
                + "        User found = repo.findById(saved.getId()).orElseThrow();\n"
                + "        assertEquals(\"oksana\", found.getUsername());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тут немає жодного мока: repo — справжній, база — справжня. Тест перевіряє те, "
                + "чого модульний не міг: що ORM згенерував коректний INSERT, що колонки "
                + "існують, що згенерований id повернувся, що дані читаються назад тими самими."));
        uk.add(LessonBlock.paragraph(
                "@Transactional тут виконує неочевидну, але критичну роль. Кожен тест "
                + "виконується у транзакції, яку Spring ВІДКОЧУЄ після завершення — незалежно "
                + "від того, зелений тест чи червоний. Без цього перший запуск створив би "
                + "користувача oksana, а другий упав би на унікальному індексі. Це та сама "
                + "ізоляція, з якої почався урок про JUnit, тільки на рівні бази."));
        uk.add(LessonBlock.note(
                "Суфікс IT замість Test у назві класу — усталена конвенція: Maven Failsafe і "
                + "типові налаштування Gradle запускають *IT окремим повільним завданням, а "
                + "*Test — швидким. Так розробник локально ганяє тільки швидкі тести, а повний "
                + "набір іде на сервері збірки."));

        uk.add(LessonBlock.heading("MockMvc: перевірити HTTP, не піднімаючи сервер"));
        uk.add(LessonBlock.paragraph(
                "Репозиторій — не єдиний стик. Другий за частотою помилок — веб-шар: "
                + "маршрути, коди відповідей, імена полів у JSON. MockMvc дозволяє надсилати "
                + "запити в контролер через справжній Spring-конвеєр, але без реального "
                + "мережевого сокета:"));
        uk.add(LessonBlock.code(
                "@SpringBootTest\n"
                + "@AutoConfigureMockMvc\n"
                + "class UserControllerIT {\n"
                + "\n"
                + "    @Autowired MockMvc mvc;\n"
                + "\n"
                + "    @Test\n"
                + "    void rejectsEmptyUsername() throws Exception {\n"
                + "        mvc.perform(post(\"/api/users\")\n"
                + "                .contentType(MediaType.APPLICATION_JSON)\n"
                + "                .content(\"{\\\"username\\\":\\\"\\\"}\"))\n"
                + "           .andExpect(status().isBadRequest())\n"
                + "           .andExpect(jsonPath(\"$.errors[0].field\").value(\"username\"));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Розберімо, що саме перевірено. perform надсилає POST на /api/users з тілом "
                + "JSON — і якщо маршрут написано з помилкою, тест впаде з 404. Перший "
                + "andExpect вимагає код 400: спрацювала валідація. Другий заглядає в тіло "
                + "відповіді через JSONPath і перевіряє, що в помилці названо саме поле "
                + "username."));
        uk.add(LessonBlock.paragraph(
                "Останній рядок вартий окремої уваги. Формат помилки — це частина публічного "
                + "контракту вашого API: мобільний застосунок показує підказку під конкретним "
                + "полем саме за цим значенням. Перейменували поле в DTO — модульні тести "
                + "лишаться зеленими, а клієнт зламається. Цей тест ловить саме такий випадок."));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу і на екранування у рядку content: щоб передати JSON "
                + "{\"username\":\"\"}, у Java-літералі кожну лапку доводиться писати як \\\". "
                + "Це негарно, тому в реальних проєктах JSON частіше складають "
                + "ObjectMapper-ом або тримають у текстовому файлі поруч із тестом."));

        uk.add(LessonBlock.heading("H2 чи Testcontainers: як вибрати базу для тестів"));
        uk.add(LessonBlock.paragraph(
                "Лишилося головне питання: яка база стоїть за @SpringBootTest? Найпростіша "
                + "відповідь — H2, вбудована база в пам'яті. Один рядок у конфігурації, старт "
                + "за мілісекунди, жодного Docker. Спокусливо. Але згадайте, чому ми взагалі "
                + "прийшли до інтеграційних тестів: щоб перевірити код проти СПРАВЖНЬОЇ "
                + "поведінки залежності."));
        uk.add(LessonBlock.paragraph(
                "H2 у режимі сумісності з PostgreSQL поводиться «майже так само». Ось де це "
                + "«майже» стає діркою:"));
        uk.add(LessonBlock.table(
                "Поведінка\tPostgreSQL у продакшені\tH2 у режимі сумісності",
                Arrays.asList(
                        "Типи jsonb, array, enum\tпрацюють\tчастково або взагалі ні",
                        "Віконні функції, CTE\tповна підтримка\tобмежена підтримка",
                        "SELECT … FOR UPDATE\tреальне блокування рядка\tчасто ігнорується",
                        "Регістр в іменах без лапок\tзводиться до нижнього\tзалежить від налаштувань",
                        "Точний текст помилок і коди\tкоди PostgreSQL\tкоди H2 — інші",
                        "Розширення (PostGIS тощо)\tдоступні\tнедоступні")));
        uk.add(LessonBlock.paragraph(
                "Кожен рядок цієї таблиці — потенційно зелений тест і зламаний продакшен. "
                + "Найпідступніший — рядок про FOR UPDATE: ваш тест на конкурентне списання "
                + "коштів проходить, бо H2 просто проігнорував блокування, а в продакшені двоє "
                + "користувачів одночасно знімають ті самі гроші. Тест не просто не допоміг — "
                + "він дав хибну впевненість, і це гірше, ніж не мати тесту взагалі."));
        uk.add(LessonBlock.paragraph(
                "Testcontainers прибирає це джерело брехні: бібліотека піднімає справжній "
                + "Docker-контейнер із тією самою версією Postgres, що й у продакшені, а після "
                + "тестів прибирає його:"));
        uk.add(LessonBlock.code(
                "@SpringBootTest\n"
                + "@Testcontainers\n"
                + "class UserRepositoryIT {\n"
                + "\n"
                + "    @Container\n"
                + "    static PostgreSQLContainer<?> db =\n"
                + "        new PostgreSQLContainer<>(\"postgres:16-alpine\");\n"
                + "\n"
                + "    @DynamicPropertySource\n"
                + "    static void dbProps(DynamicPropertyRegistry registry) {\n"
                + "        registry.add(\"spring.datasource.url\", db::getJdbcUrl);\n"
                + "        registry.add(\"spring.datasource.username\", db::getUsername);\n"
                + "        registry.add(\"spring.datasource.password\", db::getPassword);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Розберімо дві неочевидні деталі. Перша: поле db оголошене static — і причина "
                + "та сама, що у @BeforeAll з першого уроку. Статичний контейнер піднімається "
                + "один раз на весь клас (кілька секунд), а не перед кожним тестом; зробіть "
                + "його нестатичним — і кожен тест платитиме ці секунди заново."));
        uk.add(LessonBlock.paragraph(
                "Друга: @DynamicPropertySource. Порт контейнера призначається випадково при "
                + "старті, тому записати URL у application.yml заздалегідь неможливо. Цей "
                + "метод виконується ПІСЛЯ старту контейнера, але ДО створення контексту "
                + "Spring, і встигає підставити фактичну адресу. Посилання db::getJdbcUrl "
                + "передається саме як метод, а не як значення, бо в момент реєстрації "
                + "значення ще не існує."));
        uk.add(LessonBlock.note(
                "Ціна чесності — час і залежність від Docker. Розумний компроміс, який часто "
                + "обирають команди: Testcontainers для тестів, що торкаються бази, і жодних "
                + "інтеграційних тестів там, де вистачає модульних. Якщо Docker недоступний "
                + "(наприклад, обмеження корпоративної машини), H2 краще за нічого — але тоді "
                + "свідомо тримайте в голові таблицю розбіжностей вище."));

        uk.add(LessonBlock.heading("Що врешті ловить наш початковий баг"));
        uk.add(LessonBlock.paragraph(
                "Повернімося до релізного дня з початку уроку — VARCHAR(20) і сорок символів. "
                + "Модульний тест із моком був зелений. Тест на H2 міг би бути зеленим теж, "
                + "якщо схему для нього генерує Hibernate за сутністю, а не за реальним "
                + "міграційним скриптом. А тест із Testcontainers, який піднімає базу тими "
                + "самими міграціями, що й продакшен, упаде з тією ж помилкою, що прилетіла б "
                + "від користувача — тільки на десять хвилин раніше й безкоштовно."));
        uk.add(LessonBlock.paragraph(
                "Ось у чому насправді полягає ідея рівнів тестування. Питання не в тому, який "
                + "рівень «кращий». Питання в тому, ЯКОГО РОДУ помилку ви боїтеся найбільше в "
                + "конкретному місці — і чи є у вас тест, здатний її побачити."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть інтеграційний тест з Testcontainers для перевірки збереження даних в реальній базі PostgreSQL."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@SpringBootTest\n"
                + "@Testcontainers\n"
                + "class DatabaseTest {\n"
                + "    @Container\n"
                + "    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(\"postgres:15\");\n"
                + "}"));
    }
}
