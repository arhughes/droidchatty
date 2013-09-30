/* Copyright (c) 2012 Manuel Maly
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * From: https://github.com/manmal/hn-android
 */

package cc.hughes.droidchatty2;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class LinkifiedTextView extends TextView {

	public interface OnLinkClickedListener {
		void onClick(String href);
	}
	
	public interface OnLinkLongClickListener {
		void onLongClick(String href);
	}
	
	static final String TAG = "LinkifiedTextView";
	
	ClickableSpan mSelectedSpan;
	OnLinkClickedListener mLinkClickedListener;
	OnLinkLongClickListener mLinkLongClickListener;
		
    public LinkifiedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // force buffer type to Spanned
        setText("", BufferType.SPANNABLE);
        
        // turn on long clicks and add our listener
        setLongClickable(true);
        setOnLongClickListener(mLongClickListner);
    }

    /**
     * Inspired by http://stackoverflow.com/a/7327332/458603 - without this
     * custom code, clicks on links also trigger comment folding/unfolding.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	super.onTouchEvent(event);
    	
        TextView widget = (TextView) this;
        Object text = widget.getText();
        if (text instanceof Spannable) {
            Spannable buffer = (Spannable) text;

            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                float lineWidth = layout.getLineWidth(line);

                // don't activate links if clicked in the empty space after the link
                if (x <= lineWidth) {
                	
                	ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                	if (link.length != 0) {
                		if (action == MotionEvent.ACTION_UP) {
                			if (mSelectedSpan != null) {
                				onClickSpan(mSelectedSpan);
                			}
                			Selection.removeSelection(buffer);
                		} else if (action == MotionEvent.ACTION_DOWN) {
                			Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                			mSelectedSpan = link[0];
                		}
                		return true;
                	}
                }
                                	
            }
        }
        
        return false;
    }
    
    void onClickSpan(ClickableSpan span) {
    	
    	if (span instanceof URLSpan) {
    		if (mLinkClickedListener != null)
    			mLinkClickedListener.onClick(((URLSpan)span).getURL());
    		else
    			span.onClick(this);
    	} else {
    		span.onClick(this);
    	}
    	
    	mSelectedSpan = null;
    }
    
    void onLongClickSpan(ClickableSpan span) {
    	
    	// only activate long clicks of links
    	if (span instanceof URLSpan) {
    		if (mLinkLongClickListener != null)
    			mLinkLongClickListener.onLongClick(((URLSpan)span).getURL());
    		else
    			span.onClick(this);
    	}
    	
    	mSelectedSpan = null;
    }
    
    OnLongClickListener mLongClickListner = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			
			if (mSelectedSpan != null) {
				onLongClickSpan(mSelectedSpan);
				return true;
			}
						
			return false;
		}
    	
    };
    
}
