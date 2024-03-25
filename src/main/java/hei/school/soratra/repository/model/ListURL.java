package hei.school.soratra.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ListURL {
    private String original_url;
    private String transformed_url;
}