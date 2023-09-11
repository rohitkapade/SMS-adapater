package com.tml.uep.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException(
                "Instance should not be created of Utility class DateUtils");
    }
    /** Converts into "dd-mm-yyyy hh:mm:ss" format */
    public static String getFormattedDate(OffsetDateTime localDateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return localDateTime.format(formatter);
    }

    /** Converts into "yyyy-MM-dd hh:mm:ss" format */
    public static String getYearWiseFormattedDate(OffsetDateTime localDateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    public static String getFormattedDateWithoutTime(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return localDate.format(formatter);
    }

    public static OffsetDateTime getTodaysStartTime() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return getStartDateTimeOf(now);
    }

    public static OffsetDateTime getStartDateTimeOf(OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                0,
                0,
                0,
                0,
                ZoneOffset.UTC);
    }

    public static OffsetDateTime getEndDateTimeOf(OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                23,
                59,
                59,
                0,
                ZoneOffset.UTC);
    }

    public static OffsetDateTime convertToOffsetDateTimeFromISTString(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toOffsetDateTime();
    }

    public static OffsetDateTime convertToOffsetDateTimeFromDateString(String date) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        return LocalDate.parse(date).atStartOfDay(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
    }
}
