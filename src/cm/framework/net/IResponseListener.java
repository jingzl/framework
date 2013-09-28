package cm.framework.net;

import cm.framework.protocol.BaseHttpRequest;
import cm.framework.protocol.BaseHttpResponse;

/**
 * 回复处理接口
 * @author 
 *
 */
public interface IResponseListener {
	/**
	 * 回复处理
	 * @param response  回复
	 * @param request   该回复对应请求
	  * @param currentThread 对应执行线程
	 */
	void onResponse(BaseHttpResponse response, BaseHttpRequest request, ControlRunnable currentThread);
}
