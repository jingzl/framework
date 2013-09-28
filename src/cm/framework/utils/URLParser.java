package cm.framework.utils;
/**
 * URL解析器
 * @author gyx
 *
 */
public class URLParser {

	private String m_sevrer = null;
	private String m_protocol = null;
	private String m_url = null;
	
	public static URLParser parse(String URL) {
		try {
			URLParser parser = new URLParser();

			int pos = URL.indexOf(":");
			parser.m_protocol = URL.substring(0, pos);
			pos += 3;
			String subString;
			subString = URL.substring(pos, URL.length());
			pos = subString.indexOf("/");
			parser.m_sevrer = subString.substring(0, pos);
			parser.m_url = subString.substring(pos, subString.length());

			return parser;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	public String getServer() {
		return m_sevrer;
	}

	public String getProtocol() {
		return m_protocol;
	}

	public String getURL() {
		return m_url;
	}


}
