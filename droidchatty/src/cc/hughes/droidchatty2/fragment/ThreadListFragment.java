package cc.hughes.droidchatty2.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import cc.hughes.droidchatty2.FragmentContextActivity;
import cc.hughes.droidchatty2.LoadMoreArrayAdapter;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.net.ChattyService;
import cc.hughes.droidchatty2.net.ThreadList;
import cc.hughes.droidchatty2.net.ThreadList.RootPost;
import cc.hughes.droidchatty2.text.TagParser;
import cc.hughes.droidchatty2.util.TimeUtil;

/**
 * A list fragment representing a list of Threads. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ThreadDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ThreadListFragment extends ListFragment {

    final static String TAG = "ThreadListFragment";
    
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThreadListFragment() {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setListAdapter(new ThreadListAdapter(getActivity(), R.layout.thread_list_item, R.layout.row_loading, R.layout.row_finished));
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        getListView().setOnScrollListener((ThreadListAdapter)getListAdapter());
        setActivateOnItemClick(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.thread_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.thread_list_refresh)
        {
            ThreadListAdapter a = (ThreadListAdapter)getListAdapter();
            a.clear();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        RootPost rootPost = (RootPost)getListAdapter().getItem(position);
        Bundle args = new Bundle();
        args.putByteArray(ThreadDetailFragment.ARG_ROOT_POST, rootPost.toByteArray());
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(args);
        
		FragmentContextActivity fca = (FragmentContextActivity)getActivity();
		fca.changeContext(fragment, 1);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }
    
    class ThreadListAdapter extends LoadMoreArrayAdapter<RootPost> {
    	
        private ChattyService mService;
        private int mCurrentPage = 0;
        private int mLayoutRes;
        private LayoutInflater mInflater;
        private List<RootPost> mItemCache;
        
        public ThreadListAdapter(Context context, int itemResource, int loadingResource, int finishedResource) {
            super(context, loadingResource, finishedResource);
            mLayoutRes = itemResource;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mService = new ChattyService();
        }

        @Override
        public void clear() {
            super.clear();
            mCurrentPage = 0;
        }

        @Override
        protected boolean loadItems() throws Exception {
            ThreadList threads = mService.getPage(mCurrentPage + 1);
            if (threads.thread.size() > 0) {
                mItemCache = threads.thread;
                mCurrentPage += 1;
                return true;
            }
            
            mItemCache = null;
            return false;
        }

        @Override
        protected void appendItems() {
            if (mItemCache != null) {
                super.addAll(mItemCache);
                mItemCache = null;
            }
        }

		@Override
		public View getNormalView(int position, View convertView, ViewGroup parent) {
		    ViewHolder holder;
		    
		    if (convertView == null) {
		       convertView = mInflater.inflate(mLayoutRes, null);
		       holder = new ViewHolder();
               ViewInjector.inject(holder, convertView);
		       convertView.setTag(holder);
		    }
		    else {
		        holder = (ViewHolder)convertView.getTag();
		    }
			
		    RootPost rootPost = getItem(position);
		    
		    int replies = rootPost.replies - 1;
		    String replyText = replies + (replies == 1 ? " reply" : " replies");
		    String timeAgo = TimeUtil.format(getContext(), rootPost.date);
		    		    
		    holder.threadCategory.setText(rootPost.category);
		    holder.authorName.setText(rootPost.author);
		    holder.postContent.setText(TagParser.fromHtml(rootPost.body));
		    holder.threadReplies.setText(replyText);
		    holder.postTime.setText(timeAgo);
		    
			return convertView;
		}
		
		class ViewHolder {
           @ViewInjected(R.id.thread_category)
		   TextView threadCategory;

           @ViewInjected(R.id.author_name)
		   TextView authorName;

           @ViewInjected(R.id.post_content)
		   TextView postContent;

           @ViewInjected(R.id.thread_replies)
		   TextView threadReplies;

           @ViewInjected(R.id.post_time)
		   TextView postTime;
		}
    }
    
}
