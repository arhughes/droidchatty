package cc.hughes.droidchatty;

public class SearchResult
{
    private int _postId;
    private String _author;
    private String _content;
    private String _posted;
    
    public SearchResult(int postId, String author, String content, String posted)
    {
        _postId = postId;
        _author = author;
        _content = content;
        _posted = posted;
    }
    
    public int getPostId()
    {
        return _postId;
    }
    
    public String getAuthor()
    {
        return _author;
    }
    
    public String getContent()
    {
        return _content;
    }
    
    public String getPosted()
    {
        return _posted;
    }

}
