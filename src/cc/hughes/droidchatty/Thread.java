package cc.hughes.droidchatty;

import android.text.Spanned;

public class Thread {
	
	private int _threadId;
	private String _userName;
	private String _posted;
	private String _content;
	private int _replyCount;
	private int _level;
	private Spanned _preview;
	
	public int getThreadID()
	{
		return _threadId;
	}
	
	public void setThreadID(int threadId)
	{
		_threadId = threadId;	
	}
	
	public String getUserName()
	{
		return _userName;
	}
	
	public void setUserName(String userName)
	{
		_userName = userName;
	}
	
	public String getPostedTime()
	{
		return _posted;
	}
	
	public void setPostedTime(String postedTime)
	{
		_posted = postedTime;
	}
	
	public String getContent()
	{
		return _content;
	}
	
	public void setContent(String content)
	{
		_content = content;
		_preview = PostFormatter.formatContent(content, false);
	}
	
	public int getReplyCount()
	{
		return _replyCount;
	}
	
	public void setReplyCount(int replyCount)
	{
		_replyCount = replyCount;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public Spanned getPostPreview()
	{
		return _preview;
	}

}
