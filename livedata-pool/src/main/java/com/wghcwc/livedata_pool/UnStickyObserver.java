package com.wghcwc.livedata_pool;


import androidx.lifecycle.Observer;

/**
 * @author wghcwc
 * @date 19-10-22
 */
@Deprecated
public abstract class UnStickyObserver<K> implements Observer<K> {
    int version;
    Bridge bridge;
    @Override
    public void onChanged(K t) {
        if (bridge.mVersion > version) {
            unStickyObserver(t);
        }
    }
    public abstract void unStickyObserver(K t);

}
