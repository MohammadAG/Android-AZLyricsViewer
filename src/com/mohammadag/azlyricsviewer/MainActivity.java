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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity implements OnInitListener {
	private static final String TAG = "AZLyricsViewer";
	private static final boolean DEBUG = false;

	private TextView mLyricsView = null;
	private EditText mSongTitleView = null;
	private EditText mArtistNameView = null;
	private Button mFetchButton = null;

	private TextToSpeech myTTS;
	private boolean mSpeakFinding = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean isShowingQuickLyrics = false;
		Intent intent = getIntent();
		if (intent.hasExtra(Constants.KEY_SONG_NAME) && intent.hasExtra(Constants.KEY_ARTIST_NAME)) {
			int sdk = android.os.Build.VERSION.SDK_INT;
			isShowingQuickLyrics = intent.getBooleanExtra(Constants.QUICK_LYRICS, false);
			if (isShowingQuickLyrics) {
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				setTheme(sdk >= 14 ? android.R.style.Theme_Holo_Dialog : android.R.style.Theme_Dialog);
			}
		}

		setContentView(R.layout.activity_main);

		myTTS = new TextToSpeech(this, this);

		findViews();
		if (isShowingQuickLyrics) {
			mArtistNameView.setVisibility(View.GONE);
			mSongTitleView.setVisibility(View.GONE);
			mFetchButton.setVisibility(View.GONE);
		}

		mArtistNameView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					getLyrics(v);
					return true;
				}
				return false;
			}
		});


		if (intent.hasExtra(Constants.KEY_SONG_NAME) && intent.hasExtra(Constants.KEY_ARTIST_NAME)) {
			setTheme(android.R.style.Theme_Dialog);
			String songName = intent.getStringExtra(Constants.KEY_SONG_NAME);
			String artistName = intent.getStringExtra(Constants.KEY_ARTIST_NAME);

			mSpeakFinding = intent.getBooleanExtra(GoogleSearchApi.KEY_VOICE_TYPE, false);

			mSongTitleView.setText(songName);
			mArtistNameView.setText(artistName);
			getLyrics(mFetchButton);
		} else {
			mSongTitleView.requestFocus();
		}
	}

	// setup TTS
	@Override
	public void onInit(int initStatus) {
		// check for successful instantiation
		if (initStatus == TextToSpeech.SUCCESS) {
			if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) {
				myTTS.setLanguage(Locale.US);

			}
			if (mSpeakFinding) {
				myTTS.speak("Showing lyrics", TextToSpeech.QUEUE_FLUSH, null);
				mSpeakFinding = false;
			}
		}
	}

	private void findViews() {
		mLyricsView = (TextView)findViewById(R.id.lyricsTextView);
		mSongTitleView = (EditText)findViewById(R.id.songNameTextEdit);
		mFetchButton = (Button)findViewById(R.id.fetchButton);
		mArtistNameView = (EditText)findViewById(R.id.artistNameTextEdit);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@SuppressLint("DefaultLocale")
	public void getLyrics(View v) {
		if (v != null) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		String artistName = mArtistNameView.getText().toString();
		artistName = artistName.replaceAll("\\s","");
		artistName = artistName.replaceAll(" ","");

		String songName = mSongTitleView.getText().toString();
		songName = songName.replaceAll("\\s","");
		songName = songName.replaceAll(" ","");

		String urlString = "http://www.azlyrics.com/lyrics/" + artistName.toLowerCase() + "/" + songName.toLowerCase() + ".html";

		mFetchButton.setEnabled(false);
		new FetchLyrics().execute(urlString);
	}

	private void setLyrics(String lyrics) {
		mLyricsView.setText(Html.fromHtml(lyrics));
		mFetchButton.setEnabled(true);
	}

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
			if (result != null) {
				setLyrics(result);
			} else {
				setLyrics(getString(R.string.lyrics_not_found));
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			return showAbout();
		case R.id.menu_donate:
			Intent intent = new Intent(Intent.ACTION_VIEW, 
					Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=L4ZLYKZRMFSVW"));
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean showAbout() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.app_name)
		.setMessage(R.string.about_text);

		alertDialog.show();

		return true;
	}
}
