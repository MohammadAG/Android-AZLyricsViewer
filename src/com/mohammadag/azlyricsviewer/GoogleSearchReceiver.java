package com.mohammadag.azlyricsviewer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GoogleSearchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String queryText = intent.getStringExtra(GoogleSearchApi.KEY_QUERY_TEXT);

		if (!queryText.contains("lyrics"))
			return;

		String songName, artistName;

		/* This is probably stupid, but I seriously don't know how to parse strings... */
		Pattern p = Pattern.compile( "for(.*)by", Pattern.DOTALL);
		Matcher matcher = p.matcher(queryText);

		if (matcher.find()) {
			songName = matcher.group(1);
			
			int index = queryText.lastIndexOf("by");
			artistName = queryText.substring(index+2, queryText.length());
		} else {
			p = Pattern.compile( "by(.*)for", Pattern.DOTALL);
			matcher = p.matcher(queryText);
			
			if (matcher.find()) {
				artistName = matcher.group(1);
				
				int index = queryText.lastIndexOf("for");
				songName = queryText.substring(index+3, queryText.length());
			} else {
				return;
			}
		}

		Intent lyricsIntent = new Intent(context, MainActivity.class);
		lyricsIntent.putExtra(Constants.KEY_SONG_NAME, songName);
		lyricsIntent.putExtra(Constants.KEY_ARTIST_NAME, artistName);
		lyricsIntent.putExtra(GoogleSearchApi.KEY_VOICE_TYPE,
				intent.getBooleanExtra(GoogleSearchApi.KEY_VOICE_TYPE, false));
		lyricsIntent.putExtra(Constants.QUICK_LYRICS, true);
		lyricsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(lyricsIntent);
	}

}
