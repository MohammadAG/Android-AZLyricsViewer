package com.mohammadag.azlyricsviewer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void getLyrics(View v) {
		EditText artistTextEdit = (EditText)findViewById(R.id.artistNameTextEdit);
		EditText songTextEdit = (EditText)findViewById(R.id.songNameTextEdit);
		
		String artistName = artistTextEdit.getText().toString();
		artistName = artistName.replaceAll("\\s","");
		artistName = artistName.replaceAll(" ","");
		
		String songName = songTextEdit.getText().toString();
		songName = songName.replaceAll("\\s","");
		songName = songName.replaceAll(" ","");
		
		String urlString = "http://www.azlyrics.com/lyrics/";
		urlString = urlString + artistName.toLowerCase() + "/";
		urlString = urlString + songName.toLowerCase() + ".html";
		
		try {
		    HttpClient client = new DefaultHttpClient();  ;
		    HttpGet get = new HttpGet(urlString);
		    HttpResponse responseGet = client.execute(get);  
		    HttpEntity resEntityGet = responseGet.getEntity();  
		    if (resEntityGet != null) {  
		        // do something with the response
		        String response = EntityUtils.toString(resEntityGet);
		    	Pattern p = Pattern.compile(
		                "<!-- start of lyrics -->(.*)<!-- end of lyrics -->",
		                Pattern.DOTALL
		            );
		    	
		    	Matcher matcher = p.matcher(response);

		    	if (matcher.find()) {
		    		String htmlLyrics = matcher.group(1);
		    		TextView lyricsView = (TextView)findViewById(R.id.textView1);
		    		lyricsView.setText(Html.fromHtml(htmlLyrics));
		    	} else {
		    		Log.i("AZLyricsViewer", "doesn't match");
		    	}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}
