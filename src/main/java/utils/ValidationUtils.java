package utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.netty.util.internal.StringUtil;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class ValidationUtils {

    private static final List<String> excludeFromString = Arrays.asList("null", "nill", "empty", "blank");
    private static final String datePattern = "yyyy-MM-dd";
    private static final String dateTimePattern = "yyyy-MM-dd hh:mm:ss";

    public static <T> T mapToObj(JsonObject object, Class<T> type) {
        for (Field declaredField : type.getDeclaredFields()) {
            if (declaredField.getAnnotation(NotBlank.class) == null &&  declaredField.getAnnotation(NotNull.class) == null)
                continue;
            if (declaredField.getAnnotation(NotBlank.class) != null || declaredField.getAnnotation(NotNull.class) != null) {
                if (object.getMap().get(declaredField.getName()) == null)
                    throw new ValidationException(declaredField.getName().toUpperCase()+"_IS_INVALID");
            }
            if (declaredField.getType().equals(String.class)) {
                mustString(object.getMap().get(declaredField.getName()), declaredField.getName());
            } else if (declaredField.getType().equals(Integer.class) || declaredField.getType().equals(Double.class) || declaredField.getType().equals(Long.class)) {
                mustNumber(object.getMap().get(declaredField.getName()), declaredField.getName());
            } else if (declaredField.getType().equals(Date.class)) {
                JsonFormat jsonFormat = declaredField.getAnnotation(JsonFormat.class);
                if (jsonFormat.pattern().equals(datePattern)) {
                    mustStringDate(object.getMap().get(declaredField.getName()), declaredField.getName(), 1);
                } else if (jsonFormat.pattern().equals(dateTimePattern)) {
                    mustStringDate(object.getMap().get(declaredField.getName()), declaredField.getName(), 2);
                }
            } else if (declaredField.getType().equals(Boolean.class)) {
                mustBoolean(object.getMap().get(declaredField.getName()), declaredField.getName());
            } else if (object.getMap().get(declaredField.getName()).getClass().equals(LinkedHashMap.class)){
                mapToObj(JsonObject.mapFrom(object.getMap().get(declaredField.getName())), declaredField.getType());
            } else {
                throw new ValidationException(declaredField.getName().toUpperCase()+"_IS_INVALID");
            }
        }
        return Json.CODEC.fromValue(object.getMap(), type);
    }

    private static void mustString(Object object, String name) {
        if (!(object instanceof String)) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }

        try {
            String input = (String) object;
            if (excludeFromString.contains(input.toLowerCase()) || StringUtil.isNullOrEmpty(input)) {
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private static void mustStringDate(Object object, String name, int dateType) {
        if (!(object instanceof String)) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }

        String input = (String) object;
        if (excludeFromString.contains(input.toLowerCase()) || StringUtil.isNullOrEmpty(input)) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }
        String[] splitDate = input.split(" ");
        if (splitDate.length != dateType) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }
        String date = splitDate[0];
        String time = "";
        if (splitDate.length > 1) {
            time = splitDate[1];
        }
        String[] datSplit = date.split("-");
        if (datSplit.length != 3)
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        if (datSplit[0].length() != 4) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }
        if (datSplit[1].length() != 2 || Integer.parseInt(datSplit[1]) < 1 || Integer.parseInt(datSplit[1]) > 12) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }
        if (datSplit[2].length() != 2 || Integer.parseInt(datSplit[2]) < 1 || Integer.parseInt(datSplit[2]) > 31)
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");

        LocalDateTime localDate = DateTimeConverter.convertFromStringYYYYMMDDToLocalDateTime(date);
        if (localDate.getMonthValue() != Integer.parseInt(datSplit[1]))
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        if (localDate.getDayOfMonth() != Integer.parseInt(datSplit[2]))
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");

        if (!StringUtil.isNullOrEmpty(time)) {
            String[] timeSplit = time.split(":");
            if (timeSplit.length != 3)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");

            if (timeSplit[0].length() != 2 || Integer.parseInt(timeSplit[0]) > 23)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
            if (timeSplit[1].length() != 2 || Integer.parseInt(timeSplit[1]) > 59)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
            if (timeSplit[2].length() != 2 || Integer.parseInt(timeSplit[2]) > 59)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }

    }

    private static void mustBoolean(Object object, String name) {
        if (!(object instanceof Boolean)) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }
    }

    private static void mustNumber(Object object, String name) {
        if ((object instanceof Boolean) || object instanceof String) {
            throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }
        if (object instanceof Integer) {
            Integer i = (Integer) object;
            if (i < 0)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }

        if (object instanceof Double) {
            Double i = (Double) object;
            if (i < 0)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }

        if (object instanceof Long){
            Long i = (Long) object;
            if (i < 0)
                throw new ValidationException(name.toUpperCase()+ "_IS_INVALID");
        }


    }
}
