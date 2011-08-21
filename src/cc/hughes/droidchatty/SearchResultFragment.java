package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultFragment extends ListFragment
{
    ArrayList<SearchResult> _results;
    SearchResultsAdapter _adapter;
    
    String _term;
    String _author;
    String _parentAuthor;
    
    int _pageNumber = 0;
    
    boolean _dualPane = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        Intent intent = getActivity().getIntent();
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            _term = intent.getStringExtra(SearchManager.QUERY);
            _author = "";
            _parentAuthor = "";
        }
        else
        {
            _term = intent.getExtras().getString("terms");
            _author = intent.getExtras().getString("author");
            _parentAuthor = intent.getExtras().getString("parentAuthor");
        }
        
       	View singleThread = getActivity().findViewById(R.id.singleThread);
       	_dualPane = singleThread != null && singleThread.getVisibility() == View.VISIBLE;
       
       	getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        _results = new ArrayList<SearchResult>();
        _adapter = new SearchResultsAdapter(this.getActivity(), _results);
        setListAdapter(_adapter);
        
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                displayThread(_adapter.getItem(position));
            }
        });
    }
    
    private void displayThread(SearchResult result)
    {
        if (_dualPane)
        {
            ThreadViewFragment view = (ThreadViewFragment)getFragmentManager().findFragmentById(R.id.singleThread);
            if (view.getPostId() != result.getPostId());
                view.loadPost(Post.fromSearchResult(result));
        }
        else
        {
            Intent i = new Intent(this.getActivity(), SingleThreadView.class);
            i.putExtra("postId", result.getPostId());
            startActivity(i);
        }
    }
    
    private class SearchResultsAdapter extends LoadingAdapter<SearchResult>
    {
        public SearchResultsAdapter(Context context, ArrayList<SearchResult> items)
        {
            super(context, R.layout.search_result_row, R.layout.row_loading, items);
        }

        @Override
        protected View createView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = (ViewHolder)convertView.getTag();
            
            if (holder == null)
            {
                holder = new ViewHolder();
                holder.userName = (TextView)convertView.findViewById(R.id.textUserName);
                holder.content = (TextView)convertView.findViewById(R.id.textContent);
                holder.posted = (TextView)convertView.findViewById(R.id.textPostedTime);
                convertView.setTag(holder);
            }

            // get the thread to display and populate all the data into the layout
            SearchResult t = getItem(position);
            holder.userName.setText(t.getAuthor());
            holder.content.setText(PostFormatter.formatContent(t.getAuthor(), t.getContent(), null, false, true));
            holder.posted.setText(t.getPosted());
                    
            return convertView;
        }
        
        @Override
        protected ArrayList<SearchResult> loadData() throws Exception
        {
            ArrayList<SearchResult> results = ShackApi.search(_term, _author, _parentAuthor, _pageNumber + 1);
            _pageNumber++;
            
            return results;
        }
        
        class ViewHolder
        {
            TextView userName;
            TextView content;
            TextView posted;
        }
        
    }
}
