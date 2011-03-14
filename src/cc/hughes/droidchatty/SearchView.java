package cc.hughes.droidchatty;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SearchView extends Activity
{
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        Button searchButton = (Button)findViewById(R.id.search);
        searchButton.setOnClickListener(onSearchClick);
    }
    
    OnClickListener onSearchClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            EditText termEditor = (EditText)findViewById(R.id.searchTerm);
            EditText authorEditor = (EditText)findViewById(R.id.searchAuthor);
            EditText parentEditor = (EditText)findViewById(R.id.searchParentAuthor);
            String term = termEditor.getText().toString();
            String author = authorEditor.getText().toString();
            String parentAuthor = parentEditor.getText().toString();
            
            Intent i = new Intent(SearchView.this, SearchResultsView.class);
            i.putExtra("terms", term);
            i.putExtra("author", author);
            i.putExtra("parentAuthor", parentAuthor);
            startActivity(i);
        }
    };
    
    public void vanitySearch(View view)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        EditText term = (EditText)findViewById(R.id.searchTerm);
        term.setText(prefs.getString("userName", null));
    }
    
    public void ownSearch(View view)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        EditText author = (EditText)findViewById(R.id.searchAuthor);
        author.setText(prefs.getString("userName", null));
    }
    
    public void parentSearch(View view)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        EditText parent = (EditText)findViewById(R.id.searchParentAuthor);
        parent.setText(prefs.getString("userName", null));
    }

}
