package cc.hughes.droidchatty2.net;

import com.squareup.wire.Wire;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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

            Wire wire = new Wire();
            return wire.parseFrom(connection.getInputStream(), ThreadList.class);
        } finally {
            connection.disconnect();
        }

    }
    
    public Thread getThread(String id) throws IOException {

        URL url = new URL(BASE_URL + "thread.php?id=" + id);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestProperty("User-Agent", USER_AGENT);
            Wire wire = new Wire();
            return wire.parseFrom(connection.getInputStream(), Thread.class);
        } finally {
            connection.disconnect();
        }
        
    }

}
