package cc.hughes.droidchatty2.net;

import android.text.Spanned;

import java.io.Serializable;

import cc.hughes.droidchatty2.text.TagParser;

public class Message implements Serializable {
    public static final String FOLDER_INBOX = "inbox";
    public static final String FOLDER_SENT = "sent";

    public boolean unread;
    public int id;
    public String from;
    public String to;
    public String subject;
    public String date;
    public String body;

    private Spanned mBodyParsed;

    public Spanned bodyParsed() {
        if (mBodyParsed == null)
            mBodyParsed = TagParser.fromHtml(body);
        return mBodyParsed;
    }

}
