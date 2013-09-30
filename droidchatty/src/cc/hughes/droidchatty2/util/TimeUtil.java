package cc.hughes.droidchatty2.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateUtils;

public class TimeUtil {
    
    static final SimpleDateFormat mShackDateFormat = new SimpleDateFormat("MMM dd, yyyy h:mma zzz", Locale.US);
    
    static long parseDateTime(String dateTime)
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
    
    public static String format(Context context, String dateTime)
    {
    	long time = parseDateTime(dateTime);
    	
    	if (time == 0)
    		return dateTime;
    	
    	return DateUtils.getRelativeDateTimeString(context, time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
    }

}
