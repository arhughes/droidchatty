package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.R.color;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
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
import android.widget.TextView;

public class ThreadView extends ListActivity {
	
	private ProgressDialog _progressDialog = null;
	private ArrayList<Thread> _threads = null;
	private ThreadAdapter _adapter;
	
	private Runnable _retrieveThreads = new Runnable()
	{
    	public void run()
    	{
    		getThreads();
    	}
	};
    
    private Runnable _displayThreads = new Runnable()
    {
    	public void run()
    	{
    		if (_threads != null && _threads.size() > 0)
    		{
    			// remove the elements, and add in all the new ones
    			_adapter.clear();
    			for (Thread t : _threads)
    			{
    				_adapter.add(t);
    			}
    			_progressDialog.dismiss();
    			_adapter.notifyDataSetChanged();
    		}
    	}
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // setup the list
        _threads = new ArrayList<Thread>();
        _adapter = new ThreadAdapter(this, R.layout.row, _threads);
        setListAdapter(_adapter);
        
        // listen for clicks
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				displayThread(_threads.get(position));
			}
        });
        
        // let's get this thing going already!
        startRefresh();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    	case R.id.refresh:
    		startRefresh();
    		return true;
		default:
			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void startRefresh()
    {
        java.lang.Thread thread = new java.lang.Thread(null, _retrieveThreads, "Backgroundl");
        thread.start();
        _progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving threads...", true);
    }
    
    private void displayThread(Thread thread)
    {
    	Intent i = new Intent(this, SingleThreadView.class);
    	i.putExtra(SingleThreadView.THREAD_ID, thread.getThreadID());
    	i.putExtra(SingleThreadView.THREAD_AUTHOR, thread.getUserName());
    	i.putExtra(SingleThreadView.THREAD_POSTED, thread.getPostedTime());
    	i.putExtra(SingleThreadView.THREAD_CONTENT, thread.getContent());
    	startActivity(i);
    }
    
    private void getThreads()
    {
    	try
    	{
    		_threads = ShackApi.getThreads();
    		Log.i("Array", "" + _threads.size());
    	} catch (Exception ex)
    	{
    		Log.e("Background", ex.getMessage());
    	}
    	
    	runOnUiThread(_displayThreads);
    }
    
	class ThreadAdapter extends ArrayAdapter<Thread> {
		
		private ArrayList<Thread> items;
		
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
				v = vi.inflate(R.layout.row, null);
			}
			
			// get the thread to display and populate all the data into the layout
			Thread t = items.get(position);
			if (t != null)
			{
				TextView tvUserName = (TextView)v.findViewById(R.id.textUserName);
				TextView tvContent = (TextView)v.findViewById(R.id.textContent);
				TextView tvPosted = (TextView)v.findViewById(R.id.textPostedTime);
				TextView tvReplyCount = (TextView)v.findViewById(R.id.textReplyCount);
				if (tvUserName != null)
					tvUserName.setText(t.getUserName());
				if (tvContent != null)
					tvContent.setText(fixContent(t.getContent()));
				if (tvPosted != null)
					tvPosted.setText(t.getPostedTime());
				if (tvReplyCount != null)
					tvReplyCount.setText("(" + t.getReplyCount() + ")");
				
				// special highlight for shacknews posts
				if (t.getUserName().equalsIgnoreCase("Shacknews"))
					v.setBackgroundColor(Color.rgb(0x19, 0x26, 0x35));
				else
					v.setBackgroundColor(color.background_dark);
				
				// special highlight for employee and mod names
				if (User.isEmployee(t.getUserName()))
					tvUserName.setTextColor(Color.GREEN);
				else if (User.isModerator(t.getUserName()))
					tvUserName.setTextColor(Color.RED);
				else
					tvUserName.setTextColor(Color.rgb(0xf3, 0xe7, 0xb5));
			}
			return v;
		}
		private Spanned fixContent(String content)
		{
			// convert shack's css into real font colors since Html.fromHtml doesn't supporty css of any kind
			content = content.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");
			content = content.replaceAll("<span class=\"jt_green\">(.*?)</span>", "<font color=\"#8dc63f\">$1</font>");
			content = content.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_olive\">(.*?)</span>", "<font color=\"#808000\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_lime\">(.*?)</span>",  "<font color=\"#c0ffc0\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
	        content = content.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
	        content = content.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
	        content = content.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
	        content = content.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<del>1</del>");
	        content = content.replaceAll("<br />", "&nbsp;");
			return Html.fromHtml(content);
		}
	
	}
}