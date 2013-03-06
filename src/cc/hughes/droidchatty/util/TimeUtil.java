package cc.hughes.droidchatty.util;

import java.text.SimpleDateFormat;

public class TimeUtil {
    
    static final SimpleDateFormat mShackDateFormat = new SimpleDateFormat("MMM dd, yyyy h:mma zzz");
    
    public static long parseDateTime(String dateTime)
    {
        try
        {
            return mShackDateFormat.parse(dateTime).getTime();
        }
        catch (Exception ex)
        {
        }
        
        return 0;
    }

}
