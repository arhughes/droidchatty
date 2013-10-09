package cc.hughes.droidchatty2.text;

import java.util.ArrayList;
import java.util.List;

import cc.hughes.droidchatty2.net.Message;

public class MessageParser extends Parser {

    public MessageParser(String content) {
        super(content);
    }

    public List<Message> parse() throws Exception {

        List<Message> messages = new ArrayList<Message>();

        seek(0, "<li class=\"message");
        while (peek(0, "<li class=\"message") >= 0) {
            Message message = new Message();

            String classes = clip(new String[] { "<li class=\"message", "\""}, "\"");

            message.Read = classes.contains("read");
            message.Id = clip(new String[] { "<input type=\"checkbox\" class=\"mid\" name=\"messages[]\"", "value=", "\"" }, ">");
            message.OtherUser = clip(new String[] { "<span class=\"message-username\"", ">"}, "</span>");
            message.Subject = clip(new String[] { "<span class=\"message-subject\"", ">"}, "</span>");
            message.Date = clip(new String[] { "<span class=\"message-date\"", ">"}, "</span>");
            message.Body = TagParser.fromHtml(clip(new String[] { "<div class=\"message-body\"", ">"}, "</div>"));

            messages.add(message);
        }
        return messages;
    }

}
