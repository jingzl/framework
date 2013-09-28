package cm.framework.protocol;

import java.io.IOException;
import java.io.OutputStream;

import cm.framework.net.ControlRunnable;
import cm.framework.net.INetStateListener;
import cm.framework.utils.xml.KXmlSerializer;
import cm.framework.utils.xml.XmlSerializer;

/**
 * XML协议请求包抽象基类，搭建请求包的基本骨架， 完成协议公共头的构建， 
 * 并对外提供必要接口方便请求的发送及相应回复的创建
 * 
 * @author gyx
 * 
 */
public abstract class BaseXMLRequest extends BaseHttpRequest {

	// 请求应用服务ID
	protected String requestId;
	// 请求应用服务版本
	protected String requestVersion = "";
	
	public BaseXMLRequest() {

	}

	public void fillOutputStream(ControlRunnable currentThread, OutputStream output,
			INetStateListener stateReceiver) throws IOException {
		XmlSerializer serializer = new KXmlSerializer();
		serializer.setOutput(output, "utf-8");
		serializer.startDocument(null, null);
		fillBody(serializer);
		serializer.endDocument();
	}

	public String getRequestId() {
		return requestId;
	}

	@Override
	public String[][] getExtraHeaders() {
		String[][] aryHeaders = new String[1][2];
		aryHeaders[0][0] = "Content-Type";
		aryHeaders[0][1] = "text/html;charset=GBK";
		
		return aryHeaders;
	}

	@Override
	public boolean needCacheResponse() {
		return true;
	}

	/**
	 * 请求包体填充抽象接口，子类实现此接口完成具体请求包体的构建
	 * 
	 * @param serializer：xml流构建器
	 */
	protected abstract void fillBody(XmlSerializer serializer)
			throws IOException;


}