package ir.azki.reservationSystem.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Validation {
    public static boolean isNullOrBlank(String key) {
        return key == null ? true : key.isBlank();
    }

    public static boolean isNullOrZero(Long key) {
        if (key == null) {
            return true;
        } else {
            return key == 0L;
        }
    }
}
