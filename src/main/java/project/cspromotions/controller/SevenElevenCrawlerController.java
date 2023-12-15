package project.cspromotions.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.cspromotions.service.ProductService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SevenElevenCrawlerController {
    @Autowired
    private ProductService productService;

    @GetMapping("/crawl-and-store")
    public String crawlAndStoreData(Model model) {
        WebDriver driver = null;
        Connection connection = null;

        try {
            // 크롤링 및 데이터베이스 저장 수행
            driver = initializeChromeDriver();
            connection = initializeDatabase();

            // 성공 메시지 전송
            crawlAndStore(driver, connection);
            model.addAttribute("message", "Data fetched and stored successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            // 에러 메시지 전송
            model.addAttribute("error", "Error fetching and storing data.");
        } finally {
            // 자원 정리
            if (driver != null) {
                driver.quit();
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return "result-page";
    }

    private WebDriver initializeChromeDriver() {
        // 크롬드라이버 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:\\java\\chromedriver-win64\\chromedriver.exe");

        // 크롬 옵션 설정 (headless 모드로 실행)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        // WebDriver 생성
        return new ChromeDriver(options);
    }

    private Connection initializeDatabase() throws SQLException {
        // H2 데이터베이스를 사용하도록 변경
        String jdbcUrl = "jdbc:h2:mem:testdb";
        String username = "sa";
        String password = "password";

        // 데이터베이스 연결
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void crawlAndStore(WebDriver driver, Connection connection) throws Exception {
        // 세븐 페이지 열기
        String url = "https://www.7-eleven.co.kr/product/presentList.asp";
        driver.get(url);
        Thread.sleep(2000);

        // 이벤트 실행
        elevenEvent(driver, connection);
    }

    private void elevenEvent(WebDriver driver, Connection connection) throws Exception {
        // 이벤트 정보 설정
        String[] xpath = {
                "//*[@id='actFrm']/div[3]/div[1]/ul/li[1]/a",
                "//*[@id='actFrm']/div[3]/div[1]/ul/li[2]/a",
                "//*[@id='actFrm']/div[3]/div[1]/ul/li[3]/a",
                "//*[@id='actFrm']/div[3]/div[1]/ul/li[4]/a"
        };
        String[] event = {"1+1", "2+1", "증정행사", "할인행사"};

        for (int i = 0; i < event.length; i++) {
            event(driver, xpath[i], event[i], connection);
        }
    }

    private void event(WebDriver driver, String url, String event, Connection connection) throws Exception {
        // 데이터 초기화
        Map<String, List<String>> eventData = new LinkedHashMap<>();

        // 스크롤 끝까지 내리기
        ((ChromeDriver) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        // 이벤트 클릭
        WebElement element = driver.findElement(By.xpath(url));
        ((ChromeDriver) driver).executeScript("arguments[0].click();", element);

        // 더보기 클릭
        while (true) {
            try {
                driver.findElement(By.className("btn_more")).click();
                Thread.sleep(2000);
            } catch (org.openqa.selenium.NoSuchElementException e) {
                break;
            }
        }

        // 데이터 가져오기
        Document doc = Jsoup.parse(driver.getPageSource());
        Elements prodNameList = doc.select(".name");
        Elements prodPriceList = doc.select(".price > span");
        Elements prodImgList = doc.select("div.pic_product > img");

        int length = Math.min(Math.min(prodImgList.size(), prodNameList.size()), prodPriceList.size());

        for (int i = 0; i < length; i++) {
            eventData.computeIfAbsent("name", k -> new ArrayList<>()).add(prodNameList.get(i).text());
            eventData.computeIfAbsent("price", k -> new ArrayList<>()).add(prodPriceList.get(i).text());
            eventData.computeIfAbsent("img", k -> new ArrayList<>()).add(prodImgList.get(i).attr("src"));
        }

        // 데이터베이스에 데이터 저장
        saveDataToDatabase(connection, event, eventData);
    }

    private void saveDataToDatabase(Connection connection, String event, Map<String, List<String>> eventData) throws SQLException {
        // SQL 쿼리
        String sql = "INSERT INTO SEVENELEVEN (brand, event, name, price, img) VALUES ('7ELEVEN', ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            List<String> nameList = eventData.get("name");
            List<String> priceList = eventData.get("price");
            List<String> imgList = eventData.get("img");

            int size = nameList.size();

            for (int i = 0; i < size; i++) {
                preparedStatement.setString(1, event);
                preparedStatement.setString(2, nameList.get(i));
                preparedStatement.setString(3, priceList.get(i));
                preparedStatement.setString(4, imgList.get(i));
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
