package cc.hughes.droidchatty2;

import android.support.v4.app.Fragment;

public interface FragmentContextActivity {
    public void changeContext(Fragment fragment, int level);
    public void changeContext(Fragment fragment, Fragment parent);
}
