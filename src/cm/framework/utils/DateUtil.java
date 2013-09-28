package cm.framework.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	private static final SimpleDateFormat adf = new SimpleDateFormat(
			"yyyy-MM-dd", java.util.Locale.CHINA);
	private static final Calendar calendar = Calendar
			.getInstance(java.util.Locale.CHINA);
	private static final String[] weekdays = { "星期日", "星期一", "星期二", "星期三",
			"星期四", "星期五", "星期六", };

	public DateUtil() {
		// TODO Auto-generated constructor stub
	}

	public static String weekday(int y, int m, int d) throws Exception {
		return weekday("" + y, "" + m, "" + d);
	}

	public static String weekday(String y, String m, String d) throws Exception {
		return weekday(y + "-" + m + "-" + d);
	}

	public static String weekday(String ymd) throws Exception {
		Date date = adf.parse(ymd);
		calendar.setTime(date);
		int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		return weekdays[weekday];
	}

	
	public static int compareDate(String date1, String date2) {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try {
			Date dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			if (dt1.getTime() > dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	
	public static int compareDate(Date dt1, Date dt2) {
		
		try {
			if (dt1.getTime() > dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * @param date1 过去日期
	 * @param date2 当前日期
	 * @return
	 */
	public static long differDay(Date oldDate, Date curDate) 
	{  
		//用立即数，减少乘法计算的开销
	    return curDate.getTime() / 86400000 - oldDate.getTime() / 86400000;  
	} 
	
	public static String getDateByMillisecond(String str) {

		str = str + "000";
		Date date = new Date(Long.valueOf(str));
		  
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		  
		String time = format.format(date);
		  
		return time;
	}
	
	public static String getDateTimeByMillisecond(String str) {

		str = str + "000";
	    Date date = new Date(Long.valueOf(str));
	      
	    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
	  
	    String time = format.format(date);
	  
	    return time;
	}
}
