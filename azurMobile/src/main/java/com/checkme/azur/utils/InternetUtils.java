package com.checkme.azur.utils;

import android.content.Context;
import android.util.Base64;

import com.google.gson.Gson;
import com.checkme.azur.element.Constant;
import com.checkme.azur.element.Patient;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.newazur.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by wangxiaogang on 2016/12/13.
 */

public class InternetUtils {
    public static String makeAuthorization(Context context) {
        String email = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_EMAIL);
        String password = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_PASSWORD);
        byte[] bytes = String.format("%s:%s", email, password).getBytes();
        String aut = String.format("Basic %s", Base64.encodeToString(bytes, Base64.DEFAULT));
        LogUtils.d(aut);
        LogUtils.d(email + "==" + password);
        return aut;
    }

    public static String patientToString(Context context) {
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        String name = Constant.sUploadUser.getUserInfo().getName();
        String medicalId = deviceName + Constant.sUploadUser.getUserInfo().getID();
        String gender = Constant.sUploadUser.getUserInfo().getGender() == 0 ? "male" : "female";
        String birthDate = StringUtils.makeTimeString(Constant.sUploadUser.getUserInfo().getBirthDate());
        String height = Constant.sUploadUser.getUserInfo().getHeight() + context.getResources().getString(R.string.cm);
        String weight = Constant.sUploadUser.getUserInfo().getWeight() / 10 + context.getResources().getString(R.string.kg);
        String SN = PreferenceUtils.readStrPreferences(context, deviceName + Constant.SN);
        Patient.IdentifierBean identifierBean = new Patient.IdentifierBean(SN, medicalId);
        Patient patient = new Patient(identifierBean,
                name, gender, birthDate, height, weight);
        Gson gson = new Gson();
        String jsonPatient = gson.toJson(patient);
        LogUtils.d(jsonPatient);
        return jsonPatient;
    }

    public synchronized static void savePatientId(JSONObject result, Context context) {
        LogUtils.d(result.toString());
        try {
            String patientId = result.getString("patient_id");
            LogUtils.d(patientId + "==patient_ID");
            String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
            if (Constant.sUploadUser != null && Constant.sUploadUser.getUserInfo() != null)
                PreferenceUtils.savePreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID(), patientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static SSLContext getSSLContext(Context context) {
        CertificateFactory certificateFactory;
        InputStream inputStream;
        Certificate cer = null;
        KeyStore keystore;
        TrustManagerFactory trustManagerFactory;
        try {

            certificateFactory = CertificateFactory.getInstance("X.509");
            inputStream = context.getAssets().open(Constant.SSL_NAME);//这里导入SSL证书文件

            try {
                //读取证书
                cer = certificateFactory.generateCertificate(inputStream);

            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //创建一个证书库，并将证书导入证书库
            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null); //双向验证时使用
            keystore.setCertificateEntry("trust", cer);

            // 实例化信任库
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // 初始化信任库
            trustManagerFactory.init(keystore);

            SSLContext s_sSLContext = SSLContext.getInstance("TLS");
            s_sSLContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            //信任所有证书 （官方不推荐使用）
//         s_sSLContext.init(null, new TrustManager[]{new X509TrustManager() {
//
//				@Override
//				public X509Certificate[] getAcceptedIssuers() {
//					return null;
//				}
//
//				@Override
//				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
//						throws CertificateException {
//
//				}
//
//				@Override
//				public void checkClientTrusted(X509Certificate[] arg0, String arg1)
//						throws CertificateException {
//
//				}
//			}}, new SecureRandom());

            return s_sSLContext;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
