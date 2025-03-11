package configuretions;

import data.User;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BaseClass {
    protected WebDriver driver;

    public BaseClass(WebDriver driver) {
        this.driver = driver;
    }


    public void LoginTT(User user) throws InterruptedException {
        Thread.sleep(2000);
        WebElement usernameInput = driver.findElement(By.xpath("//*[@id=\"mat-input-0\"]"));
        WebElement passwordInput = driver.findElement(By.xpath("//*[@id=\"mat-input-1\"]"));

        usernameInput.sendKeys(user.getName());
        passwordInput.sendKeys(user.getPassword());

        passwordInput.sendKeys(Keys.ENTER);
        Thread.sleep(700);
    }

}