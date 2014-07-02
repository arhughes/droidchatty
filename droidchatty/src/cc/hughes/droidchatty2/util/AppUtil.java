package cc.hughes.droidchatty2.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class AppUtil {

    public static boolean isInstalled(String uri, Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(uri) != null;
    }

}
