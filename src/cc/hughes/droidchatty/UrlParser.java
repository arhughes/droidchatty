package cc.hughes.droidchatty;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class UrlParser extends Activity {

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getIntent().getAction().equals(Intent.ACTION_VIEW))
        {
            Uri uri = getIntent().getData();
            int threadId = Integer.parseInt(uri.getQueryParameter("id"));

            Intent i = new Intent(this, SingleThreadView.class);
            i.putExtra(SingleThreadView.THREAD_ID, threadId);
            startActivity(i);
        }
        else
        {
            finish();
        }
    }
}
