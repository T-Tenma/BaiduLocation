package com.tenma.baidulocation;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Tenma on 2016/11/27.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
