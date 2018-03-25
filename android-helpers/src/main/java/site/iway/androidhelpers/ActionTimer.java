package site.iway.androidhelpers;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

public class ActionTimer {

    public static abstract class Action {

        private volatile String mTag;
        private volatile long mRunTime;

        public Action(String tag) {
            if (tag == null)
                throw new RuntimeException("Tag of the action can not be null.");
            mTag = tag;
        }

        public String getTag() {
            return mTag;
        }

        public long getRunTime() {
            return mRunTime;
        }

        public void setRunTime(long runTime) {
            mRunTime = runTime;
        }

        public void setRunTimeDelayed(long delay) {
            mRunTime = System.currentTimeMillis() + delay;
        }

        public void schedule() {
            addAction(this);
        }

        public abstract void run(Context context);

    }

    private static final Object sSynchronizer = new Object();

    private static Context sApplicationContext;

    private static List<Action> sActions = new LinkedList<>();

    private static Thread sThread = new Thread() {

        public void run() {
            while (true) {
                List<Action> actions = new LinkedList<>();
                synchronized (sSynchronizer) {
                    actions.addAll(sActions);
                }

                long now = System.currentTimeMillis();
                for (Action action : actions) {
                    long runTime = action.getRunTime();
                    if (now >= runTime) {
                        removeAction(action);
                        action.run(sApplicationContext);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // nothing
                }
            }
        }

    };

    public static void initialize(Context context) {
        sApplicationContext = context;
        sThread.start();
    }

    public static void addAction(Action action) {
        synchronized (sSynchronizer) {
            String tag = action.getTag();
            if (tag == null) {
                throw new RuntimeException("Action tag can not be null.");
            }
            if (hasAction(tag)) {
                throw new RuntimeException("Action with tag " + tag + " already existed.");
            }
            sActions.add(action);
        }
    }

    public static void removeAction(Action action) {
        synchronized (sSynchronizer) {
            sActions.remove(action);
        }
    }

    public static void removeAction(String tag) {
        synchronized (sSynchronizer) {
            int size = sActions.size();
            for (int i = size - 1; i >= 0; i--) {
                Action action = sActions.get(i);
                String actionTag = action.getTag();
                if (actionTag == null) {
                    if (tag == null) {
                        sActions.remove(i);
                    }
                } else {
                    if (actionTag.equals(tag)) {
                        sActions.remove(i);
                    }
                }
            }
        }
    }

    public static boolean hasAction(Action action) {
        synchronized (sSynchronizer) {
            return sActions.contains(action);
        }
    }

    public static boolean hasAction(String tag) {
        synchronized (sSynchronizer) {
            for (Action action : sActions) {
                String actionTag = action.getTag();
                if (actionTag.equals(tag)) {
                    return true;
                }
            }
            return false;
        }
    }

}
