package com.amusia.ams;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;

public class AMSApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            AMSManager.hookAms(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


        try {
            AMSManager.hookActivityThread(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
