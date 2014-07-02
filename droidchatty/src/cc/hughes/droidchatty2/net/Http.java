package cc.hughes.droidchatty2.net;

import android.util.Base64;

import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Http {

    public static final String USER_AGENT = "DroidChatty/2.0";

    public static class RequestSettings {
        public String Url;
        public AbstractHttpEntity PostContent;
        public String Username;
        public String Password;
        public String UserAgent = Http.USER_AGENT;
        public HashMap<String, String> Headers = new HashMap<String, String>();
    }

    public static String request(RequestSettings settings) throws IOException {
        HttpURLConnection connection = open(settings);

        // get and read the response
        InputStream is = new BufferedInputStream(connection.getInputStream());
        try {
            return readStream(is);
        } finally {
            is.close();
        }
    }

    public static HttpURLConnection open(RequestSettings settings) throws IOException {
        // if we need authorization, compute it now
        String authorization = null;
        if (settings.Username != null && settings.Password != null)
            authorization =  Base64.encodeToString((settings.Username + ":" + settings.Password).getBytes(), Base64.NO_WRAP);

        URL url = new URL(settings.Url);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestProperty("User-Agent", settings.UserAgent);

            // supply basic auth if we need to
            if (authorization != null)
                connection.setRequestProperty("Authorization", "Basic " + authorization);

            // set extra headers
            for (Map.Entry<String, String> header : settings.Headers.entrySet())
                connection.setRequestProperty(header.getKey(), header.getValue());

            // posts data if it is there
            if (settings.PostContent != null) {
                Header contentType = settings.PostContent.getContentType();
                if (contentType != null)
                    connection.setRequestProperty(contentType.getName(), contentType.getValue());

                connection.setDoOutput(true);

                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                try {
                    settings.PostContent.writeTo(os);
                } finally {
                    os.close();
                }
            }

            return connection;

        } catch (IOException ex) {
            // if an error occured try to close the connection
            try {
                connection.disconnect();
            } catch (Exception n) { }

            throw ex;
        }
    }

    public static String readStream(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
