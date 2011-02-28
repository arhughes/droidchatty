package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SingleThreadView extends ListActivity {
	
	static final String THREAD_ID = "THREAD_ID";
	static final String THREAD_CONTENT = "THREAD_CONTENT";
	static final String THREAD_AUTHOR = "THREAD_AUTHOR";
	static final String THREAD_POSTED = "THREAD_POSTED";
	
	private int _currentThreadId = 0;
	private int _rootThreadId;
	private ProgressDialog _progressDialog = null;
	private ArrayList<Thread> _posts = null;
	private ThreadAdapter _adapter;
	
	private Runnable _retrieveThreads = new Runnable()
	{
    	public void run()
    	{
    		getPosts();
    	}
	};
    
    private Runnable _displayThreads = new Runnable()
    {
    	public void run()
    	{
    		if (_posts != null && _posts.size() > 0)
    		{
    			// remove the elements, and add in all the new ones
    			_adapter.clear();
    			for (Thread t : _posts)
    			{
    				_adapter.add(t);
    			}
    			_progressDialog.dismiss();
    			_adapter.notifyDataSetChanged();
    			
    			if (_currentThreadId == 0)
    			{
    				displayPost(_posts.get(0), 0);
    			}
    		}
    	}
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread_view);
		
		final Object data = getLastNonConfigurationInstance();
		
		if (data == null)
		{
			_posts = new ArrayList<Thread>();
		}
		else
		{
			_posts = (ArrayList<Thread>)data;
		}
			
		_adapter = new ThreadAdapter(this, R.layout.thread_row, _posts);
		setListAdapter(_adapter);
		
		final ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				displayPost(_posts.get(position), position);
			}
		});
		
		Bundle extras = getIntent().getExtras();
		_rootThreadId = extras.getInt(THREAD_ID);
		
		// if launched from a link, no content, author, or date is passed in
		if (extras.containsKey(THREAD_CONTENT))
		{
			String content = extras.getString(THREAD_CONTENT);
			String author = extras.getString(THREAD_AUTHOR);
			String posted = extras.getString(THREAD_POSTED);
			
			Thread thread = new Thread();
			thread.setThreadID(_rootThreadId);
			thread.setContent(content);
			thread.setPostedTime(posted);
			thread.setUserName(author);
			
			displayPost(thread, 0);
		}
		
		if (_posts.isEmpty())
			startRefresh();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.thread_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.refresh:
			startRefresh();
			return true;
		case R.id.copyUrl:
			copyCurrentPostUrl();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void copyCurrentPostUrl()
	{
		String url = "http://www.shacknews.com/chatty?id=" + _currentThreadId;
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		clipboard.setText(url);
	}
	
	private void displayPost(Thread thread, int position)
	{
		// unhighlight old selected, highlight new selected
		// should be able to do this with a selector, but damned if I could get it to work
		int previousPosition = _adapter.getSelectedPosition();
		_adapter.setSelectedPosition(position);
		_adapter.fixBackgroundColor(getListView(), previousPosition);
		_adapter.fixBackgroundColor(getListView(), position);
		
		TextView tvAuthor = (TextView)findViewById(R.id.textUserName);
		TextView tvContent = (TextView)findViewById(R.id.textContent);
		TextView tvPosted = (TextView)findViewById(R.id.textPostedTime);
		
		tvAuthor.setText(thread.getUserName());
		tvAuthor.setTextColor(User.getColor(thread.getUserName()));
		tvPosted.setText(thread.getPostedTime());
		tvContent.setText(PostFormatter.formatContent(thread.getContent(), true));
		Linkify.addLinks(tvContent, Linkify.ALL);
		tvContent.setClickable(false);
		
		_currentThreadId = thread.getThreadID();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return _posts;
	}
	
    private void startRefresh()
    {
        java.lang.Thread thread = new java.lang.Thread(null, _retrieveThreads, "Background");
        thread.start();
        _progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving posts...", true);
    }
	
	private void getPosts()
	{
    	try
    	{
			_posts = ShackApi.getThread(_rootThreadId);
    	} catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    	runOnUiThread(_displayThreads);
	}
	
	class ThreadAdapter extends ArrayAdapter<Thread> {
		
		private ArrayList<Thread> items;
		private int selectedPosition;
		
		public int getSelectedPosition()
		{
			return selectedPosition;
		}
		
		public void setSelectedPosition(int position)
		{
			selectedPosition = position;
		}
		
		public ThreadAdapter(Context context, int textViewResourceId, ArrayList<Thread> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.thread_row, null);
			}
			
			// get the thread to display and populate all the data into the layout
			Thread t = items.get(position);
			if (t != null)
			{
				TextView tvContent = (TextView)v.findViewById(R.id.textPreview);
				if (tvContent != null)
				{
					tvContent.setPadding(15 * t.getLevel(), 0, 0, 0);
					tvContent.setText(t.getPostPreview());
				}
				
				fixBackgroundColor(v, position);
			}
			
			return v;
		}
		
		public void fixBackgroundColor(ListView view, int position)
		{
			View v = view.getChildAt(position - view.getFirstVisiblePosition());
			if (v != null)
				fixBackgroundColor(v, position);
		}
		
		public void fixBackgroundColor(View view, int position)
		{
			RelativeLayout preview = (RelativeLayout)view.findViewById(R.id.previewLayout);
			if (position == selectedPosition)
				preview.setBackgroundColor(Color.rgb(0x22, 0x55, 0xdd));
			else
				preview.setBackgroundColor(Color.TRANSPARENT);
		}
		
	}
}
