package project.cspromotions.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.cspromotions.domain.Product;
import project.cspromotions.service.ProductService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/fetch-from-external")
    public String fetchProductsFromExternal(Model model) {
        try {
            String url = "https://pyony.com/brands/cu/";
            Document doc = Jsoup.connect(url).get();
            System.out.println(doc);
            Elements productElements = doc.select(".prodList li");

            List<Product> products = new ArrayList<>();

            for (Element productElement : productElements) {
                String productName = productElement.select(".prodName").text();
                String productDescription = productElement.select(".prodDescription").text();
                double productPrice = Double.parseDouble(productElement.select(".prodPrice").text().replaceAll("[^\\d.]", ""));

                Product product = new Product();
                product.setName(productName);
                product.setDescription(productDescription);
                product.setPrice(productPrice);

                products.add(product);

                // 기존에 저장된 제품인지 확인하고 저장
                productService.saveOrUpdateProduct(product);
            }

            model.addAttribute("message", "Products fetched successfully from external site.");
            model.addAttribute("products", products);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error fetching products from external site.");
        }

        return "product/fetch-result";
    }
}
