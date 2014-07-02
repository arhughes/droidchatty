package cc.hughes.droidchatty2.net;

import android.text.Spanned;

import java.io.Serializable;

import cc.hughes.droidchatty2.text.TagParser;

public final class Reply implements Serializable {

    public int id;
    public String author;
    public String date;
    public int depth;
    public String category;
    public String body;

    public TreeBullet[] bullets;
    public int newness;

    private transient Spanned mBodyParsedPreview;
    private transient Spanned mBodyParsed;

    public Spanned bodyParsedPreview()
    {
        if (mBodyParsedPreview == null)
            mBodyParsedPreview = TagParser.fromHtml(body, false);
        return mBodyParsedPreview;
    }


    public Spanned bodyParsed()
    {
        if (mBodyParsed == null)
            mBodyParsed = TagParser.fromHtml(body);
        return mBodyParsed;
    }

    public static Reply fromRootPost(RootPost root) {
        Reply reply = new Reply();
        reply.id = root.id;
        reply.author = root.author;
        reply.date = root.date;
        reply.category = root.category;
        reply.body = root.body;
        reply.depth = 0;
        reply.bullets = new TreeBullet[0];
        return reply;
    }
}
