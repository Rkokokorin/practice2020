package com.tuneit.itc.commons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class ReflectionUtils {

    private static final Log log = LogFactoryUtil.getLog(ReflectionUtils.class);

    public static Class<?> getGenericClass(Class<?> targetClass, int paramIndex) {
        for (Type iface : targetClass.getGenericInterfaces()) {
            if (iface instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) iface;
                return (Class<?>) type.getActualTypeArguments()[paramIndex];
            }
        }
        return null;
    }

    public static List<Type> getGenericInterfaces(Class<?> targetClass) {
        Queue<Class<?>> queue = new ArrayDeque<>();
        queue.add(targetClass);
        List<Type> genericInterfaces = new ArrayList<>();
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            Type[] currentGenericInterfaces = current.getGenericInterfaces();
            Collections.addAll(genericInterfaces, currentGenericInterfaces);
            Collections.addAll(queue, current.getInterfaces());
        }
        return genericInterfaces;
    }

    public ParameterizedType getParametrizedType(Class<?> targetClass, Class<?> rawClass) {
        return getGenericInterfaces(targetClass)
            .stream()
            .filter(iface -> iface instanceof ParameterizedType)
            .map(iface -> (ParameterizedType) iface)
            .filter(pt -> pt.getRawType().equals(rawClass))
            .findAny()
            .orElse(null);
    }

}
