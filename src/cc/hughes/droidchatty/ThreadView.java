package cc.hughes.droidchatty;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

public class ThreadView extends FragmentActivity
{
    @Override
    protected void onCreate(Bundle arg0)
    {
        super.onCreate(arg0);
        setContentView(R.layout.main);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        ThreadViewFragment fragment = (ThreadViewFragment)getSupportFragmentManager().findFragmentById(R.id.singleThread);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean handleVolume = prefs.getBoolean("useVolumeButtons", false);
        if (fragment != null && handleVolume)
        {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    fragment.adjustSelected(-1);
                return true;
            }
            else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    fragment.adjustSelected(1);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
