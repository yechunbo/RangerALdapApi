package com.bms.utils;

/**
* @author YeChunBo
* @time 2017年7月27日 
*
* 类说明 :properties文件获取工具类
*/
import java.io.*;
import java.util.Properties;

import org.jboss.logging.Logger;

public class PropertyUtil {
	private static final Logger logger = Logger.getLogger(PropertyUtil.class);
	private static Properties props;
	static {
		loadProps();
	}

	synchronized static private void loadProps() {
		logger.info("start load properties .......");
		props = new Properties();
		InputStream in = null;
		try {
			in = PropertyUtil.class.getClassLoader().getResourceAsStream("system.properties");
			props.load(in);
		} catch (FileNotFoundException e) {
			logger.error("system.properties file is not found ...");
		} catch (IOException e) {
			logger.error("system.properties load is failed , e=" + e.getMessage());
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				logger.error("system.properties load is failed , e=" + e.getMessage());
			}
		}
		logger.info("load properties is finished .........");
	}

	public static String getProperty(String key) {
		if (null == props) {
			loadProps();
		}
		return props.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		if (null == props) {
			loadProps();
		}
		return props.getProperty(key, defaultValue);
	}

	public static void main(String[] args) {
		String property = getProperty("ldapHost");
		System.out.println(property);
	}
}
