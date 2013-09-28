package cm.framework.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;

import cm.framework.net.ClientSession;
import cm.framework.net.ControlRunnable;
import cm.framework.net.INetStateListener;
import cm.framework.utils.xml.kXMLElement;
import cm.framework.utils.xml.kXMLParseException;


/**
 * XML协议回复包抽象基类，子类继承此类完成回复构建
 * 
 * @author gyx
 * 
 */
public abstract class BaseXMLResponse implements BaseHttpResponse {

	private InputStream inputStream = null;
	private int len;
	private String responseContent;

	public ErrorResponse parseInputStream(ControlRunnable currentThread,
			BaseHttpRequest request, InputStream inputStream, int len,
			INetStateListener stateReceiver) throws IOException {

		this.inputStream = inputStream;
		this.len = len;
		
		Header[] headers = request.headers;

		for (int i = 0; i < headers.length; i++) {
			if (headers[i].getName().equalsIgnoreCase("SessionId")) {
				ClientSession.getInstance().setSessionId(headers[i].getValue());
				break;
			}
		}

		kXMLElement doc = parseResponse(inputStream);

		if(doc == null) {
			return new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		} 
		// protocol parse error?

		// validate check fail?

		// derived class parses concrete xml protocol
		if (!extractBody(doc)) {
			return new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		}
		// 释放内存
		doc = null;
		// parse successfully
		return null;
	}

	@Override
	public ErrorResponse parseInputStream(ControlRunnable currentThread,
			BaseHttpRequest request, String responseContent, int len,
			INetStateListener stateReceiver) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * get inputstream
	 * 
	 * @return InputStream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	public int getLength() {
		return len;
	}

	/**
	 * 协议回复内容提取抽象接口
	 * 
	 * @param bodyElement
	 * @return true--success, false--fail
	 */
	protected abstract boolean extractBody(kXMLElement bodyElement);

	/**
	 * 将inputstream转成XML
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private kXMLElement parseResponse(InputStream response) throws IOException {
		kXMLElement doc = new kXMLElement();
		try {
			doc.parseFromReader(new InputStreamReader(response, "utf-8"));
			responseContent = doc.toString();
			return doc;
		} catch (IOException e) {
			return null;
		} catch (kXMLParseException e) {
			return null;
		}
	}
	/**
	 * 检查回复结果格式及返回结果成功或失败
	 * @param doc
	 * @param requestId
	 * @return 如果格式正确或结果成功返回NULL，错误返回ErrorResponse
	 */
	@SuppressWarnings("unused")
	private ErrorResponse validateCheck(kXMLElement doc, String requestId) {

		// pass validate check
		return null;
	}
	
	
	@Override
	public String getResponseContent() {
		return responseContent;
	}
}