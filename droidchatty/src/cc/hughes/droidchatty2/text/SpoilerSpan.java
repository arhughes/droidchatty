package cc.hughes.droidchatty2.text;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class SpoilerSpan extends ClickableSpan {

	static final int SPOILER_COLOR = Color.parseColor("#383838");
	
	boolean mSpoiled = false;
	
	@Override
	public void onClick(View widget) {
		mSpoiled = true;
		widget.invalidate();
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		if (!mSpoiled) {
			ds.bgColor = SPOILER_COLOR;
			ds.setColor(SPOILER_COLOR);
		}
	}
	
}
