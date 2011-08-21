package cc.hughes.droidchatty;

import android.text.Spanned;

public class Thread {

    private int _threadId;
    private String _userName;
    private String _posted;
    private String _content;
    private int _replyCount;
    private int _replyCountPrevious;
    private String _moderation;
    private boolean _replied;

    private Spanned _preview;

    public Thread(int threadId, String userName, String content, String posted, int replyCount, String moderation, boolean replied)
    {
        _threadId = threadId;
        _userName = userName;
        _content = content;
        _posted = posted;
        _replyCount = replyCount;
        _moderation = moderation;
        _replied = replied;
    }

    public int getThreadId()
    {
        return _threadId;
    }

    public String getUserName()
    {
        return _userName;
    }

    public String getPosted()
    {
        return _posted;
    }

    public String getContent()
    {
        return _content;
    }

    public int getReplyCount()
    {
        return _replyCount;
    }

    public int getReplyCountPrevious()
    {
        return _replyCountPrevious;
    }

    public void setReplyCountPrevious(int replyCountPrevious)
    {
        _replyCountPrevious = replyCountPrevious;
    }
    
    public String getModeration()
    {
        return _moderation;
    }
    
    public boolean getReplied()
    {
        return _replied;
    }

    public Spanned getPreview(boolean showTags)
    {
        if (_preview == null)
            _preview = PostFormatter.formatContent(this, false, showTags);
        return _preview;
    }

}
