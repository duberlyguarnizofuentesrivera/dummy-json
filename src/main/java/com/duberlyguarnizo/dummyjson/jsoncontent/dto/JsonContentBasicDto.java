/*
 * dummy-json
 * Copyright (c) 2023 Duberly Guarnizo Fuentes Rivera <duberlygfr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.duberlyguarnizo.dummyjson.jsoncontent.dto;

import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContent;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 991L;
}