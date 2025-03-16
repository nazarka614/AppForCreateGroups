package pageOpject;

import configuretions.BaseClass;
import data.CourseManager;
import data.User;
import org.jdesktop.swingx.JXDatePicker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import org.openqa.selenium.TimeoutException;

public class TestWindow extends JFrame {

    private JButton startButton;
    private JButton stopButton;
    private JTextArea textArea;
    private WebDriver driver;
    private CreateGroup createGroup;
    private Thread testThread;

    // Поля для ввода данных курса
    private JComboBox<String> courseNameComboBox;
    private CourseManager courseManager;
    private JTextField sumTextField;
    private JXDatePicker datePicker; // Заменяем JTextField на JXDatePicker
    private JComboBox<String> dayComboBox;
    private JButton addCourseButton;
    private JButton clearAllButton; // Кнопка "Очистить всё"
    private static JComboBox<String> unitComboBox;

    private Image backgroundImage;

    public TestWindow() {
        courseManager = new CourseManager();

        // Загрузка курсов из файла
        courseManager.loadCoursesFromFile("src/main/resources/courses.txt");

        // Получение списка курсов
        List<String> courseNames = courseManager.getCourseNames();

        // Загрузка изображения для фона
        try {
            backgroundImage = ImageIO.read(new File("src/main/resources/hillel.jpg")); // Укажите путь к изображению
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось загрузить изображение фона.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Инициализация JComboBox с загруженными данными
        setTitle("Test Runner");
        setSize(600, 500); // Увеличиваем размер окна для удобства
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Создание панели с фоном
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    // Растягиваем изображение на всю панель
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout()); // Используем BorderLayout для удобного размещения компонентов

        // Инициализация JTextArea с увеличенным размером
        textArea = new JTextArea();
        textArea.setEditable(true);
        textArea.setLineWrap(true); // Перенос строк
        textArea.setWrapStyleWord(true); // Перенос по словам

        // Установка предпочтительного размера (ширина и высота)
        textArea.setPreferredSize(new Dimension(500, 150)); // Ширина 500, высота 150 (примерно 7 строк)

        // Добавление JTextArea в JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Всегда показывать вертикальную полосу прокрутки
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Горизонтальная прокрутка, если нужно

        // Инициализация компонентов для ввода данных курса
        courseNameComboBox = new JComboBox<>(courseNames.toArray(new String[0]));
        sumTextField = new JTextField(15); // Увеличиваем ширину поля ввода

        // Инициализация JXDatePicker для выбора даты
        datePicker = new JXDatePicker();
        datePicker.setFormats(new SimpleDateFormat("dd.MM.yy")); // Формат даты: дд.мм.гг
        datePicker.setDate(new Date()); // Установка текущей даты по умолчанию

        dayComboBox = new JComboBox<>(new String[]{"Понедельник и четверг", "Вторник и пятница"});
        addCourseButton = new JButton("Добавить курс");

        // Кнопка "Очистить всё"
        clearAllButton = new JButton("Очистить всё");
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllCourses();
            }
        });

        // Обработчик для кнопки "Добавить курс"
        addCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCourse();
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
                    testThread.interrupt();
                }
            }
        });

        unitComboBox = new JComboBox<>(new String[]{"u1", "u3", "u4", "Hillel IT School"});

        // Панель для ввода данных курса
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setOpaque(false); // Делаем панель прозрачной, чтобы был виден фон

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Отступы между компонентами
        gbc.anchor = GridBagConstraints.WEST; // Выравнивание по левому краю

        // Добавление компонентов с отступами
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Название курса:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(courseNameComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Цена:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(sumTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Дата начала:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(datePicker, gbc); // Добавляем JXDatePicker вместо JTextField

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("День недели:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(dayComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Unit:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(unitComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2; // Растягиваем на две колонки
        gbc.anchor = GridBagConstraints.CENTER; // Выравнивание по центру
        inputPanel.add(addCourseButton, gbc);

        // Панель для кнопок управления тестом
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Отступы между кнопками
        buttonPanel.setOpaque(false); // Делаем панель прозрачной, чтобы был виден фон
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearAllButton);

        // Основная панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false); // Делаем панель прозрачной, чтобы был виден фон
        mainPanel.add(scrollPane); // Добавляем JScrollPane с JTextArea
        mainPanel.add(inputPanel);
        mainPanel.add(buttonPanel);

        // Добавление основной панели на панель с фоном
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);

        // Добавление панели с фоном в окно
        add(backgroundPanel);
    }

    private void addCourse() {
        String courseName = (String) courseNameComboBox.getSelectedItem();
        String sum = sumTextField.getText();
        String startDate = new SimpleDateFormat("dd.MM.yy").format(datePicker.getDate()); // Получаем дату из JXDatePicker
        String day = (String) dayComboBox.getSelectedItem();

        // Проверка заполнения полей
        if (courseName.isEmpty() || sum.isEmpty() || startDate.isEmpty() || day.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Преобразование дня недели в числовой формат
        int dayValue = day.equals("Понедельник и четверг") ? 0 : 1;

        // Добавление курса в текстовое поле
        textArea.append(courseName + ", " + sum + ", " + startDate + ", " + dayValue + "\n");

        // Очистка полей после добавления
        sumTextField.setText("");
        datePicker.setDate(new Date()); // Сброс даты на текущую
    }

    private void clearAllCourses() {
        // Очистка текстового поля
        textArea.setText("");
    }

    private List<CourseData> parseCourses() {
        List<CourseData> courses = new ArrayList<>();
        String text = textArea.getText();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String[] parts = line.split(", ");
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
                List<CourseData> courses = parseCourses();
                System.out.println("Курсы успешно распарсены: " + courses.size() + " курсов.");

                if (courses.isEmpty()) {
                    System.out.println("Список курсов пуст. Тест завершен.");
                    return;
                }

                driver = new ChromeDriver();
                System.out.println("Драйвер успешно инициализирован.");
                driver.manage().window().maximize();
                System.out.println("Браузер открыт на весь экран.");
                driver.get("https://tt.hillel.it/login");
                System.out.println("Переход на страницу логина выполнен.");

                createGroup = new CreateGroup(driver);
                System.out.println("CreateGroup успешно инициализирован.");

                for (CourseData course : courses) {
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Тест прерван.");
                        textArea.append("Тест прерван.\n");
                        break;
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

                if (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Тест завершен успешно.");
                }
            } catch (Exception e) {
                System.out.println("Тест завершен с ошибкой: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("Тест завершен. Браузер остается открытым.");
            }
        });

        testThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TestWindow window = new TestWindow();
            window.setVisible(true);
        });
    }

    public static class CourseData {
        private String courseName;
        private String sum;
        private String startDate;
        private int day;

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
            super(driver);
        }

        public void enterLoginAndPassword(User user, CourseData courseData) throws AWTException, InterruptedException {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            WebDriverWait wait1 = new WebDriverWait(driver, 2);

            // Попытка ввести имя пользователя
            try {
                WebElement usernameField = wait1.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='mat-input-0']")));
                usernameField.sendKeys(user.getName());
            } catch (TimeoutException e) {
                System.out.println("Поле ввода имени пользователя не найдено. Пропускаем.");
            }

            // Попытка ввести пароль
            try {
                WebElement passwordField = wait1.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='mat-input-1']")));
                passwordField.sendKeys(user.getPassword());
            } catch (TimeoutException e) {
                System.out.println("Поле ввода пароля не найдено. Пропускаем.");
            }

            // Попытка нажать кнопку входа
            try {
                WebElement loginButton = wait1.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
                loginButton.click();
            } catch (TimeoutException e) {
                System.out.println("Кнопка входа не найдена. Пропускаем.");
            }

            // Используем данные из courseData
            String courseName = courseData.getCourseName();
            String sum = courseData.getSum();
            String startDate = courseData.getStartDate();
            int day = courseData.getDay();

            // Переход на страницу расписания
            Thread.sleep(2000);
            String selectedUnit = (String) unitComboBox.getSelectedItem();

            // Выбор URL в зависимости от выбранного Unit
            String scheduleUrl;
            switch (selectedUnit) {
                case "u1":
                    scheduleUrl = "https://tt.hillel.it/schedule/62bc7f3e66a30457bf950e15?date=10.03.2025&status=active";
                    driver.get(scheduleUrl);
                    break;
                case "u3":
                    scheduleUrl = "https://tt.hillel.it/schedule/62bc7f9966a30457bf950f38?date=10.03.2025&status=active";
                    driver.get(scheduleUrl);
                    break;
                case "u4":
                    scheduleUrl = "https://tt.hillel.it/schedule/62bc7fb066a30457bf950f89?date=10.03.2025&status=active";
                    driver.get(scheduleUrl);
                    break;
                case "Hillel IT School":
                    scheduleUrl = "https://tt.hillel.it/schedule/6223342596442367a40972a6?date=18.12.2023&status=active";
                    driver.get(scheduleUrl);
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный Unit: " + selectedUnit);
            }

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