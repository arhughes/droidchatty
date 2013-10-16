package cc.hughes.droidchatty2.net;

import android.content.SharedPreferences;
import android.util.Log;

import com.squareup.wire.Wire;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cc.hughes.droidchatty2.text.MessageParser;

public class ChattyService {

    final static String TAG = "ChattyService";

    final static String USER_AGENT = "DroidChatty/2.0";
    final static String BASE_URL = "http://shackapidev.hughes.cc/v2/";
    final static String SHACKNEWS_POST_URL = "http://www.shacknews.com/api/chat/create/17.json";
    final static String SHACKNEWS_SIGN_IN = "https://www.shacknews.com/account/signin";
    final static String SHACKNEWS_MESSAGE_GET = "https://www.shacknews.com/messages/";

    final static String FAKE_STORY_ID = "17";
    final static String FAKE_NEWS_ID = "2";

    SharedPreferences mPreferences;
    CookieManager mCookieManager;

    public ChattyService(SharedPreferences preferences) {
        mPreferences = preferences;

        // ensure we are saving cookies
        mCookieManager = (CookieManager)CookieHandler.getDefault();
        if (mCookieManager == null) {
            mCookieManager = new CookieManager();
            CookieHandler.setDefault(mCookieManager);
        }
    }

    public ThreadList getPage() throws IOException {
        return getPage(1);
    }

    public ThreadList getPage(int page) throws IOException {
        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url =  BASE_URL + "page.php?page=" + page;

        HttpURLConnection connection = Http.open(settings);
        try {
            Wire wire = new Wire();
            return wire.parseFrom(connection.getInputStream(), ThreadList.class);
        } finally {
            connection.disconnect();
        }

    }
    
    public Thread getThread(String id) throws IOException {
        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url =  BASE_URL + "thread.php?id=" + id;

        HttpURLConnection connection = Http.open(settings);
        try {
            Wire wire = new Wire();
            return wire.parseFrom(connection.getInputStream(), Thread.class);
        } finally {
            connection.disconnect();
        }
        
    }

    public ThreadList search(int page, String terms, String author, String parentAuthor) throws IOException {
        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = BASE_URL + "search.php?page=" + page + "&terms=" + URLEncoder.encode(terms, "UTF-8");

        HttpURLConnection connection = Http.open(settings);
        try {
            connection.setRequestProperty("User-Agent", USER_AGENT);

            Wire wire = new Wire();
            return wire.parseFrom(connection.getInputStream(), ThreadList.class);
        } finally {
            connection.disconnect();
        }

    }

    public int post(int parentId, String content, boolean isNewsThread) throws Exception {
        Http.RequestSettings settings = new Http.RequestSettings();

        settings.Url = SHACKNEWS_POST_URL;
        settings.Username = mPreferences.getString("pref_shacknews_user", null);
        settings.Password = mPreferences.getString("pref_shacknews_password", null);

        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("content_type_id", isNewsThread ? FAKE_NEWS_ID : FAKE_STORY_ID));
        values.add(new BasicNameValuePair("content_id", isNewsThread ? FAKE_NEWS_ID : FAKE_STORY_ID));
        values.add(new BasicNameValuePair("body", content));
        if (parentId > 0)
            values.add(new BasicNameValuePair("parent_id", Integer.toString(parentId)));

        settings.PostContent = new UrlEncodedFormEntity(values);

        String output = Http.request(settings);

        JSONObject result = new JSONObject(output);
        if (!result.has("data"))
            throw new Exception("Missing response data:\n" + output);

        return result.getJSONObject("data").getInt("post_insert_id");
    }

    public List<Message> getMessages(String folder, int page) throws Exception {

        // make sure we are trying for the right thing
        if (!folder.equals(Message.FOLDER_INBOX) && !folder.equals(Message.FOLDER_SENT))
            throw new Exception("Invalid message folder: " + folder);

        login();

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = SHACKNEWS_MESSAGE_GET + folder + "?page=" + page;

        String output = Http.request(settings);

        MessageParser parser = new MessageParser(output);
        return parser.parse();
    }

    public void markMessageAsRead(String id) throws Exception {
        login();

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = SHACKNEWS_MESSAGE_GET + "read";

        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("mid", id));
        settings.PostContent = new UrlEncodedFormEntity(values);

        // should probably check to make sure it was successful
        Http.request(settings);
    }

    public void sendMessage(String to, String subject, String message) throws Exception {
        login();

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = SHACKNEWS_MESSAGE_GET + "send";

        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("to", to));
        values.add(new BasicNameValuePair("subject", subject));
        values.add(new BasicNameValuePair("message", message));
        settings.PostContent = new UrlEncodedFormEntity(values);

        // should probably check to make sure it was successful
        Http.request(settings);
    }

    private boolean login() throws Exception {

        // already have a login cookie?
        HttpCookie loginCookie = getCookie(SHACKNEWS_SIGN_IN, "_shack_li_");
        if (loginCookie != null && !loginCookie.hasExpired()) {
            Log.d(TAG, "Reusing existing login cookie.");
            return true;
        }

        Log.d(TAG, "No existing login cookie, logging in.");

        String username = mPreferences.getString("pref_shacknews_user", null);
        String password = mPreferences.getString("pref_shacknews_password", null);

        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("get_fields[]", "result"));
        values.add(new BasicNameValuePair("user-identifier", username));
        values.add(new BasicNameValuePair("supplied-pass", password));
        values.add(new BasicNameValuePair("remember-login", "1"));

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = SHACKNEWS_SIGN_IN;
        settings.Headers.put("X-Requested-With", "XMLHttpRequest");
        settings.PostContent = new UrlEncodedFormEntity(values, "UTF-8");

        String output = Http.request(settings);

        Log.d(TAG, "Response from login request: " + output);

        JSONObject response = new JSONObject(output);
        if (response.has("result")) {
            JSONObject result = response.getJSONObject("result");
            return result.getBoolean("valid");
        }

        return false;
    }

    HttpCookie getCookie(String url, String name) throws URISyntaxException {
        for (HttpCookie cookie : mCookieManager.getCookieStore().get(new URI(url))) {
            if (cookie.getName().equals(name))
                return cookie;
        }

        return null;
    }


}
