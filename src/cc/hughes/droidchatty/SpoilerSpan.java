package cc.hughes.droidchatty;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class SpoilerSpan extends ClickableSpan
{
    static final int SPOILER_COLOR = Color.parseColor("#383838");
    
    View _view;
    boolean _spoiled = false;
    
    public SpoilerSpan(View view)
    {
        _view = view;
    }
    
    @Override
    public void onClick(View widget)
    {
        // only spoil if the view was given
        if (_view != null)
        {
            _spoiled = true;
            _view.invalidate();
        }
    }

    @Override
    public void updateDrawState(TextPaint ds)
    {
        // if it hasn't been spoiled yet, hide the text
        if (!_spoiled)
        {
            ds.bgColor = SPOILER_COLOR;
            ds.setColor(SPOILER_COLOR);
        }
    }
}
