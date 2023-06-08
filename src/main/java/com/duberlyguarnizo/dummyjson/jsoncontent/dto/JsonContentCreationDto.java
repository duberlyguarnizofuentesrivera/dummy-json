package com.duberlyguarnizo.dummyjson.jsoncontent.dto;

import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContent;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * DTO for {@link JsonContent}
 */
@Value
public class JsonContentCreationDto implements Serializable {
    Long id;
    @NotBlank
    String name;
    @NotBlank
    @Length(message = "Your JSON response is not the right size!", min = 3, max = 2048)
    String json;
    @URL(protocol = "")
    String path;
}