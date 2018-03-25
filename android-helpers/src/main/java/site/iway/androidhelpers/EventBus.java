package site.iway.androidhelpers;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class EventBus {

    public interface EventListener {
        public void onEvent(int event, Object data);
    }

    private static SparseArray<List<EventListener>> mListeners = new SparseArray<>();

    public static void register(int event, EventListener handler) {
        List<EventListener> list = mListeners.get(event);
        if (list == null) {
            list = new ArrayList<>();
            list.add(handler);
            mListeners.put(event, list);
        } else {
            list.add(handler);
        }
    }

    public static void unregister(int event, EventListener handler) {
        List<EventListener> list = mListeners.get(event);
        if (list != null) {
            list.remove(handler);
        }
    }

    public static void unregister(int event) {
        mListeners.delete(event);
    }

    public static void unregisterAll() {
        mListeners.clear();
    }

    public static void notify(int event, Object data) {
        List<EventListener> list = mListeners.get(event);
        if (list != null) {
            List<EventListener> copyList = new ArrayList<>();
            copyList.addAll(list);
            for (EventListener handler : copyList) {
                handler.onEvent(event, data);
            }
        }
    }

    public static void notify(int event) {
        notify(event, null);
    }

}
