package cc.hughes.droidchatty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadListFragment extends ListFragment
{
    private boolean _dualPane;
    ThreadLoadingAdapter _adapter;

    // list view saved state while rotating
    private Parcelable _listState = null;
    private int _listPosition = 0;
    private int _itemPosition = 0;
    private int _itemChecked = ListView.INVALID_POSITION;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setRetainInstance(true);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

       	View singleThread = getActivity().findViewById(R.id.singleThread);
       	_dualPane = singleThread != null && singleThread.getVisibility() == View.VISIBLE;
       
       	getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
       
       	setHasOptionsMenu(true);
       	
       	if (_adapter == null)
       	{
       		// no adapter? must be a new view
       		_adapter = new ThreadLoadingAdapter(getActivity(), new ArrayList<Thread>());
       		setListAdapter(_adapter);
       	}
       	else
       	{
       		// user rotated the screen, try to go back to where they where
       		if (_listState != null)
       			getListView().onRestoreInstanceState(_listState);
       		getListView().setSelectionFromTop(_listPosition,  _itemPosition);
       		
       		if (_itemChecked != ListView.INVALID_POSITION)
       			getListView().setItemChecked(_itemChecked, true);
       	}
       	
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);

    	// we should put this info into the outState, but the compatibility framework
    	// seems to swallow it somewhere   	
    	ListView listView = getListView();
    	_listState = listView.onSaveInstanceState();
    	_listPosition = listView.getFirstVisiblePosition();
    	View itemView = listView.getChildAt(0);
    	_itemPosition = itemView == null ? 0 : itemView.getTop();
    	_itemChecked = listView.getCheckedItemPosition();
    }
        
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        showDetails(position);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.refresh:
                refreshThreads();
                return true;
            case R.id.add:
                post();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            case R.id.search:
                showSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void refreshThreads()
    {
        getListView().clearChoices();
        _adapter.clear();
    }
    
    private static final int POST_THREAD = 0;
    private void post()
    {
        Intent i = new Intent(getActivity(), ComposePostView.class);
        startActivityForResult(i, POST_THREAD);
    }
    
    private void showSettings()
    {
        Intent i = new Intent(getActivity(), PreferenceView.class);
        startActivity(i);
    }
    
    private void showSearch()
    {
        Intent i = new Intent(getActivity(), SearchView.class);
        startActivity(i);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case POST_THREAD:
                if (resultCode == Activity.RESULT_OK)
                {
                    // read the resulting thread id from the post
                    int postId = data.getExtras().getInt("postId");
                    
                    // hey, thats the same thing I just wrote!
                    Intent i = new Intent(getActivity(), SingleThreadView.class);
                    i.putExtra("postId", postId);
                    startActivity(i);
                }
                break;
            default:
                break;
        }
    }

    
    void showDetails(int index)
    {
        Thread thread = _adapter.getItem(index);
        
        // probably clicked the "Loading..." or something
        if (thread == null)
            return;
        
        getListView().setItemChecked(index, true);
        
        if (_dualPane)
        {
            ThreadViewFragment view = (ThreadViewFragment)getFragmentManager().findFragmentById(R.id.singleThread);
            if (view.getPostId() != thread.getThreadId())
                view.loadPost(Post.fromThread(thread));
        }
        else
        {
            Intent intent = new Intent();
            intent.setClass(getActivity(), SingleThreadView.class);
            intent.putExtra("postId", thread.getThreadId());
            intent.putExtra("userName", thread.getUserName());
            intent.putExtra("posted", thread.getPosted());
            intent.putExtra("content", thread.getContent());
            intent.putExtra("moderation", thread.getModeration());
            startActivity(intent);
        }
    }

    private void updatePostCounts(ArrayList<Thread> threads)
    {
        // set the number of replies that are new
        Hashtable<Integer, Integer> counts = getPostCounts();
        for (Thread t : threads)
        {
            if (counts.containsKey(t.getThreadId()))
                t.setReplyCountPrevious(counts.get(t.getThreadId()));
        }

        try
        {
            storePostCounts(counts, threads);
        } catch (IOException e)
        {
            // yeah, who cares
            Log.e("ThreadView", "Error storing post counts.", e);
        }
    }

    private Hashtable<Integer, Integer> getPostCounts()
    {
        Hashtable<Integer, Integer> counts = new Hashtable<Integer, Integer>();

        if (getActivity().getFileStreamPath("post_count.cache").exists())
        {
            // look at that, we got a file
            try {
                FileInputStream input = getActivity().openFileInput("post_count.cache");
                try
                {
                    DataInputStream in = new DataInputStream(input);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = reader.readLine();
                    while (line != null)
                    {
                        if (line.length() > 0)
                        {
                            String[] parts = line.split("=");
                            counts.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                        }
                        line = reader.readLine();
                    }
                }
                finally
                {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return counts;
    }

    final static int POST_CACHE_HISTORY = 1000;
    private void storePostCounts(Hashtable<Integer, Integer> counts, ArrayList<Thread> threads) throws IOException
    {
        // update post counts for threads viewing right now
        for (Thread t : threads)
            counts.put(t.getThreadId(), t.getReplyCount());

        List<Integer> postIds = Collections.list(counts.keys());
        Collections.sort(postIds);

        // trim to last 1000 posts
        if (postIds.size() > POST_CACHE_HISTORY)
            postIds.subList(postIds.size() - POST_CACHE_HISTORY, postIds.size() - 1);

        FileOutputStream output = getActivity().openFileOutput("post_count.cache", Activity.MODE_PRIVATE);
        try
        {
            DataOutputStream out = new DataOutputStream(output);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            for (Integer postId : postIds)
            {
                writer.write(postId + "=" + counts.get(postId));
                writer.newLine();
            }
            writer.flush();
        }
        finally
        {
            output.close();
        }
    }
    
    
    private class ThreadLoadingAdapter extends LoadingAdapter<Thread>
    {
    	private HashSet<Integer> _threadIds = new HashSet<Integer>();
        private int _pageNumber = 0;
        private Boolean _showTags;
        private Boolean _stripNewLines;
        private int _previewLines;
        
        public ThreadLoadingAdapter(Context context, ArrayList<Thread> items)
        {
            super(context, R.layout.row, R.layout.row_loading, items);
            setShowTags();
        }
        
        @Override
        public void clear()
        {
            _pageNumber = 0;
            _threadIds.clear();
            setShowTags();
            super.clear();
        }
        
        void setShowTags()
        {
            Activity activity = ThreadListFragment.this.getActivity();
            if (activity != null)
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                _showTags = prefs.getBoolean("showTagsInThreadList", true);
                _stripNewLines = prefs.getBoolean("previewStripNewLines", true);
                _previewLines = Integer.parseInt(prefs.getString("previewLineCount", "2"));
            }
        }
        
        @Override
        protected View createView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = (ViewHolder)convertView.getTag();
            if (holder == null)
            {
                holder = new ViewHolder();
                holder.moderation = (View)convertView.findViewById(R.id.threadModeration);
                holder.userName = (TextView)convertView.findViewById(R.id.textUserName);
                holder.content = (TextView)convertView.findViewById(R.id.textContent);
                holder.posted = (TextView)convertView.findViewById(R.id.textPostedTime);
                holder.replyCount = (TextView)convertView.findViewById(R.id.textReplyCount);
                holder.defaultTimeColor = holder.posted.getTextColors().getDefaultColor();
                
                convertView.setTag(holder);
            }
            
            // get the thread to display and populate all the data into the layout
            Thread t = getItem(position);
            holder.userName.setText(t.getUserName());
            holder.content.setMaxLines(_previewLines);
            holder.content.setText(t.getPreview(_showTags, _stripNewLines));
            holder.posted.setText(t.getPosted());
            holder.replyCount.setText(formatReplyCount(t));

            // special highlight for shacknews posts, hopefully the thread_selector color thing will
            // reset the background to transparent when scrolling
            if (t.getUserName().equalsIgnoreCase("Shacknews"))
                convertView.setBackgroundColor(getResources().getColor(R.color.news_post_background));
            else
                convertView.setBackgroundResource(R.color.thread_selector);
            
            if (t.getModeration().equalsIgnoreCase("nws"))
                holder.moderation.setBackgroundColor(Color.RED);
            else
                holder.moderation.setBackgroundColor(Color.TRANSPARENT);
            
            if (t.getReplied())
                holder.posted.setTextColor(getResources().getColor(R.color.user_paricipated));
            else
                holder.posted.setTextColor(holder.defaultTimeColor);

            // special highlight for employee and mod names
            holder.userName.setTextColor(User.getColor(t.getUserName()));
            
            return convertView;
        }
        
        private Spanned formatReplyCount(Thread thread)
        {
            String first = "(" + thread.getReplyCount();
            String second = "";
            String third = ")";

            if (thread.getReplyCount() > thread.getReplyCountPrevious())
            {
                int new_replies = thread.getReplyCount() - thread.getReplyCountPrevious();
                second = " +" + new_replies;
            }

            SpannableString formatted = new SpannableString(first + second + third);
            if (second.length() > 0)
                formatted.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.new_post_count)), first.length(), first.length() + second.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return formatted;
        }

        @Override
        protected ArrayList<Thread> loadData() throws Exception
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ThreadListFragment.this.getActivity());
            String userName = prefs.getString("userName", "");
            
            // grab threads from the api
            ArrayList<Thread> new_threads = ShackApi.getThreads(_pageNumber + 1, userName);
            _pageNumber++;
            
            // remove threads already displayed
            Iterator<Thread> iter = new_threads.iterator();
            while (iter.hasNext())
            	if (!_threadIds.add(iter.next().getThreadId()))
            		iter.remove();
            
            // update the "new" post counts
            updatePostCounts(new_threads);
            
            return new_threads;
        }
        
        private class ViewHolder
        {
            View moderation;
            TextView userName;
            TextView content;
            TextView posted;
            TextView replyCount;
            int defaultTimeColor;
        }
    }

}

