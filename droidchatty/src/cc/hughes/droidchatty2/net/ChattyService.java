package cc.hughes.droidchatty2.net;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import com.fasterxml.jackson.core.JsonFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import cc.hughes.droidchatty2.text.MessageParser;

public class ChattyService {

    final static String TAG = "ChattyService";

    final static String BASE_URL = "https://winchatty.com/v2/";
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

    public List<RootPost> getPage() throws IOException {
        return getPage(1);
    }

    public List<RootPost> getPage(int page) throws IOException {
        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = BASE_URL + "getChattyRootPosts?offset=" + (40 * (page - 1));

        HttpURLConnection connection = Http.open(settings);
        try {
            return parseRootPosts(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }
    
    public List<Reply> getThread(int id) throws IOException {
        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url =  BASE_URL + "getThread?id=" + id;

        HttpURLConnection connection = Http.open(settings);
        try {
            return parseThread(connection);
        } finally {
            connection.disconnect();
        }
    }

    public List<RootPost> search(int page, String terms, String author, String parentAuthor) throws IOException {

        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();
        builder.appendPath("search");
        builder.appendQueryParameter("offset", String.valueOf(35 * (page - 1)));
        builder.appendQueryParameter("limit", "35");
        builder.appendQueryParameter("terms", terms);
        builder.appendQueryParameter("author", author);
        builder.appendQueryParameter("parentAuthor", parentAuthor);

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = builder.build().toString();

        HttpURLConnection connection = Http.open(settings);
        try {
            return parseSearchResults(connection);
        } finally {
            connection.disconnect();
        }
    }

    private List<RootPost> parseRootPosts(InputStream input) throws IOException {
        List<RootPost> posts = new ArrayList<RootPost>(40);

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(input);
        try {
            parser.nextToken(); // START_OBJECT
            while (parser.nextToken() != JsonToken.END_OBJECT)
            {
                if (parser.getCurrentName().equals("rootPosts"))
                {
                    parser.nextToken();
                    while (parser.nextToken() != JsonToken.END_ARRAY)
                    {
                        RootPost post = new RootPost();
                        while (parser.nextToken() != JsonToken.END_OBJECT)
                        {
                            String fieldName = parser.getCurrentName();
                            parser.nextToken();

                            if ("id".equals(fieldName))
                                post.id = parser.getValueAsInt();
                            else if ("author".equals(fieldName))
                                post.author = parser.getValueAsString();
                            else if ("date".equals(fieldName))
                                post.date = parser.getValueAsString();
                            else if ("body".equals(fieldName))
                                post.body = parser.getValueAsString();
                            else if ("postCount".equals(fieldName))
                                post.replies = (parser.getValueAsInt());
                            else if ("isParticipant".equals(fieldName))
                                post.replied = parser.getValueAsBoolean();
                            else if ("category".equals(fieldName))
                                post.category = parser.getValueAsString();

                        }
                        posts.add(post);
                    }
                }
            }
        }
        finally {
            parser.close();
        }

        return posts;
    }

    private List<Reply> parseThread(HttpURLConnection connection) throws IOException {
        SparseArray<Reply> reply_map = new SparseArray<Reply>();
        SparseArray<TreeSet<Reply>> parent_map = new SparseArray<TreeSet<Reply>>();

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(connection.getInputStream());
        try {
            parser.nextToken(); // START_OBJECT
            parser.nextToken(); // END_OBJECT
            if (parser.getCurrentName().equals("threads")) {
                parser.nextToken(); // START_ARRAY
                parser.nextToken(); // START_OBJECT
                parser.nextToken(); // thread_id
                parser.nextToken(); // thread_id_value
                parser.nextToken(); // posts
                if (parser.getCurrentName().equals("posts")) {

                    parser.nextToken(); // START_ARRAY
                    while (parser.nextToken() != JsonToken.END_ARRAY)
                    {
                        Reply reply = new Reply();
                        while (parser.nextToken() != JsonToken.END_OBJECT)
                        {
                            String fieldName = parser.getCurrentName();
                            parser.nextToken();

                            if ("id".equals(fieldName))
                                reply.id = parser.getValueAsInt();
                            else if ("author".equals(fieldName))
                                reply.author = parser.getValueAsString();
                            else if ("date".equals(fieldName))
                                reply.date = parser.getValueAsString();
                            else if ("body".equals(fieldName))
                                reply.body = parser.getValueAsString();
                            else if ("category".equals(fieldName))
                                reply.category = parser.getValueAsString();
                            else if ("parentId".equals(fieldName)) {

                                int parent_id = parser.getValueAsInt();
                                TreeSet<Reply> children = parent_map.get(parent_id);
                                if (children == null)
                                {
                                    children = new TreeSet<Reply>(ReplyCompare.ID_COMPARATOR);
                                    parent_map.put(parent_id, children);
                                }
                                children.add(reply);
                            }

                        }

                        reply_map.put(reply.id, reply);
                    }
                }
            }
        }
        finally {
            parser.close();
        }

        List<Reply> replies = orderReplies(reply_map, parent_map);

        // now that replies are in order we can add teh bullets
        addBullets(replies);

        return replies;
    }

    private static void addBullets(List<Reply> replies) {
        // add required bullets
        HashSet<Integer> levels = new HashSet<Integer>();
        for (int i = replies.size() - 1; i >= 0; i--)
        {
            Reply post = replies.get(i);

            TreeBullet[] bullets = new TreeBullet[post.depth];

            // use line for depths where there is a post below this one
            for (int j = 1; j < post.depth; j++)
                bullets[j - 1] = levels.contains(j) ? TreeBullet.Extend : TreeBullet.Blank;

            // end if this is the last item at this depth, branch if one lower than us
            if (post.depth > 0)
                bullets[post.depth - 1] = levels.contains(post.depth) ? TreeBullet.Branch : TreeBullet.End;

            // remove levels deeper than this one
            for (Iterator<Integer> j = levels.iterator(); j.hasNext();) {
                Integer level = j.next();
                if (level > post.depth)
                    j.remove();
            }

            // add the current depth
            levels.add(post.depth);
            post.bullets = bullets;
        }
    }

    private List<Reply> orderReplies(SparseArray<Reply> reply_map, SparseArray<TreeSet<Reply>> parent_map) {
        // make ordered list big enough for all posts
        List<Reply> ordered_replies = new ArrayList<Reply>(reply_map.size());

        // root post will have parent id of 0
        Reply root = parent_map.get(0).first();
        ordered_replies.add(root);

        // if there are children to the root post add them now
        TreeSet<Reply> children = parent_map.get(root.id);
        if (children != null)
            orderReplies(ordered_replies, parent_map, children, 1);

        // replies are in order by the post id, set the last 10's "newness"
        for (int i = 0; i < 10 && i < reply_map.size(); i++)
            reply_map.valueAt(reply_map.size() - i - 1).newness = 10 - i;

        return ordered_replies;
    }

    private void orderReplies(List<Reply> ordered_posts, SparseArray<TreeSet<Reply>> parent_map, TreeSet<Reply> children, int depth) {

        // find all children of current post and add them to the list
        for (Reply reply : children)
        {
            ordered_posts.add(reply);
            reply.depth = depth;

            // if this post has children, add them now
            TreeSet<Reply> next_children = parent_map.get(reply.id);
            if (next_children != null)
                orderReplies(ordered_posts, parent_map, next_children, depth + 1);
        }
    }

    List<RootPost> parseSearchResults(HttpURLConnection connection) throws IOException {
        List<RootPost> posts = new ArrayList<RootPost>(35);

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(connection.getInputStream());
        try {
            parser.nextToken(); // START_OBJECT
            parser.nextToken(); // posts

            while (parser.nextToken() != JsonToken.END_ARRAY)
            {
                RootPost post = new RootPost();
                while (parser.nextToken() != JsonToken.END_OBJECT)
                {
                    String fieldName = parser.getCurrentName();
                    parser.nextToken();

                    if ("id".equals(fieldName))
                        post.id = parser.getValueAsInt();
                    else if ("author".equals(fieldName))
                        post.author = parser.getValueAsString();
                    else if ("date".equals(fieldName))
                        post.date = parser.getValueAsString();
                    else if ("body".equals(fieldName))
                        post.body = parser.getValueAsString();
                    else if ("category".equals(fieldName))
                        post.category = parser.getValueAsString();

                }
                posts.add(post);
            }
        }
        finally {
            parser.close();
        }

        return posts;
    }

    public void post(int parentId, String content) throws Exception {

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = BASE_URL + "postComment";

        String userName = mPreferences.getString("pref_shacknews_user", null);
        String password = mPreferences.getString("pref_shacknews_password", null);
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("username", userName));
        values.add(new BasicNameValuePair("password", password));
        values.add(new BasicNameValuePair("parentId", Integer.toString(parentId)));
        values.add(new BasicNameValuePair("text", content));

        settings.PostContent = new UrlEncodedFormEntity(values);

        String output = Http.request(settings);

        JSONObject result = new JSONObject(output);
        if (!result.has("result"))
            throw new Exception("Missing response data:\n" + result.getString("message"));

    }

    public List<Message> getMessages(String folder, int page) throws Exception {

        // make sure we are trying for the right thing
        if (!folder.equals(Message.FOLDER_INBOX) && !folder.equals(Message.FOLDER_SENT))
            throw new Exception("Invalid message folder: " + folder);

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = BASE_URL + "getMessages";

        String userName = mPreferences.getString("pref_shacknews_user", null);
        String password = mPreferences.getString("pref_shacknews_password", null);
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("username", userName));
        values.add(new BasicNameValuePair("password", password));
        values.add(new BasicNameValuePair("folder", folder));
        values.add(new BasicNameValuePair("page", Integer.toString(page)));

        settings.PostContent = new UrlEncodedFormEntity(values);

        HttpURLConnection connection = Http.open(settings);
        try {
            return parseMessages(connection);
        } finally {
            connection.disconnect();
        }
    }

