package project.cspromotions.controller.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductForm {
    private String name;
    private String description;
    private String price;
    private MultipartFile imageFile;  // 이미지를 처리하기 위한 필드


}
