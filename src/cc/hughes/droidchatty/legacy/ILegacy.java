package cc.hughes.droidchatty.legacy;

import android.content.Context;

public interface ILegacy
{
    boolean hasCamera(Context context);
    int getRequiredImageRotation(String path);
}
