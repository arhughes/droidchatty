package cc.hughes.droidchatty2.net;

import android.text.Spanned;

import java.io.Serializable;

public class Message implements Serializable {
    public static final String FOLDER_INBOX = "inbox";
    public static final String FOLDER_SENT = "sent";

    public boolean Read;
    public String Id;
    public String OtherUser;
    public String Subject;
    public String Date;
    public Spanned Body;

}
