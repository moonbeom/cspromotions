package project.cspromotions.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.cspromotions.domain.Product;
import project.cspromotions.domain.SevenEleven;
import project.cspromotions.service.SevenElevenService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
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

    private String[] classNames = {
            ".tab_layer04 li:nth-child(1) a",
            ".tab_layer04 li:nth-child(2) a",
            ".tab_layer04 li:nth-child(3) a",
            ".tab_layer04 li:nth-child(4) a"
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
            model.addAttribute("message", "데이터 성공적으로 가져와 저장했습니다.");
            model.addAttribute("products", productList);

            // URL로 데이터 조회
            List<SevenEleven> dataByUrl = sevenElevenService.findByUrl("https://www.7-eleven.co.kr/product/presentList.asp");
            System.out.println("조회된 데이터 개수: " + dataByUrl.size());
        } catch (Exception e) {
            e.printStackTrace();
            // 에러 메시지 전송
            model.addAttribute("error", "데이터 가져오기 및 저장 중 오류가 발생했습니다.");
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
            List<Product> eventProducts = event(driver, classNames[i], event[i], connection, i + 1);
            System.out.println("Event " + event[i] + " products: " + eventProducts);
            productList.addAll(eventProducts);
        }

        // 데이터베이스에 데이터 저장
        saveDataToDatabase(productList);

        return productList;
    }


    private List<Product> event(WebDriver driver, String className, String event, Connection connection, int index) {
        List<Product> eventDataList = new ArrayList<>();

        // 해당 이벤트 페이지로 이동
        driver.get("https://www.7-eleven.co.kr/product/presentList.asp");

        // 대기 시간 추가 (페이지가 완전히 로딩될 때까지 기다리기)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(className)));

        // JavaScript를 사용하여 각 탭을 클릭
        ((JavascriptExecutor) driver).executeScript("fncTab('" + (index + 1) + "');");

        // 대기 시간 추가 (탭이 전환될 때까지 기다리기)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tab_layer04 li.on")));

        // 데이터 가져오기
        Document doc = Jsoup.parse(driver.getPageSource());
        Elements listItems = doc.select("#listUl li");

        for (Element listItem : listItems) {
            String title = listItem.select(".img_list_tit_02").text();
            if (title.equals(event)) {
                Elements tagList = listItem.select(".tag_list_01 li");
                Elements picProduct = listItem.select(".pic_product img");

                for (int i = 0; i < tagList.size(); i++) {
                    Product product = new Product();
                    product.setEvent(event);
                    product.setName(tagList.get(i).text());

                    // 특정 HTML 구조에 따라 속성을 조정해야 할 수 있습니다.
                    product.setImg(picProduct.get(i).attr("src"));
                    eventDataList.add(product);
                }
                break; // 일치하는 이벤트를 찾은 후 루프를 종료합니다.
            }
        }
        return eventDataList;
    }

    private void saveDataToDatabase(List<Product> eventDataList) {
        System.out.println("데이터를 데이터베이스에 저장 중...");
        sevenElevenService.saveAll(eventDataList);
        System.out.println("데이터 저장 성공.");
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
