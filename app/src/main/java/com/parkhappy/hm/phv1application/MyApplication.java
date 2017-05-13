package com.parkhappy.hm.phv1application;

import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by dell on 12/1/2016.
 */
public class MyApplication extends android.support.multidex.MultiDexApplication{//android.app.Application {
    private static Context mycontext;
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        MyApplication.mycontext=getApplicationContext();//

        MultiDex.install(this);
    }


    public static Context getAppContext() {//
        return MyApplication.mycontext;
    }

    public static synchronized MyApplication getInstance() {

        return mInstance;
    }


}
