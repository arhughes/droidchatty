package cc.hughes.droidchatty2.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

public class TimeUtil {
    
    static final SimpleDateFormat mShackDateFormat = new SimpleDateFormat("MMM dd, yyyy h:mma zzz", Locale.US);
    static final SimpleDateFormat mShackMessageDateFormat = new SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.US);

    static long parseDateTime(String dateTime, SimpleDateFormat format)
    {
        try
        {
            return format.parse(dateTime).getTime();
        }
        catch (Exception ex)
        {
            Log.i("TimeUtil", "Error parsing time: " + dateTime, ex);
        }
        
        return 0;
    }
    
    public static String format(Context context, String dateTime)
    {
    	long time = parseDateTime(dateTime, mShackDateFormat);
    	
    	if (time == 0)
            time = parseDateTime(dateTime, mShackMessageDateFormat);

        if (time == 0)
    		return dateTime;
    	
    	return DateUtils.getRelativeDateTimeString(context, time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
    }

}
