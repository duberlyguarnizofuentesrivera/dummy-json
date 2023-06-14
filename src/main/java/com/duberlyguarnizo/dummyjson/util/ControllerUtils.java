package com.duberlyguarnizo.dummyjson.util;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.List;

public class ControllerUtils {
    public static List<Order> processPageSort(String[] sort) {
        List<Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            // when sorting more than 2 fields: sortOrder="field,direction"
            for (String sortOrder : sort) {
                String[] sortPart = sortOrder.split(",");
                //TODO: validate sortPart[0] (field) is a valid field
                orders.add(new Order(getSortDirection(sortPart[1]), sortPart[0]));
            }
        } else {
            // sort=[field, direction]
            orders.add(new Order(getSortDirection(sort[1]), sort[0]));
        }
        return orders;
    }

    private static Sort.Direction getSortDirection(String direction) {
        if (direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("ascending")) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }
}
