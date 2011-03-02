package cc.hughes.droidchatty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ComposePostView extends Activity {

	private int _replyToPostId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post);
        
        // grab the post being replied to
        _replyToPostId = getIntent().getExtras().getInt(SingleThreadView.THREAD_ID);
        
        Button reply_button = (Button)findViewById(R.id.replyButton);
        Button cancel_button = (Button)findViewById(R.id.cancelButton);
        
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
		    int postId = ShackApi.postReply(_replyToPostId, content);
		    
		    // inform the calling activity that we posted, and what our new post id is
		    Intent reply = new Intent();
		    reply.putExtra(SingleThreadView.THREAD_ID, postId);
		    setResult(RESULT_OK, reply);
		    
		    // lets get the hell out of here!
		    finish();
		}
	};
	
}
