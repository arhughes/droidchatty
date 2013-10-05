package cc.hughes.droidchatty2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class LoadMoreArrayAdapter<T> extends BaseAdapter implements OnScrollListener {

    protected final static int LAYOUT_NONE = -1;

    private Context mContext;
    private List<T> mObjects;
    private boolean mNotifyOnChange = true;

    private final Object mLock = new Object();

    int mLoadingRes;
    int mFinishedRes;
    
    LayoutInflater mInflater;
    View mLoadingView;
    
    AtomicBoolean mKeepLoading = new AtomicBoolean(true);

    public LoadMoreArrayAdapter(Context context, int loadingResource, int finishedResource) {
        this(context, loadingResource, finishedResource, new ArrayList<T>());
    }

    public LoadMoreArrayAdapter(Context context, int loadingResource, int finishedResource, List<T> items) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLoadingRes = loadingResource;
        mFinishedRes = finishedResource;
        mObjects = items;
    }

    public Context getContext() {
        return mContext;
    }

    public List<T> getItems() {
        return mObjects;
    }

    public void add(T item) {
        synchronized (mLock) {
            mObjects.add(item);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addAll(Collection<? extends T> items) {
        synchronized (mLock) {
            mObjects.addAll(items);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }
    
    @Override
    public void onScroll(AbsListView aview, int firstVisible, int visibleCount, int totalCount) {
        if (mKeepLoading.get() && mLoadingView == null && (firstVisible + visibleCount + 2) >= totalCount) {
            mLoadingView = getLoadingView(aview);
            startLoadingItems();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public int getCount() {
        if (mKeepLoading.get() || mFinishedRes != LAYOUT_NONE)
            return mObjects.size() + 1;
        return mObjects.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        if (position == mObjects.size())
            return IGNORE_ITEM_VIEW_TYPE;
        return super.getItemViewType(position);
    }
    
    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }
    
    @Override
    public T getItem(int position) {
        if (position >= mObjects.size())
            return null;
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    
    @Override
    public boolean isEnabled(int position) {
        
        // disable the loading item, but enable the finished item
        if (position >= mObjects.size())
            return !mKeepLoading.get();
        
        return super.isEnabled(position);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == mObjects.size()) {
            if (mKeepLoading.get()) {
                if (mLoadingView == null) {
                    mLoadingView = getLoadingView(parent);
                    startLoadingItems();
                }
                return mLoadingView;
            } else if (!mKeepLoading.get()) {
                return getFinishedView(parent);
            }
        }
     
        return getNormalView(position,  convertView, parent);
    }
    
    protected View getLoadingView(ViewGroup parent) {
       return mInflater.inflate(mLoadingRes, parent, false);
    }
    
    protected View getFinishedView(ViewGroup parent) {
       View view = mInflater.inflate(mFinishedRes, parent, false);
       view.setOnClickListener(mFinishedOnClickListener);
       return view;
    }

    protected void setKeepLoading(boolean value) {
        mKeepLoading.set(value);
        if (mNotifyOnChange) super.notifyDataSetChanged();
    }
    
    OnClickListener mFinishedOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mKeepLoading.set(true);
            LoadMoreArrayAdapter.super.notifyDataSetChanged();
        }
    };
    
    void startLoadingItems() {
        LoadItemsTask task = new LoadItemsTask(this);
        task.execute();
    }
    
    abstract protected View getNormalView(int position, View convertView, ViewGroup parent);
    abstract protected boolean loadItems() throws Exception;
    abstract protected void appendItems();
    
    class LoadItemsTask extends AsyncTask<Void, Void, Exception> {

        private static final String TAG = "LoadItemsTask";
        boolean mLoadMore = false;
        LoadMoreArrayAdapter<?> mAdapter;
        
        public LoadItemsTask(LoadMoreArrayAdapter<?> adapter) {
            mAdapter = adapter;
        }
        
        @Override
        protected Exception doInBackground(Void... arg0) {
            try {
                mLoadMore = mAdapter.loadItems();
            }
            catch (Exception e) {
                Log.i(TAG, "Error loading items", e);
                return e;
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Exception e) {
            
            mAdapter.mKeepLoading.set(mLoadMore);
            if (e == null)
                mAdapter.appendItems();
            
            mAdapter.mLoadingView = null;
            mAdapter.notifyDataSetChanged();
        }
    }
}