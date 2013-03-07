package cc.hughes.droidchatty.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import cc.hughes.droidchatty.net.Message.ThreadList;

public class ChattyService {

    final static String USER_AGENT = "DroidChatty/2.0";
    final static String BASE_URL = "http://shackapidev.hughes.cc/v2/";

    public ThreadList getPage() throws IOException {
        return getPage(1);
    }

    public ThreadList getPage(int page) throws IOException {

        URL url = new URL(BASE_URL + "page.php?page=" + page);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestProperty("User-Agent", USER_AGENT);
            return ThreadList.parseFrom(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

    }
    
    public Message.Thread getThread(String id) throws IOException {

        URL url = new URL(BASE_URL + "thread.php?id=" + id);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestProperty("User-Agent", USER_AGENT);
            return Message.Thread.parseFrom(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
        
    }

}
