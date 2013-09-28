package cm.framework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 验证程序中各种字符串的合法性
 * 
 * @author Administrator
 *
 */

public class ValidateUtil {

	/**
	 * 验证用户名输入框输入的内容
	 * 
	 * @param userName 用户名 
	 * @param errorMsg 错误信息,errorMsg必须在外部初始化
	 * @return 用户名是否符合要求
	 */
	public static boolean validateUserName(String userName, StringBuffer errorMsg) {
		errorMsg.delete(0, errorMsg.length());
		
		if (userName == null || userName.length() < 1) {
			errorMsg.append("用户名不能为空！");
			return false;
		}
//		// 判断长度是否大于10个字
//		if (userName.length() > 10) {
//			errorMsg.append("用户名长度不能大于10个字！");
//			return false;
//		}

		if (userName.contains("&") || userName.contains("<") || userName.contains(">")) {
			errorMsg.append("不能输入非法字符！");
			return false;
		}

		return true;
	}
	
	/**
	 * 验证用户名输入框输入的内容
	 * 
	 * @param userName 用户名
	 * @return 用户名是否符合要求
	 */
	public static boolean validateUserName(String userName) {
		StringBuffer errorMsg = new StringBuffer();
		return validateUserName(userName, errorMsg);
	}
	
	/**
	 * 验证用户ID的合法性（字母开头，允许4-16字节，允许字母数字下划线）
	 * 
	 * @param userID
	 * @param errorMsg 错误信息,errorMsg必须在外部初始化
	 * @return 用户ID是否符合要求
	 */
	public static boolean validateUserID(String userID, StringBuffer errorMsg) {
		
		errorMsg.delete(0, errorMsg.length());
		String regEx = "^[0-9a-zA-Z]{3,15}$";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(userID);
		if (!matcher.find()) {
			errorMsg.append("用户名必须为英文字母或数字，允许4-16个字");
			return false;
		}
		return true;
	}
	
	/**
	 * 验证用户ID的合法性（字母开头，允许4-16字节，允许字母数字下划线）
	 * 
	 * @param userID
	 * @return 用户ID是否符合要求
	 */
	public static boolean validateUserID(String userID) {
		
		StringBuffer errorMsg = new StringBuffer();
		return validateUserID(userID, errorMsg);
	}

	/**
	 * 检查字符串是否为电话号码
	 * 
	 * @param phoneNumber 待验证的电话号码
	 * @param errorMsg 错误信息,errorMsg必须在外部初始化
	 * @return 是否为有效的电话号码
	 */
	public static boolean isPhoneNumberValid(String phoneNumber, StringBuffer errorMsg) {
		errorMsg.delete(0, errorMsg.length());
		
		if (phoneNumber == null || phoneNumber.trim().equals("") ) {
			errorMsg.append("手机号不能为空！");
			return false;
		}
		
		boolean isValid = false;
		/*
		 * 可接受的电话格式有: ^\\(? : 可以使用 "(" 作为开头 (\\d{3}): 紧接着三个数字 \\)? : 可以使用")"接续
		 * [- ]? : 在上述格式后可以使用具选择性的 "-". (\\d{4}) : 再紧接着三个数字 [- ]? : 可以使用具选择性的
		 * "-" 接续. (\\d{4})$: 以四个数字结束. 可以比较下列数字格式: (123)456-78900,
		 * 123-4560-7890, 12345678900, (123)-4560-7890
		 */
		String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{5})$";
		String expression2 = "^\\(?(\\d{3})\\)?[- ]?(\\d{4})[- ]?(\\d{4})$";
		String expression3 = "^(0){0,1}(1)(3|4|5|8){1}[0-9]{9}$";
		CharSequence inputStr = phoneNumber;
		/* 创建Pattern */
		Pattern pattern = Pattern.compile(expression);
		/* 将Pattern 以参数传入Matcher作Regular expression */
		Matcher matcher = pattern.matcher(inputStr);
		/* 创建Pattern2 */
		Pattern pattern2 = Pattern.compile(expression2);
		/* 将Pattern2 以参数传入Matcher2作Regular expression */
		Matcher matcher2 = pattern2.matcher(inputStr);

		Pattern pattern3 = Pattern.compile(expression3);
		/* 将Pattern2 以参数传入Matcher2作Regular expression */
		Matcher matcher3 = pattern3.matcher(inputStr);
		if (matcher3.matches() && (matcher.matches() || matcher2.matches())) {
			isValid = true;
		}
		else {
			errorMsg.append("手机号填写不正确请重新输入！");
		}
		return isValid;
	}
	
	/**
	 * 验证密码是否有效
	 * 
	 * @param password
	 * @param errorMsg 错误信息,errorMsg必须在外部初始化
	 * @return
	 */
	public static boolean validatePassword(String password, StringBuffer errorMsg) {
		errorMsg.delete(0, errorMsg.length());
		
		if (password == null || password.trim().equals("") ) {
			errorMsg.append("密码不能为空！");
			return false;
		} else if(password.contains(" ")) {
			errorMsg.append("密码不能包含空格！");
			return false;
		} else if(password.length() < 6) {
			errorMsg.append("密码不能少于6位！");
			return false;
		}
		
		return true;
	}
	
	/**
	 * 验证密码是否有效
	 * 
	 * @param password
	 * @return
	 */
	public static boolean validatePassword(String password) {
		StringBuffer errorMsg = new StringBuffer();
		return validatePassword(password, errorMsg);
	}
	
	/**
	 * 比对密码是否一样
	 * 
	 * @param password1
	 * @param password2
	 * @param errorMsg 错误信息,errorMsg必须在外部初始化
	 * @return 密码一样返回true，不一样返回false
	 */
	public static boolean comparePasswords(String password1,String password2, StringBuffer errorMsg) {
		errorMsg.delete(0, errorMsg.length());
		
		if (password1.equals(password2)) {
			return true;
		}
		errorMsg.append("两次输入的密码不一致！");
		return false;
	}
	
	/**
	 * 比对密码是否一样
	 * 
	 * @param password1
	 * @param password2
	 * @return 密码一样返回true，不一样返回false
	 */
	public static boolean comparePasswords(String password1,String password2) {
		
		StringBuffer errorMsg = new StringBuffer();
		return comparePasswords(password1, password2, errorMsg);
	}
	
	/**
	 * 检查字符串是否为电话号码
	 * 
	 * @param phoneNumber 待验证的电话号码
	 * @return 是否为有效的电话号码
	 */
	public static boolean isPhoneNumberValid(String phoneNumber) {
		StringBuffer errorMsg = new StringBuffer();
		return isPhoneNumberValid(phoneNumber, errorMsg);
	}
	
	/**
     * 验证输入的邮箱格式是否符合
     * 
     * @param email
     * @return 是否合法
     */
	public static boolean validateEmailFormat(String email, StringBuffer errorMsg)
    {
		errorMsg.delete(0, errorMsg.length());
        boolean tag = true;
        final String pattern1 = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
        final Pattern pattern = Pattern.compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
            errorMsg.append("邮箱格式不正确！");
        }
        return tag;
    }
	
	/**
	 * 验证输入的邮箱格式是否符合
	 * 
	 * @param email
	 * @return 是否合法
	 */
	public static boolean validateEmailFormat(String email) {
		StringBuffer errorMsg = new StringBuffer();
		return validateEmailFormat(email, errorMsg);
	}
}
