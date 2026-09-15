package ru.practicum.shareit.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public final class CopyUtil {

    public static void copyNonNullProperties(Object source, Object target) {
        if (source == null) {
            throw new ValidationException("Объект источника не может быть null", target, "request object");
        }
        String[] nullFieldsNames = getNullFieldsNames(source);
        BeanUtils.copyProperties(source, target, nullFieldsNames);
    }

    private static String[] getNullFieldsNames(Object source, String... manualExcluded) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>(Arrays.asList(manualExcluded));
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null) {
                Object srcValue = src.getPropertyValue(pd.getName());

                if (srcValue == null) {
                    emptyNames.add(pd.getName());
                }
            }
        }

        return emptyNames.toArray(new String[0]);
    }
}
