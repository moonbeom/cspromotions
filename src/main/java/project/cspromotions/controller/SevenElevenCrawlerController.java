package project.cspromotions.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.cspromotions.domain.Product;
import project.cspromotions.domain.SevenEleven;
import project.cspromotions.service.SevenElevenService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SevenElevenCrawlerController {

    private final SevenElevenService sevenElevenService;

    @Autowired
    public SevenElevenCrawlerController(SevenElevenService sevenElevenService) {
        this.sevenElevenService = sevenElevenService;
    }

    private String[] event = {"1+1", "2+1", "증정행사", "할인행사"};

    private String[] xpath = {
            "//*[@id='actFrm']/div[3]/div[1]/ul/li[1]/a",
            "//*[@id='actFrm']/div[3]/div[1]/ul/li[2]/a",
            "//*[@id='actFrm']/div[3]/div[1]/ul/li[3]/a",
            "//*[@id='actFrm']/div[3]/div[1]/ul/li[4]/a"
    };

    @GetMapping("/crawl-and-store")
    public String crawlAndStoreData(Model model) {
        WebDriver driver = null;
        Connection connection = null;

        try {
            // 크롤링 및 데이터베이스 저장 수행
            driver = initializeChromeDriver();
            connection = initializeDatabase();

            // 데이터 가져오기
            List<Product> productList = crawlAndStore(driver, connection);

            // 성공 메시지 및 데이터 전송
            model.addAttribute("message", "Data fetched and stored successfully.");
            model.addAttribute("products", productList);
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

    private List<Product> crawlAndStore(WebDriver driver, Connection connection) {
        List<Product> productList = new ArrayList<>();

        for (int i = 0; i < event.length; i++) {
            List<Product> eventProducts = event(driver, xpath[i], event[i], connection);
            productList.addAll(eventProducts);
        }

        // 데이터베이스에 데이터 저장
        saveDataToDatabase(connection, productList);

        return productList;
    }

    private List<Product> event(WebDriver driver, String url, String event, Connection connection) {
        List<Product> eventDataList = new ArrayList<>();

        // 데이터 가져오기
        Document doc = Jsoup.parse(driver.getPageSource());
        Elements prodNameList = doc.select(".name");
        Elements prodPriceList = doc.select(".price > span");
        Elements prodImgList = doc.select("div.pic_product > img");

        int length = Math.min(Math.min(prodImgList.size(), prodNameList.size()), prodPriceList.size());

        for (int i = 0; i < length; i++) {
            Product product = new Product();
            product.setEvent(event);
            product.setName(prodNameList.get(i).text());
            product.setPrice(Double.parseDouble(prodPriceList.get(i).text()));
            product.setImg(prodImgList.get(i).attr("src"));
            eventDataList.add(product);
        }

        return eventDataList;
    }

    private void saveDataToDatabase(Connection connection, List<Product> eventDataList) {
        sevenElevenService.saveAll(eventDataList);
    }

    private WebDriver initializeChromeDriver() {
        // 크롬드라이버 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:/java/chromedriver-win64/chromedriver.exe");

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
}
