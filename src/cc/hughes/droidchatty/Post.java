package cc.hughes.droidchatty;

import android.text.Spanned;

public class Post {

    private int _postId;
    private String _userName;
    private String _content;
    private String _posted;
    private int _level;

    private Spanned _preview;

    public Post(int postId, String userName, String content, String posted, int level)
    {
        _postId = postId;
        _userName = userName;
        _content = content;
        _posted = posted;
        _level = level;
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

    public Spanned getPreview()
    {
        if (_preview == null)
            _preview = PostFormatter.formatContent(this, false);
        return _preview;
    }

}
