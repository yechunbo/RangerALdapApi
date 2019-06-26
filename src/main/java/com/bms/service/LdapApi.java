package com.bms.service;

import java.util.List;

/**
* @author YeChunBo
* @time 2017年7月27日 
*
* 类说明 
*/

public interface LdapApi {
	
	/**
	 * 查询
	 * @param searchDN
	 * @param filter
	 */
	public List<String> queryLdap(String searchDN, String filter);
	
	/**
	 * 创建条目
	 * @param baseDN
	 * @param uid
	 * @param userPwd
	 * @return 0:已存在；1：创建成功; 2: 创建失败
	 */
	public Integer createEntry(String baseDN, String uid, String userPwd);
	
	/**
	 * 删除条目
	 * @param requestDN
	 * @return 0:不存在；1：删除成功; 2: 删除失败
	 */
	public Integer deleteEntry(String requestDN);
	
	/**
	 * 调用shell脚本创建keytab
	 * @param userName
	 * @return
	 */
	public boolean createKeytab(String userName);
	
}
