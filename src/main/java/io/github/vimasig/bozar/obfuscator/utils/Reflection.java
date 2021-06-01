package io.github.vimasig.bozar.obfuscator.utils;

import java.lang.reflect.Field;

public class Reflection<T> {

    private final T obj;
    public Reflection(T obj) {
        this.obj = obj;
    }

    public void setDeclaredField(String name, Object value) {
        try {
            Field f = this.obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
