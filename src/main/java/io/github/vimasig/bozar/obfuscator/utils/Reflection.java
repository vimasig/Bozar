package io.github.vimasig.bozar.obfuscator.utils;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class Reflection<T> {

    private final T obj;

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
