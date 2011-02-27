package cc.hughes.droidchatty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShackApi
{
	static final String BASE_URL = "http://shackapi.stonedonkey.com/";
	static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	public static ArrayList<Thread> getThreads() throws ClientProtocolException, IOException, JSONException
	{
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		JSONObject json = getJson(BASE_URL + "index.json");
		
		// go through each of the comments and pull out the data that is used
		JSONArray comments = json.getJSONArray("comments");
		for (int i = 0; i < comments.length(); i++)
		{
			JSONObject comment = comments.getJSONObject(i);
			
			Thread thread = new Thread();
			thread.setContent(comment.getString("body"));
			thread.setUserName(comment.getString("author"));
			thread.setThreadID(comment.getInt("id"));
			thread.setPostedTime(comment.getString("date"));
			thread.setReplyCount(comment.getInt("reply_count"));
			
			threads.add(thread);
		}
		
		return threads;
	}
	
	public static ArrayList<Thread> getThread(int threadId) throws ClientProtocolException, IOException, JSONException
	{
		ArrayList<Thread> posts = new ArrayList<Thread>();
		
		JSONObject json = getJson(BASE_URL + "thread/" + threadId + ".json");
		
		// go through each of the comments and pull out the data that is used
		JSONArray comments = json.getJSONArray("comments");
		for (int i = 0; i < comments.length(); i++)
		{
			JSONObject comment = comments.getJSONObject(i);
			
			Thread thread = new Thread();
			thread.setContent(comment.getString("body"));
			thread.setUserName(comment.getString("author"));
			thread.setThreadID(comment.getInt("id"));
			thread.setPostedTime(comment.getString("date"));
			thread.setLevel(0);
			
			posts.add(thread);
			
			processPosts(comment, 1, posts);
		}
		
		return posts;
	}
	
	private static void processPosts(JSONObject comment, int level, ArrayList<Thread> posts) throws JSONException
	{
		JSONArray comments = comment.getJSONArray("comments");
		
		for (int i = 0; i < comments.length(); i++)
		{
			JSONObject p = comments.getJSONObject(i);
			
			Thread post = new Thread();
			post.setContent(p.getString("body"));
			post.setUserName(p.getString("author"));
			post.setThreadID(p.getInt("id"));
			post.setPostedTime(p.getString("date"));
			post.setLevel(level);
			posts.add(post);
			
			processPosts(p, level + 1, posts);
		}
	}
	
	private static JSONObject getJson(String url) throws ClientProtocolException, IOException, JSONException
	{
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
