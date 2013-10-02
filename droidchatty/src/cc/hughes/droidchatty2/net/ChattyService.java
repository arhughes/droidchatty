package cc.hughes.droidchatty2.net;

import android.content.SharedPreferences;
import android.util.Base64;

import com.squareup.wire.Wire;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChattyService {

    final static String USER_AGENT = "DroidChatty/2.0";
    final static String BASE_URL = "http://shackapidev.hughes.cc/v2/";

    final static String SHACKNEWS_POST_URL = "http://www.shacknews.com/api/chat/create/17.json";

    final static String FAKE_STORY_ID = "17";
    final static String FAKE_NEWS_ID = "2";

    SharedPreferences mPreferences;

    public ChattyService() { }

    public ChattyService(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public ThreadList getPage() throws IOException {
        return getPage(1);
    }

    private void requirePreferences() throws Exception {
        if (mPreferences == null)
            throw new Exception("Preferences must be supplied.");
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

    public int post(int parentId, String content, boolean isNewsThread) throws Exception {
        requirePreferences();

        String userName = mPreferences.getString("pref_shacknews_user", null);
        String password = mPreferences.getString("pref_shacknews_password", null);

        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("content_type_id", isNewsThread ? FAKE_NEWS_ID : FAKE_STORY_ID));
        values.add(new BasicNameValuePair("content_id", isNewsThread ? FAKE_NEWS_ID : FAKE_STORY_ID));
        values.add(new BasicNameValuePair("body", content));
        if (parentId > 0)
            values.add(new BasicNameValuePair("parent_id", Integer.toString(parentId)));

        String encoded = Base64.encodeToString((userName + ":" + password).getBytes(), Base64.NO_WRAP);
        UrlEncodedFormEntity e = new UrlEncodedFormEntity(values);
        String output;

        URL url = new URL(SHACKNEWS_POST_URL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            OutputStream os = connection.getOutputStream();
            try {
                e.writeTo(os);
                os.flush();
            } finally {
                os.close();
            }

            InputStream is = connection.getInputStream();
            try {
                output = readStream(is);
            } finally {
                is.close();
            }

        } finally {
            connection.disconnect();
        }


        JSONObject result = new JSONObject(output);
        if (!result.has("data"))
            throw new Exception("Missing response data:\n" + output);

        return result.getJSONObject("data").getInt("post_insert_id");
    }

    static String readStream(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
