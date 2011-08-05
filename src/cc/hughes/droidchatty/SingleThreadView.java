package cc.hughes.droidchatty;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SingleThreadView extends FragmentActivity
{
    public static final String THREAD_ID = "threadId";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null)
        {
        	//create the fragment, and show it!
        	ThreadViewFragment frag = new ThreadViewFragment();
        	frag.setArguments(getIntent().getExtras());
        	getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
        }
    }
}

