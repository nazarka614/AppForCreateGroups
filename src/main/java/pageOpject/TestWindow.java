package pageOpject;

import configuretions.BaseClass;
import data.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class TestWindow extends JFrame {

    private JButton startButton;
    private JButton stopButton; // Кнопка "Прервать тест"
    private JTextArea textArea;
    private WebDriver driver;
    private CreateGroup createGroup;
    private Thread testThread; // Поток для выполнения теста

    public TestWindow() {
        // Настройка окна
        setTitle("Test Runner");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Создание текстового поля
        textArea = new JTextArea();
        textArea.setEditable(true); // Разрешаем редактирование

        // Создание кнопки "Добавить шаблон"
        JButton addTemplateButton = new JButton("Добавить шаблон");
        addTemplateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Добавляем пример курса в текстовое поле
                textArea.append("Програмування Python для школярів, 9700, 15.04.25, 1\n");
            }
        });

        // Создание кнопки "Запустить тест"
        startButton = new JButton("Запустить тест");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runTest();
            }
        });

        // Создание кнопки "Прервать тест"
        stopButton = new JButton("Прервать тест");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (testThread != null && testThread.isAlive()) {
                    testThread.interrupt(); // Прерываем поток
                    textArea.append("Тест прерван пользователем.\n");
                    System.out.println("Тест прерван пользователем.");
                }
            }
        });

        // Добавление компонентов на окно
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(textArea));

        // Панель для кнопок
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addTemplateButton);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton); // Добавляем кнопку "Прервать тест"

        panel.add(buttonPanel);
        add(panel);
    }

    private List<CourseData> parseCourses() {
        List<CourseData> courses = new ArrayList<>();
        String text = textArea.getText(); // Получаем текст из текстового поля
        String[] lines = text.split("\n"); // Разделяем текст на строки

        for (String line : lines) {
            String[] parts = line.split(", "); // Разделяем строку на части
            if (parts.length == 4) {
                String courseName = parts[0];
                String sum = parts[1];
                String startDate = parts[2];
                int day = Integer.parseInt(parts[3]);
                courses.add(new CourseData(courseName, sum, startDate, day));
            }
        }

        return courses;
    }

    private void runTest() {
        testThread = new Thread(() -> {
            try {
                // Парсим курсы из текстового поля
                List<CourseData> courses = parseCourses();
                System.out.println("Курсы успешно распарсены: " + courses.size() + " курсов.");

                // Проверяем, что список курсов не пуст
                if (courses.isEmpty()) {
                    System.out.println("Список курсов пуст. Тест завершен.");
                    textArea.append("Test completed successfully! (No courses to process)\n");
                    return; // Завершаем выполнение, если курсов нет
                }

                // Инициализация драйвера
                driver = new ChromeDriver();
                System.out.println("Драйвер успешно инициализирован.");
                driver.manage().window().maximize();
                System.out.println("Браузер открыт на весь экран.");
                driver.get("https://tt.hillel.it/login");
                System.out.println("Переход на страницу логина выполнен.");

                // Инициализация CreateGroup с передачей драйвера
                createGroup = new CreateGroup(driver);
                System.out.println("CreateGroup успешно инициализирован.");

                // Запуск теста для каждого курса
                for (CourseData course : courses) {
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Тест прерван.");
                        textArea.append("Тест прерван.\n");
                        break; // Выходим из цикла, если поток прерван
                    }

                    try {
                        System.out.println("Обработка курса: " + course.getCourseName());
                        createGroup.enterLoginAndPassword(new User(), course);
                        System.out.println("Курс успешно обработан: " + course.getCourseName());
                    } catch (Exception e) {
                        System.out.println("Ошибка при обработке курса: " + course.getCourseName());
                        e.printStackTrace();
                    }
                }

                // Выводим результат в текстовое поле
                if (!Thread.currentThread().isInterrupted()) {
                    textArea.append("Test completed successfully!\n");
                    System.out.println("Тест завершен успешно.");
                }
            } catch (Exception e) {
                textArea.append("Test failed: " + e.getMessage() + "\n");
                System.out.println("Тест завершен с ошибкой: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Не закрываем драйвер, чтобы браузер остался открытым
                System.out.println("Тест завершен. Браузер остается открытым.");
            }
        });

        testThread.start(); // Запускаем поток
    }

    public static void main(String[] args) {
        // Запуск окна в потоке обработки событий
        SwingUtilities.invokeLater(() -> {
            TestWindow window = new TestWindow();
            window.setVisible(true);
        });
    }


    public static class CourseData {
        private String courseName; // Название курса
        private String sum; // Сумма курса
        private String startDate; // Дата начала курса (в формате "дд.Мм.гггг")
        private int day; // 0 означает понедельник и четверг, 1 означает вторник и пятница

        public CourseData(String courseName, String sum, String startDate, int day) {
            this.courseName = courseName;
            this.sum = sum;
            this.startDate = startDate;
            this.day = day;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getSum() {
            return sum;
        }

        public String getStartDate() {
            return startDate;
        }

        public int getDay() {
            return day;
        }
    }

    public static class CreateGroup extends BaseClass {
        public CreateGroup(WebDriver driver) {
            super(driver); // Передаем driver в BaseClass
        }

        public void enterLoginAndPassword(User user, CourseData courseData) throws AWTException, InterruptedException {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            LoginTT(user); // Теперь driver не null

            // Используем данные из courseData
            String courseName = courseData.getCourseName();
            String sum = courseData.getSum();
            String startDate = courseData.getStartDate();
            int day = courseData.getDay();

            // Переход на страницу расписания
            Thread.sleep(2000);
            driver.get("https://tt.hillel.it/schedule/6223342596442367a40972a6?date=18.12.2023&status=active");

            // Открытие меню для создания группы
            waitForElementAndClick(wait, By.xpath("//mat-icon[contains(@class, 'layout__menu-icon--add') and contains(text(), 'add_circle')]"));
            waitForElementAndClick(wait, By.xpath("//span[contains(@class, 'layout__menu-mat-text--add') and text()=' групу ']"));
            waitForElementAndClick(wait, By.xpath("//mat-icon[contains(@class, 'layout__menu-icon') and contains(text(), 'menu')]"));
            Thread.sleep(2000);

            // Выбор курса и ввод стоимости
            waitForElementAndClick(wait, By.xpath("//*[@id=\"mat-select-14\"]"));
            waitForElementAndClick(wait, By.xpath("//span[text()=' " + courseName + " ']"));
            waitForElementAndSendKeys(wait, By.id("mat-input-5"), sum);

            // Ввод даты
            waitForElementAndSendKeys(wait, By.id("mat-input-4"), startDate);

            for (int i = 0; i < 2; i++) {
                // Открываем выпадающее меню выбора дня недели
                WebElement dayOfWeekElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"mat-select-8\"]")));
                dayOfWeekElement.click();

                // Выбор нужного дня недели
                WebElement dayOption = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(getDayOptionXpath(day, i))));
                dayOption.click();

                // Ввод времени
                WebElement inputElement3 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mat-input-7")));
                inputElement3.clear();
                inputElement3.sendKeys("19:15");
                Thread.sleep(1000);

                // Выбор аудитории
                WebElement audit = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//mat-select[@formcontrolname='classe']")));
                audit.click();
                Thread.sleep(1000);

                List<WebElement> options = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@role='listbox']//mat-option")));

                // Выбираем случайный элемент
                if (!options.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(options.size());
                    WebElement randomOption = options.get(randomIndex);
                    String selectedOptionText = randomOption.getText().trim();

                    randomOption.click();
                    System.out.println("Выбрана случайная опция: " + selectedOptionText);
                } else {
                    throw new NoSuchElementException("Список опций пуст");
                }
                Thread.sleep(1000);

                // Выбор опции "Викладач"
                selectOptionByText(wait, "//*[@id=\"mat-select-value-13\"]", "Викладач");

                // Нажатие на кнопку "Додати"
                clickButtonWithWait(wait, "//button[contains(., ' Додати ')]");

                // Сохранение данных и поиск курса на второй итерации
                if (i == 1) {
                    saveDataAndSearch(wait, courseName, startDate);
                }
            }
        }

        private void waitForElementAndSendKeys(WebDriverWait wait, By locator, String text) {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
        }

        private void waitForElementAndClick(WebDriverWait wait, By locator) {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        }

        private String getDayOptionXpath(int day, int index) {
            if (day == 0) {
                return index == 0 ? "//mat-option[@id='mat-option-23']" : "//mat-option[@id='mat-option-26']";
            } else {
                return index == 0 ? "//mat-option[@id='mat-option-24']" : "//mat-option[@id='mat-option-27']";
            }
        }

        private void selectOptionByText(WebDriverWait wait, String selectXPath, String optionText) {
            WebElement placeholder = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selectXPath)));
            placeholder.click();

            WebElement option = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//mat-option/span[text()=' " + optionText + " ']")));
            option.click();
        }

        private void clickButtonWithWait(WebDriverWait wait, String buttonXPath) throws InterruptedException {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(buttonXPath)));
            button.click();
            Thread.sleep(1000);  // Небольшая задержка для стабильности после нажатия
        }

        private void saveDataAndSearch(WebDriverWait wait, String course, String date) throws InterruptedException {
            WebElement saveDataButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()=' Зберегти дані ']")));
            saveDataButton.click();

            // Ожидание завершения процесса сохранения данных
            wait.until(ExpectedConditions.invisibilityOf(saveDataButton));

            WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[contains(@class, 'mat-mdc-input-element') and @placeholder='Курс, викладач']\n")));
            searchInput.clear();
            searchInput.sendKeys(course);
            Thread.sleep(1000);

            // Ждем, пока список загрузится
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='cdk-overlay-5']")));
            Thread.sleep(900);

            // Находим все элементы списка
            List<WebElement> options = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//mat-optgroup[@role='group']//mat-option\n")));

            Thread.sleep(900);

            // Ищем нужный элемент по тексту и кликаем на него
            boolean courseFound = false;
            for (WebElement option : options) {
                if (option.getText().trim().equals(course)) {
                    option.click();
                    courseFound = true;
                    break;
                }
            }

            // Если элемент не найден, выбрасываем исключение
            if (!courseFound) {
                throw new NoSuchElementException("Курс '" + course + "' не найден в списке");
            }

            Thread.sleep(500);

            // Ждем, пока таблица с группами загрузится
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody/tr/td[contains(@class, 'mdc-data-table__cell')]\n")));
            Thread.sleep(800);

            // Находим все строки таблицы
            List<WebElement> rows = driver.findElements(By.xpath("//tbody[@role='rowgroup']//tr[contains(@class, 'table__row')]\n"));
            Thread.sleep(800);

            // Ищем нужную группу по названию курса и дате старта
            boolean groupFound = false;
            for (WebElement row : rows) {
                try {
                    WebElement courseElement = row.findElement(By.xpath(".//td[contains(@class, 'cdk-column-course_title')]//a"));
                    WebElement dateElement = row.findElement(By.xpath(".//td[contains(@class, 'cdk-column-start_date')]"));
                    Thread.sleep(1000);
                    String courseName = courseElement.getText().trim();
                    String startDate = dateElement.getText().trim();

                    if (courseName.equals(course) && startDate.equals(date)) {
                        // Нашли нужную группу, выводим URL в консоль
                        String groupUrl = courseElement.getAttribute("href");
                        System.out.println("Курс: " + courseName + "\nДата старту: " + startDate + "\nURL группы: " + groupUrl);

                        groupFound = true;
                        Thread.sleep(1000);
                        break;
                    }
                } catch (NoSuchElementException e) {
                    // Если элементы в строке не найдены, продолжаем с следующей строки
                    System.out.println("Не удалось найти элементы в текущей строке таблицы");
                }
            }

            // Если группа не найдена, выбрасываем исключение
            if (!groupFound) {
                throw new NoSuchElementException("Группа для курса '" + course + "' с датой старта '" + date + "' не найдена");
            }
        }
    }
}