    List<Message> parseMessages(HttpURLConnection connection) throws IOException {
        List<Message> messages = new ArrayList<Message>(35);

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(connection.getInputStream());
        try {

            // skip everything until we get to messages
            while (!"messages".equals(parser.getCurrentName())) {
                parser.nextToken();
            }

            parser.nextToken(); // read past "messages"

            while (parser.nextToken() != JsonToken.END_ARRAY)
            {
                Message message = new Message();
                while (parser.nextToken() != JsonToken.END_OBJECT)
                {
                    String fieldName = parser.getCurrentName();
                    parser.nextToken();

                    if ("id".equals(fieldName))
                        message.id = parser.getValueAsInt();
                    else if ("from".equals(fieldName))
                        message.from = parser.getValueAsString();
                    else if ("to".equals(fieldName))
                        message.to = parser.getValueAsString();
                    else if ("subject".equals(fieldName))
                        message.subject = parser.getValueAsString();
                    else if ("body".equals(fieldName))
                        message.body = parser.getValueAsString();
                    else if ("date".equals(fieldName))
                        message.date = parser.getValueAsString();
                    else if ("unread".equals(fieldName))
                        message.unread = parser.getValueAsBoolean();

                }
                messages.add(message);
            }
        }
        finally {
            parser.close();
        }

        return messages;
    }

