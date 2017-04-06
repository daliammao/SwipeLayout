package com.daliammao.swipelayout.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.daliammao.swipelayout.R;
import com.daliammao.widget.swipelayoutlib.SwipeLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: zhoupengwei
 * @time:16/3/31-下午9:25
 * @Email: 496946423@qq.com
 * @desc:
 */
public class TextAdapter extends BaseAdapter {
    private List<String> listData;
    private Set<Integer> isopen;
    private Context mContext;
    private int mItemColor;

    public TextAdapter(Context context, List<String> listData) {
        this(context, listData, Color.WHITE);
    }

    public TextAdapter(Context context, List<String> listData, int itemColor) {
        this.mContext = context;
        this.listData = listData;
        this.mItemColor = itemColor;
        this.isopen = new HashSet();
    }

    @Override
    public int getCount() {
        return listData == null ? 0 : listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData == null ? null : listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.adapter_text_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.explosion = (TextView) convertView.findViewById(R.id.explosion);
            holder.swipe = (SwipeLayout) convertView.findViewById(R.id.swipe);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String data = listData.get(position);
        convertView.setBackgroundColor(mItemColor);


        holder.text.setText(data);
        holder.swipe.removeAllSwipeListener();
        if(isopen.contains(position)){
            holder.swipe.open(false);
        }else{
            holder.swipe.close(false);
        }

        holder.swipe.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {

            }

            @Override
            public void onOpen(SwipeLayout layout, SwipeLayout.DragEdge edge) {
                isopen.add(position);
            }

            @Override
            public void onStartClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {

            }

            @Override
            public void onClose(SwipeLayout layout, SwipeLayout.DragEdge edge) {
                isopen.remove(position);
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
        holder.explosion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("和我签订契约成为魔法少女吧");
                builder.setTitle("提示");
                builder.setPositiveButton("今天没吃药", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((Activity)mContext).setResult(Activity.RESULT_OK);
                        ((Activity)mContext).finish();
                    }
                });
                builder.setNegativeButton("还是算了吧", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView text;
        TextView explosion;
        SwipeLayout swipe;
    }
}
