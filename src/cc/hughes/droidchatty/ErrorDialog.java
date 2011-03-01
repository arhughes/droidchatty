package cc.hughes.droidchatty;

import android.app.AlertDialog;
import android.content.Context;

public class ErrorDialog implements Runnable
{
    private Context _context;
    private String _title;
    private String _text;
    
    public ErrorDialog(Context context, String title, String text)
    {
        _context = context;
        _title = title;
        _text = text;
    }

    @Override
    public void run()
    {
        display(_context, _title, _text);
    }
    
    public static void display(Context context, String title, String text)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

}
