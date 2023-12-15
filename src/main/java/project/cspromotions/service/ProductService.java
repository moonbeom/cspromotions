package project.cspromotions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.cspromotions.domain.Product;
import project.cspromotions.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    public void saveOrUpdateProduct(Product product) {
        productRepository.save(product);
    }
}
