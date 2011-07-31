package cc.hughes.droidchatty;

import android.text.Spanned;

public class Post {

    private int _postId;
    private String _userName;
    private String _content;
    private String _posted;
    private int _level;
    private String _moderation;
    private int _order = Integer.MAX_VALUE;

    private Spanned _preview;

    public Post(int postId, String userName, String content, String posted, int level, String moderation)
    {
        _postId = postId;
        _userName = userName;
        _content = content;
        _posted = posted;
        _level = level;
        _moderation = moderation;
    }

    public int getPostId()
    {
        return _postId;
    }

    public String getUserName()
    {
        return _userName;
    }

    public String getContent()
    {
        return _content;
    }

    public String getPosted()
    {
        return _posted;
    }

    public int getLevel()
    {
        return _level;
    }
    
    public String getModeration()
    {
        return _moderation;
    }
    
    public void setOrder(int value)
    {
        _order = value;
    }
    
    public int getOrder()
    {
        return _order;
    }

    public Spanned getPreview()
    {
        if (_preview == null)
            _preview = PostFormatter.formatContent(this, false);
        return _preview;
    }

}
