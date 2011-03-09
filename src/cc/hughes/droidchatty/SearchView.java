package cc.hughes.droidchatty;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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
            String term = termEditor.getText().toString();
            
            Intent i = new Intent(SearchView.this, SearchResultsView.class);
            i.putExtra("terms", term);
            startActivity(i);
        }
    };

}
