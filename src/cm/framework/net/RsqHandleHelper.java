package cm.framework.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import cm.framework.include.APN;
import cm.framework.protocol.BaseHttpRequest;
import cm.framework.protocol.BaseHttpResponse;
import cm.framework.protocol.BaseResponse;
import cm.framework.protocol.ErrorResponse;


/**
 * 请求处理辅助类
 * 
 * @author 
 * 
 */
class RsqHandleHelper {

	public static INetConnectionListener iNetConnectionListener;
	
	static BaseResponse getResponseImpl(ControlRunnable currentThread, BaseHttpRequest request,
			INetStateListener stateReceiver) {

		DefaultHttpClient connection = null;
		BaseHttpResponse httpResponse = null;
		HttpUriRequest httpUriRequest = null;
		HttpResponse response = null;

		try {
			// 设置连接
			connection = connectServer(currentThread, request, stateReceiver);
			// 传递参数
			httpUriRequest = buildAndSendRsq(connection, currentThread, request,
					stateReceiver);
			// 执行，得到返回值
			response = connection.execute(httpUriRequest);
			// 解析返回结果
			BaseResponse rsp = recvAndParseRsp(response, currentThread, request,
					stateReceiver);
			if (rsp instanceof ErrorResponse) {
				return rsp;
			}
			httpResponse = (BaseHttpResponse) rsp;
			return httpResponse;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			ErrorResponse err = new ErrorResponse(
					ErrorResponse.ERROR_PARAM_INVALID, e.getMessage());
			return err;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			ErrorResponse err = new ErrorResponse(
					ErrorResponse.ERROR_PARAM_INVALID, e.getMessage());
			return err;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			// 切换连接方式，返回false一般表明之前有切换尝试过，
			// 仍失败应该是手机当前没有可用连接,故返回错误回复.

			ErrorResponse err = new ErrorResponse(
					ErrorResponse.ERROR_NET_NO_CONNECTION);
			if (stateReceiver != null) {
				stateReceiver.onNetError(request, currentThread, err);
			}
			return err;

		} catch (SocketTimeoutException e) {
			e.printStackTrace();

			ErrorResponse err = new ErrorResponse(
					ErrorResponse.ERROR_NET_TIMEOUT, ErrorResponse.ERROR_DESC_NET);
			if (stateReceiver != null) {
				stateReceiver.onNetError(request, currentThread, err);
			}
			return err;
		} catch (SocketException e) {
			e.printStackTrace();
			// 切换连接方式，返回false一般表明之前有切换尝试过，
			// 仍失败应该是手机当前没有可用连接,故返回错误回复.

			ErrorResponse err = new ErrorResponse(
					ErrorResponse.ERROR_NET_TIMEOUT, ErrorResponse.ERROR_DESC_NET);
			if (stateReceiver != null) {
				stateReceiver.onNetError(request, currentThread, err);
			}
			return err;

		} catch (IOException e) {
			e.printStackTrace();
			// 切换连接方式，返回false一般表明之前有切换尝试过，
			// 仍失败应该是手机当前没有可用连接,故返回错误回复.

			if (e instanceof UnknownHostException) {

				ErrorResponse err = new ErrorResponse(
						ErrorResponse.ERROR_NET_DISCONNECTED);
				if (stateReceiver != null) {
					stateReceiver.onNetError(request, currentThread, err);
				}
				return err;

			} else {

				ErrorResponse err = new ErrorResponse(
						ErrorResponse.ERROR_NET_DISCONNECTED);
				if (stateReceiver != null) {
					stateReceiver.onNetError(request, currentThread, err);
				}
				return err;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorResponse err = new ErrorResponse(
					ErrorResponse.ERROR_NET_DISCONNECTED);
			if (stateReceiver != null) {
				stateReceiver.onNetError(request, currentThread, err);
			}
			return err;
		} finally {
			closeConnection(connection);
		}
	}

	/**
	 * 建立网络连接
	 * @param currentThread 请求cookie
	 * @param request 请求
	 * @param stateReceiver 状态接收器
	 * @return DefaultHttpClient
	 * @throws IOException
	 */
	private static DefaultHttpClient connectServer(ControlRunnable currentThread,
			BaseHttpRequest request, INetStateListener stateReceiver)
			throws IOException {
		DefaultHttpClient connection = null;

		if (stateReceiver != null) {
			stateReceiver.onStartConnect(request, currentThread);
		}

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, request
				.getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(my_httpParams, request
				.getConnectionTimeout());

		connection = new DefaultHttpClient(my_httpParams);

		// 如果不是WIFI连接
		if (currentThread.getNetConnectionListener() != null &&
				!currentThread.getNetConnectionListener().getConnectivityManager().getNetworkInfo(1)
				.isConnected()) {
			APN currentAPN = currentThread.getNetConnectionListener().getCurrentAPN();
			if (currentAPN != null && currentAPN.proxy != null && !currentAPN.proxy.equals("")) {

				HttpHost proxy = new HttpHost(currentAPN.proxy, currentAPN.port);
				connection.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
		}

		if (stateReceiver != null) {
			stateReceiver.onConnected(request, currentThread);
		}

		return connection;
	}
	
	/**
	 * 构建并传递请求参数
	 * @param connection 网络连接
	 * @param currentThread	请求cookie
	 * @param request 请求
	 * @param stateReceiver 状态接收器
	 * @return HttpUriRequest
	 * @throws IOException
	 */
	private static HttpUriRequest buildAndSendRsq(DefaultHttpClient connection,
			ControlRunnable currentThread, BaseHttpRequest request,
			INetStateListener stateReceiver) throws IOException {

		HttpUriRequest httpUriRequest = null;

		if (stateReceiver != null) {
			stateReceiver.onStartSend(request, currentThread, -1);
		}

		if (request.getMethod() == BaseHttpRequest.GET) {
			httpUriRequest = new HttpGet(request.getAbsoluteURI());
			
			if(request.isNeedExtHeader()) {
				if(request.isNeedGZip()) {
					httpUriRequest.addHeader("Accept-Encoding", "gzip");
				}
	
				if(ClientSession.getInstance().getSessionId() != null 
						&& !ClientSession.getInstance().getSessionId().equals("")) {
					httpUriRequest.addHeader("SessionId", ClientSession.getInstance().getSessionId());
				}
				
				httpUriRequest.addHeader("User-Agent", ClientSession.sHeaderUserAgent);
			
			}
			
			String[][] aryHeaders = request.getExtraHeaders();
			if (aryHeaders != null) {
				int length = aryHeaders.length;
				if (aryHeaders != null) {
					for (int i = 0; i < length; ++i) {
						if (aryHeaders[i].length != 2) {
							throw new IllegalArgumentException(
									"aryheader must be 2 columns!");
						}

						httpUriRequest.addHeader(aryHeaders[i][0], aryHeaders[i][1]);
							
					}
				}
				
			}
			
		} else {

			HttpPost httpPost = new HttpPost(request.getAbsoluteURI());
			AbstractHttpEntity entity = null;
			
			if(request.getPostParams() == null) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				request.fillOutputStream(currentThread, outputStream, stateReceiver);
	
				entity = new ByteArrayEntity(outputStream.toByteArray());
	
			} else {
				entity = new UrlEncodedFormEntity(request.getPostParams(), request.getCharset());
			}
			httpPost.setEntity(entity);
			
			if(request.isNeedExtHeader()) {
					
				if (request.isNeedGZip())
					httpPost.addHeader("Accept-Encoding", "gzip");
				
				if(ClientSession.getInstance().getSessionId() != null 
						&& !ClientSession.getInstance().getSessionId().equals("")) {
					httpPost.addHeader("SessionId", ClientSession.getInstance().getSessionId());
				}
				
				httpPost.addHeader("User-Agent", ClientSession.sHeaderUserAgent);
			}
			
			String[][] aryHeaders = request.getExtraHeaders();
			if (aryHeaders != null) {
				int length = aryHeaders.length;
				if (aryHeaders != null) {
					for (int i = 0; i < length; ++i) {
						if (aryHeaders[i].length != 2) {
							throw new IllegalArgumentException(
									"aryheader must be 2 columns!");
						}
						httpPost.addHeader(aryHeaders[i][0], aryHeaders[i][1]);

					}
				}
			}
			
			httpUriRequest = httpPost;
		}

		if (stateReceiver != null) {
			stateReceiver.onSendFinish(request, currentThread);
		}

		return httpUriRequest;
	}

	/**
	 * 接收回复内容并解析
	 * @param response 网络回复
	 * @param currentThread 请求Cookie
	 * @param request 请求
	 * @param stateReceiver 状态接收器
	 * @return 回复内容
	 * @throws IOException
	 */
	private static BaseResponse recvAndParseRsp(HttpResponse response,
			ControlRunnable currentThread, BaseHttpRequest request,
			INetStateListener stateReceiver) throws IOException {
		BaseHttpResponse httpResponse = null;

		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {

			request.headers = response.getAllHeaders();

			int len = (int) response.getEntity().getContentLength();

			// 针对当前协议，返回内容长度不应该为0,故出现此情况返回错误
			if (len == 0) {
				return new ErrorResponse(ErrorResponse.ERROR_SERVER);
			}

			if (stateReceiver != null) {
				stateReceiver.onStartRecv(request, currentThread, len);
			}

			InputStream instream = response.getEntity().getContent();
			Header contentEncoding = response
					.getFirstHeader("Content-Encoding");

			if (contentEncoding != null
					&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {

				instream = new GZIPInputStream(instream);
			}

			httpResponse = request.createResponse();
			ErrorResponse err = httpResponse.parseInputStream(currentThread,
					request, instream, len, stateReceiver);

			if (stateReceiver != null) {
				stateReceiver.onRecvFinish(request, currentThread);
			}

			if (err != null) {
				httpResponse = null;
				return err;
			}
			return httpResponse;
		} else {
			return new ErrorResponse(ErrorResponse.ERROR_SERVER);
		}
	}

	/**
	 * 关闭网络连接
	 * @param connection
	 */
	private static void closeConnection(DefaultHttpClient connection) {
		try {
			if (connection != null)
				connection.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
