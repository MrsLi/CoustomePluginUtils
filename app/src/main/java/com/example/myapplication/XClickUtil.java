package com.example.myapplication;

import android.view.View;

public class XClickUtil {


    /**
     * 最近一次点击的时间
     */
    private static long mLastClickTime;
    /**
     * 最近一次点击的控件ID
     */
    private static int mLastClickViewId;
    /**
     * 点击对象的hashcode
     */
    private static int mHascode;

    /**
     * 是否是快速点击
     *
     * @param v  点击的控件
     * @return  true:是，false:不是
     */
    public static boolean isFastDoubleClick(int hashcode, View v) {
//        Log.i("Xclick", "isFastDoubleClick: "+hashcode);
        int viewId = v.getId();
        long time = System.currentTimeMillis();
        long timeInterval = Math.abs(time - mLastClickTime);
        if (timeInterval < 1000 && viewId == mLastClickViewId &&hashcode==mHascode) {
            return true;
        } else {
            mLastClickTime = time;
            mLastClickViewId = viewId;
            mHascode = hashcode;
            return false;
        }
    }
}
