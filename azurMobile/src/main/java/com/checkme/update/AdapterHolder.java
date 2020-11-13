package com.checkme.update;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by gongguopei on 2018/1/8.
 */

public class AdapterHolder {
    private final SparseArray<View> mViews;
    private final int mPosition;
    private final View mConvertView;

    private AdapterHolder(ViewGroup parent, int layoutId, int position) {
        this.mPosition = position;
        this.mViews = new SparseArray<View>();
        mConvertView = LayoutInflater.from(parent.getContext()).inflate(
                layoutId, parent, false);
        // setTag
        mConvertView.setTag(this);
    }

    /**
     * 拿到全部View
     *
     * @return
     */
    public SparseArray<View> getAllView() {
        return mViews;
    }

    /**
     * 拿到一个ViewHolder对象
     *
     * @param convertView
     * @param parent
     * @param layoutId
     * @param position
     * @return
     */
    public static AdapterHolder get(View convertView, ViewGroup parent,
                                    int layoutId, int position) {
        if (convertView == null) {
            return new AdapterHolder(parent, layoutId, position);
        } else {
            return (AdapterHolder) convertView.getTag();
        }
    }

    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 通过控件的Id获取对于的控件，如果没有则加入views
     *
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param text
     * @return
     */
    public AdapterHolder setText(int viewId, CharSequence text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }




    public int getPosition() {
        return mPosition;
    }

}
