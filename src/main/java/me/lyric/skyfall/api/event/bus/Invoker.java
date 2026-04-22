package me.lyric.skyfall.api.event.bus;

import lombok.Getter;
import me.lyric.skyfall.api.event.Event;

import java.lang.reflect.Method;

@FunctionalInterface
public interface Invoker {
    void invoke(Event event);

    @Getter
    final class MethodInvoker {
        private final Method method;
        private final Invoker invoker;
        /**
         * Cached event type
         */
        private final Class<?> eventType;
        private final int priority;

        public MethodInvoker(Method method, Invoker invoker) {
            this.method = method;
            this.invoker = invoker;
            this.eventType = method.getParameters()[0].getType();
            this.priority = method.getAnnotation(ITheAnnotation.class).priority();
        }
    }
}
