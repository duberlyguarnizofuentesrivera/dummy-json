package com.duberlyguarnizo.dummyjson.jsoncontent.dto;

import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContent;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * DTO for {@link JsonContent}
 */
@Value
public class JsonContentBasicDto implements Serializable {
    Long id;
    @NotBlank
    String name;
    @URL(protocol = "")
    String path;
}