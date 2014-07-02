package cc.hughes.droidchatty2.net;

import android.text.Spanned;

import java.io.Serializable;

import cc.hughes.droidchatty2.text.TagParser;

public final class RootPost implements Serializable {

    public int id;
    public String author;
    public String date;
    public int replies;
    public Boolean replied;
    public String category;
    public String body;

    public int newReplies;

    private transient Spanned mBodyParsed;

    public Spanned bodyParsed()
    {
        if (mBodyParsed == null)
            mBodyParsed = TagParser.fromHtml(body);
        return mBodyParsed;
    }
}
