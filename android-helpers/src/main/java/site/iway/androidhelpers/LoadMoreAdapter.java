package site.iway.androidhelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class LoadMoreAdapter<T> extends BaseAdapter {

    public interface OnLoadMoreListener {
        public void onLoadMore();
    }

    protected Context mContext;
    protected LayoutInflater mLayoutInflater;

    public LoadMoreAdapter(Context context) {
        mContext = context;
        String serviceName = Context.LAYOUT_INFLATER_SERVICE;
        Object service = mContext.getSystemService(serviceName);
        mLayoutInflater = (LayoutInflater) service;
    }

    protected OnClickListener mOnClickListener;
    protected OnLoadMoreListener mOnLoadMoreListener;

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener l) {
        mOnLoadMoreListener = l;
    }

    protected List<T> mData;

    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        if (mData == null)
            mData = new ArrayList<>();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(T data) {
        if (mData == null)
            mData = new ArrayList<>();
        mData.add(data);
        notifyDataSetChanged();
    }

    public int getNormalCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public final int getCount() {
        return getNormalCount() + 1;
    }

    public T getNormalItem(int position) {
        return mData.get(position);
    }

    @Override
    public final T getItem(int position) {
        if (position == getCount() - 1)
            return null;
        return getNormalItem(position);
    }

    public long getNormalItemId(int position) {
        return position;
    }

    @Override
    public final long getItemId(int position) {
        if (position == getCount() - 1)
            return Long.MAX_VALUE;
        return getNormalItemId(position);
    }

    public int getNormalViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public final int getViewTypeCount() {
        return getNormalViewTypeCount() + 1;
    }

    public int getNormalViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public final int getItemViewType(int position) {
        if (position == getCount() - 1)
            return getViewTypeCount() - 1;
        return getNormalViewType(position);
    }

    private boolean mIsLoadingMoreData = false;
    private boolean mHasMoreData = true;

    public void setStatus(boolean loadingMoreData, boolean hasMoreData) {
        mIsLoadingMoreData = loadingMoreData;
        mHasMoreData = hasMoreData;
        notifyDataSetChanged();
    }

    public boolean isLoadingMoreData() {
        return mIsLoadingMoreData;
    }

    public boolean hasMoreData() {
        return mHasMoreData;
    }

    public abstract View getNormalView(int position, View convertView, ViewGroup parent);

    public abstract View getLoadMoreView(View convertView, ViewGroup parent);

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (position == getCount() - 1) {
            if (mHasMoreData && !mIsLoadingMoreData) {
                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
            return getLoadMoreView(convertView, parent);
        }
        return getNormalView(position, convertView, parent);
    }

}
