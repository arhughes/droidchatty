package cc.hughes.droidchatty;

public class SearchResult
{
    private int _postId;
    private String _author;
    private String _content;
    
    public SearchResult(int postId, String author, String content)
    {
        _postId = postId;
        _author = author;
        _content = content;
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

}
