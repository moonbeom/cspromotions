package project.cspromotions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cspromotions.domain.Promotion;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