    public void markMessageAsRead(int id) throws Exception {

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = BASE_URL + "markMessageRead";

        String userName = mPreferences.getString("pref_shacknews_user", null);
        String password = mPreferences.getString("pref_shacknews_password", null);
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("username", userName));
        values.add(new BasicNameValuePair("password", password));
        values.add(new BasicNameValuePair("messageId", Integer.toString(id)));
        settings.PostContent = new UrlEncodedFormEntity(values);

        // should probably check to make sure it was successful
        Http.request(settings);
    }

    public void sendMessage(String to, String subject, String message) throws Exception {

        Http.RequestSettings settings = new Http.RequestSettings();
        settings.Url = BASE_URL + "sendMessage";

        String userName = mPreferences.getString("pref_shacknews_user", null);
        String password = mPreferences.getString("pref_shacknews_password", null);
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("username", userName));
        values.add(new BasicNameValuePair("password", password));
        values.add(new BasicNameValuePair("to", to));
        values.add(new BasicNameValuePair("subject", subject));
        values.add(new BasicNameValuePair("body", message));
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


    static class ReplyCompare implements Comparator<Reply> {
        public static final ReplyCompare ID_COMPARATOR = new ReplyCompare();

        @Override
        public int compare(Reply reply, Reply reply2) {
            // shouldn't overflow int here for a long time
            return reply.id - reply2.id;
        }
    }
}
