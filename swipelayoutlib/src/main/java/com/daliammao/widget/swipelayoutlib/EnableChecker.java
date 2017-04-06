package com.daliammao.widget.swipelayoutlib;

import android.view.View;

import com.daliammao.widget.swipelayoutlib.utils.ScrollUtil;

/**
 * @author: zhoupengwei
 * @time:16/4/1-下午8:32
 * @Email: 496946423@qq.com
 * @desc: 检查对应的view能否进行滑动
 */
public class EnableChecker {

    public static boolean LeftToSwipeEnable(View view) {
        return !ScrollUtil.checkLeftToRight(view);
    }

    public static boolean LeftBackSwipeEnable(View view) {
        return !ScrollUtil.checkRightToLeft(view);
    }

    public static boolean TopToSwipeEnable(View view) {
        return !ScrollUtil.checkTopToBottom(view);
    }

    public static boolean TopBackSwipeEnable(View view) {
        return !ScrollUtil.checkBottomToTop(view);
    }

    public static boolean RightToSwipeEnable(View view) {
        return !ScrollUtil.checkRightToLeft(view);
    }

    public static boolean RightBackSwipeEnable(View view) {
        return !ScrollUtil.checkLeftToRight(view);
    }

    public static boolean BottomToSwipeEnable(View view) {
        return !ScrollUtil.checkBottomToTop(view);
    }

    public static boolean BottomBackSwipeEnable(View view) {
        return !ScrollUtil.checkTopToBottom(view);
    }
}
