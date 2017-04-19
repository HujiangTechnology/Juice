package com.hujiang.juice.common.utils.rest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by xujia on 2016/6/14.
 */
public abstract class ParameterTypeReference<T> {
    private final Type type;

    public static void notNull(Object object, String message) {
        if(object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isInstanceOf(Class<?> clazz, Object obj) {
        notNull(clazz, "Type to check against must not be null");
        if(!clazz.isInstance(obj)) {
            throw new IllegalArgumentException("Object of class [" + (obj != null?obj.getClass().getName():"null") + "] must be an instance of " + clazz);
        }
    }

    public static void isTrue(boolean expression) {
        if(!expression) {
            throw new IllegalArgumentException("[Assertion failed] - this expression must be true");
        }
    }

    protected ParameterTypeReference() {
        Class parameterTypeReferenceSubclass = findParameterTypeReferenceSubclass(this.getClass());
        Type type = parameterTypeReferenceSubclass.getGenericSuperclass();
        isInstanceOf(ParameterizedType.class, type);
        ParameterizedType parameterizedType = (ParameterizedType)type;
        isTrue(parameterizedType.getActualTypeArguments().length == 1);
        this.type = parameterizedType.getActualTypeArguments()[0];
    }

    public Type getType() {
        return this.type;
    }

    public boolean equals(Object obj) {
        return this == obj || obj instanceof ParameterTypeReference && this.type.equals(((ParameterTypeReference)obj).type);
    }

    public int hashCode() {
        return this.type.hashCode();
    }

    public String toString() {
        return "ParameterTypeReference<" + this.type + ">";
    }

    private static Class<?> findParameterTypeReferenceSubclass(Class<?> child) {
        Class parent = child.getSuperclass();
        if(Object.class == parent) {
            throw new IllegalStateException("Expected ParameterTypeReference superclass");
        } else {
            return ParameterTypeReference.class == parent?child:findParameterTypeReferenceSubclass(parent);
        }
    }
}

