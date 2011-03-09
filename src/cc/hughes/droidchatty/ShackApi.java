package cc.hughes.droidchatty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ShackApi
{
    static final String POST_URL = "http://new.shacknews.com/api/chat/create/17.json";
    
    static final String BASE_URL = "http://shackapi.stonedonkey.com/";
    static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    public static ArrayList<SearchResult> search(String term, String author, String parentAuthor) throws Exception
    {
        String url = BASE_URL + "Search/?json=true&SearchTerm=" + URLEncoder.encode(term, "UTF8") + "&Author=" + URLEncoder.encode(author, "UTF8") + "&ParentAuthor=" + URLEncoder.encode(parentAuthor, "UTF8");
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        JSONObject result = getJson(url);
        
        JSONArray comments = result.getJSONArray("comments");
        for (int i = 0; i < comments.length(); i++)
        {
            JSONObject comment = comments.getJSONObject(i);

            int id = comment.getInt("id");
            String userName = comment.getString("author");
            String body = comment.getString("preview");
            String posted = comment.getString("date");
            
            results.add(new SearchResult(id, userName, body, posted));
        }
        
        return results;
    }
    
    public static String postReply(Context context, int replyToThreadId, String content) throws Exception
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userName = prefs.getString("userName", null);
        String password = prefs.getString("password", null);
        
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("content_type_id", "17");
        values.put("content_id", "");
        values.put("body", content);
        if (replyToThreadId > 0)
            values.put("parent_id", Integer.toString(replyToThreadId));
        
        return postJson(POST_URL, userName, password, values);
    }

    public static ArrayList<Thread> getThreads() throws ClientProtocolException, IOException, JSONException
    {
        ArrayList<Thread> threads = new ArrayList<Thread>();

        JSONObject json = getJson(BASE_URL + "index.json");

        // go through each of the comments and pull out the data that is used
        JSONArray comments = json.getJSONArray("comments");
        for (int i = 0; i < comments.length(); i++)
        {
            JSONObject comment = comments.getJSONObject(i);

            int id = comment.getInt("id");
            String userName = comment.getString("author");
            String body = comment.getString("body");
            String date = comment.getString("date");
            int replyCount = comment.getInt("reply_count");

            Thread thread = new Thread(id, userName, body, date, replyCount);
            threads.add(thread);
        }

        return threads;
    }

    public static ArrayList<Post> getPosts(int threadId) throws ClientProtocolException, IOException, JSONException
    {
        ArrayList<Post> posts = new ArrayList<Post>();
        HashSet<Integer> post_tracker = new HashSet<Integer>();

        JSONObject json = getJson(BASE_URL + "thread/" + threadId + ".json");

        // go through each of the comments and pull out the data that is used
        JSONArray comments = json.getJSONArray("comments");
        for (int i = 0; i < comments.length(); i++)
        {
            JSONObject comment = comments.getJSONObject(i);

            int postId = comment.getInt("id");
            String userName = comment.getString("author");
            String body = comment.getString("body");
            String date = comment.getString("date");

            Post post = new Post(postId, userName, body, date, 0);
            posts.add(post);
            post_tracker.add(postId);

            processPosts(comment, 1, posts, post_tracker);
        }

        return posts;
    }

    private static void processPosts(JSONObject comment, int level, ArrayList<Post> posts, HashSet<Integer> post_tracker) throws JSONException
    {
        JSONArray comments = comment.getJSONArray("comments");

        for (int i = 0; i < comments.length(); i++)
        {
            JSONObject p = comments.getJSONObject(i);

            int postId = p.getInt("id");
            String userName = p.getString("author");
            String body = p.getString("body");
            String date = p.getString("date");

            // only add this post if we haven't seen this post before
            // fixes duplicate posts coming back from the API
            // the duplicate post could end up in the wrong place though maybe?
            if (post_tracker.add(postId))
            {
                Post post = new Post(postId, userName, body, date, level);
                posts.add(post);
            }
            else
            {
                Log.w("ShackAPI", "Skipped duplicate post #" + postId);
            }

            processPosts(p, level + 1, posts, post_tracker);
        }
    }

    private static JSONObject getJson(String url) throws ClientProtocolException, IOException, JSONException
    {
        Log.d("DroidChatty", "Requested: " + url);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept-Encoding", "gzip");

        HttpResponse response = (HttpResponse)client.execute(get);
        HttpEntity entity = response.getEntity();

        if (entity != null)
        {
            String content = null;
            InputStream inStream = entity.getContent();
            try
            {
                // check for gzip'ed content
                Header encoding = response.getFirstHeader("Content-Encoding");
                if (encoding != null && encoding.getValue().equalsIgnoreCase("gzip"))
                    inStream = new GZIPInputStream(inStream);

                content = readStream(inStream);
                Log.d("DroidChatty", "Response: " + content);
            }
            finally
            {
                inStream.close();
            }

            // parse the darn thing
            return new JSONObject(content);
        }

        // well, that shouldn't happen
        throw new IOException("No response from website found.");
    }
    
    private static String postJson(String url, String userName, String password, HashMap<String, String> values) throws Exception
    {
        DefaultHttpClient client = new DefaultHttpClient();
        
        String data = "";
        for (Map.Entry<String, String> v : values.entrySet())
            data += v.getKey() + "=" + URLEncoder.encode(v.getValue(), "UTF8") + "&";
        Log.d("postJson", "data=" + data);
        
        HttpPut put = new HttpPut(url);
        put.setHeader("Authorization", "Basic " + Base64.encodeBytes((userName + ":" + password).getBytes()));
        put.setHeader("Accept-Encoding", "gzip");
        put.setEntity(new StringEntity(data, "UTF8"));
        
        HttpResponse response = client.execute(put);
        HttpEntity entity = response.getEntity();

        if (entity != null)
        {
            InputStream inStream = entity.getContent();
            try
            {
                // check for gzip'ed content
                Header encoding = response.getFirstHeader("Content-Encoding");
                if (encoding != null && encoding.getValue().equalsIgnoreCase("gzip"))
                    inStream = new GZIPInputStream(inStream);

                String content = readStream(inStream);
                Log.d("postJson", "response=" + content);
                return content;
            }
            finally
            {
                inStream.close();
            }
        }
        
        throw new Exception("No response from website.");
    }
    
    private static String readStream(InputStream stream) throws IOException
    {
        StringWriter output = new StringWriter();
        InputStreamReader input = new InputStreamReader(stream);

        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return output.toString();
    }
}
