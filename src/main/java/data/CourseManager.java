package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseManager {
    private List<String> courseNames;

    public CourseManager() {
        this.courseNames = new ArrayList<>();
    }

    /**
     * Загружает названия курсов из файла.
     *
     * @param filePath путь к файлу с названиями курсов
     */
    public void loadCoursesFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    courseNames.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    /**
     * Возвращает список названий курсов.
     *
     * @return список названий курсов
     */
    public List<String> getCourseNames() {
        return courseNames;
    }

    /**
     * Добавляет новое название курса в список.
     *
     * @param courseName название курса
     */
    public void addCourse(String courseName) {
        if (courseName != null && !courseName.trim().isEmpty()) {
            courseNames.add(courseName.trim());
        }
    }

    /**
     * Удаляет название курса из списка.
     *
     * @param courseName название курса
     */
    public void removeCourse(String courseName) {
        courseNames.remove(courseName);
    }
}