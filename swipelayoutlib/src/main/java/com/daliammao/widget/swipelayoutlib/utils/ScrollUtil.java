package com.daliammao.widget.swipelayoutlib.utils;

import android.view.View;

/**
 * @author: zhoupengwei
 * @time:16/3/31-下午6:27
 * @Email: 496946423@qq.com
 * @desc: 判断View是否可以往一个方向滑动
 */
public class ScrollUtil {

    /**
     * 从触摸左往右滑,view可以滑动
     *
     * @param view
     * @return
     */
    public static boolean checkRightToLeft(View view){
        return view.canScrollHorizontally(1);
    }

    /**
     * 从触摸左往右滑,view可以滑动
     *
     * @param view
     * @return
     */
    public static boolean checkLeftToRight(View view){
        return view.canScrollHorizontally(-1);
    }

    /**
     * 从触摸上往下滑,view可以滑动
     *
     * @param view
     * @return
     */
    public static boolean checkTopToBottom(View view){
        return view.canScrollVertically(-1);
    }

    /**
     * 从触摸下往上滑,view可以滑动
     *
     * @param view
     * @return
     */
    public static boolean checkBottomToTop(View view){
        return view.canScrollVertically(1);
    }
}
