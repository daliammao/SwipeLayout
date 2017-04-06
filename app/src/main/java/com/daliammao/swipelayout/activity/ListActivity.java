package com.daliammao.swipelayout.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.daliammao.swipelayout.R;
import com.daliammao.swipelayout.adapter.TextAdapter;
import com.daliammao.widget.swipelayoutlib.EnableChecker;
import com.daliammao.widget.swipelayoutlib.SwipeLayout;
import com.daliammao.widget.swipelayoutlib.handler.SwipeEnabledHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhoupengwei
 * @time:16/3/31-下午9:21
 * @Email: 496946423@qq.com
 * @desc:
 */
public class ListActivity extends Activity {

    private TextView mToolbarTitle;
    private Toolbar mToolbar;

    List<String> textListData = new ArrayList();
    List<String> textListData2 = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("第一节:探天应穴");
        mToolbar.setNavigationIcon(R.mipmap.ic_left_arrow);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        final ListView textlist = (ListView) findViewById(R.id.textlist);
        final ListView textlist2 = (ListView) findViewById(R.id.imagelist);
        final SwipeLayout swipeLayout = (SwipeLayout) findViewById(R.id.godfather);
        swipeLayout.setSwipeEnableHandler(new SwipeEnabledHandler() {
            @Override
            public boolean onSwipeEnabled(SwipeLayout layout, SwipeLayout.SwipeAction action) {
                switch (action) {
                    case SwipeTopTo:
                        return EnableChecker.TopToSwipeEnable(textlist);
                    case SwipeTopBack:
                        return EnableChecker.TopBackSwipeEnable(textlist2);
                    case SwipeBottomTo:
                        return EnableChecker.BottomToSwipeEnable(textlist);
                    case SwipeBottomBack:
                        return EnableChecker.BottomBackSwipeEnable(textlist2);
                }
                return false;
            }
        });

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
                mToolbarTitle.setText("第二节:挤按睛明穴");
            }

            @Override
            public void onStartClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, SwipeLayout.DragEdge edge, int leftOffset, int topOffset, boolean isCloseBeforeDragged) {
                mToolbarTitle.setText("第一节:探天应穴");
            }

            @Override
            public void onHandRelease(SwipeLayout layout, SwipeLayout.DragEdge edge, float xvel, float yvel) {
            }

            @Override
            public void onDragOpenToClose(boolean isCloseBeforeDragged) {

            }

            @Override
            public void onDragCloseToOpen(boolean isCloseBeforeDragged) {

            }
        });

        textListData.add("( ↑ω↑ )");
        textListData.add("( ↓ω↓ )");
        textListData.add("( ↑ω↑ )");
        textListData.add("( ↓ω↓ )");
        textListData.add("( ←ω← )");
        textListData.add("( →ω→ )");
        textListData.add("( ←ω← )");
        textListData.add("( →ω→ )");
        textListData.add("( ↙ω↙ )");
        textListData.add("( ↘ω↘ )");
        textListData.add("( ↙ω↙ )");
        textListData.add("( ↘ω↘ )");
        textListData.add("( ↖ω↖ )");
        textListData.add("( ↗ω↗ )");
        textListData.add("( ↖ω↖ )");
        textListData.add("( ↗ω↗ )");
        textListData.add("( ←ω→ )");
        textListData.add("( →ω← )");
        textListData.add("( ←ω→ )");
        textListData.add("( →ω← )");

        TextAdapter textAdapter = new TextAdapter(this, textListData, Color.WHITE);
        textlist.setAdapter(textAdapter);

        textListData2.add("( ↑ω↑ )");
        textListData2.add("( ↖ω↗ )");
        textListData2.add("( ←ω→ )");
        textListData2.add("( ↙ω↘ )");
        textListData2.add("( ↓ω↓ )");
        textListData2.add("( ↘ω↙ )");
        textListData2.add("( →ω← )");
        textListData2.add("( ↗ω↖ )");
        textListData2.add("( ↑ω↑ )");
        textListData2.add("( ↑ω↘ )");
        textListData2.add("( ↙ω↑ )");
        textListData2.add("( ↑ω↗ )");
        textListData2.add("( ↖ω↑ )");
        textListData2.add("( ←ω→ )");
        textListData2.add("( →ω← )");
        textListData2.add("( ↖ω↓ )");
        textListData2.add("( ↓ω↗ )");
        textListData2.add("( →ω↔ )");
        textListData2.add("( ↔ω← )");
        textListData2.add("( ↔ω↔ )");
        textListData2.add("(╯°O°)╯┻━┻");

        TextAdapter textAdapter2 = new TextAdapter(this, textListData2, Color.YELLOW);
        textlist2.setAdapter(textAdapter2);
    }
}
