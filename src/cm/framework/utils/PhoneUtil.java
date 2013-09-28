package cm.framework.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
/**
 * 发短信工具类
 * @author gyx
 *
 */
public class PhoneUtil {
	
	/**
	 * 发送短信，不显示发短信界面
	 * 
	 * @param activity 当前界面
	 * @param phoneNum 发送目的手机的手机号
	 * @param smsBody 短信内容
	 */
	public static void SendSmsWithoutShow(Activity activity, String phoneNum, String smsBody) {

		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent mPI = PendingIntent.getBroadcast(activity, 0, new Intent(), 0);
		try {
			/*
			 * 两个条件都检查通过的情况下,发送短信
			 * 先建构一PendingIntent对象并使用getBroadcast()广播
			 * 将PendingIntent,电话,短信文字等参数 传入sendTextMessage()方法发送短信
			 */

			smsManager.sendTextMessage(phoneNum, null, smsBody, mPI, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送短信，显示发短信界面
	 * 
	 * @param activity 当前界面
	 * @param phoneNum 发送目的手机的手机号
	 * @param smsBody 短信内容
	 */
	public static void SendSms(Activity activity, String phoneNum, String smsBody) {
		
		Uri uri = Uri.parse("smsto:"+phoneNum);
		Intent it = new Intent(Intent.ACTION_SENDTO, uri);
		it.putExtra("sms_body", smsBody);
		activity.startActivity(it);
	}
}
