package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.utils.PropertyUtil;

/**
 * @author YeChunBo
 * @time 2017年8月2日
 *
 *       类说明 ：项目启动类，启动该项目只需求启动该类便可
 */

@SpringBootApplication
@RestController
public class Main implements EmbeddedServletContainerCustomizer{

	@RequestMapping("/")
	public String getHello() {
		return "Hello Spring Boot .....";
	}

	// 修改访问的默认端口
	public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {
		configurableEmbeddedServletContainer.setPort(Integer.parseInt(PropertyUtil.getProperty("projectPort")));
	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
