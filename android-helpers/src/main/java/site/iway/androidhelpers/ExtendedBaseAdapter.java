package site.iway.androidhelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class ExtendedBaseAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected LayoutInflater mLayoutInflater;

    public ExtendedBaseAdapter(Context context) {
        mContext = context;
        String serviceName = Context.LAYOUT_INFLATER_SERVICE;
        Object service = mContext.getSystemService(serviceName);
        mLayoutInflater = (LayoutInflater) service;
    }

    protected List<T> mData = new ArrayList<>();

    public void setData(List<T> data) {
        mData.clear();
        if (data != null)
            mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        if (data != null)
            mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(T data) {
        mData.add(data);
        notifyDataSetChanged();
    }

    protected OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
