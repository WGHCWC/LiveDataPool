package com.wghcwc.livedata_pool;


import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * @author wghcwc
 * @date 19-10-22
 */
@Deprecated
public abstract class NormalObserver<K> implements Observer<K> {
    private LifecycleOwner owner;

    public LifecycleOwner getOwner() {
        return owner;
    }

    public void setOwner(LifecycleOwner owner) {
        this.owner = owner;
    }
}
