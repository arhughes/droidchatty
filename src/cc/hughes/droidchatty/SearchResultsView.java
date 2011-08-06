package cc.hughes.droidchatty;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SearchResultsView extends FragmentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
    }
}
