package com.bms.controller;

import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bms.service.ldapimpl.LdapApiImpl;

/**
 * @author YeChunBo
 * @time 2017年8月2日
 *
 *       类说明 Ldap api接口控制类
 */

@SpringBootApplication
@RestController
public class LdapController {

	private static Logger log = Logger.getLogger(LdapController.class);
	public static final String DEFAULT_LDAP_PWD = "zwzx_dsj"; // ldap user 默认的用户密码
	private static final Integer operateEntryFail = 2;
	LdapApiImpl ldapApiImpl = new LdapApiImpl();

	/**创建ldap 用户
	 * 请求样例：http://localhost:8567/create_ldap_user?uid=bms_test5&pwd=123
	 * @param basedN：非必需，默认为：ou=people,dc=hadoop,dc=apache,dc=org
	 * @param uid：必需
	 * @param pwd：非必需，默认为：zwzx_dsj
	 * @return
	 */
	@RequestMapping("/create_ldap_user")
	@ResponseBody
	Integer createLdapUser(
			@RequestParam(value = "basedN", required = false, defaultValue = "ou=people,dc=hadoop,dc=apache,dc=org") String basedN,
			@RequestParam(value = "uid", required = true) String uid,
			@RequestParam(value = "pwd", required = false, defaultValue = DEFAULT_LDAP_PWD) String pwd) {

		log.info("LdapController createEntry the base DN is: " + basedN + " ,and the uid is: " + uid + " ,and the usePwd is: " + pwd);
		
		return ldapApiImpl.createEntry(basedN, uid, pwd);
	}

	
	
	
	/**
	 * 查询所有Ldap用户
	 * @param searchDN：非必需参数
	 * @param filter：非必需参数
	 * @return
	 */
	@RequestMapping("/query_ldap_user")
	@ResponseBody
	List<String> queryLdapUser(
			@RequestParam(value = "searchDN", required = false, defaultValue = "ou=people,dc=hadoop,dc=apache,dc=org") String searchDN,
			@RequestParam(value = "filter", required = false, defaultValue = "objectClass=person") String filter) {
	
		log.info("LdapController of searchDN: " + searchDN + ", filter :" + filter);

		return ldapApiImpl.queryLdap(searchDN, filter);
	}

	
	
	/**
	 * 删除Ldap用户
	 * @param uid：用户名。eg:hive
	 * @return
	 */
	@RequestMapping("/delete_ldap_user")
	@ResponseBody
	Integer deleteLdapUser(@RequestParam(value = "uid", required = true) String uid) {
		// 不能删除admin,因为admin是管理用户的用户，如果需要删除可通过Ambari或AD studio工具进行删除
		if(uid.equals("admin")){
			log.info("DeleteLdapUser of admin is not allow.");
			return operateEntryFail;
		}
		log.info("LdapController of deleteLdapUser: " + uid);
		uid = "uid=" + uid + ",ou=people,dc=hadoop,dc=apache,dc=org";
		
		return ldapApiImpl.deleteEntry(uid);
	}
	
}
