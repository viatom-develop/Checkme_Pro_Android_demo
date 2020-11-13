package com.checkme.azur.utils;

import android.os.Handler;
import android.os.Message;

public class MsgUtils {

	
	static public void sendMsg(Handler handler,Object obj,int what,int arg1){
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.obj = obj;
		msg.what = what;
		msg.arg1 = arg1;
		handler.sendMessage(msg);
	}
	
	
	static public void sendMsg(Handler handler,Object obj,int what){
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.obj = obj;
		msg.what = what;
		handler.sendMessage(msg);
	}
	
	static public void sendMsg(Handler handler,int what){
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = what;
		handler.sendMessage(msg);
	}
	
	
	static public void sendMsg(Handler handler,int what,int arg1){
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		handler.sendMessage(msg);
	}
	
	
	static public void sendMsg(Handler handler,int what,int arg1,int arg2){
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		handler.sendMessage(msg);
	}
	
	
	static public void sendMsg(Handler handler,Object obj){
		Message msg = new Message();
		msg.obj = obj;
		handler.sendMessage(msg);
	}
	
	
	static public void sendMsg(Handler handler,int what,Object obj,int arg1){
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.obj = obj;
		msg.arg1 = arg1;
		msg.what = what;
		handler.sendMessage(msg);
	}
}
