package cm.framework.net;

import cm.framework.include.CacheData;
import cm.framework.protocol.BaseHttpRequest;
import cm.framework.protocol.BaseHttpResponse;
import cm.framework.protocol.BaseResponse;
import cm.framework.protocol.ErrorResponse;

public class ControlRunnable extends Thread {
	// http请求
	private BaseHttpRequest request;
	// 回复处理器
	private IResponseListener rspReceiver;
	// 错误处理器
	private IErrorListener errorReceiver;
	// 状态处理器
	private INetStateListener stateReceiver;

	private boolean isCanceled = false;
	
	private INetConnectionListener iNetConnectionListener;

	/**
	 * 提交请求
	 * 
	 * @param controlRunnable
	 *            请求cookie
	 * @param request
	 *            请求
	 * @param rspReceiver
	 *            回复处理器
	 * @param errorReceiver
	 *            错误处理器
	 * @param stateReceiver
	 *            状态处理器
	 * @return
	 */
	public ControlRunnable(BaseHttpRequest request,
			IResponseListener rspReceiver, IErrorListener errorReceiver,
			INetStateListener stateReceiver) {

		this.request = request;
		this.rspReceiver = rspReceiver;
		this.errorReceiver = errorReceiver;
		this.stateReceiver = stateReceiver;

	}

	public void run() {
		try {
			if (isCanceled()) {

				if (stateReceiver != null) {
					stateReceiver.onCancel(request, this);
				}
				return;
			}

			// 先判断是否从缓存中读取
			if (request.needCacheResponse()) {

				switch (request.getCacheMethod()) {
				// 临时缓存
				case BaseHttpRequest.CACHE_TEMP: {
					BaseHttpResponse cacheRsp = ClientSession
							.getCacheResponse(request);

					if (cacheRsp != null) {

						if (rspReceiver != null) {
							rspReceiver.onResponse(cacheRsp, request, this);

							// 让接收器有机会知道处理完全OK,null表示成功
							if (errorReceiver != null) {
								errorReceiver.onError(null, request, this);
							}
						}
					} else {
						getDataFromServer();
					}
					break;
				}

				// 永久缓存
				case BaseHttpRequest.CACHE_PERMANENT: {
					// 根据缓存标识获取缓存信息（缓存时间，ID）
					CacheData cacheData = ClientSession.getCacheFromDB(request);
					if (cacheData != null) {
						// 根据缓存规则判断是否从缓存中读取
						if (request.getCacheByRule(cacheData)) {

							BaseHttpResponse httpResponse = request
									.createResponse();
							httpResponse.parseInputStream(this, request,
									cacheData.response, cacheData.len,
									stateReceiver);

							if (rspReceiver != null) {
								rspReceiver.onResponse(httpResponse, request,
										this);

								// 让接收器有机会知道处理完全OK,null表示成功
								if (errorReceiver != null) {
									errorReceiver.onError(null, request, this);
								}
							}

						} else {
							getDataFromServer();
						}
					} else {
						getDataFromServer();
					}
					break;
				}
				}
			} else {
				getDataFromServer();
			}

		} catch (Exception e) {
			// 出现其他任何异常，置本处理器错误标志，
			// 告诉使用者自己无法再使用。
			e.printStackTrace();
			if (stateReceiver != null) {
				stateReceiver.onCancel(request, this);
			}
			if (errorReceiver != null) {
				errorReceiver.onError(new ErrorResponse(
						ErrorResponse.ERROR_THREAD, "work thread error!"),
						request, this);
			}
		}

	}

	// 从服务器端读取
	private void getDataFromServer() {
		iNetConnectionListener = ClientSession.getInstance().getNetConnectionListener();
		
		BaseResponse rsp = RsqHandleHelper.getResponseImpl(this, request,
				stateReceiver);

		if (isCanceled()) {

			if (stateReceiver != null) {
				stateReceiver.onCancel(request, this);
			}
			return;
		}

		if (rsp instanceof ErrorResponse) {
			if(((ErrorResponse) rsp).getErrorType() == ErrorResponse.ERROR_NULL_RESULT) {
				// 判断是否需要缓存
				if (request.needCacheResponse()) {
					
					switch (request.getCacheMethod()) {
						// 永久缓存
						case BaseHttpRequest.CACHE_PERMANENT:
							// 先删除已过期数据
							ClientSession.deleteCache(request);
							break;
					}
				}
			}
			if (errorReceiver != null) {

				errorReceiver.onError((ErrorResponse) rsp, request, this);
			}
		} else {
			// 判断是否需要缓存
			if (request.needCacheResponse()) {

				switch (request.getCacheMethod()) {
				// 临时缓存
				case BaseHttpRequest.CACHE_TEMP:

					ClientSession.addCacheResponse(request,
							(BaseHttpResponse) rsp);
					break;

				// 永久缓存
				case BaseHttpRequest.CACHE_PERMANENT:
					// 先删除已过期数据
					ClientSession.deleteCache(request);
					// 将新数据缓存
					ClientSession.addCacheToDB(request, (BaseHttpResponse) rsp);
					break;
				}
			}
			if (rspReceiver != null) {

				rspReceiver.onResponse((BaseHttpResponse) rsp, request, this);

			}

			// 让接收器有机会知道处理完全OK,null表示成功
			if (errorReceiver != null) {
				errorReceiver.onError(null, request, this);
			}
		}
	}

	private boolean isCanceled() {
		return isCanceled;
	}

	private void setCancelflag(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public final void cancel() {
		setCancelflag(true);
		if (isAlive()) {
			interrupt();
		}
	}
	
	public INetConnectionListener getNetConnectionListener() {
		return iNetConnectionListener;
	}

}