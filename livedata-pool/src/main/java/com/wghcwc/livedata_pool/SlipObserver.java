package com.wghcwc.livedata_pool;


import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * @author wghcwc
 * @date 19-10-22
 */
@Deprecated
public abstract class SlipObserver<K> implements Observer<K> {
    private LifecycleOwner owner;
    private boolean needSlip;

    public LifecycleOwner getOwner() {
        return owner;
    }

    public boolean isNeedSlip() {
        return needSlip;
    }

    public void setNeedSlip(boolean needSlip) {
        this.needSlip = needSlip;
    }

    public void setOwner(LifecycleOwner owner) {
        this.owner = owner;
    }

    @Override
    public void onChanged(K t) {
        if (!needSlip) {
            slipObserver(t);
        }
    }


    public abstract void slipObserver(K t);

}
