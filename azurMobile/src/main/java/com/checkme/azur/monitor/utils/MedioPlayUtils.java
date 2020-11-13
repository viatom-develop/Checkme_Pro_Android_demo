package com.checkme.azur.monitor.utils;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.checkme.azur.monitor.ui.MonitorActivity;

import java.io.IOException;

public class MedioPlayUtils {

	static AssetManager asset = MonitorActivity.instance.getAssets();
	/*public static MediaPlayer player ;

	public static MediaPlayer getIndtance(){

		if(player == null){
			player = new MediaPlayer();
		}
		return player;
	}*/

	public static void playVoice(String date) {
		final MediaPlayer player  = new MediaPlayer();
		//	player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {

			AssetFileDescriptor fileDescriptor = asset.openFd(date);

			player.setDataSource(fileDescriptor.getFileDescriptor(),
					fileDescriptor.getStartOffset(),
					fileDescriptor.getLength());

			fileDescriptor.close();
			player.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.start();

		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				player.stop();
				player.release();
			}
		});


	}

	/*public void stop(){
		if(player!=null){
			if(player.isPlaying()){
				player.stop();
				player.release();
			}
		}

	}*/
	static {
		//加载库，重要
		System.loadLibrary("adpcm_docode");
	}
}
