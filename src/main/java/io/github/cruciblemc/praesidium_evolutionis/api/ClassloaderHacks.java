package io.github.cruciblemc.praesidium_evolutionis.api;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

/**
 * A hack class to call code inside a classloader context which will be appropriately remapped and call the right classes.
 * Seriously, please don't use this, this class only exists for the ultra slim chance you absolutely need to call reusable code within
 * a plugin classloader.
 *
 * @author juanmuscaria
 */
// TODO: Probably this will break on Java 9+, also resource leakage from this is probably not ideal....
//  replace it with something else in the future?
public class ClassloaderHacks {
    final ReflectionHelper.MethodInvoker defineClass = ReflectionHelper.getMethod(ClassLoader.class,
            "defineClass",
            String.class, byte[].class, int.class, int.class);

    private final ClassLoader context;

    public ClassloaderHacks(ClassLoader cl) {
        context = cl;
    }

    /**
     * Injects a function within another classloader
     *
     * @param function original function with your desired code.
     * @param <T>      function arg
     * @param <R>      function return
     * @return a copy of your function injected in the desired classloader.
     */
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    public <T, R> Function<T, R> inject(SFunction<T, R> function) {
        try {
            Class<?> clazz = injectClass(function.getClass());
            // Creates a new function object inside the classloader
            Constructor<?> c = clazz.getDeclaredConstructors()[0];
            c.setAccessible(true);
            return (Function<T, R>) c.newInstance(new Object[c.getParameterCount()]);
        } catch (NullPointerException e) {
            // this piece of code is illegal in over 700 countries
            SerializedLambda lambda = serialize(function);
            return (Function<T, R>) injectLambda(lambda);
        }
    }

    // ???????????????????????
    private static SerializedLambda serialize(SFunction<?, ?> lambda) throws ReflectiveOperationException {
        SerializedLambda serializedLambda;
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        serializedLambda = (SerializedLambda) method.invoke(lambda);
        return serializedLambda;
    }

    // would it even be possible to make "this" works on injected lambdas?
    private Function<?, ?> injectLambda(SerializedLambda lambda) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        Class<?> capturing = injectClass(Class.forName(lambda.getCapturingClass()));
        Method $ = capturing.getDeclaredMethod("$deserializeLambda$", SerializedLambda.class);
        $.setAccessible(true);
        return (Function<?, ?>) $.invoke(null, lambda);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Class<?> injectClass(Class<?> clazz) {
        try {
            return Class.forName(clazz.getName(), false, context);
        } catch (ClassNotFoundException ignore) {
        }
        String name = clazz.getName().replace('.', '/') + ".class";
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(name)) {
            byte[] bytes = ByteStreams.toByteArray(Objects.requireNonNull(in, "Class file not found " + name));
            return (Class<?>) defineClass.invoke(context, clazz.getName(), bytes, 0, bytes.length);
        } catch (NullPointerException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Error while injecting " + name, e);
        }
    }

    public interface SFunction<T, R> extends Serializable, Function<T, R> {
    }
}
