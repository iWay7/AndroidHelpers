package site.iway.androidhelpers;

import android.os.Handler;

import java.util.LinkedList;
import java.util.List;

public class UIThread {

    public interface UIEventHandler {
        public void onEvent(String event, Object data);
    }

    private static final Object sLock = new Object();

    private static Thread sUIThread;
    private static Handler sUIHandler;
    private static List<UIEventHandler> sHandlers;

    public static void initialize() {
        sUIThread = Thread.currentThread();
        sUIHandler = new Handler();
        sHandlers = new LinkedList<>();
    }

    public static boolean check() {
        Thread currentThread = Thread.currentThread();
        return currentThread == sUIThread;
    }

    public static void run(Runnable runnable) {
        if (check())
            runnable.run();
        else
            sUIHandler.post(runnable);
    }

    public static void run(Runnable runnable, long delayMillis) {
        sUIHandler.postDelayed(runnable, delayMillis);
    }

    public static void register(UIEventHandler handler) {
        synchronized (sLock) {
            sHandlers.add(handler);
        }
    }

    public static void unregister(UIEventHandler handler) {
        synchronized (sLock) {
            sHandlers.remove(handler);
        }
    }

    private static class EventBroadcaster implements Runnable {

        private String event;
        private Object data;
        private List<UIEventHandler> handlers;

        public EventBroadcaster(String s, Object o) {
            event = s;
            data = o;
            handlers = new LinkedList<>();
            synchronized (sLock) {
                handlers.addAll(sHandlers);
            }
        }

        @Override
        public void run() {
            for (UIEventHandler handler : handlers) {
                handler.onEvent(event, data);
            }
        }
    }

    public static void event(String event, Object data) {
        EventBroadcaster broadcaster = new EventBroadcaster(event, data);
        run(broadcaster);
    }

    public static void event(String event) {
        event(event, null);
    }

}
