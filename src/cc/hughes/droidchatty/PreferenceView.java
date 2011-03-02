package cc.hughes.droidchatty;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferenceView extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
