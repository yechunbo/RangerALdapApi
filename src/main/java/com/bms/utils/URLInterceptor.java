package com.bms.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author YeChunBo
 * @time 2017年9月8日
 *
 *       类说明: ip 拦截器，只有在配置文件中定义了的ip 才可以访问接口
 */

public class URLInterceptor implements HandlerInterceptor {

	private static final Logger logger = Logger.getLogger(URLInterceptor.class);

	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 在请求处理之前进行调用（Controller方法调用之前）调用,
	 *  返回true 则放行， false 则将直接跳出方法
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
//		String ip = getIpAddr(request);
//		String ipStr = PropertyUtil.getProperty("ipWhiteList"); // 获取可以访问系统的白名单
//		String[] ipArr = ipStr.split("\\|");
//		List<String> ipList = java.util.Arrays.asList(ipArr);
//
//		if (ipList.contains(ip)) {
//			logger.info("the request ip is : " + ip);
//			return true;
//		} else {
//			logger.error(ip + " is not contains ipWhiteList .......");
//			response.setHeader("Content-type","text/html;charset=UTF-8");//向浏览器发送一个响应头，设置浏览器的解码方式为UTF-8
//		    String data = "Sorry, ip " + ip + " ,there is no access right, please contact the administrator to open access.";
//		    OutputStream stream = response.getOutputStream();
//		    stream.write(data.getBytes("UTF-8"));
//			return false;
//		}
		// 为方便测试在没有正式上线先不进行白名单的过滤
		String ip = getIpAddr(request);
		logger.info("The request ip is : " + ip);
		return true;
	}

	/**
	 * 获取访问的ip地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

}
