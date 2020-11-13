package com.viatom.azur.tools;

import java.util.Date;

import com.viatom.azur.element.Constant;

import android.media.MediaPlayer;
import android.os.Environment;

public class VoiceManager {
	private static Date tempDate;

	public static void playVoice(Date date) {
		MediaPlayer player = new MediaPlayer();
		String path = Constant.dir
				+ "/"+StringMaker.makeDateFileName(date, Constant.CMD_TYPE_VOICE_CONVERTED);
		try {
			player.setDataSource(path);
			player.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		player.start();
	}

	public static void convertVoice(Date date) {
		tempDate = date;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				AdpcmDecoder.decodeAdpcm(Constant.dir+"/"+StringMaker.makeDateFileName(tempDate,
						Constant.CMD_TYPE_VOICE), Constant.dir+"/"+StringMaker.makeDateFileName(
						tempDate, Constant.CMD_TYPE_VOICE_CONVERTED));

				VoiceManager.playVoice(tempDate);
			}
		}).start();

	}

	static {
		//加载库，重要
		System.loadLibrary("adpcm_docode");
	}

}
