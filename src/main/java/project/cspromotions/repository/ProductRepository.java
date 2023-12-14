package project.cspromotions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cspromotions.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
