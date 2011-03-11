package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ThreadViewFragment extends ListFragment
{
    public static ThreadViewFragment newInstance(int postId)
    {
        ThreadViewFragment f = new ThreadViewFragment();
        
        Bundle args = new Bundle();
        args.putInt("postId", postId);
        f.setArguments(args);
        
        return f;
    }
    
    PostLoadingAdapter _adapter;
    int _rootPostId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _adapter = new PostLoadingAdapter(getActivity(), new ArrayList<Post>());
        setListAdapter(_adapter);
        
        _rootPostId = getArguments().getInt("postId");
        
        // inflate our view and keep track of it
        View view = inflater.inflate(R.layout.thread_view, null);
        TextView user = (TextView)view.findViewById(R.id.textUserName);
        user.setText(Integer.toString(_rootPostId));
        return view;
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
