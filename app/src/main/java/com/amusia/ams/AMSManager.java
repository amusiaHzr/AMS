package com.amusia.ams;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public final class AMSManager {

    public static void hookAms(final Context context)
            throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            NoSuchFieldException {

        Object mIActivityManager = null;
        Object mSingleton = null;


        if (OSVersion.isAndroidOS_21_22_23_24_25()) {  //android 低版本 21~25
            Class mActivityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");

            Method getDefault = mActivityManagerNativeClass.getDeclaredMethod("getDefault");
            getDefault.setAccessible(true);
            mIActivityManager = getDefault.invoke(null); //得到ams对象

            Field gDefault = mActivityManagerNativeClass.getDeclaredField("gDefault");
            gDefault.setAccessible(true);
            mSingleton = gDefault.get(null); //得到当前ActivityManagerNative中的singleton属性

        } else if (OSVersion.isAndroidOS_26_27_28()) {  //android 高版本 26~28
            Class mActivityManagerClass = Class.forName("android.app.ActivityManager");
            Method mGetServiceMethod = mActivityManagerClass.getMethod("getService");
            mGetServiceMethod.setAccessible(true);
            mIActivityManager = mGetServiceMethod.invoke(null);


            Field iActivityManagerSingletonField =
                    mActivityManagerClass.getDeclaredField("IActivityManagerSingleton");
            iActivityManagerSingletonField.setAccessible(true);
            mSingleton = iActivityManagerSingletonField.get(null);
        }

        /**
         * android 21~28使用的是IActivityManager 可以通用
         * android 29 用的是 ActivityTaskManager 需要单独处理
         */
        if (OSVersion.isAndroidOS_26_27_28() || OSVersion.isAndroidOS_21_22_23_24_25()) {
            Class mIActivityManagerProxy = Class.forName("android.app.IActivityManager"); // hook系统的IActivityManager
            final Object finalMIActivityManager = mIActivityManager;  // 这个其实就是AMS对象
            Object proxy = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{mIActivityManagerProxy}, new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    if ("startActivity".equals(method.getName())) {
                        Intent proxyIntent = new Intent(context, ProxyActivity.class);

                        Intent target = (Intent) objects[2];
                        objects[2] = proxyIntent;
                        proxyIntent.putExtra("target", target);
                    }
                    //用反射得到的AMS去执行startActivity方法
                    return method.invoke(finalMIActivityManager, objects);
                }
            });
            /**
             * 将Ams对象中的singleton属性设置为动态代理对象
             */
            //得到singleton类
            Class mSingletonClass = Class.forName("android.util.Singleton");
            //得到singleton属性
            Field mInstance = mSingletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            //把Ams中的singleton设置为动态代理对象
            mInstance.set(mSingleton, proxy);
        }

        /**
         * android 10.0 单独处理
         */
        if (OSVersion.isAndroidOS_29()) {

            /**
             * 通过10.0源码阅读可以知道
             *  IActivityTaskManager的目录为： android.app.IActivityTaskManager
             * 这个接口是通过 frameworks/base/core/java/android/app/IActivityTaskManager.aidl
             * 这个AIDL文件生成的接口
             */
            Object mIActivityTaskManager = null;


            Class mActivityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");


            Field iActivityTaskManagerSingletonField = mActivityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton");
            iActivityTaskManagerSingletonField.setAccessible(true);
            mSingleton = iActivityTaskManagerSingletonField.get(null);

            /**
             * 这里由于10.0 hide 了getService 无法通过getService得到mIActivityTaskManager
             */
//            Method mGetServiceMethod = mActivityTaskManagerClass.getDeclaredMethod("getService");
//            mGetServiceMethod.setAccessible(true);
//            mIActivityTaskManager = mGetServiceMethod.invoke(null); //得到activity_task


            /**
             * 这里由于10.0 hide 了getService 所以需要通过 Singleton 的get方法得到 mIActivityTaskManager
             */
            Class SingletonClass = Class.forName("android.util.Singleton");
            Method getMethod = SingletonClass.getDeclaredMethod("get");
            getMethod.setAccessible(true);
            mIActivityTaskManager = getMethod.invoke(mSingleton);


            Class proxyIActivityTaskManagerClass = Class.forName("android.app.IActivityTaskManager");
            final Object finalMIActivityTaskManager = mIActivityTaskManager;
            Object proxyIActivityTaskManager = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{proxyIActivityTaskManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {

                    if ("startActivity".equals(method.getName())) {
                        Intent proxyIntent = new Intent(context, ProxyActivity.class);
                        Intent target = (Intent) objects[2];
                        objects[2] = proxyIntent;
                        proxyIntent.putExtra("target", target);
                    }
                    return method.invoke(finalMIActivityTaskManager, objects);
                }
            });

            Class mSingletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            mInstanceField.set(mSingleton, proxyIActivityTaskManager);
        }

    }

    public static void hookActivityThread(final Context context) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        //获取ActivityThread对象
        Class mActivityThreadClass = Class.forName("android.app.ActivityThread");
        Field mActivityThreadField = mActivityThreadClass.getDeclaredField("sCurrentActivityThread");
        mActivityThreadField.setAccessible(true);
        Object mActivityThread = mActivityThreadField.get(null); //此时获取到的就是真正的ActivityThread对象

        //  获取Handler对象
        Field mHField = mActivityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mH = (Handler) mHField.get(mActivityThread);//通过当前ActivityThread对象获取mH对象

        //设置Handler中的callback
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        if (OSVersion.isAndroidOS_21_22_23_24_25()) {
            mCallbackField.set(mH, new MyCallback_21_22_23_24_25());
        } else if (OSVersion.isAndroidOS_26_27_28() || OSVersion.isAndroidOS_29()) {
            mCallbackField.set(mH, new MyCallback_26_27_28());
        }

    }

    /**
     * android 21~25的Handler回调
     */
    private static final class MyCallback_21_22_23_24_25 implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                try {
                    Object mActivityClientRecord = msg.obj; //得到mActivityClientRecord对象
                    Field intentField = mActivityClientRecord.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent proxyIntent = (Intent) intentField.get(mActivityClientRecord);
                    Intent targetIntent = proxyIntent.getParcelableExtra("target");
                    if (targetIntent != null) {
                        intentField.set(mActivityClientRecord, targetIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    /**
     * android 26~28的Handler回调
     */
    private static final class MyCallback_26_27_28 implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 159) { // EXECUTE_TRANSACTION
                try {
                    Object mClientTransaction = msg.obj; //得到ClientTransaction对象

                    Class mClientTransactionClass = Class.forName("android.app.servertransaction.ClientTransaction");
                    Field mActivityCallbacksField = mClientTransactionClass.getDeclaredField("mActivityCallbacks");
                    mActivityCallbacksField.setAccessible(true);
                    List mActivityCallbacks = (List) mActivityCallbacksField.get(mClientTransaction);
                    if (mActivityCallbacks.size() == 0) {
                        return false;
                    }

                    Object mLaunchActivityItem = mActivityCallbacks.get(0);

                    Class mLaunchActivityItemClass = Class.forName("android.app.servertransaction.LaunchActivityItem");

                    if (mLaunchActivityItemClass.isInstance(mLaunchActivityItem)) {

                        Field intentField = mLaunchActivityItemClass.getDeclaredField("mIntent");
                        intentField.setAccessible(true);
                        Intent proxyIntent = (Intent) intentField.get(mLaunchActivityItem);
                        Intent targetIntent = proxyIntent.getParcelableExtra("target");
                        if (targetIntent != null) {
                            intentField.set(mLaunchActivityItem, targetIntent);
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
