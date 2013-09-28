package cm.framework.net;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import cm.framework.db.CacheDB;
import cm.framework.include.CacheData;
import cm.framework.protocol.BaseHttpRequest;
import cm.framework.protocol.BaseHttpResponse;
import cm.framework.protocol.BaseResponse;
import cm.framework.utils.Cache;



/**
 * 全局唯一的客户端会话类，负责与服务器交互，主要功能： 
 * 1. 用户登录，注销及会话相关查询； 
 * 2. 客户端与服务器交互时请求的发送及回复的接收； 
 * 3. 相关状态及请求处理结果的通知 注：平凡提交过多的异步请求可能造成很大线程开销，上层调用需注意
 * 
 * @author 
 * 
 */
public class ClientSession {

	// 当前会话的ID
	private String sessionId;
	// 用户名
	private String userName;
	// 密码
	private String passWord;
	// 统一接入入口
	private String accessPoint;
	// 默认错误处理器
	private IErrorListener defErrorReceiver;
	// 默认状态处理器
	private INetStateListener defStateReceiver;
	// 当前会话状态
	public static final int STATE_LOGOFF = 0; // 未登录
	public static final int STATE_LOGINED = 1; // 已登录
	public static final int STATE_LOGINING = 2; // 正在登录

	private static ClientSession instance;
	// SimSerialNumber
	public String SMD = "";
	public String IMEI = "";
	public String IMSI = "";
	// 屏幕宽度高度
	public int screenWidth;
	public int screenHeight;
	// Header中统一增加User-Agent: 产品名_版本号_平台
	public static String sHeaderUserAgent; 
	// Header中添加Authorization: Basic（passport：password）
	public static String sAuthorization;
	// 线程池
	private ExecutorService mThreadPool = Executors.newFixedThreadPool(5);
	// 本地回复缓存,最大缓存数量为10
	private static Cache rspCache = new Cache(10);
	// 内存数据库
	public static CacheDB cacheDBHelper;
	// 本地图片缓存,最大缓存数量为10
	private static Cache imageCache = new Cache(10);
	
	private INetConnectionListener iNetConnectionListener;
	
	private ClientSession() {

	}
	
	public static ClientSession getInstance() {
		synchronized (ClientSession.class) {
			
			if(instance == null) {
				instance = new ClientSession();
			}
			
			return instance;
		}
	}

	public void setUserName(String username) {
		this.userName = username;
	}
	
	public void setPassword(String password) {
		this.passWord = password;
	}

	/**
	 * 获取当前登录用户名
	 * 
	 * @return
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 获取当前登录用户密码 return
	 */
	public String getPassword() {
		return passWord;
	}

	/**
	 * 获取当前会话的ID
	 * 
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 获取异步请求锁
	 * 
	 * @return
	 */
	public Object getAsynRsqLock() {
		return this;
	}

	/**
	 * 同步获取请求回复接口 调用此接口后在得到回复前会一直阻塞注, 当出现任何错误时获取到得回复实际类型为ErrorResponse,
	 * 故调用此接口后需要利用instanceof关键字判断是否是错误回复， 并进行相应错误处理。
	 * 
	 * @param request
	 * @return
	 */
	public BaseResponse syncGetResponse(BaseHttpRequest request) {
		return RsqHandleHelper.getResponseImpl(null, request, null);
	}

	/**
	 * 异步获取请求回复接口 使用默认错误接收器及网络状态接收器作为通知接口,并采用互斥方式，防止多线程并发请求
	 * 
	 * @param request
	 * @param receiver
	 *            return: 见下一接口说明
	 */
	public int asynGetResponse(BaseHttpRequest request,
			IResponseListener receiver) {
		return asynGetResponse(request, receiver, defErrorReceiver,
				defStateReceiver);
	}

	/**
	 * 异步获取请求回复接口 使用默认错误接收器及网络状态接收器作为通知接口,并采用互斥方式，防止多线程并发请求
	 * 
	 * @param request
	 * @param receiver
	 *            return: 见下一接口说明
	 */
	public int asynGetResponse(BaseHttpRequest request,
			IResponseListener receiver, IErrorListener defErrorReceiver) {
		return asynGetResponse(request, receiver, defErrorReceiver,
				defStateReceiver);
	}

	/**
	 * 异步获取请求回复接口 采用互斥方式，防止多线程并发请求
	 * 
	 * @param request
	 * @param rspReceiver
	 * @param errReceiver
	 * @param stateReceiver
	 *            return: 标识该请求的cookie，上层可利用该cookie来取消此次请求
	 */
	synchronized public int asynGetResponse(BaseHttpRequest request,
			IResponseListener rspReceiver, IErrorListener errReceiver,
			INetStateListener stateReceiver) {
		synchronized (getAsynRsqLock()) {
			return asynGetResponseWithoutLock(request, rspReceiver,
					errReceiver, stateReceiver);
		}
	}

