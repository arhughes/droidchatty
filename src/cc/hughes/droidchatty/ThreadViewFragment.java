package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.ClipboardManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class ThreadViewFragment extends ListFragment
{
    public static ThreadViewFragment newInstance(int postId, String userName, String posted, String content, String moderation)
    {
        ThreadViewFragment f = new ThreadViewFragment();
        
        Bundle args = new Bundle();
        args.putInt("postId", postId);
        args.putString("userName", userName);
        args.putString("posted", posted);
        args.putString("content", content);
        args.putString("moderation", moderation);
        
        f.setArguments(args);
        
        return f;
    }
    
    PostLoadingAdapter _adapter;
    int _rootPostId;
    int _currentPostId;
    boolean _postDisplayed = false;
    
    public int getPostId()
    {
       return getArguments().getInt("postId"); 
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _rootPostId = getArguments().getInt("postId");
        _currentPostId = _rootPostId;
        
        _adapter = new PostLoadingAdapter(getActivity(), new ArrayList<Post>());
        setListAdapter(_adapter);
        
        return inflater.inflate(R.layout.thread_view, null);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        Bundle args = getArguments();
        String action = getActivity().getIntent().getAction();
        Uri uri = getActivity().getIntent().getData();
        
        if (args.containsKey("content"))
        {
            String userName = args.getString("userName");
            String content = args.getString("content");
            String posted = args.getString("posted");
            String moderation = args.containsKey("moderation") ? args.getString("moderation") : "";
            Post post = new Post(_rootPostId, userName, content, posted, 0, moderation);
            displayPost(post);
        }
        else if (action != null && action.equals(Intent.ACTION_VIEW) && uri != null)
        {
            String id = uri.getQueryParameter("id");
            if (id == null)
            {
                ErrorDialog.display(getActivity(), "Error", "Invalid URL Found");
                return;
            }
            
            _currentPostId = Integer.parseInt(id);
            _rootPostId = _currentPostId;
        }
        
        // makes links actually clickable
        TextView content = (TextView)getView().findViewById(R.id.textContent);
        content.setMovementMethod(new LinkMovementMethod());
        
        setHasOptionsMenu(true);
        registerForContextMenu(content);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        displayPost(position);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.copyUrl:
                copyPostUrl();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    void copyPostUrl()
    {
        String url = "http://www.shacknews.com/chatty?id=" + _currentPostId;
        ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
        clipboard.setText(url);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);          
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.post_context, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.thread_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.refreshThread:
                _adapter.clear();
                return true;
            case R.id.reply:
                postReply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private static final int POST_REPLY = 0;
    private void postReply()
    {
        Intent i = new Intent(getActivity(), ComposePostView.class);
        i.putExtra(SingleThreadView.THREAD_ID, _currentPostId);
        startActivityForResult(i, POST_REPLY);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case POST_REPLY:
                if (resultCode == Activity.RESULT_OK)
                {
                    // read the resulting thread id from the post
                    int postId = data.getExtras().getInt("postId");
                    
                    _rootPostId = postId;
                    _currentPostId = postId;
                    _adapter.clear();
                }
                break;
            default:
                break;
        }
    } 
    
    private void displayPost(int position)
    {
        getListView().setItemChecked(position, true);
        Post post = _adapter.getItem(position);
        
        if (!_postDisplayed)
            getListView().setSelection(position);
        
        displayPost(post);
    }

    private void displayPost(Post post)
    {
        View view = getView();
        
        TextView userName = (TextView)view.findViewById(R.id.textUserName);
        TextView posted = (TextView)view.findViewById(R.id.textPostedTime);
        TextView content = (TextView)view.findViewById(R.id.textContent);
        ScrollView scroll = (ScrollView)view.findViewById(R.id.scroll);
        View moderation = (View)view.findViewById(R.id.threadModeration);
        
        userName.setText(post.getUserName());
        posted.setText(post.getPosted());
        content.setText(PostFormatter.formatContent(post, content, true));
        
        if (post.getModeration().equalsIgnoreCase("nws"))
            moderation.setBackgroundColor(Color.RED);
        else
            moderation.setBackgroundColor(Color.TRANSPARENT);
        
        scroll.scrollTo(0, 0);
        
        _currentPostId = post.getPostId();
        _postDisplayed = true;
    }
    
    private class PostLoadingAdapter extends LoadingAdapter<Post>
    {
        private boolean _loaded = false;
        private String _userName = "";
        
        public PostLoadingAdapter(Context context, ArrayList<Post> items)
        {
            super(context, R.layout.thread_row, R.layout.row_loading, items);
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ThreadViewFragment.this.getActivity());
            _userName = prefs.getString("userName", "");
        }
        
        @Override
        public void clear()
        {
            _loaded = false;
            super.clear();
        }
        
        @Override
        protected boolean getMoreToLoad()
        {
            return !_loaded;
        }

        @Override
        protected View createView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = (ViewHolder)convertView.getTag();
            if (holder == null)
            {
                holder = new ViewHolder();
                holder.content = (TextView)convertView.findViewById(R.id.textPreview);
                holder.moderation = (View)convertView.findViewById(R.id.postModeration);
                convertView.setTag(holder);
            }

            // get the thread to display and populate all the data into the layout
            Post t = getItem(position);
            
            
            holder.content.setPadding(15 * t.getLevel(), 0, 0, 0);
            holder.content.setText(t.getPreview());
            
            if (t.getModeration().equalsIgnoreCase("nws"))
                holder.moderation.setBackgroundColor(Color.RED);
            else
                holder.moderation.setBackgroundColor(Color.TRANSPARENT);

            if (t.getUserName().equalsIgnoreCase(_userName))
            {
                // highlight your own posts
                holder.content.setTextColor(getResources().getColor(R.color.user_paricipated));
            }
            else
            {
                // highlight newer posts
                int color = 255 - (12 * Math.min(t.getOrder(), 10));
                holder.content.setTextColor(Color.argb(255, color, color, color));
            }
            
            return convertView;
        }

        @Override
        protected ArrayList<Post> loadData() throws Exception
        {
            ArrayList<Post> posts = ShackApi.getPosts(_rootPostId);
            _loaded = true;
            return posts;
        }
        
        @Override
        protected void afterDisplay()
        {
            try
            {
                int length = getCount();
                for (int i = 0; i < length; i++)
                {
                    if (getItem(i).getPostId() == _currentPostId)
                    {
                        if (!_postDisplayed)
                            displayPost(i);
                        break;
                    }
                }
            } catch (Exception e)
            {
                Log.w("DroidChatty", "Error selecting root post", e);
            }
            
        }
        
        private class ViewHolder
        {
            TextView content;
            View moderation;
        }
        
    }
    
}
