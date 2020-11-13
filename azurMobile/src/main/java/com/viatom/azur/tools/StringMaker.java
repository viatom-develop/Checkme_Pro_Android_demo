package com.viatom.azur.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;

import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.BPCalItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.User;

public class StringMaker {

	/**
	 * Make a special format date string
	 * @param date
	 * @return
	 */
	static public String makeDateString(Date date){
		if (date == null) {
			return "";
		}

		String datesStr = new String();
		//Distinction between East and West date format
		if (!LocaleUtils.isWestLanguage()) {
			datesStr += (date.getYear()+1900);
			datesStr += "-";
			datesStr += (date.getMonth()+1);
			datesStr += "-";
			datesStr += date.getDate();
		}else {
			datesStr = String.valueOf(date.getDate());
			datesStr += "-";
			datesStr += Constant.getString(Constant.MONTH[date.getMonth()]);
			datesStr += "-";
			datesStr += String.valueOf(date.getYear()+1900);
		}

		return datesStr;
	}

	/**
	 * Make a special format time string
	 * @param date
	 * @return
	 */
	static public String makeTimeString(Date date){
		String TimesStr = new String();
		TimesStr = String.format("%02d", date.getHours());
		TimesStr += ":";
		TimesStr += String.format("%02d", date.getMinutes());
		TimesStr += ":";
		TimesStr += String.format("%02d", date.getSeconds());
		return TimesStr;
	}

	/**
	 * Make String like format "XhXmXs"
	 * @param second
	 * @return
	 */
	static public String makeSecondToMinute(int second){
		int m = second/60;
		int s = second%60;
		if (m < 60) {
			return new String(m + Constant.getString(R.string.m)
					+ s + Constant.getString(R.string.s));
		}
		int h = m/60;
		m = m%60;
		return new String( h + Constant.getString(R.string.h)
				+ m + Constant.getString(R.string.m)
				+ s + Constant.getString(R.string.s));
	}

	/**
	 * Make spo2 result string
	 * @param spo2Value
	 * @return
	 */
	static public String makeSPO2StrResult(int spo2Value){
		String Result;
		if(spo2Value>=93)
			Result = Constant.getString(Constant.SPO2_STR_REULT_LIST[0]);
		else if(spo2Value>=67)
			Result = Constant.getString(Constant.SPO2_STR_REULT_LIST[1]);
		else
			Result = Constant.getString(Constant.SPO2_STR_REULT_LIST[2]);
		return Result;
	}

	/**
	 * Make sleep monitor result string
	 * @param imgResult
	 * @param drops
	 * @return
	 */
	public static String makeSLMResultStr(byte imgResult, int drops) {
		String result;
		if (drops != 0) {
			result = Constant.getString(Constant.SLM_STR_REULT_LIST[1]);
		} else if (drops == 0 && imgResult == 0) {
			result = Constant.getString(Constant.SLM_STR_REULT_LIST[0]);
		} else
			result = Constant.getString(Constant.SLM_STR_REULT_LIST[2]);
		return result;
	}

	/**
	 * make file name by date and file type
	 * @param date
	 * @param type
	 * @return
	 */
	static public String makeDateFileName(Date date,byte type){
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
		switch(type){
			case Constant.CMD_TYPE_VOICE:
				fileName += ".wav";
				break;
			case Constant.CMD_TYPE_VOICE_CONVERTED:
				fileName += "_Voice.wav";
				break;
			default:
				break;
		}
		return fileName;
	}

