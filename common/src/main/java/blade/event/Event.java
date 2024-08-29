package blade.event;

import blade.Bot;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class Event<T> {
    private final Class<? extends T> typeClass;
    private final T empty;
    private final Function<T[], T> factory;
    private final Map<Bot, Invoker<T>> invokers = new WeakHashMap<>();

    @SuppressWarnings("unchecked")
    public Event(Class<? extends T> typeClass, Function<T[], T> factory) {
        this.typeClass = typeClass;
        this.factory = factory;
        this.empty = factory.apply((T[]) Array.newInstance(typeClass, 0));
    }

    public void register(Bot bot, T listener) {
        Invoker<T> invoker = invokers.computeIfAbsent(bot, v -> new Invoker<>(typeClass, factory));
        invoker.register(listener);
        invoker.update(factory);
    }

    public T call(Bot bot) {
        Invoker<T> invoker = invokers.get(bot);
        if (invoker == null) return empty;
        return invoker.handler;
    }

    private static final class Invoker<T> {
        private T[] listeners;
        private T handler;

        @SuppressWarnings("unchecked")
        private Invoker(Class<? extends T> typeClass, Function<T[], T> factory) {
            listeners = (T[]) Array.newInstance(typeClass, 0);
            update(factory);
        }

        public void update(Function<T[], T> factory) {
            handler = factory.apply(listeners);
        }

        public void register(T listener) {
            int oldLength = listeners.length;
            listeners = Arrays.copyOf(listeners, oldLength + 1);
            listeners[oldLength] = listener;
        }
    }
}
