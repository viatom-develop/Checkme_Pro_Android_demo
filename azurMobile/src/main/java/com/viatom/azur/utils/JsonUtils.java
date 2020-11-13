package com.viatom.azur.utils;

import android.content.Context;
import android.view.View;

import com.viatom.azur.element.CheckmeDevice;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.ECGInnerItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.SLMInnerItem;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.measurement.TempItem;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.tools.StringMaker;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static String makeSpo2String(SPO2Item item) {
        String Times = StringMaker.makeTimeString(item.getDate());
        String Dates = StringMaker.makeDateString(item.getDate());
        String IMGResult = item.getImgResult() + "";
        String Oxygen = item.getOxygen() == 0 ? "--" : (item.getOxygen() + "%");
        String PR = item.getPr() == 0 ? "--" : (item.getPr() + "/min");
        String PI = item.getPi() == 0 ? "--" : ("PI " + item.getPi());
        String SpO2 = "{" +
                "\"Time\": \"" + Times + "\"," +
                "\"Dates\": \"" + Dates + "\"," +
                "\"IMGResult\": \"" + IMGResult + "\"," +
                "\"PR\": \"" + PR + "\"," +
                "\"Oxygen\": \"" + Oxygen + "\"," +
                "\"PI\": \"" + PI + "\"" +
                "}";
        return SpO2;
    }

    public static String makeTMPString(TempItem Item) {
        String Times = StringMaker.makeTimeString(Item.getDate());
        String Dates = StringMaker.makeDateString(Item.getDate());
        String MeasuringMode = Item.getMeasuringMode() + "";
        String IMGResult = Item.getImgResult() + "";
        String Result;
        if (Constant.thermometerUnit == Constant.THERMOMETER_C) {
            Result = Item.getResult() + "℃";
        } else {
            Result = String.format("%.1f", cToF(Item.getResult())) + "℉";
        }
        String temp = "{" +
                "\"Time\": \"" + Times + "\"," +
                "\"Dates\": \"" + Dates + "\"," +
                "\"MeasuringMode\":\"" + MeasuringMode + "\"" +
                "\"IMGResult\": \"" + IMGResult + "\"," +
                "\"Result\":\"" + Result + "\"" +
                "}";
        return temp;
    }

    private static float cToF(float c) {
        float f = c * 9 / 5 + 32;
        return f;
    }

    public static String makeDailyCheckObservation(Context context, DailyCheckItem item) {
        String name = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_USER_NAME);
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        String medical_id = deviceName + Constant.sUploadUser.getUserInfo().getID();
        String patient_id = PreferenceUtils.readStrPreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID());
        LogUtils.d(patient_id+"==patient_ID");
        String SN = PreferenceUtils.readStrPreferences(context, deviceName + Constant.SN);
        String identifyVal = SN + medical_id + StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_DLC);
        String effectiveDateTime = StringUtils.makeTimeString(item.getDate());

        String faceText = item.getECGIMGResult() == 0 ? "smile face" : (item.getECGIMGResult() == 1 ? "cry face" : "none");
        String HR = item.getHR() != 0 ? item.getHR() + "" : "--";
       // ECGInnerItem innerItem = FileUtils.readECGInnerItem(Constant.dir, StringMaker.makeDateFileName(item.getDate(), MeasurementConstant.CMD_TYPE_ECG_NUM));
        ECGInnerItem innerItem = FileUtils.readECGInnerItem(Constant.dir, StringMaker.makeDateFileName(item.getDate(), MeasurementConstant.CMD_TYPE_ECG_NUM), context);

        String QRS = (innerItem != null && innerItem.getQRS() != 0) ? innerItem.getQRS() + "" : "--";
        String ST;
        if (innerItem == null || innerItem.getCheckMode() == Constant.ECG_CHECK_MODE_HH ||
                innerItem.getCheckMode() == Constant.ECG_CHECK_MODE_HC || innerItem.getST() == 0XFF)//错误特征值
            ST = "--";
        else {
            float tempST = ((float) innerItem.getST()) / 100;
            ST = ((tempST >= 0 ? "+" : "") + String.valueOf(tempST));
        }
        String QT;
        String QTc;
        String fileName = StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_NUM);
        if (FileDriver.isFileExist(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME)) {
            byte[] buf = FileDriver.read(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME);
            if (buf[0] == 1) {
                QT = "--";
                QTc = "--";
            } else {
                QT=innerItem.getQT() == 0 ? "--" : String.valueOf(innerItem.getQT());
                QTc =innerItem.getQTc() == 0 ? "--" : String.valueOf(innerItem.getQTc()) ;
            }

        } else {
            QT = "--";
            QTc = "--";
        }
        String PVC=innerItem==null?"0":innerItem.getPVCs()+"";
        String sbp=StringMaker.makeBPValueJsonStr(item);
        String SpO2=item.getSPO2()==0?"--":item.getSPO2()+"";
        String PI=-item.getPI() == 0 ? "--" : item.getPI()+"";
        String MDC_ECG_ELEC_POTL_I=StringUtils.byte2hex(innerItem==null?new byte[0]:innerItem.getEcgDat());
        String dailyCheckObservation = "{" +
                "\"resourceType\": \"Observation\"," +
                "\"id\": \"daily check\"," +
                "\"identifier\": {" +
                "\"system\": \"https://cloud.viatomtech.com/fhir\"," +
                "\"value\": \"" + identifyVal + "\"" +
                "}," +
                "\"category\": {\"coding\": {" +
                "\"system\": \"http://hl7.org/fhir/observation-category\"," +
                "\"code\": \"procedure\"," +
                "\"display\": \"Procedure\"" +
                "}}," +
                "\"code\": {\"coding\": {" +
                "\"system\": \"https://cloud.viatomtech.com/fhir\"," +
                "\"code\": \"160401\"," +
                "\"display\": \"Daily Check\"" +
                "}}," +
                "\"effectiveDateTime\": \"" + effectiveDateTime + "\"," +
                "\"subject\": {" +
                "\"reference\": \"" + patient_id + "\"," +
                "\"display\": \"" + name + "\"" +
                "}," +
                "\"interpretation\": {" +
                "\"coding\": {" +
                "\"system\": \"http://hl7.org/fhir/v2/0078\"," +
                "\"code\": \"N\"," +
                "\"display\": \"Normal\"" +
                "}," +
                "\"text\": \"" + faceText + "\"" +
                "}," +
                "\"device\": {" +
                "\"sn\": \"" + SN + "\"," +
                "\"display\": \"Checkme Pro CE\"" +
                "}," +
                "\"component\": [" +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8867-4\"," +
                "\"display\": \"Heart rate\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \"" + HR + "\"," +
                "\"unit\": \"/min\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"/min\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8633-0\"," +
                "\"display\": \"QRS duration\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \"" + QRS + "\"," +
                "\"unit\": \"ms\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"ms\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"76053-8\"," +
                "\"display\": \"ST Segment.Lead I\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \"" + ST + "\"," +
                "\"unit\": \"mv\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"mv\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8634-8\"," +
                "\"display\": \"Q-T interval\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+QT+"\"," +
                "\"unit\": \"ms\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"ms\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8636-3\"," +
                "\"display\": \"Q-T interval corrected\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+QTc+"\"," +
                "\"unit\": \"ms\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"ms\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"76126-2\"," +
                "\"display\": \"Premature ventricular contractions [#]\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+PVC+"\"," +
                "\"unit\": \"beats\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"beats\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"https://en.wikipedia.org/wiki/Rate_pressure_product\"," +
                "\"code\": \"RPP\"," +
                "\"display\": \"Rate Pressure Product\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \"--\"," +
                "\"unit\": \" \"," +
                "\"system\": \"https://en.wikipedia.org/wiki/Rate_pressure_product\"," +
                "\"code\": \" \"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8480-6\"," +
                "\"display\": \"Systolic blood pressure\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+sbp+"\"," +
                "\"unit\": \"mmHg\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"mm[Hg]\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8462-4\"," +
                "\"display\": \"Diastolic blood pressure\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \"--\"," +
                "\"unit\": \"mmHg\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"mm[Hg]\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"20564-1\"," +
                "\"display\": \"Oxygen saturation in Blood\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+SpO2+"\"," +
                "\"unit\": \"%\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"%\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"61006-3\"," +
                "\"display\": \"Perfusion index Tissue by Pulse oximetry\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+PI+"\"," +
                "\"unit\": \" \"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \" \"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"urn:oid:2.16.840.1.113883.6.24\"," +
                "\"code\": \"131329\"," +
                "\"display\": \"MDC_ECG_ELEC_POTL_I\"" +
                "}}," +
                "\"valueString\": \""+MDC_ECG_ELEC_POTL_I+"\"" +
                "}" +
                "]" +
                "}";
        LogUtils.d(dailyCheckObservation);
        return dailyCheckObservation;
    }

    public static String makeECGObservation(Context context, ECGItem item) {
        String name = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_USER_NAME);
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        String medical_id = deviceName + Constant.sUploadUser.getUserInfo().getID();
        String patient_id = PreferenceUtils.readStrPreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID());
        LogUtils.d(patient_id+"==patient_ID");
        String SN = PreferenceUtils.readStrPreferences(context, deviceName + Constant.SN);
        String identifyVal = SN + medical_id + StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_LIST);
        String effectiveDateTime = StringUtils.makeTimeString(item.getDate());

        //ECGInnerItem innerItem = FileUtils.readECGInnerItem(Constant.dir, StringMaker.makeDateFileName(item.getDate(), MeasurementConstant.CMD_TYPE_ECG_NUM));
        ECGInnerItem innerItem = FileUtils.readECGInnerItem(Constant.dir, StringMaker.makeDateFileName(item.getDate(), MeasurementConstant.CMD_TYPE_ECG_NUM), context);

        String ECGText;
        String HR;
        String QRS;
        String ST;
        String QT;
        String QTc;
        String PVC;
        String MDC_ECG_ELEC_POTL_I;
        if (innerItem==null){
            ECGText="";
            HR="--";
            QRS="--";
            ST="--";
            QT="--";
            QTc="--";
            PVC="0";
            MDC_ECG_ELEC_POTL_I=StringUtils.byte2hex(new byte[0]);
        }else {
            ECGText=StringMaker.makeECGResult(innerItem.getStrResultIndex(), 2, true);
            HR=innerItem.getHR() == 0 ? "--" : innerItem.getHR()+"";
            QRS=innerItem.getQRS()==0?"--":innerItem.getQRS()+"";
            ST=StringMaker.makeSTJsonValueStr(innerItem.getCheckMode(),innerItem.getST());
            String fileName = StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_NUM);
            if (FileDriver.isFileExist(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME)) {
                byte[] buf = FileDriver.read(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME);
                if (buf[0] == 1) {
                    QT = "--";
                    QTc = "--";
                } else {
                    QT=innerItem.getQT() == 0 ? "--" : String.valueOf(innerItem.getQT());
                    QTc =innerItem.getQTc() == 0 ? "--" : String.valueOf(innerItem.getQTc()) ;
                }

            } else {
                QT = "--";
                QTc = "--";
            }
            PVC=innerItem.getPVCs()+"";
            MDC_ECG_ELEC_POTL_I=StringUtils.byte2hex(innerItem.getEcgDat());
        }

        String ECGObservation = "{" +
                "\"resourceType\": \"Observation\"," +
                "\"id\": \"ECG\"," +
                "\"identifier\": {" +
                "\"system\": \"https://cloud.viatomtech.com/fhir\"," +
                "\"value\": \""+identifyVal+"\"" +
                "}," +
                "\"category\": {\"coding\": {" +
                "\"system\": \"http://hl7.org/fhir/observation-category\"," +
                "\"code\": \"procedure\"," +
                "\"display\": \"Procedure\"" +
                "}}," +
                "\"code\": {\"coding\": {" +
                "\"system\": \"urn:oid:2.16.840.1.113883.6.24\"," +
                "\"code\": \"131328\"," +
                "\"display\": \"MDC_ECG_ELEC_POTL\"" +
                "}}," +
                "\"effectiveDateTime\": \""+effectiveDateTime+"\"," +
                "\"subject\": {" +
                "\"reference\": \""+patient_id+"\"," +
                "\"display\": \""+name+"\"" +
                "}," +
                "\"interpretation\": {" +
                "\"coding\": {" +
                "\"system\": \"http://hl7.org/fhir/v2/0078\"," +
                "\"code\": \"N\"," +
                "\"display\": \"Normal\"" +
                "}," +
                "\"text\": \""+ECGText+"\"" +
                "}," +
                "\"device\": {" +
                "\"sn\": \""+SN+"\"," +
                "\"display\": \"Checkme Pro CE\"" +
                "}," +
                "\"component\": [" +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8867-4\"," +
                "\"display\": \"Heart rate\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+HR+"\"," +
                "\"unit\": \"/min\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"/min\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8633-0\"," +
                "\"display\": \"QRS duration\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+QRS+"\"," +
                "\"unit\": \"ms\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"ms\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"76053-8\"," +
                "\"display\": \"ST amplitude.lead I\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+ST+"\"," +
                "\"unit\": \"mv\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"mv\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8634-8\"," +
                "\"display\": \"Q-T interval\"" +
                "}}," +
                "\"valueQuantity\": {" +
            "\"value\": \""+QT+"\"," +
                "\"unit\": \"ms\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"ms\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8636-3\"," +
                "\"display\": \"Q-T interval corrected\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+QTc+"\"," +
                "\"unit\": \"ms\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"ms\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"76126-2\"," +
                "\"display\": \"Premature ventricular contractions [#]\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+PVC+"\"," +
                "\"unit\": \"beats\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"beats\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"https://en.wikipedia.org/wiki/Rate_pressure_product\"," +
                "\"code\": \"RPP\"," +
                "\"display\": \"Rate Pressure Product\"" +
                "}}," +
                "\"valueQuantity\": {" +
                "\"value\": \"--\"," +
                "\"unit\": \" \"," +
                "\"system\": \"https://en.wikipedia.org/wiki/Rate_pressure_product\"," +
                "\"code\": \" \"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {\"coding\": {" +
                "\"system\": \"urn:oid:2.16.840.1.113883.6.24\"," +
                "\"code\": \"131329\"," +
                "\"display\": \"MDC_ECG_ELEC_POTL_I\"" +
                "}}," +
                "\"valueString\": \""+MDC_ECG_ELEC_POTL_I+"\"" +
                "}" +
                "]" +
                "}";
        LogUtils.d(ECGObservation);
        return ECGObservation;
    }

    public static String makeSPO2Observation(Context context, SPO2Item item) {
        String name = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_USER_NAME);
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        String medical_id = deviceName + Constant.sUploadUser.getUserInfo().getID();
        String patient_id = PreferenceUtils.readStrPreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID());
        LogUtils.d(patient_id+"==patient_ID");
        String SN = PreferenceUtils.readStrPreferences(context, deviceName + Constant.SN);
        String identifyVal = SN + medical_id + StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_LIST);
        String effectiveDateTime = StringUtils.makeTimeString(item.getDate());

        String SpO2=item.getOxygen()==0?"--":(item.getOxygen()+"");
        String Pr=item.getPr()==0?"--":(item.getPr()+"");
        String PI=item.getPi()==0?"--":(item.getPi()+"");
        String SPO2Observation = "{" +
                "\"resourceType\": \"Observation\"," +
                "\"id\": \"SpO2\"," +
                "\"identifier\":" +
                "{" +
                "\"system\": \"https://cloud.viatomtech.com/fhir\"," +
                "\"value\": \""+identifyVal+"\"" +
                "}," +
                "\"category\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://hl7.org/fhir/observation-category\"," +
                "\"code\": \"vital-signs\"," +
                "\"display\": \"Vital Signs\"" +
                "}" +
                "}," +
                "\"code\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"20564-1\"," +
                "\"display\": \"Oxygen saturation in Blood\"" +
                "}" +
                "}," +
                "\"effectiveDateTime\": \""+effectiveDateTime+"\"," +
                "\"subject\":" +
                "{" +
                "\"reference\": \""+patient_id+"\"," +
                "\"display\": \""+name+"\"" +
                "}," +
                "\"interpretation\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://hl7.org/fhir/v2/0078\"," +
                "\"code\": \"L\"," +
                "\"display\": \"Low\"" +
                "}," +
                "\"text\": \"--\"" +
                "}," +
                "\"device\":{" +
                "\"sn\": \""+SN+"\"," +
                "\"display\": \"Checkme Pro CE\"" +
                "}," +
                "\"component\": [" +
                "{" +
                "\"code\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"20564-1\"," +
                "\"display\": \"Oxygen saturation in Blood\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+SpO2+"\"," +
                "\"unit\": \"%\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"%\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8889-8\"," +
                "\"display\": \"Heart rate by Pulse oximetry\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+Pr+"\"," +
                "\"unit\": \"/min\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"/min\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"61006-3\"," +
                "\"display\": \"Perfusion index Tissue by Pulse oximetry\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+PI+"\"," +
                "\"unit\": \"%\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"%\"" +
                "}" +
                "}" +
                "]" +
                "}";
        LogUtils.d(SPO2Observation);
        return SPO2Observation;
    }

    public static String makeTemperatureObservation(Context context, TempItem item) {
        String name = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_USER_NAME);
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        String medical_id = deviceName + Constant.sUploadUser.getUserInfo().getID();
        String patient_id = PreferenceUtils.readStrPreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID());
        LogUtils.d(patient_id+"==patient_ID");
        String SN = PreferenceUtils.readStrPreferences(context, deviceName + Constant.SN);
        String identifyVal = SN + medical_id + StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_LIST);
        String effectiveDateTime = StringUtils.makeTimeString(item.getDate());

        String TMP=item.getResult()+"";

        String tempObservation = "{" +
                "\"resourceType\": \"Observation\"," +
                "\"id\": \"Body temperature\"," +
                "\"identifier\":{" +
                "\"system\": \"urn:ietf:rfc:3986\"," +
                "\"value\": \""+identifyVal+"\"" +
                "}," +
                "\"category\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://hl7.org/fhir/observation-category\"," +
                "\"code\": \"vital-signs\"," +
                "\"display\": \"Vital Signs\"" +
                "}" +
                "}," +
                "\"code\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8310-5\"," +
                "\"display\": \"Body temperature\"" +
                "}" +
                "}," +
                "\"effectiveDateTime\": \""+effectiveDateTime+"\"," +
                "\"subject\":{" +
                "\"reference\": \""+patient_id+"\"," +
                "\"display\": \""+name+"\"" +
                "}," +
                "\"interpretation\": {" +
                "\"coding\":{" +
                "\"system\": \"http://hl7.org/fhir/v2/0078\"," +
                "\"code\": \"L\"," +
                "\"display\": \"Low\"" +
                "}," +
                "\"text\": \"--\"" +
                "}," +
                "\"device\":{" +
                "\"sn\": \""+SN+"\"," +
                "\"display\": \"Checkme Pro CE\"" +
                "}," +
                "\"component\": [" +
                "{" +
                "\"code\":{" +
                "\"coding\": {" +
                "\"system\": \"http://loinc.org\"," +
                "\"code\": \"8310-5\"," +
                "\"display\": \"Body temperature\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+TMP+"\"," +
                "\"unit\": \"Cel\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"deg C\"" +
                "}" +
                "}" +
                "]" +
                "}";
        LogUtils.d(tempObservation);
        return tempObservation;
    }

    public static String makeSlmObservation(Context context, SLMItem item) {
        String name = PreferenceUtils.readStrPreferences(context, Constant.CURRENT_USER_NAME);
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        String medical_id = deviceName + Constant.sUploadUser.getUserInfo().getID();
        String patient_id = PreferenceUtils.readStrPreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID());
        LogUtils.d(patient_id+"==patient_ID");
        String SN = PreferenceUtils.readStrPreferences(context, deviceName + Constant.SN);
        String identifyVal = SN + medical_id + StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_LIST);
        String effectiveDateTime = StringUtils.makeTimeString(item.getDate());

        String TotalTime=StringMaker.makeSecondToMinute(item.getTotalTime());
        int STAT=item.getLowOxygenNum();
        String drops=StringMaker.makeSecondToMinute(item.getLowOxygenTime());
        String AvO2=(item.getAverageOxygen())==0?"--":item.getAverageOxygen()+"";
        String LowestO2=(item.getLowestOxygen())==0?"--":item.getLowestOxygen()+"";
        SLMInnerItem innerItem=FileUtils.readSLMInnerItem(Constant.dir,StringMaker.makeDateFileName(item.getDate(),Constant.CMD_TYPE_SLM_NUM));
        String SLEEP_I=innerItem==null?StringUtils.byte2hex(new byte[0]):StringUtils.int2hex(innerItem.getSpo2Dat());
        String SLMObservation = "{" +
                "\"resourceType\": \"Observation\"," +
                "\"id\": \"sleep\"," +
                "\"identifier\":{" +
                "\"system\": \"https://cloud.viatomtech.com/fhir\"," +
                "\"value\": \""+identifyVal+"\"" +
                "}," +
                "\"category\": {" +
                "\"coding\":{" +
                "\"system\": \"http://hl7.org/fhir/observation-category\"," +
                "\"code\": \"procedure\"," +
                "\"display\": \"Procedure\"" +
                "}" +
                "}," +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160502\"," +
                "\"display\": \"Sleep\"" +
                "}" +
                "}," +
                "\"effectiveDateTime\": \""+effectiveDateTime+"\"," +
                "\"subject\": {" +
                "\"reference\": \""+patient_id+"\"," +
                "\"display\": \""+name+"\"" +
                "}," +
                "\"interpretation\": {" +
                "\"coding\":" +
                "{" +
                "\"system\": \"http://hl7.org/fhir/v2/0078\"," +
                "\"code\": \"N\"," +
                "\"display\": \"Normal\"" +
                "}," +
                "\"text\": \"--\"" +
                "}," +
                "\"device\":{" +
                "\"sn\": \""+SN+"\"," +
                "\"display\": \"Checkme Pro CE\"" +
                "}," +
                "\"component\": [" +
                "{" +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160501-9\"," +
                "\"display\": \"Total duration\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+TotalTime+"\"," +
                "\"unit\": \"s\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"s\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160501-10\"," +
                "\"display\": \"<90% STAT.\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": "+STAT+"," +
                "\"unit\": \" \"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \" \"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160501-11\"," +
                "\"display\": \"Drops\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+drops+"\"," +
                "\"unit\": \"s\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"s\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160501-12\"," +
                "\"display\": \"Avg SpO2\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+AvO2+"\"," +
                "\"unit\": \"%\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"%\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160501-13\"," +
                "\"display\": \"Lowest SpO2\"" +
                "}" +
                "}," +
                "\"valueQuantity\": {" +
                "\"value\": \""+LowestO2+"\"," +
                "\"unit\": \"%\"," +
                "\"system\": \"http://unitsofmeasure.org\"," +
                "\"code\": \"%\"" +
                "}" +
                "}," +
                "{" +
                "\"code\": {" +
                "\"coding\": {" +
                "\"system\": \"https://api.viatomtech.com.cn/fhir\"," +
                "\"code\": \"160501-8\"," +
                "\"display\": \"SLEEP_I\"" +
                "}" +
                "}," +
                "\"valueString\": \""+SLEEP_I+"\"" +
                "}" +
                "]" +
                "}";
        LogUtils.d(SLMObservation);
        return SLMObservation;
    }

    public static JSONObject makeDeviceJson(CheckmeDevice info) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("Region", info.getRegion());
        json.put("Model", info.getModel());
        json.put("Hardware", info.getHardware());
        json.put("Software", info.getSoftware());
        json.put("Language", info.getLanguage());

        return json;

    }
}
