package com.mohammadag.azlyricsviewer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class LyricsFetcher {
	public interface OnLyricsFetchedListener {
		abstract void onLyricsFetched(boolean foundLyrics, String lyrics);
	}

	private static final String TAG = "AZLyricsViewer:LyricsFetcher";
	private static final boolean DEBUG = false;
	
	private class FetchLyrics extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... arg0) {
			try {
				HttpClient client = new DefaultHttpClient();  ;
				HttpGet get = new HttpGet(arg0[0]);
				HttpResponse responseGet = client.execute(get);  
				HttpEntity resEntityGet = responseGet.getEntity();  
				if (resEntityGet != null) {  
					String response = EntityUtils.toString(resEntityGet);
					Pattern p = Pattern.compile(
							"<!-- start of lyrics -->(.*)<!-- end of lyrics -->",
							Pattern.DOTALL
							);

					Matcher matcher = p.matcher(response);

					if (matcher.find()) {
						String htmlLyrics = matcher.group(1);
						return htmlLyrics;
					} else {
						if (DEBUG) Log.i(TAG, "doesn't match");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (mListener != null) {
				 mListener.onLyricsFetched(result != null, result);
			}
			super.onPostExecute(result);
		}
	}
	
	private LyricsFetcher.OnLyricsFetchedListener mListener = null;
	
	public LyricsFetcher(Context context) { }
	
	public void fetchLyrics(String songName, String artistName) {
		Locale locale = Locale.getDefault();
		String urlString = "http://www.azlyrics.com/lyrics/"
		+ artistName.toLowerCase(locale)
		+ "/" + songName.toLowerCase(locale) + ".html";
		new FetchLyrics().execute(urlString);
	}
	
	public void setOnLyricsFetchedListener(OnLyricsFetchedListener listener) {
		mListener = listener;
	}
}
