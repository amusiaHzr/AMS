package com.amusia.ams;

import android.os.Build;

public class OSVersion {

    /**
     * TODO 终极目标： AMS 全版本兼容
     *      * API Level 21 --- Android 5.0
     *      * API Level 22 --- Android 5.1
     *      * API Level 23 --- Android 6.0
     *      * API Level 24 --- Android 7.0
     *      * API Level 25 --- Android 7.1.1
     *      * API Level 26 --- Android 8.0
     *      * API Level 27 --- Android 8.1
     *      * API Level 28 --- Android 9.0
     */

    /**
     * todo 低版本的AMS类别
     * API Level 21  ----- Android 5.0
     * API Level 22  ----- Android 5.1
     * API Level 23  ----- Android 6.0
     * API Level 24  ----- Android 7.0
     * API Level 25  ----- Android 7.1
     */
    /**
     * 判断当前系统版本 21 22 23 24 25 以及 21版本之下的
     *
     * @return
     */
    public static boolean isAndroidOS_21_22_23_24_25() {
        int V = Build.VERSION.SDK_INT;
        if (V < 26) {
            return true;
        }
        return false;
    }

    /**
     * todo 高版本的AMS类别
     * API Level 26  ----- Android 8.0
     * API Level 27  ----- Android 8.1
     * API Level 28  ----- Android 9.0
     */
    public static boolean isAndroidOS_26_27_28() {
        int V = Build.VERSION.SDK_INT;
        if ((V > 26 || V == 26) && (V < 28 || V == 28)) {
            return true;
        }
        return false;
    }

    /**
     * API Level 29  -----  Android 10.0
     *
     * @return
     */
    public static boolean isAndroidOS_29() {
        int V = Build.VERSION.SDK_INT;
        if (V == 29) {
            return true;
        }
        return false;
    }


    /**
     * todo 最新版本的AMS类别
     * API Level 29  -----
     * API Level 30  -----
     * API Level 30+  -----
     */
}
