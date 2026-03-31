package com.akfsno.wordclockwidgets;

import java.util.HashMap;
import java.util.Map;

public class NumberToWords {

    private static final String[] units = {
        "", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"
    };

    private static final String[] teens = {
        "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать",
        "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"
    };

    private static final String[] tens = {
        "", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"
    };

    private static final String[] hours = {
        "двенадцать", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать"
    };

    public static String convertHour(int hour) {
        if (hour == 0) return "двенадцать";
        if (hour > 12) hour -= 12;
        return hours[hour];
    }

    public static String convertHour24(int hour) {
        if (hour == 0) return "ноль";
        if (hour < 0) return "";
        if (hour < 10) return units[hour];
        if (hour < 20) return teens[hour - 10];
        if (hour < 24) return convert(hour);
        return "";
    }

    public static String convertSecond(int second, boolean useWords, boolean addZero) {
        if (useWords) {
            if (addZero && second >= 1 && second <= 9) {
                return "ноль " + convertSecondOrdinal(second);
            }
            return convertSecondOrdinal(second);
        } else {
            return String.valueOf(second);
        }
    }

    private static String convertSecondOrdinal(int second) {
        if (second == 0) return "ноль";
        if (second == 1) return "одна";
        if (second == 2) return "две";
        if (second < 10) return units[second];
        if (second < 20) return teens[second - 10];
        int ten = second / 10;
        int unit = second % 10;
        String tenStr = tens[ten];
        String unitStr = unit == 1 ? "одна" : unit == 2 ? "две" : (unit > 0 ? " " + units[unit] : "");
        return tenStr + unitStr;
    }

    public static String[] convertSecondVertical(int second, boolean useWords, boolean addZero) {
        if (useWords) {
            String fullText = convertSecond(second, true, addZero);
            String[] words = fullText.split(" ");
            return words;
        } else {
            return new String[]{String.valueOf(second)};
        }
    }

    public static String convertMinute(int minute, boolean addZero) {
        if (minute == 0) {
            return "ноль";
        }
        if (addZero && minute >= 1 && minute <= 9) {
            return "ноль " + convert(minute);
        }
        if (minute < 60) {
            return convert(minute);
        }
        return "";
    }

    private static String convert(int number) {
        if (number < 10) {
            return units[number];
        } else if (number < 20) {
            return teens[number - 10];
        } else {
            int ten = number / 10;
            int unit = number % 10;
            return tens[ten] + (unit > 0 ? " " + units[unit] : "");
        }
    }

    private static String convertOrdinal(int number) {
        if (number == 1) return "первое";
        if (number == 2) return "второе";
        if (number == 3) return "третье";
        if (number == 4) return "четвёртое";
        if (number == 5) return "пятое";
        if (number == 6) return "шестое";
        if (number == 7) return "седьмое";
        if (number == 8) return "восьмое";
        if (number == 9) return "девятое";
        if (number == 10) return "десятое";
        if (number == 11) return "одиннадцатое";
        if (number == 12) return "двенадцатое";
        if (number == 13) return "тринадцатое";
        if (number == 14) return "четырнадцатое";
        if (number == 15) return "пятнадцатое";
        if (number == 16) return "шестнадцатое";
        if (number == 17) return "семнадцатое";
        if (number == 18) return "восемнадцатое";
        if (number == 19) return "девятнадцатое";
        if (number == 20) return "двадцатое";
        if (number == 30) return "тридцатое";
        int ten = number / 10;
        int unit = number % 10;
        String tenStr = tens[ten];
        String unitStr = "";
        if (unit == 1) unitStr = "первое";
        else if (unit == 2) unitStr = "второе";
        else if (unit == 3) unitStr = "третье";
        else if (unit == 4) unitStr = "четвёртое";
        else if (unit == 5) unitStr = "пятое";
        else if (unit == 6) unitStr = "шестое";
        else if (unit == 7) unitStr = "седьмое";
        else if (unit == 8) unitStr = "восьмое";
        else if (unit == 9) unitStr = "девятое";
        return tenStr + " " + unitStr;
    }

    public static String getDayNight(int hour) {
        if (hour >= 4 && hour < 12) return "утра";
        if (hour >= 12 && hour < 17) return "дня";
        if (hour >= 17 && hour < 23) return "вечера";
        return "ночи";
    }

    private static final String[] daysOfWeek = {
        "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"
    };

    public static String getDayOfWeek(int dayOfWeek) {
        return daysOfWeek[dayOfWeek];
    }

    private static final String[] months = {
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    };

    public static String convertDate(int day, int month, int year) {
        String dayStr = convertOrdinal(day);
        String monthStr = months[month - 1];
        return dayStr + " " + monthStr;
    }
}