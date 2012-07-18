package cc.hughes.droidchatty;

import android.app.backup.BackupManager;
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

	@Override
	protected void onPause()
	{	
		BackupManager backup = new BackupManager(this);
		backup.dataChanged();
		super.onPause();
	}
}
