package com.daliammao.swipelayout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daliammao.swipelayout.R;
import com.daliammao.widget.swipelayoutlib.SwipeLayout;

public class MainActivity extends AppCompatActivity {

    private SwipeLayout swipeLayout;
    private SwipeLayout people;
    TextView face;

    private Switch switchLeft;
    private Switch switchTop;
    private Switch switchRight;
    private Switch switchBottom;
    private Switch switchModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeLayout = (SwipeLayout) findViewById(R.id.godfather);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.back_left));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Top, swipeLayout.findViewById(R.id.back_top));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.back_right));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Bottom, swipeLayout.findViewById(R.id.back_bottom));

        switchLeft = (Switch) findViewById(R.id.switch_left);
        switchTop = (Switch) findViewById(R.id.switch_top);
        switchRight = (Switch) findViewById(R.id.switch_right);
        switchBottom = (Switch) findViewById(R.id.switch_bottom);
        switchModel = (Switch) findViewById(R.id.switch_model);

        face = (TextView) findViewById(R.id.tv_face);
        Button goList = (Button) findViewById(R.id.go_list);
        goList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        switchLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                face.setText("( ↙ω↙ )");
                swipeLayout.setLeftSwipeEnabled(isChecked);
            }
        });

        switchTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                face.setText("( ↑ω↑ )");
                swipeLayout.setTopSwipeEnabled(isChecked);
            }
        });

        switchRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                face.setText("( ↘ω↘ )");
                swipeLayout.setRightSwipeEnabled(isChecked);
            }
        });

        switchBottom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                face.setText("( ↓ω↓ )");
                swipeLayout.setBottomSwipeEnabled(isChecked);
            }
        });

        switchModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                swipeLayout.setShowMode(isChecked ? SwipeLayout.ShowMode.LayDown : SwipeLayout.ShowMode.PullOut);
            }
        });

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
                switch (edge) {
                    case Left:
                        face.setText("( ↙ω↙ )");
                        break;
                    case Top:
                        face.setText("( ↑ω↑ )");
                        break;
                    case Right:
                        face.setText("( ↘ω↘ )");
                        break;
                    case Bottom:
                        face.setText("( ↓ω↓ )");
                        break;
                }
            }

            @Override
            public void onOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onStartClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
                face.setText("( ˇωˇ )");

                if (edge == SwipeLayout.DragEdge.Bottom) {
                    Toast.makeText(MainActivity.this, "请关闭底部滑动权限\n上滑围观群众", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onUpdate(SwipeLayout layout, SwipeLayout.DragEdge edge, int leftOffset, int topOffset, boolean isCloseBeforeDragged) {

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

        people = (SwipeLayout) findViewById(R.id.people);

        people.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
                switch (edge) {
                    case Left:
                        Toast.makeText(MainActivity.this, "你竟然发现了彩蛋", Toast.LENGTH_LONG).show();
                        break;
                    case Bottom:
                        Toast.makeText(MainActivity.this, "你竟然发现了隐藏关卡", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onStartClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, SwipeLayout.DragEdge edge, int leftOffset, int topOffset, boolean isCloseBeforeDragged) {

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 0) {
            switchLeft.setChecked(false);
            people.close(false);
            Toast.makeText(MainActivity.this, "右滑围观群众即可获得签订契约方法,你准备好了吗?", Toast.LENGTH_LONG).show();
        }
    }
}
