package com.wghcwc.livedata_pool;




import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wghcwc
 * @date 18-9-28
 * 事件
 */
public class EventPool {
    private static final String TAG = "EventPool";
    private static volatile EventPool defaultInstance = new EventPool();
    private static final Map<String, Bridge<?>> bridgeMap = new HashMap<>();
    private static Object object = new Object();

    private EventPool() {

    }

    @SuppressWarnings("unchecked")
    public <T> Bridge<T> get(@NonNull Class<T> tClass, String tag) {
        String name = tClass.getName() + ":" + tag;
        Bridge<T> tBridge = (Bridge<T>) bridgeMap.get(name);
        if (tBridge == null) {
            tBridge = new Bridge<>();
            tBridge.key = name;
            bridgeMap.put(name, tBridge);
        }
        return tBridge;
    }

    public static void clearAll(){
        bridgeMap.clear();
    }

    public <T> Bridge<T> get(@NonNull Class<T> tClass) {
        return get(tClass, "");
    }

    /**
     * 任意类型
     */
    public static void setValue(String tag, Object o) {
        if (o == null) return;
        defaultInstance.get(o.getClass(), tag).setObj(o);
    }

    public static void setValue(Object o) {
        if (o == null) return;
        defaultInstance.get(o.getClass()).setObj(o);
    }

    public static void postValue(String tag, Object o) {
        if (o == null) return;
        defaultInstance.get(o.getClass(), tag).postObj(o);
    }

    public static void postValue(Object o) {
        if (o == null) return;
        defaultInstance.get(o.getClass()).postObj(o);
    }

    public static <T> Bridge<T> of(@NonNull Class<T> tClass, String tag) {
        return defaultInstance.get(tClass, tag);
    }

    /**
     * 无tag会导致逻辑不清晰,tag应避免魔法值!!!
     */
    @Deprecated
    public static <T> Bridge<T> of(@NonNull Class<T> tClass) {
        return defaultInstance.get(tClass);
    }

    /**
     * Obj类型,只关心tag
     */
    public static void callTag(String tag) {
        defaultInstance.get(Object.class, tag).setValue(object);
    }

    public static void postTag(String tag) {
        defaultInstance.get(Object.class, tag).postValue(object);
    }


    public static Bridge<Object> getTag(String tag) {
        return defaultInstance.get(Object.class, tag);
    }

    /**
     * 整形
     */

    public static void callInt(String tag, int value) {
        defaultInstance.get(Integer.class, tag).setValue(value);
    }

    public static void postInt(String tag, int value) {
        defaultInstance.get(Integer.class, tag).postValue(value);
    }

    public static Bridge<Integer> getInt(String tag) {
        return defaultInstance.get(Integer.class, tag);
    }

    /**
     * String
     */

    public static void callStr(String tag, String value) {
        if (value == null) return;
        defaultInstance.get(String.class, tag).setValue(value);
    }

    public static void postStr(String tag, String value) {
        if (value == null) return;
        defaultInstance.get(String.class, tag).postValue(value);
    }

    public static Bridge<String> getStr(String tag) {
        return defaultInstance.get(String.class, tag);
    }

    /**
     * boolean
     */
    public static void callBool(String tag, boolean value) {
        defaultInstance.get(Boolean.class, tag).setValue(value);
    }

    public static void postBool(String tag, boolean value) {
        defaultInstance.get(Boolean.class, tag).postValue(value);
    }

    public static Bridge<Boolean> getBool(String tag) {
        return defaultInstance.get(Boolean.class, tag);
    }


    public class Bridge<T> implements LifecycleObserver {
        private EveryLiveData<T> everyLiveData;
        private String key;
        private Map<LifecycleOwner, Observer<T>> observers;

        Bridge() {
            everyLiveData = new EveryLiveData<>();
            observers = new HashMap<>();
        }

        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            everyLiveData.observe(owner, observer);
        }

        public void observeForever(@NonNull Observer<T> observer) {
            everyLiveData.observeForever(observer);
        }

        public void unStickyForever(@NonNull Observer<T> observer) {
            everyLiveData.unStickyForever(observer);
        }

        public void unSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            everyLiveData.unSticky(owner, observer);
        }

        public void setValue(T t) {
            everyLiveData.setValue(t);
        }

        public void postValue(T t) {
            everyLiveData.postValue(t);
        }

        @SuppressWarnings("unchecked")
        public void setObj(Object o) {
            everyLiveData.setValue((T) o);
        }

        @SuppressWarnings("unchecked")
        public void postObj(Object o) {
            everyLiveData.postValue((T) o);

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void onDestroy(LifecycleOwner owner) {
//            everyLiveData.removeObservers(owner);
        }


    }
}
