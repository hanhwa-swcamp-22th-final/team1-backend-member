package com.conk.member.common.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AdminPayloadCompat {

    private AdminPayloadCompat() {
    }

    public static Map<String, Object> raw(Object payload) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (payload != null) {
            BeanWrapper wrapper = new BeanWrapperImpl(payload);
            for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
                String name = descriptor.getName();
                if ("class".equals(name) || descriptor.getReadMethod() == null) {
                    continue;
                }
                result.put(name, wrapper.getPropertyValue(name));
            }
        }
        result.put("data", payload);
        return result;
    }

    public static Map<String, Object> items(List<?> items) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("data", Collections.singletonMap("items", items));
        return result;
    }
}
