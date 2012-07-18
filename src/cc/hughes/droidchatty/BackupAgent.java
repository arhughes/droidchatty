package cc.hughes.droidchatty;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupAgent extends BackupAgentHelper 
{
	static final String PREFS = "cc.hughes.droidchatty_preferences";
	static final String MY_PREFS_BACKUP_KEY = "preferences";
	
	public void onCreate()
	{
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
		addHelper(MY_PREFS_BACKUP_KEY, helper);
	}
	
}
