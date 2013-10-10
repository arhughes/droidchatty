package cc.hughes.droidchatty2.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import cc.hughes.droidchatty2.R;

public class ClearableEditText extends EditText {

    Drawable mClearButton;

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mClearButton = getResources().getDrawable(R.drawable.ic_action_cancel);
        updateClearButtonVisibility();
    }

    private void updateClearButtonVisibility() {
        boolean visible = (this.getText().length() > 0);

        // make sure the height of our button matches the EditText
        if (mClearButton != null) {
            int size = this.getHeight() - this.getPaddingBottom() - this.getPaddingTop();
            mClearButton.setBounds(0, 0, size, size);
        }

        Drawable[] drawables = getCompoundDrawables();
        setCompoundDrawables(drawables[0], drawables[1], visible ? mClearButton : null, drawables[3]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // if the clear button is visible
        if (event.getAction() == MotionEvent.ACTION_UP && getCompoundDrawables()[2] != null) {
            int buttonLeft = getWidth() - getPaddingRight() - mClearButton.getBounds().width();
            if (event.getX() > buttonLeft) {
                // clear all the text!
                setText("");
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        updateClearButtonVisibility();
    }

}
