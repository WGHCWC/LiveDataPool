package com.wghcwc.livedata_pool;




/**
 * @author wghcwc
 * @date 19-10-19
 */
@Deprecated
public class LiveDataPoolSlow {
  /*  private static volatile LiveDataPoolSlow defaultInstance;
    private final Map<Class<?>, Map<String, LiveDataInfo>> DATA_INFO_CACHE = new ConcurrentHashMap<>();
    private final Map<Class<?>, LinkedList<Object>> objectMap = new HashMap<>();
    private final Map<Object, Map<String, MutableLiveData>> activeData = new ConcurrentHashMap<>();


    public static LiveDataPoolSlow pool() {
        if (defaultInstance == null) {
            synchronized (LiveDataPoolSlow.class) {
                if (defaultInstance == null) {
                    defaultInstance = new LiveDataPoolSlow();
                }
            }
        }
        return defaultInstance;
    }

    public void push(Object obj) {
        Class<?> aClass = obj.getClass();
        Map<String, LiveDataInfo> liveDataInfo = getLiveDataInfo(aClass);
        if (liveDataInfo.isEmpty()) {
            return;
        }
        LinkedList<Object> objectLinkedList = objectMap.get(aClass);
        if (objectLinkedList == null) {
            objectLinkedList = new LinkedList<>();
        }
        objectLinkedList.push(obj);
        objectMap.put(aClass, objectLinkedList);
    }

    private Map<String, LiveDataInfo> getLiveDataInfo(Class<?> aClass) {

        Map<String, LiveDataInfo> liveDataInfoList = DATA_INFO_CACHE.get(aClass);
        if (liveDataInfoList != null) {
            return liveDataInfoList;
        }
        liveDataInfoList = new HashMap<>();
        Field[] fields = aClass.getDeclaredFields();
        if (fields.length == 0) {
            return liveDataInfoList;
        }
        boolean enableMultipleObj = false;
        MultipleObj multipleObj = aClass.getAnnotation(MultipleObj.class);
        if (multipleObj != null) {
            enableMultipleObj = true;
        }
        for (Field field : fields) {
            if (!LiveData.class.isAssignableFrom(field.getType())) {
                continue;
            }
            Push push = field.getAnnotation(Push.class);
            if (push == null) {
                continue;
            }
            Class<?> valueType = push.type();
            String tag = push.tag();
            String fieldName = field.getName();
            if ("".equals(tag)) {
                tag = fieldName;
            }
            String getterMethodName = getGetterMethodName(fieldName);
            liveDataInfoList.put(tag, new LiveDataInfo(valueType, aClass, tag, getterMethodName,
                    fieldName, field, enableMultipleObj));
        }
        DATA_INFO_CACHE.put(aClass, liveDataInfoList);
        return liveDataInfoList;
    }


    public String getGetterMethodName(String fieldName) {

        char[] chars = fieldName.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char) (chars[0] - 32);
        }
        return "get" + new String(chars);
    }

    private class LiveDataInfo {
        private Class<?> valueType;
        private Class<?> clazz;
        private String tag;
        private String getterMethodName;
        private String fieldName;
        private MutableLiveData target;
        private Field field;
        private boolean enableMultipleObj;

        private LiveDataInfo(Class<?> valueType, Class<?> clazz, String tag, String getterMethodName,
                             String fieldName, Field field, boolean enableMultipleObj) {
            this.valueType = valueType;
            this.clazz = clazz;
            this.tag = tag;
            this.getterMethodName = getterMethodName;
            this.fieldName = fieldName;
            this.field = field;
            this.enableMultipleObj = enableMultipleObj;
        }
    }

    @SuppressWarnings("unchecked")
    public void set(Class<?> tClass, String tag, Object val) {
        Map<String, LiveDataInfo> liveDataMap = DATA_INFO_CACHE.get(tClass);
        if (liveDataMap == null) {
            return;
        }
        LiveDataInfo liveDataInfo = liveDataMap.get(tag);
        if (liveDataInfo == null) {
            return;
        }
        LinkedList<Object> objects = objectMap.get(tClass);
        if (objects == null) {
            return;
        }
        for (Object object : objects) {
            Map<String, MutableLiveData> mutableLiveDataList = activeData.get(object);
            if (mutableLiveDataList == null) {
                mutableLiveDataList = new HashMap<>();
                activeData.put(object, mutableLiveDataList);
            }
            MutableLiveData mutableLiveData = mutableLiveDataList.get(tag);
            if (mutableLiveData == null) {
                mutableLiveData = getDataObj(liveDataInfo, object);
                mutableLiveDataList.put(tag, mutableLiveData);
            }
            if (!liveDataInfo.enableMultipleObj) {
                if (mutableLiveData.hasActiveObservers()) {
                    if (liveDataInfo.valueType.isInstance(val)) {
                        mutableLiveData.setValue(val);
                    } else {
                        Log.e("LiveDataPool", "TypeError: cant cast " + val.getClass().getName() + "to" + liveDataInfo.valueType);
                    }
                    break;
                } else {
                    continue;
                }
            }
            if (liveDataInfo.valueType.isInstance(val)) {
                mutableLiveData.setValue(val);
            } else {
                Log.e("LiveDataPool", "TypeError: cant cast " + val.getClass().getName() + "to" + liveDataInfo.valueType);
            }
        }
    }


    private MutableLiveData getDataByMethod(LiveDataInfo liveDataInfo, Object obj) {

        try {
            Method getMethod = liveDataInfo.clazz.getMethod(liveDataInfo.getterMethodName);
            return (MutableLiveData) getMethod.invoke(obj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new NullPointerException(liveDataInfo.fieldName + "不可访问并且没有get方法" + liveDataInfo.getterMethodName + "()");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException(liveDataInfo.getterMethodName + "()不可访问");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalStateException(liveDataInfo.getterMethodName + "()调用失败");
        }
    }

    private MutableLiveData getDataObj(LiveDataInfo liveDataInfo, Object obj) {
        if (liveDataInfo.field.getModifiers() == Modifier.PUBLIC) {
            try {
                return (MutableLiveData) liveDataInfo.field.get(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return getDataByMethod(liveDataInfo, obj);

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

    }*/
}
