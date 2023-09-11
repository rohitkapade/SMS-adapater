package com.tml.uep.utils;

import org.springframework.util.StringUtils;

public class MaskingUtils {
    /*
     * This class is used to mask the Customer information like Phone Number,E-mail
     * Address,Customer Name,City,Text Message,Location or any String
     */

    public static String maskMobileNumber(String value) {
        if (StringUtils.isEmpty(value)) return value;
        else return value.replaceAll("[^\\d\\+]", "").replaceAll("\\d(?=\\d{4})", "*");
    }

    public static String maskString(String value) {
        if (StringUtils.isEmpty(value)) return value;
        else return value.replaceAll("\\w(?=\\w{3})", "*");
    }
}
