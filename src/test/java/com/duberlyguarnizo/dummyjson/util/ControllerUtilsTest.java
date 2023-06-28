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

package com.duberlyguarnizo.dummyjson.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class ControllerUtilsTest {
    @Container
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @Autowired
    ControllerUtils utils;

    @Test
    void getRequestLocale() {
    }

    @Test
    void processPageSortDoesReturnCorrectResultOnValidSortList() {
        List<String> sortOkList = List.of(
                "name,asc",
                "id,desc",
                "path,asc");
        var expectedResult = List.of(new Sort.Order(Sort.Direction.ASC, "name"),
                new Sort.Order(Sort.Direction.DESC, "id"),
                new Sort.Order(Sort.Direction.ASC, "path"));

        String[] sortOkArray = sortOkList.toArray(new String[0]);
        var result = utils.processPageSort(sortOkArray);
        var size = result.size();
        assertEquals(3, size);
        assertEquals(expectedResult, result);
    }


    @Test
    void processPageSortReturnsDefaultOnInvalidSingleSort() {
        String[] elem1 = new String[]{",,name"};
        String[] elem2 = new String[]{"asc,,"};
        String[] elem3 = new String[]{"asc:name"};
        String[] elem4 = new String[]{"asc_name"};
        String[] elem5 = new String[]{",name"};
        List<String[]> sortNotOkList = List.of(elem1, elem2, elem3, elem4, elem5);
        for (String[] element : sortNotOkList) {
            var result = utils.processPageSort(element);
            assertEquals("id: ASC", result.get(0).toString());
        }
    }

    @Test
    void processPageSortIgnoresErrorOnMixedValidAndInvalidSortList() {
        //On error with mixed results, an exception is thrown and the
        // default sort is returned. Therefore, the result list is "cut"
        // where the exception happened, and "id:ASC" is added to the list
        //(in case the first element was invalid).
        //This method was preferred instead of launching an exception, so
        //requests are not hold back just for the sake of sorting.
        // Duberly Guarnizo, 2023.
        List<String> sortOkList = List.of(
                "name,asc",
                "",
                ",id",
                "path,asc");
        var expectedResult = List.of(new Sort.Order(Sort.Direction.ASC, "name"),
                new Sort.Order(Sort.Direction.ASC, "id"));

        String[] sortOkArray = sortOkList.toArray(new String[0]);
        var result = utils.processPageSort(sortOkArray);
        var size = result.size();
        assertEquals(2, size); //list is cut where exception happened
        assertEquals(expectedResult, result); //expected is first element + default (id:ASC) where the cut happened
    }
}