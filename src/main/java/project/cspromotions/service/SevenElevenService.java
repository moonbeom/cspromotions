package project.cspromotions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.cspromotions.domain.Product;
import project.cspromotions.domain.SevenEleven;
import project.cspromotions.repository.SevenElevenRepository;

import java.util.ArrayList;
import java.util.List;
@Service
public class SevenElevenService {

    private final SevenElevenRepository sevenElevenRepository;

    @Autowired
    public SevenElevenService(SevenElevenRepository sevenElevenRepository) {
        this.sevenElevenRepository = sevenElevenRepository;
    }

    public void saveAll(List<Product> eventDataList) {
        List<SevenEleven> sevenElevenList = convertToSevenElevenList(eventDataList);
        sevenElevenRepository.saveAll(sevenElevenList);
    }

    private List<SevenEleven> convertToSevenElevenList(List<Product> productList) {
        List<SevenEleven> sevenElevenList = new ArrayList<>();

        for (Product product : productList) {
            SevenEleven sevenEleven = new SevenEleven();
            sevenEleven.setBrand("7ELEVEN");
            sevenEleven.setEvent(product.getEvent());
            sevenEleven.setName(product.getName());
            sevenEleven.setPrice(product.getPrice());
            sevenEleven.setImg(product.getImg());
            sevenEleven.setUrl("https://www.7-eleven.co.kr/product/presentList.asp");  // 실제 세븐일레븐 URL을 여기에 설정
            sevenElevenList.add(sevenEleven);
        }

        return sevenElevenList;
    }

    public List<SevenEleven> getAllData() {
        return sevenElevenRepository.findAll();
    }
    public List<SevenEleven> findByUrl(String url) {
        return sevenElevenRepository.findByUrl(url);
    }
}
