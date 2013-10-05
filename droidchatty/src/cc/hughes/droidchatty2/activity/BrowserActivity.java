package cc.hughes.droidchatty2.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;

public class BrowserActivity extends ActionBarActivity implements View.OnKeyListener {
    public static final String ARG_URL = "URL";

    @ViewInjected(R.id.browser_webview)
    private WebView mWebView;

    @ViewInjected(R.id.browser_progress)
    ProgressBar mProgressBar;

    boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.browser);
        ViewInjector.inject(this);

        // initial setup
        mWebView.setOnKeyListener(this);
        mWebView.setWebViewClient(new InternalWebViewClient());
        mWebView.setWebChromeClient(new InternalWebChromeClient());
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);

        // now load the url
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String url = extras.getString(ARG_URL);
            mWebView.loadUrl(url);
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_options, menu);

        MenuItem back = menu.findItem(R.id.browser_back);
        MenuItem forward = menu.findItem(R.id.browser_forward);
        MenuItem refresh = menu.findItem(R.id.browser_refresh);
        MenuItem cancel = menu.findItem(R.id.browser_cancel);

        // gray out back/forward as appropriate
        setMenuEnabled(back, mWebView.canGoBack());
        setMenuEnabled(forward, mWebView.canGoForward());

        // cancel when loading, refresh when not
        refresh.setVisible(!mIsLoading);
        cancel.setVisible(mIsLoading);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.browser_back:
                mWebView.goBack();
                return true;
            case R.id.browser_forward:
                mWebView.goForward();
                return true;
            case R.id.browser_refresh:
                mWebView.reload();
                return true;
            case R.id.browser_cancel:
                mWebView.stopLoading();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setMenuEnabled(MenuItem item, boolean enabled) {
        item.setEnabled(enabled);

        if (!enabled) {
            Drawable icon = item.getIcon();
            icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            item.setIcon(icon);
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    private class InternalWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private class InternalWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {

            if (progress < 100 && !mIsLoading)
            {
                mIsLoading = true;
                invalidateOptionsMenu();
            }

            mProgressBar.setProgress(progress);

            if (progress == 100)
            {
                mIsLoading = false;
                invalidateOptionsMenu();
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            getSupportActionBar().setTitle(title);
        }
    }
}
