package cc.hughes.droidchatty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ComposePostView extends Activity {

	private int _replyToPostId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post);
        
        // grab the post being replied to, if this is a reply
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(SingleThreadView.THREAD_ID))
            _replyToPostId = getIntent().getExtras().getInt(SingleThreadView.THREAD_ID);
        
        Button reply_button = (Button)findViewById(R.id.replyButton);
        Button cancel_button = (Button)findViewById(R.id.cancelButton);
        
        if (_replyToPostId == 0)
            setTitle(getTitle() + " - New Post");
        else
            setTitle(getTitle() + " - Reply");
        
        reply_button.setOnClickListener(onButtonClick);
        cancel_button.setOnClickListener(new OnClickListener()
        {
    		@Override
    		public void onClick(View v)
    		{
    		    finish();
    		}
        });
	}
	
	OnClickListener onButtonClick = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
		    // grab the content to post
		    EditText et = (EditText)findViewById(R.id.textContent);
		    String content = et.getText().toString();
		    
		    // post that junk
		    try
            {
                ShackApi.postReply(ComposePostView.this, _replyToPostId, content);
            } catch (Exception e)
            {
                Log.e("DroidChatty", "Error posting reply", e);
                ErrorDialog.display(ComposePostView.this, "Error", "An error occured while posting:\n" + e.getMessage());
            }
            
		    // inform the calling activity that we posted
		    Intent reply = new Intent();
		    reply.putExtra(SingleThreadView.THREAD_ID, _replyToPostId);
		    setResult(RESULT_OK, reply);
		    
		    // lets get the hell out of here!
		    finish();
		}
	};
}
