package cc.hughes.droidchatty;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cc.hughes.droidchatty.dummy.DummyContent;
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

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onThreadListItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onThreadListItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThreadListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ThreadListAdapter(getActivity(), R.layout.thread_list_item)); 
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setActivateOnItemClick(true);
        
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        
        ThreadListLoadTask task = new ThreadListLoadTask((ThreadListAdapter)getListAdapter());
        task.execute(1);
        
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onThreadListItemSelected(DummyContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
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

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
    class ThreadListAdapter extends ArrayAdapter<RootPost> {
    	
        private int mLayoutRes;
        private LayoutInflater mInflater;
        
    	public ThreadListAdapter(Context context, int resource) {
			super(context, resource);
			
			mLayoutRes = resource;
			mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		ThreadList mThreadList;
    	
    	public void addThreads(ThreadList list) {
    		mThreadList = list;
    		super.notifyDataSetChanged();
    	}   	
    	
		@Override
		public int getCount() {
			return mThreadList == null ? 0 : mThreadList.getThreadCount();		
		}

		@Override
		public RootPost getItem(int index) {
			return mThreadList == null ? null : mThreadList.getThread(index);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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
		    Log.i("DroidChatty", rootPost.getDate());
		    
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

    class ThreadListLoadTask extends AsyncTask<Integer, Void, ThreadList> {

    	ThreadListAdapter mAdapter;
    	
    	public ThreadListLoadTask(ThreadListAdapter adapter) {
    		mAdapter = adapter;
    	}
    	
		@Override
		protected ThreadList doInBackground(Integer... args) {
			
			int page = args[0];
			
			ChattyService service = new ChattyService();
			try {
				return service.getPage(page);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(ThreadList result) {
			mAdapter.addThreads(result);
		}
    	
    }
    
}
