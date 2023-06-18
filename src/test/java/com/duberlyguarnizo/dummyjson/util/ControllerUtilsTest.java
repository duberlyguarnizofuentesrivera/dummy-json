package com.duberlyguarnizo.dummyjson.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ControllerUtilsTest {
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