package cc.hughes.droidchatty;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import cc.hughes.droidchatty.net.ChattyService;
import cc.hughes.droidchatty.net.Message.ThreadList;
import cc.hughes.droidchatty.net.Message.ThreadList.RootPost;
import cc.hughes.droidchatty.util.TimeUtil;

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
        
        setListAdapter(new ThreadListAdapter(getActivity(), R.layout.thread_list_item, R.layout.row_loading, R.layout.row_finished));
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        getListView().setOnScrollListener((ThreadListAdapter)getListAdapter());
        setActivateOnItemClick(true);
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
        protected boolean loadItems() throws Exception {
            ThreadList threads = mService.getPage(mCurrentPage + 1);
            if (threads.getThreadCount() > 0) {
                mItemCache = threads.getThreadList();
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
		       holder.threadCategory = (TextView)convertView.findViewById(R.id.thread_category);
		       holder.authorName = (TextView)convertView.findViewById(R.id.author_name);
		       holder.postContent = (TextView)convertView.findViewById(R.id.post_content);
		       holder.threadReplies = (TextView)convertView.findViewById(R.id.thread_replies);
		       holder.postTime = (TextView)convertView.findViewById(R.id.post_time);
		       convertView.setTag(holder);
		    }
		    else {
		        holder = (ViewHolder)convertView.getTag();
		    }
			
		    RootPost rootPost = getItem(position);
		    
		    int replies = rootPost.getReplies() - 1;
		    String replyText = replies + (replies == 1 ? " reply" : " replies");
		    
		    long time = TimeUtil.parseDateTime(rootPost.getDate());
		    CharSequence timeAgo = DateUtils.getRelativeDateTimeString(getContext(), time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
		    
		    holder.threadCategory.setText(rootPost.getCategory());
		    holder.authorName.setText(rootPost.getAuthor());
		    holder.postContent.setText(Html.fromHtml(rootPost.getBody()));
		    holder.threadReplies.setText(replyText);
		    holder.postTime.setText(timeAgo);
		    
			return convertView;
		}
		
		class ViewHolder {
		   TextView threadCategory;
		   TextView authorName;
		   TextView postContent;
		   TextView threadReplies;
		   TextView postTime;
		}
    }
    
}
