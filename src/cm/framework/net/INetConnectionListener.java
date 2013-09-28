package cm.framework.net;

import android.net.ConnectivityManager;
import cm.framework.include.APN;

/**
 * 网络连接监听
 * @author 
 *
 */
public interface INetConnectionListener {

	/**
	 * 获取网络连接Manager
	 * @return
	 */
	public ConnectivityManager getConnectivityManager();
	/**
	 * 获取APN
	 * @return
	 */
	public APN getCurrentAPN();
}
