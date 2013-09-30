package cc.hughes.droidchatty2.text;

import android.text.style.URLSpan;
import android.view.View;

public class InternalURLSpan extends URLSpan {

    public static interface LinkListener {
        public void onLinkClicked(String href);
    }

    String mHref;
    LinkListener mListener;

    public InternalURLSpan(String href) {
        super(href);
        mHref = href;
    }

    public void setLinkListener(LinkListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (mListener != null)
            mListener.onLinkClicked(mHref);
        else
            super.onClick(view);
    }
}
