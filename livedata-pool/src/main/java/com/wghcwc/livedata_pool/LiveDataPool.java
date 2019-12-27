package com.wghcwc.livedata_pool;


import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wghcwc
 * @date 19-10-20
 */
@Deprecated
public class LiveDataPool {
    private static volatile LiveDataPool defaultInstance;
    private final Map<Class<?>, Map<String, LiveDataInfo>> DATA_INFO_CACHE = new ConcurrentHashMap<>();
    private final Map<Class<?>, LinkedList<Object>> objectMap = new HashMap<>();
    private final Map<Object, Map<String, MutableLiveData>> activeData = new ConcurrentHashMap<>();
    private final Map<Class<?>, Constructor> CONSTRUCT_CACHE = new ConcurrentHashMap<>();
    private LiveDataPool() {

    }

    public static LiveDataPool pool() {
        if (defaultInstance == null) {
            synchronized (LiveDataPool.class) {
                if (defaultInstance == null) {
                    defaultInstance = new LiveDataPool();
                }
            }
        }
        return defaultInstance;
    }

    public void push(Object obj) {
        Class<?> aClass = obj.getClass();
        String clsName = aClass.getName();

        Constructor constructor = CONSTRUCT_CACHE.get(aClass);
        if (constructor == null) {
            try {
                Class<?> poolClass = aClass.getClassLoader().loadClass(clsName + "_Pool");
                constructor = poolClass.getConstructor(aClass);
                CONSTRUCT_CACHE.put(aClass, constructor);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("LiveDataPool", "push: " + aClass.getName() + "构造器获取出错");
                return;
            }
        }

        try {
            constructor.newInstance(obj);
        } catch (Exception e) {
            Log.e("LiveDataPool", "push: " + aClass.getName() + "构造器创建对象出错");
            e.printStackTrace();
        }
    }

    public void push(Object target, Class<?> tClazz, MutableLiveData data, Class<?> type, String tag,
                     boolean enable) {
        Map<String, LiveDataInfo> dataInfoMap = DATA_INFO_CACHE.get(tClazz);
        if (dataInfoMap == null) {
            dataInfoMap = new HashMap<>();
            DATA_INFO_CACHE.put(tClazz, dataInfoMap);
        }
        dataInfoMap.put(tag, new LiveDataInfo(type, tClazz, enable));
        LinkedList<Object> objects = objectMap.get(tClazz);
        if (objects == null) {
            objects = new LinkedList<>();
            objectMap.put(tClazz, objects);
        }
        objects.add(target);
        Map<String, MutableLiveData> mutableLiveDataMap = activeData.get(target);
        if (mutableLiveDataMap == null) {
            mutableLiveDataMap = new HashMap<>();
            activeData.put(target, mutableLiveDataMap);
        }
        mutableLiveDataMap.put(tag, data);
    }

    private class LiveDataInfo {
        private Class<?> valueType;
        private Class<?> clazz;
        private boolean enableMultipleObj;

        public LiveDataInfo(Class<?> valueType, Class<?> clazz, boolean enableMultipleObj) {
            this.valueType = valueType;
            this.clazz = clazz;
            this.enableMultipleObj = enableMultipleObj;
        }
    }

    @SuppressWarnings("unchecked")
    public void set(Class<?> tClass, String tag, Object val) {
        List<MutableLiveData> mutableLiveDataList = getLiveDataInfo(tClass, tag, val);
        if (mutableLiveDataList == null) {
            return;
        }
        for (MutableLiveData mutableLiveData : mutableLiveDataList) {
            mutableLiveData.setValue(val);
        }
    }

    @SuppressWarnings("unchecked")
    public void post(Class<?> tClass, String tag, Object val) {
        List<MutableLiveData> mutableLiveDataList = getLiveDataInfo(tClass, tag, val);
        if (mutableLiveDataList == null) {
            return;
        }
        for (MutableLiveData mutableLiveData : mutableLiveDataList) {
            mutableLiveData.postValue(val);
        }

    }

    private List<MutableLiveData> getLiveDataInfo(Class<?> tClass, String tag, Object val) {


        Map<String, LiveDataInfo> liveDataMap = DATA_INFO_CACHE.get(tClass);
        if (liveDataMap == null || liveDataMap.isEmpty()) {
            return null;
        }
        LiveDataInfo liveDataInfo = liveDataMap.get(tag);
        if (liveDataInfo == null) {
            return null;
        }
        LinkedList<Object> objects = objectMap.get(tClass);
        if (objects == null || objects.isEmpty()) {
            return null;
        }
        if (!liveDataInfo.valueType.isInstance(val)) {
            return null;
        }

        List<MutableLiveData> mutableLiveDataList = new ArrayList<>();
        for (Object object : objects) {
            Map<String, MutableLiveData> mutableLiveDataMap = activeData.get(object);
            if (mutableLiveDataMap == null || mutableLiveDataMap.isEmpty()) {
                continue;
            }

            MutableLiveData mutableLiveData = mutableLiveDataMap.get(tag);
            if (mutableLiveData == null) {

                continue;
            }
            if (!liveDataInfo.enableMultipleObj && !mutableLiveData.hasActiveObservers()) {
                continue;
            }
            mutableLiveDataList.add(mutableLiveData);
        }
        return mutableLiveDataList;

    }


    public void clear(Object object) {
        LinkedList<Object> objects = objectMap.get(object.getClass());
        if (objects == null) {
            return;
        }
        objects.remove(object);
        Map<String, MutableLiveData> liveDataMap = activeData.get(object);
        if (liveDataMap == null) {
            return;
        }
        liveDataMap.clear();
        activeData.remove(object);

    }
}