	/**
	 * Make spo2 result string
	 * @param spo2Value
	 * @return
	 */
	static public SpannableString makeSPO2Str(int spo2Value){
		SpannableString spannableString;
		if(spo2Value==0)
			spannableString= new SpannableString("SpO2 --");
		else
			spannableString= new SpannableString("SpO2 " + spo2Value +"%");
		spannableString.setSpan(new SubscriptSpan(), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableString.setSpan(new RelativeSizeSpan(0.7f), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}

	/**
	 * Make st value
	 * @param checkMode
	 * @param ST
	 * @return
	 */
	public static String makeSTValueStr(byte checkMode,int ST){
		float tempST;
		if(checkMode==Constant.ECG_CHECK_MODE_HH||checkMode==Constant.ECG_CHECK_MODE_HC)//hand-hand
			return "";
		else if(ST==0XFF)//error value
			return "ST --mV";
		else{
			tempST = (float)ST/100;
			return ("ST "+(tempST>=0?"+":"")+String.valueOf(tempST)+"mV");
		}
	}

	public static String makeSTJsonValueStr(byte checkMode,int ST){
		float tempST;
		if(checkMode==Constant.ECG_CHECK_MODE_HH||checkMode==Constant.ECG_CHECK_MODE_HC)//hand-hand
			return "";
		else if(ST==0XFF)//error value
			return "--";
		else{
			tempST = (float)ST/100;
			return ((tempST>=0?"+":"")+String.valueOf(tempST));
		}
	}

	/**
	 * Make ECG Result string
	 * @param index
	 * @param wantResultNum
	 * @param multiline
	 * @return
	 */
	public static String makeECGResult(int index, int wantResultNum, boolean multiline){
		String result = "";
		if(index==0xFF)
			return Constant.getString(Constant.ECG_STR_RESULT_LIST[8]);
		else if(index==0)
			return Constant.getString(Constant.ECG_STR_RESULT_LIST[0]);
		else{
			int resultNum = 0;
			for(int i=0;i<16;i++){
				if ((index & (1<<i)) == 0) {
					result += "";
				}else {
					if (resultNum >= 1) {//The second result
						if (multiline) {
							result += "\n" + (Constant.getString(Constant.ECG_STR_RESULT_LIST[i+1]));
						}else {
							result += "  " + (Constant.getString(Constant.ECG_STR_RESULT_LIST[i+1]));
						}
					}else {//The first result
						result += (Constant.getString(Constant.ECG_STR_RESULT_LIST[i+1]));
					}
					resultNum ++;
					if (resultNum >= wantResultNum) {
						return result;
					}
				}
			}
			return result;
		}
	}

	/**
	 * Make BP result string
	 * @param dlcItem
	 * @return
	 */
	public static String makeBPValueStr(DailyCheckItem dlcItem) {
		User curUser = Constant.curUser;
		BPCalItem calItem = curUser.getBpCalItem();
		if(curUser.getBpCalItem().getCalType()==Constant.BP_TYPE_NONE){//no calibration
			return "";
		}else if (dlcItem.getDate().before(calItem.getCalDate())) {//out of date
			return "";
		}else if (curUser.getBpCalItem().getCalType()==Constant.BP_TYPE_RE) {//relative calibration
			if(dlcItem.getBPFlag()==0xFF){//not available value
				return("");
			}else {//normal value
				return ("BP " + (dlcItem.getBP()>-1 ? "+" : "") + dlcItem.getBP() + "%");
			}
		}else if (curUser.getBpCalItem().getCalType()==Constant.BP_TYPE_ABS) {//absolutely calibration
			if(dlcItem.getBPFlag()==0xFF){//not available value
				return(Constant.getString(R.string.bp_sys) + " -- " + " mmHg");
			}else {//normal value
				return (Constant.getString(R.string.bp_sys) +  " " + dlcItem.getBP() + " mmHg");
			}
		}
		return "";
	}
	/**
	 * Make BP result string
	 * @param dlcItem
	 * @return
	 */
	public static String makeBPValueJsonStr(DailyCheckItem dlcItem) {
		BPCalItem calItem = Constant.sUploadUser.getBpCalItem();
		if(Constant.sUploadUser.getBpCalItem().getCalType()==Constant.BP_TYPE_NONE){//no calibration
			return "--";
		}else if (dlcItem.getDate().before(calItem.getCalDate())) {//out of date
			return "--";
		}else if (Constant.sUploadUser.getBpCalItem().getCalType()==Constant.BP_TYPE_RE) {//relative calibration
			if(dlcItem.getBPFlag()==0xFF){//not available value
				return("--");
			}else {//normal value
				return ( (dlcItem.getBP()>-1 ? "+" : "") + dlcItem.getBP() );
			}
		}else if (Constant.sUploadUser.getBpCalItem().getCalType()==Constant.BP_TYPE_ABS) {//absolutely calibration
			if(dlcItem.getBPFlag()==0xFF){//not available value
				return( " -- " );
			}else {//normal value
				return ( dlcItem.getBP()+"");
			}
		}
		return "";
	}

	/**
	 * Make BP calibration date string
	 * @return
	 */
	public static String makeBPCalDate(DailyCheckItem dlcItem) {
		BPCalItem calItem = Constant.curUser.getBpCalItem();
		if(calItem.getCalType()==Constant.BP_TYPE_RE) {
			if (dlcItem.getDate().before(calItem.getCalDate())||dlcItem.getBPFlag()==0xFF) {//not available date
				return "";
			}else {//available date
				return Constant.getString(R.string.refer) + StringMaker
						.makeDateString(calItem.getCalDate());
			}
		}else {
			return "";
		}
	}

	/**
	 * Make filter information string
	 * @param lead
	 * @param filter
	 * @return
	 */
	public static String makeFilterStr(int lead, int filter) {
		int FILTER_NORMAL = 0;
		int FILTER_WIDE = 1;

		String outStr = Constant.getString(R.string.bandwidth);

		if (lead==Constant.ECG_CHECK_MODE_HH || lead==Constant.ECG_CHECK_MODE_HC) {//internal
			if (filter == FILTER_WIDE) {//normal
//				return "0.57Hz - 21Hz";
				return outStr + Constant.getString(R.string.wide);
			}else if (filter == FILTER_NORMAL) {//strengthen
//				return "1.15Hz - 16Hz";
				return outStr + Constant.getString(R.string.normal);
			}
		}else if (lead==Constant.ECG_CHECK_MODE_1L || lead==Constant.ECG_CHECK_MODE_12L) {//external
			if (filter == FILTER_WIDE) {//normal
//				return "0.05Hz - 41Hz";
				return outStr + Constant.getString(R.string.wide);
			}else if (filter == FILTER_NORMAL) {//strengthen
//				return "0.57Hz - 21Hz";
				return outStr + Constant.getString(R.string.normal);
			}
		}

		return "Unknow bandwidth";
	}

	/**
	 * 生成温度字符串
	 * @param temp
	 * @param isCelsius
	 * @return
	 */
	public static String makeTemperatureStr(float temp, boolean isCelsius) {
		if (isCelsius) {
			//摄氏度
			return new String(temp == 0 ? "--℃" : temp + "℃");
		}else {
			if (temp == 0) {
				return "--℉";
			}else {
				temp = temp * 9 / 5 + 32;
				return (String.format("%.1f", temp)+"℉");
			}
		}
	}

}
