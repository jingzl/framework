package cm.framework.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cm.framework.net.ClientSession;
import cm.framework.net.ControlRunnable;
import cm.framework.net.INetStateListener;

/**
 * JSON协议回复包抽象基类，子类继承此类完成回复构建
 * 
 * @author gyx
 * 
 */
public abstract class BaseJSONRsponse implements BaseHttpResponse {

	private InputStream inputStream = null;
	
	private int len;

	private String responseContent;
	// 是否从缓存获取
	public boolean isGetFromCache = false;
	
	public ErrorResponse parseInputStream(ControlRunnable currentThread,
			BaseHttpRequest request, InputStream inputStream, int len,
			INetStateListener stateReceiver) throws IOException {

		this.inputStream = inputStream;
		this.len = len;
		isGetFromCache = false;
		
		Header[] headers = request.headers;

		for (int i = 0; i < headers.length; i++) {
			if (headers[i].getName().equalsIgnoreCase("SessionId")) {
				ClientSession.getInstance().setSessionId(headers[i].getValue());
				break;
			}
		}
		
		JSONObject doc = parseResponse(inputStream);

		if (doc == null) {
			return new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		}
		// protocol parse error?

		// validate check fail?
		ErrorResponse errorResponse = null;
		try {
			errorResponse = validateCheck(doc);
		} catch (JSONException e) {
			e.printStackTrace();
			errorResponse = new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		}

		if (errorResponse != null) {
			return errorResponse;
		}

		// derived class parses concrete json protocol
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
		
		this.len = len;
		isGetFromCache = true;
		
		JSONObject doc = parseResponse(responseContent);

		if (doc == null) {
			return new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		}
		// protocol parse error?

		// validate check fail?
		ErrorResponse errorResponse = null;
		try {
			errorResponse = validateCheck(doc);
		} catch (JSONException e) {
			e.printStackTrace();
			errorResponse = new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		}

		if (errorResponse != null) {
			return errorResponse;
		}

		// derived class parses concrete json protocol
		if (!extractBody(doc)) {
			return new ErrorResponse(ErrorResponse.ERROR_PROTOTOL);
		}
		// 释放内存
		doc = null;
		// parse successfully
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
	 * @param headerElement
	 * @param bodyElement
	 * @return true--success, false--fail
	 */
	protected abstract boolean extractBody(JSONObject bodyElement);

	/**
	 * 将inputstream转成JSON
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private JSONObject parseResponse(InputStream response) throws IOException {
		if (response == null)
			return null;
		else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = response.read()) != -1) {
				baos.write(i);
			}

			try {
				String str = new String(baos.toByteArray(), "UTF-8");
				responseContent = str;
				
				return new JSONObject(str);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

		}

	}
	
	/**
	 * 将inputstream转成JSON
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private JSONObject parseResponse(String response) throws IOException {
		if (response == null)
			return null;
		else {
			
			try {
				return new JSONObject(response);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

		}

	}

	/**
	 * 检查回复结果格式及返回结果成功或失败
	 * 
	 * @param object
	 * @return 如果格式正确或结果成功返回NULL，错误返回ErrorResponse
	 * @throws JSONException
	 */
	private ErrorResponse validateCheck(JSONObject object) throws JSONException {
		
		Log.e("Response", object.toString());
		
		int result = object.getInt("status");

		if ( result != 0 )
		{
			try 
			{
				/*
				JSONObject errJsonObject = object.getJSONObject("data");
				int errcode = errJsonObject.getInt("error_code");
				String errormsg = errJsonObject.getString("error_msg");
				*/
				String errormsg = object.getString( "res" );
				return new ErrorResponse( result, errormsg );
			}
			catch (JSONException e)
			{
				/*
				String errdes = object.getString("data");
				if(errdes.equals("null")) {
					return new ErrorResponse(ErrorResponse.ERROR_NULL_RESULT, errdes);
				} else {
					return new ErrorResponse(0, errdes);
				}
				*/
				return new ErrorResponse(ErrorResponse.ERROR_NULL_RESULT, "null");
			}
			
		}
		
		return null;
	}

	@Override
	public String getResponseContent() {
		return responseContent;
	}
}