	/**
	 * 异步获取请求回复接口 使用默认错误接收器及网络状态接收器作为通知接口,非锁定方式，这里考虑到上层可能一次会批量调用
	 * 接口多次，为提高效率，在调用之前需要上层负责锁定，以下接口同样如此。
	 * 
	 * @param request
	 * @param receiver
	 *            return: 见下一接口说明
	 */
	public int asynGetResponseWithoutLock(BaseHttpRequest request,
			IResponseListener receiver) {
		return asynGetResponseWithoutLock(request, receiver, defErrorReceiver,
				defStateReceiver);
	}

	/**
	 * 异步获取请求回复接口
	 * 
	 * @param request
	 * @param rspReceiver
	 * @param errReceiver
	 * @param stateReceiver
	 *            return: 标识该请求的cookie，上层可利用该cookie来取消此次请求
	 */
	public int asynGetResponseWithoutLock(BaseHttpRequest request,
			IResponseListener rspReceiver, IErrorListener errReceiver,
			INetStateListener stateReceiver) {
		
		mThreadPool.execute(new ControlRunnable(request, rspReceiver, errReceiver, stateReceiver));
		return 0;
	}

	/**
	 * 取消指定请求的处理,对于无效cookie值会被忽略
	 * 
	 * @param rsqCookie
	 *            : 提交请求时返回的cookie
	 */
	public void cancel(ControlRunnable controlRunnable) {
		synchronized (getAsynRsqLock()) {
			cancelWithoutLock(controlRunnable);
		}
	}

	/**
	 * 取消指定请求的处理,对于无效cookie值会被忽略
	 * 
	 * @param rsqCookie
	 *            : 提交请求时返回的cookie
	 */
	public void cancelWithoutLock(ControlRunnable controlRunnable) {
		if(controlRunnable != null)
			controlRunnable.cancel();
	}

	/**
	 * 取消当前所有请求
	 */
	public void cancelAll() {
		
	}

	/**
	 * 设置默认错误接收器
	 * 
	 * @param receiver
	 */
	public void setDefErrorReceiver(IErrorListener receiver) {
		defErrorReceiver = receiver;
	}

	/**
	 * 获取默认错误接收器
	 * 
	 * @return
	 */
	public IErrorListener getDefErrorReceiver() {
		return defErrorReceiver;
	}

	/**
	 * 设置默认网络状态接收器
	 * 
	 * @param receiver
	 */
	public void setDefStateReceiver(INetStateListener receiver) {
		defStateReceiver = receiver;
	}

	/**
	 * 获取默认网络状态接口器
	 * 
	 * @return
	 */
	public INetStateListener getDefStateReceiver() {
		return defStateReceiver;
	}

	/**
	 * 获取新的统一接入入口
	 * 
	 * @return
	 */
	public String getAccessPoint() {
		return accessPoint;
	}

	public void SetAccessPoint(String accessPoint) {
	  this.accessPoint = accessPoint;
	}
	
	synchronized static BaseHttpResponse getCacheResponse(
			BaseHttpRequest request) {
		return (BaseHttpResponse) rspCache.get(new Integer(request.hashCode()));
	}

	synchronized static void addCacheResponse(BaseHttpRequest request,
			BaseHttpResponse response) {
		rspCache.add(new Integer(request.hashCode()), response);
	}
	
	public synchronized static Bitmap getImgFromCache(String url) {
		return (Bitmap)imageCache.get(url);
	}

	public synchronized static void addImgCache(String url, Bitmap bitmap) {
		imageCache.add(url, bitmap);
	}
	
	
	// 从DB缓存中获取数据
	synchronized static CacheData getCacheFromDB(BaseHttpRequest request) {
		if(cacheDBHelper != null) {
			return cacheDBHelper.getCacheByUrl(request.cacheData.url, ClientSession.getInstance().getUserName());
		}
		return null;
	}

	// 添加缓存数据到DB
	synchronized static void addCacheToDB(BaseHttpRequest request,
			BaseHttpResponse response) {
		
		if(cacheDBHelper != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:SS");      
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间      
			String date = formatter.format(curDate); 
			
			request.cacheData.createDate = date;
			request.cacheData.response = response.getResponseContent();
			request.cacheData.len = response.getLength();
			request.cacheData.userid = ClientSession.getInstance().getUserName();
			
			cacheDBHelper.saveCache(request.cacheData);
		}
	}
	
	// 删除过期缓存数据
	synchronized static void deleteCache(BaseHttpRequest request) {
		
		if(cacheDBHelper != null) {
			cacheDBHelper.deleteCache(request.cacheData.url);
		}
	}
	
	public INetConnectionListener getNetConnectionListener() {
		return iNetConnectionListener;
	}

	public void setNetConnectionListener(
			INetConnectionListener iNetConnectionListener) {
		this.iNetConnectionListener = iNetConnectionListener;
	}

}
