package cc.hughes.droidchatty;

import android.text.Spanned;

public class Thread {

    private int _threadId;
    private String _userName;
    private String _posted;
    private String _content;
    private int _replyCount;
    private int _replyCountPrevious;

    private Spanned _preview;

    public Thread(int threadId, String userName, String content, String posted, int replyCount)
    {
        _threadId = threadId;
        _userName = userName;
        _content = content;
        _posted = posted;
        _replyCount = replyCount;

        _preview = PostFormatter.formatContent(this, false);
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

    public Spanned getPreview()
    {
        return _preview;
    }

}
