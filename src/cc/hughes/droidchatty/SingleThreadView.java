package cc.hughes.droidchatty;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.widget.TextView;

public class SingleThreadView extends Activity {
	
	static final String THREAD_ID = "THREAD_ID";
	static final String THREAD_CONTENT = "THREAD_CONTENT";
	static final String THREAD_AUTHOR = "THREAD_AUTHOR";
	static final String THREAD_POSTED = "THREAD_POSTED";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread_view);
		
		Bundle extras = getIntent().getExtras();
		int threadId = extras.getInt(THREAD_ID);
		String content = extras.getString(THREAD_CONTENT);
		String author = extras.getString(THREAD_AUTHOR);
		String posted = extras.getString(THREAD_POSTED);
		
		TextView tvAuthor = (TextView)findViewById(R.id.textUserName);
		TextView tvContent = (TextView)findViewById(R.id.textContent);
		TextView tvPosted = (TextView)findViewById(R.id.textPostedTime);
		
		tvAuthor.setText(author);
		tvPosted.setText(posted);
		tvContent.setText(fixContent(content));
		Linkify.addLinks(tvContent, Linkify.ALL);
		tvContent.setClickable(false);
	}
	
	private Spanned fixContent(String content)
	{
		// convert shack's css into real font colors since Html.fromHtml doesn't supporty css of any kind
		content = content.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");
		content = content.replaceAll("<span class=\"jt_green\">(.*?)</span>", "<font color=\"#8dc63f\">$1</font>");
		content = content.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
        content = content.replaceAll("<span class=\"jt_olive\">(.*?)</span>", "<font color=\"#808000\">$1</font>");
        content = content.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
        content = content.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
        content = content.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
        content = content.replaceAll("<span class=\"jt_lime\">(.*?)</span>",  "<font color=\"#c0ffc0\">$1</font>");
        content = content.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
        content = content.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
        content = content.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
        content = content.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
        content = content.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<del>1</del>");
		return Html.fromHtml(content);
	}

}
