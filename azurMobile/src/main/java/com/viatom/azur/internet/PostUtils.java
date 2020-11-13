package com.viatom.azur.internet;

import com.viatom.azur.utils.LogUtils;

import java.io.IOException;

import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

public class PostUtils {

//	public static JSONObject doPost(String url, List<NameValuePair> nvps)
//			throws IOException {
//
//		// 建立连接&设置参数
//		DefaultHttpClient httpClient = new DefaultHttpClient();
//		HttpPost httpPost = new HttpPost(url);
//		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//
//		// 获得response
//		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);//超时
//		HttpResponse response = httpClient.execute(httpPost);
//		HttpEntity entity = response.getEntity();
//		String strEntity = EntityUtils.toString(entity);
//
//		httpClient.getConnectionManager().shutdown();
//
//		// 生成json数据
//		try {
//			LogUtils.d(strEntity);
//			JSONObject jsonObject = new JSONObject(strEntity);
//			return jsonObject;
//		} catch (JSONException e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			return null;
//		}
//	}



}
