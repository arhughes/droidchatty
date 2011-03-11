package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadViewFragment extends ListFragment
{
    public static ThreadViewFragment newInstance(int postId, String userName, String posted, String content)
    {
        ThreadViewFragment f = new ThreadViewFragment();
        
        Bundle args = new Bundle();
        args.putInt("postId", postId);
        args.putString("userName", userName);
        args.putString("posted", posted);
        args.putString("content", content);
        
        f.setArguments(args);
        
        return f;
    }
    
    PostLoadingAdapter _adapter;
    int _rootPostId;
    
    public int getPostId()
    {
       return getArguments().getInt("postId"); 
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _adapter = new PostLoadingAdapter(getActivity(), new ArrayList<Post>());
        setListAdapter(_adapter);
        
        Bundle args = getArguments();
        _rootPostId = args.getInt("postId");
        
        // inflate our view and keep track of it
        View view = inflater.inflate(R.layout.thread_view, null);
        
        ListView listView = (ListView)view.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        if (args.containsKey("content"))
        {
            String userName = args.getString("userName");
            String content = args.getString("content");
            String posted = args.getString("posted");
            Post post = new Post(_rootPostId, userName, content, posted, 0);
            displayPost(view, post);
        }
        
        return view;
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        l.setItemChecked(position, true);
        Post post = _adapter.getItem(position);
        displayPost(getView(), post);
    }

    private void displayPost(View view, Post post)
    {
        TextView userName = (TextView)view.findViewById(R.id.textUserName);
        TextView posted = (TextView)view.findViewById(R.id.textPostedTime);
        TextView content = (TextView)view.findViewById(R.id.textContent);
        
        userName.setText(post.getUserName());
        posted.setText(post.getPosted());
        content.setText(post.getContent());
    }
    
    private class PostLoadingAdapter extends LoadingAdapter<Post>
    {
        public PostLoadingAdapter(Context context, ArrayList<Post> items)
        {
            super(context, R.layout.thread_row, R.layout.row_loading, items);
        }

        @Override
        protected View createView(int position, View convertView, ViewGroup parent)
        {
            TextView content = (TextView)convertView.getTag();
            if (content == null)
            {
                content = (TextView)convertView.findViewById(R.id.textPreview);
                convertView.setTag(content);
            }

            // get the thread to display and populate all the data into the layout
            Post t = getItem(position);
            content.setPadding(15 * t.getLevel(), 0, 0, 0);
            content.setText(t.getPreview());

            return convertView;
        }

        @Override
        protected ArrayList<Post> loadData() throws Exception
        {
            return ShackApi.getPosts(_rootPostId);
        }
    }
    
}
