package site.iway.androidhelpers;

import java.util.LinkedList;
import java.util.List;

@Deprecated
public class EventPoster {

    public interface EventListener {
        public void onEvent(int event, Object data);
    }

    private static LinkedList<EventListener> sListeners = new LinkedList<>();
    private static Object sSynchronizer = new Object();

    public static void register(EventListener listener) {
        synchronized (sSynchronizer) {
            sListeners.add(listener);
        }
    }

    public static void unregister(EventListener handler) {
        synchronized (sSynchronizer) {
            sListeners.remove(handler);
        }
    }

    public static void post(int event, Object data) {
        List<EventListener> copiedList = new LinkedList<>();
        synchronized (sSynchronizer) {
            copiedList.addAll(sListeners);
        }
        for (EventListener handler : copiedList) {
            handler.onEvent(event, data);
        }
    }

    public static void post(int event) {
        post(event, null);
    }

}
