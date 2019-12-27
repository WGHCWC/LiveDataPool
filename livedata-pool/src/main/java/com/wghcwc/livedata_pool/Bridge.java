package com.wghcwc.livedata_pool;

import android.util.Log;


import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author wghcwc
 * @date 19-11-10
 */
@Deprecated
public class Bridge<T> implements BridgeInterface<T>, LifecycleObserver {
    private MutableLiveData<T> tMutableLiveData;
    Class<T> valueType;
    private LinkedList<SlipObserver<T>> slipObservers;
    private LinkedList<NormalObserver> normalObservers;
    int mVersion;

    Bridge() {
        this.tMutableLiveData = new MutableLiveData<>();
        slipObservers = new LinkedList<>();
        normalObservers = new LinkedList<>();
    }

    @Override
    public void observe(LifecycleOwner owner, Observer<T> obs) {

        if (obs instanceof SlipObserver) {
            ((SlipObserver) obs).setOwner(owner);
            slipObservers.add((SlipObserver<T>) obs);
            owner.getLifecycle().addObserver(this);
        }

        if (obs instanceof UnStickyObserver) {
            ((UnStickyObserver<T>) obs).version = mVersion;
            ((UnStickyObserver<T>) obs).bridge = this;
        }
        if (obs instanceof NormalObserver) {
            ((NormalObserver) obs).setOwner(owner);
            normalObservers.add((NormalObserver) obs);
            owner.getLifecycle().addObserver(this);
            return;
        }
        tMutableLiveData.observe(owner, obs);
    }

    @Override
    public void setValue(T t) {
        if (checkType(t.getClass())) {
            checkSlip();
            checkNormal(t);
            tMutableLiveData.setValue(t);
        }
    }

    @Override
    public void postValue(T t) {
        if (checkType(t.getClass())) {
            checkSlip();
            checkNormal(t);
            tMutableLiveData.postValue(t);
        }
    }

    private void checkNormal(T t) {
        if (!normalObservers.isEmpty()) {
            for (NormalObserver normalObserver : normalObservers) {
                if (normalObservers != null) {
                    normalObserver.onChanged(t);

                }
            }
        }
    }


    private void checkSlip() {
        mVersion++;
        if (!slipObservers.isEmpty()) {
            for (SlipObserver slipObserver : slipObservers) {
                if (slipObserver == null) continue;
                if (slipObserver.getOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    slipObserver.setNeedSlip(false);
                } else {
                    slipObserver.setNeedSlip(true);
                }
            }
        }
    }

    private boolean checkType(Class<?> tClass) {
        if (valueType.isAssignableFrom(tClass)) {
            return true;
        }
        Log.e("ss", "setValue/postValue: Type Check Error. Expect: " + valueType.getSimpleName() + "But Found" + tClass.getSimpleName());
        return false;
    }


    public MutableLiveData getData() {
        return tMutableLiveData;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy(LifecycleOwner owner) {
        SlipObserver slipObserver;
        for (SlipObserver<T> observer : slipObservers) {
            if (observer.getOwner() == owner) {
                slipObservers.remove(observer);
                observer.getOwner().getLifecycle().removeObserver(this);
                break;
            }
        }
        NormalObserver normalObserver;
        for (NormalObserver observer : normalObservers) {
            if (observer.getOwner() == owner) {
                normalObservers.remove(observer);
                observer.getOwner().getLifecycle().removeObserver(this);
                break;
            }
        }
    }
}