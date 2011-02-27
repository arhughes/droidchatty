package cc.hughes.droidchatty;

public class Thread {
	
	private int _threadId;
	private String _userName;
	private String _posted;
	private String _content;
	private int _replyCount;
	
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
	}
	
	public int getReplyCount()
	{
		return _replyCount;
	}
	
	public void setReplyCount(int replyCount)
	{
		_replyCount = replyCount;
	}

}
