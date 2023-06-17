package com.duberlyguarnizo.dummyjson.util;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ControllerUtils {

    private ControllerUtils() {
        //Utility class, not meant to be instanced
    }

    /**
     * Get the Locale for the translation of the i18n key specified.
     * If LocaleContextHolder processing returns null, this will
     * return de default language (English),
     *
     * @return Locale of the request, or default ("en").
     */
    public static Locale getRequestLocale() {
        Locale locale = new Locale("en");
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        if (localeContext != null) {
            Locale contextLocale = localeContext.getLocale();
            if (contextLocale != null) {
                locale = contextLocale;
            }
        }
        return locale;
    }

    public static List<Order> processPageSort(String[] sort) {
        List<Order> orders = new ArrayList<>();
        try {
            if (sort[0].contains(",")) {
                // when sorting more than 2 fields: sortOrder="field,direction"
                for (String sortOrder : sort) {
                    String[] sortPart = sortOrder.split(",");
                    orders.add(new Order(getSortDirection(sortPart[1]), sortPart[0]));
                }
            } else {
                // sort=[field, direction]
                orders.add(new Order(getSortDirection(sort[1]), sort[0]));
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            //if processing fails, return default
            orders.add(new Order(getSortDirection("asc"), "id"));
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
