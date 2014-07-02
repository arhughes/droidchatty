package cc.hughes.droidchatty2.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.net.ChattyService;

public class PostActivity extends Activity {

    public static final String PARENT_ID = "PARENT_ID";
    public static final String IS_NEWS_THREAD = "IS_NEWS_THREAD";
    public static final String RESULT_POST_ID = "RESULT_POST_ID";

    private int mParentId = 0;

    @ViewInjected(R.id.edit_post_reply)
    Button mReplyButton;

    @ViewInjected(R.id.edit_post_cancel)
    Button mCancelButton;

    @ViewInjected(R.id.edit_post_content)
    EditText mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ViewInjector.inject(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mParentId = extras.getInt(PARENT_ID, 0);
        }

        Log.i("PostActivity", "Replying to post: " + mParentId);

        mReplyButton.setOnClickListener(mReplyButtonClick);
        mCancelButton.setOnClickListener(mCancelButtonClick);
    }

    void displayError(Exception ex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Error occurred while posting:\n" + ex.getMessage());
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    void returnPost() {
        //Intent result = new Intent();
        //result.putExtra(RESULT_POST_ID, postId);
        setResult(RESULT_OK);
        //setResult(RESULT_OK, result);
        finish();
    }

    View.OnClickListener mReplyButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String content = mEditor.getText().toString();

            new PostTask().execute(content);
        }
    };

    View.OnClickListener mCancelButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    class PostTask extends AsyncTask<String, Void, Void> {

        Exception mException;
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(PostActivity.this, "Posting", "Attempting to post...");
        }

        @Override
        protected Void doInBackground(String... params) {
            String content = params[0];

            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);
                ChattyService service = new ChattyService(prefs);
                service.post(mParentId, content);
            } catch (Exception ex) {
                mException = ex;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mProgressDialog.dismiss();

            if (mException != null)
                displayError(mException);
            else
                returnPost();
        }
    }

}
