package com.daliammao.widget.swipelayoutlib.utils;

import android.content.Context;

/**
 * @author: zhoupengwei
 * @time:16/3/31-上午10:33
 * @Email: 496946423@qq.com
 * @desc:
 */
public class UiUtil {
    public static int dp2px(Context context,float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
