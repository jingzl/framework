package cm.framework.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * String和InputStream的转换
 * @author gyx
 *
 */
public class StremUtil {

	/**
	 * String –> InputStream
	 * @param str
	 * @return
	 */
	public static InputStream stringToInputStream(String str) {
		InputStream is = new ByteArrayInputStream(str.getBytes());
		return is;

	}
	
	/**
	 * InputStream–>String
	 * @param is
	 * @return
	 */
	public static String inputStreamToString(InputStream is) {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		
		try {
			
			while ((line = in.readLine()) != null){
				buffer.append(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();

	}
}
