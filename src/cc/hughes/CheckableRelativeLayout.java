package cc.hughes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable
{
    private boolean _checked;
    
    private static final int[] CHECKED_STATE_SET =
    {
        android.R.attr.state_checked
    };
    
    public CheckableRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (_checked)
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }
    
    public void toggle()
    {
        setChecked(!_checked);
    }
    
    public boolean isChecked()
    {
        return _checked;
    }
    
    public void setChecked(boolean checked)
    {
        if (checked != _checked)
        {
            _checked = checked;
            refreshDrawableState();
        }
    }
    

}
