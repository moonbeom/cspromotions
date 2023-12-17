package project.cspromotions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cspromotions.domain.SevenEleven;

import java.util.List;


public interface SevenElevenRepository extends JpaRepository<SevenEleven, Long> {
    List<SevenEleven> findByUrl(String url);
}

