package cc.hughes.droidchatty;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultsView extends ListActivity
{
    
    ArrayList<SearchResult> _results;
    SearchResultsAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        
        _results = doSearch();
        _adapter = new SearchResultsAdapter(this, R.layout.search_result_row, _results);
        setListAdapter(_adapter);
        
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                displayThread(_results.get(position));
            }
            
        });
        
    }
    
    private ArrayList<SearchResult> doSearch()
    {
        String term = getIntent().getExtras().getString("terms");
        String author = getIntent().getExtras().getString("author");
        String parentAuthor = getIntent().getExtras().getString("parentAuthor");
        
        try
        {
            return ShackApi.search(term, author, parentAuthor);
        } catch (Exception e)
        {
            Log.e("DroidChatty", "Error fetching search results", e);
            ErrorDialog.display(this, "Error", "Error fetching search results.");
        }
        
        return null;
    }
    
    private void displayThread(SearchResult result)
    {
        Intent i = new Intent(this, SingleThreadView.class);
        i.putExtra(SingleThreadView.THREAD_ID, result.getPostId());
        startActivity(i);
    }
    
    private class SearchResultsAdapter extends ArrayAdapter<SearchResult>
    {
        private ArrayList<SearchResult> items;

        public SearchResultsAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> items)
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
            SearchResult t = items.get(position);
            if (t != null)
            {
                TextView tvUserName = (TextView)v.findViewById(R.id.textUserName);
                TextView tvContent = (TextView)v.findViewById(R.id.textContent);
                TextView tvPosted = (TextView)v.findViewById(R.id.textPostedTime);
                if (tvUserName != null)
                    tvUserName.setText(t.getAuthor());
                if (tvContent != null)
                    tvContent.setText(t.getContent());
                if (tvPosted != null)
                    tvPosted.setText(t.getPosted());
            }
            return v;
        }
    }

}
