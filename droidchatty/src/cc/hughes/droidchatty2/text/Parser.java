package cc.hughes.droidchatty2.text;

public abstract class Parser {

    private Integer[] mCursors;
    private String mContent ;
    private int mLength;

    public Parser(String content) {
        mContent = content;
        mLength = content.length();
        mCursors = new Integer[] { 0, 0 };
    }

    protected int peek(int cursor, String keyword) {
        return mContent.indexOf(keyword, mCursors[cursor]);
    }

    protected String clip(String[] beforeKeywords, String afterKeyword) throws Exception {
        seek(0, beforeKeywords);
        incr(0);
        seek(1, afterKeyword);
        return read();
    }

    protected String clip(String beforeKeywords, String afterKeyword) throws Exception {
        seek(0, beforeKeywords);
        incr(0);
        seek(1, afterKeyword);
        return read();
    }

    protected void seek(int cursor, String[] keywords) throws Exception {
        for (String keyword : keywords)
            seek(cursor, keyword);
    }

    protected void seek(int cursor, String keywords) throws Exception {
        int i = mCursors[0];
        int j = mContent.indexOf(keywords, i);
        if (j < 0)
            throw new Exception("Did not find '" + keywords + "' starting at index '" + i + "'");
        mCursors[cursor] = j;
    }

    protected void incr(int cursor) throws Exception {
        mCursors[cursor]++;
        if (mCursors[cursor] >= mLength)
            throw new Exception("Unexpected end of HTML data.");
    }

    protected String read() {
        return mContent.substring(mCursors[0], mCursors[1]);
    }
}
