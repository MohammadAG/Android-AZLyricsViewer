package com.mohammadag.azlyricsviewer;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
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

import com.mohammadag.azlyricsviewer.LyricsFetcher.OnLyricsFetchedListener;

public class MainActivity extends Activity implements OnInitListener, OnLyricsFetchedListener {
	private TextView mLyricsView = null;
	private EditText mSongTitleView = null;
	private EditText mArtistNameView = null;
	private Button mFetchButton = null;

	private TextToSpeech myTTS;
	private boolean mSpeakFinding = false;
	private LyricsFetcher mLyricsFetcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLyricsFetcher = new LyricsFetcher(this);
		mLyricsFetcher.setOnLyricsFetchedListener(this);

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
			mLyricsView.setText(R.string.loading);
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

	@Override
	protected void onDestroy() {
		if (myTTS != null) {
			myTTS.stop();
			myTTS.shutdown();
		}

		super.onDestroy();
	}

	private void findViews() {
		mLyricsView = (TextView) findViewById(R.id.lyricsTextView);
		mSongTitleView = (EditText) findViewById(R.id.songNameTextEdit);
		mFetchButton = (Button) findViewById(R.id.fetchButton);
		mArtistNameView = (EditText) findViewById(R.id.artistNameTextEdit);
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

		String artistName =
				Utils.removeNonAlphabeticalChars(mArtistNameView.getText());

		String songName =
				Utils.removeNonAlphabeticalChars(mSongTitleView.getText());
		mFetchButton.setEnabled(false);
		mLyricsFetcher.fetchLyrics(songName, artistName);
	}

	private void setLyrics(String lyrics) {
		mLyricsView.setText(Html.fromHtml(lyrics));
		mFetchButton.setEnabled(true);
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

	@Override
	public void onLyricsFetched(boolean foundLyrics, String lyrics) {
		if (!foundLyrics)
			lyrics = getString(R.string.lyrics_not_found);
		
		setLyrics(lyrics);
	}
}
