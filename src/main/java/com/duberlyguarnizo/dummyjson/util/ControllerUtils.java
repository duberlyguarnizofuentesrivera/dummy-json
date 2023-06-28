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

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ControllerUtils {
    private final MessageSource messageSource;

    public ControllerUtils(MessageSource messageSource) {
        //Utility class, not meant to be instanced
        this.messageSource = messageSource;
    }

    /**
     * Get the Locale for the translation of the i18n key specified.
     * If LocaleContextHolder processing returns null, this will
     * return de default language (English),
     *
     * @return Locale of the request, or default ("en").
     */
    private Locale getRequestLocale() {
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

    public String getMessage(String code) {
        return messageSource.getMessage(code, null, this.getRequestLocale());
    }

    public String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, this.getRequestLocale());
    }


    public List<Order> processPageSort(String[] sort) {
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

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("ascending")) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }
}
