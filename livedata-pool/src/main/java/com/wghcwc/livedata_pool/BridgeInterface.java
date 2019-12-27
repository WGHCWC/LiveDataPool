package com.wghcwc.livedata_pool;


import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * @author wghcwc
 * @date 19-11-10
 */
@Deprecated
public interface BridgeInterface<T> {

    void observe(LifecycleOwner owner, Observer<T> obs);

    void setValue(T t);

    void postValue(T t);



}