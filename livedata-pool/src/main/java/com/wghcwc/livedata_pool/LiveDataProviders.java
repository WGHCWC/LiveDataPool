package com.wghcwc.livedata_pool;




import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author wghcwc
 * @date 19-9-27
 * LiveData获取类
 */
@Deprecated
public class LiveDataProviders implements LifecycleObserver {
    private static final String TAG = "LiveDataProviders";
    private static volatile LiveDataProviders defaultInstance;
    private static final Map<String, BridgeInterface<?>> bridgeMap = new HashMap<>();
    private final Map<Class<?>, LinkedList<String>> classSetMap = new HashMap<>();
    private final Map<Class<?>, String> ONE_CACHE = new HashMap<>();

    private LiveDataProviders() {

    }

    public static LiveDataProviders getInstance() {
        if (defaultInstance == null) {
            synchronized (LiveDataProviders.class) {
                if (defaultInstance == null) {
                    defaultInstance = new LiveDataProviders();
                }
            }
        }
        return defaultInstance;
    }

    @SuppressWarnings("unchecked")
    public <T> BridgeInterface<T> get(@NonNull Class<T> tClass, String tag) {
        String name = tClass.getName() + ":" + tag;
        Bridge<T> tBridge = (Bridge<T>) bridgeMap.get(name);
        if (tBridge == null) {
            tBridge = new Bridge<>();
            tBridge.valueType = tClass;
            bridgeMap.put(name, tBridge);
        }
        return tBridge;
    }

    @SuppressWarnings("unchecked")
    public static <T> BridgeInterface<T> of(@NonNull Class<T> tClass, String tag) {
        String name = tClass.getName() + ":" + tag;
        Bridge<T> tBridge = (Bridge<T>) bridgeMap.get(name);
        if (tBridge == null) {
            tBridge = new Bridge<>();
            tBridge.valueType = tClass;
            bridgeMap.put(name, tBridge);
        }
        return tBridge;
    }

    public <T> BridgeInterface<T> get(Class<T> tClass) {
        return get(tClass, "");
    }
}
