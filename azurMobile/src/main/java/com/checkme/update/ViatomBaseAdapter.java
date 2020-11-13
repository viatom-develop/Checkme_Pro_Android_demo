package com.checkme.update;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by gongguopei on 2018/1/8.
 */

public class ViatomBaseAdapter<T> extends BaseAdapter implements AbsListView.OnScrollListener{

    protected Collection<T> mData;
    protected final int mItemLayoutId;
    protected AbsListView mList;
    protected boolean isScrolling;
    protected Context mCxt;
    protected LayoutInflater mInflater;

    private AbsListView.OnScrollListener listener;

    public ViatomBaseAdapter(AbsListView view, Collection<T> mData, int itemLayoutId) {
        if (mData == null) {
            mData = new ArrayList<T>(0);
        }
        this.mData = mData;
        this.mItemLayoutId = itemLayoutId;
        this.mList = view;
        this.mCxt = view.getContext();
        this.mInflater = LayoutInflater.from(mCxt);
        mList.setOnScrollListener(this);
    }

    public void addOnScrollListener(AbsListView.OnScrollListener l) {
        this.listener = l;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        if (mData instanceof List) {
            return ((List<T>) mData).get(position);
        } else if (mData instanceof Set) {
            return new ArrayList<T>(mData).get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AdapterHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder, getItem(position), isScrolling, position);
        return viewHolder.getConvertView();
    }

    private AdapterHolder getViewHolder(int position, View convertView,
                                        ViewGroup parent) {
        return AdapterHolder.get(convertView, parent, mItemLayoutId, position);
    }

    public void convert(AdapterHolder helper, T item, boolean isScrolling) {
    }

    public void convert(AdapterHolder helper, T item, boolean isScrolling, int position) {
        convert(helper, getItem(position), isScrolling);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 设置是否滚动的状态
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isScrolling = false;
            this.notifyDataSetChanged();
        } else {
            isScrolling = true;
        }
        if (listener != null) {
            listener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (listener != null) {
            listener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
    }
}
