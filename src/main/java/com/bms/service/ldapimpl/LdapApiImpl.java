package com.bms.service.ldapimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import com.bms.service.LdapApi;
import com.bms.utils.PropertyUtil;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SubentriesRequestControl;

/**
 * @author YeChunBo
 * @time 2017年7月27日
 *
 *       类说明 Ldap java api 操作
 */

public class LdapApiImpl implements LdapApi {

	private static Logger log = Logger.getLogger(LdapApiImpl.class);
	// 当前配置信息
	private static String ldapHost = PropertyUtil.getProperty("ldapHost");
	private static String ldapPort = PropertyUtil.getProperty("ldapPort");
	private static String ldapBindDN = PropertyUtil.getProperty("ldapBindDN");
	private static String ldapPassword = PropertyUtil.getProperty("ldapPassword");
	private static LDAPConnection connection = null;

	/** entry 已存在 */
	private static final Integer EntryIsExist = 0;
	/** entry 不存在 */
	private static final Integer EntryIsNotExist = 3;
	/** entry 操作成功 */
	private static final Integer operateEntrySuccess = 1;
	/** entry 操作失败 */
	private static final Integer operateEntryFail = 2;

	static {
		if (connection == null) {
			try {
				connection = new LDAPConnection(ldapHost, Integer.parseInt(ldapPort), ldapBindDN, ldapPassword);
			} catch (Exception e) {
				log.error("Connect to ldap is failed, the fail message is：" + e.getMessage());
			}
		}
	}

	public Integer createEntry(String baseDN, String uid, String userPwd) {

		Integer operateFlag = new Integer(operateEntryFail);
		log.info("CreateEntry the base DN is: " + baseDN + " ,and the uid is: " + uid + " ,and the usePwd is: "
				+ userPwd);

		String entryDN = "uid=" + uid + "," + baseDN;
		try {
			SearchResultEntry entry = connection.getEntry(entryDN);
			if (entry == null) {
				// 不存在则创建
				ArrayList<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute("objectClass", "organizationalPerson", "person", "inetOrgPerson", "top"));
				attributes.add(new Attribute("sn", "person"));
				attributes.add(new Attribute("cn", "person"));
				attributes.add(new Attribute("uid", uid));

				// 如果传的密码参数为空则将其uid设置为其密码
				if ("".equals(userPwd) || userPwd == null)
					attributes.add(new Attribute("userPassword", uid));
				else
					attributes.add(new Attribute("userPassword", userPwd));

				try {
					connection.add(entryDN, attributes); // Ldap有时在创建用户是抛出异常，但是其创建用户依然成功，则让其调用生成keytab脚本
				} catch (Exception e) {
					entry = connection.getEntry(entryDN);
					if (entry != null) { // 如果创建的用户非空则说明用户已创建
						operateFlag = operateEntrySuccess;
					} else {
						operateFlag = operateEntryFail;
						log.error("Create entry of [" + entryDN + "] is failed, the error message is: " + e.getMessage());
					}
				}

				operateFlag = operateEntrySuccess;
				log.info("CreateEntry of [" + entryDN + "] is successed, and the operateFlag is " + operateFlag);

				boolean createKeytabFlag = false;
				if (operateFlag == operateEntrySuccess) {
					// 如果Ldap用户创建成功则同时也创建其对应的keytab
					createKeytabFlag = createKeytab(uid);

					// 调用shell脚本同步Ldap用户到Ranger中,脚本需要ssh过去到Ranger系统
					String ldap2RangerCommand = PropertyUtil.getProperty("ldapUserSync2RangerPath");
					boolean flag = exceteShell(ldap2RangerCommand);
					log.info(ldap2RangerCommand + " excete is " + flag);

//					// 调用shell脚本同步Ldap用户到Ambari中,脚本需要ssh过去到Ambari系统
//					String Ldap2AmbariCommand = PropertyUtil.getProperty("ldapUserSync2AmbariUserPath");
//					boolean ldap2AmbariFlag = exceteShell(Ldap2AmbariCommand);
//					log.info(Ldap2AmbariCommand + " excete is " + ldap2AmbariFlag);
				}
				log.info("CreateKeytab of [" + entryDN + "] is:" + createKeytabFlag);
			} else {
				operateFlag = EntryIsExist;
				log.warn("The entry of [" + entryDN + "] already exists.");
			}
		} catch (Exception e) {
			operateFlag = operateEntryFail;
			log.error("Create entry of [" + entryDN + "] is failed, the error message is: " + e.getMessage());
		}
		return operateFlag;
	}

	public List<String> queryLdap(String searchDN, String filter) {
		log.info("QueryLdap the searchDn is: " + searchDN + " ,and the filter is: " + filter);
		ArrayList<String> entryList = new ArrayList<String>();
		try {
			SearchRequest searchRequest = new SearchRequest(searchDN, SearchScope.SUB, "(" + filter + ")");
			searchRequest.addControl(new SubentriesRequestControl());
			SearchResult searchResult = connection.search(searchRequest);

			log.info("A total of [" + searchResult.getSearchEntries().size() + "] entry was queried. ");

			int index = 1;
			for (SearchResultEntry entry : searchResult.getSearchEntries()) {
				entryList.add(entry.getDN());
				log.info((index++) + "\t" + entry.getDN());
			}
		} catch (Exception e) {
			log.error("Query failed, the fail message is：" + e.getMessage());
		}
		return entryList;
	}

	public Integer deleteEntry(String requestDN) {

		Integer deleteFlag = new Integer(EntryIsNotExist);
		log.info("Delete entry of requestDN " + requestDN);

		try {
			SearchResultEntry entry = connection.getEntry(requestDN);

			if (entry == null) {
				log.warn("DeleteEntry of [" + requestDN + "] is not exist.");
				return deleteFlag;
			}
			// 删除
			connection.delete(requestDN);
			deleteFlag = operateEntrySuccess;
			log.info("Delete of [" + requestDN + "] is successed.");
		} catch (Exception e) {
			deleteFlag = operateEntryFail;
			log.error("Delete of [" + requestDN + "] is failed the error message is : " + e.getMessage());
		}
		return deleteFlag;
	}

	public boolean createKeytab(String userName) {
		boolean createKeytabFlag = true;
		InputStreamReader stdISR = null;
		InputStreamReader errISR = null;
		Process process = null;
		String command = PropertyUtil.getProperty("keytabShellPath") + " " + userName;
		try {
			process = Runtime.getRuntime().exec(command);
			String line = null;

			stdISR = new InputStreamReader(process.getInputStream());
			BufferedReader stdBR = new BufferedReader(stdISR);
			while ((line = stdBR.readLine()) != null) {
				log.info("CreateKeytab line:" + line); // 将执行脚本过程输入到日志中
			}

			errISR = new InputStreamReader(process.getErrorStream());
			BufferedReader errBR = new BufferedReader(errISR);
			while ((line = errBR.readLine()) != null) {
				log.error("CreateKeytab error line:" + line); // 将执行脚本过程中出现的错误与警告输入到日志中
			}
		} catch (Exception e) {
			createKeytabFlag = false;
			log.error("Excute shell is failed, errMassage:" + e.getMessage());
		} finally {
			try {
				if (stdISR != null) {
					stdISR.close();
				}
				if (errISR != null) {
					errISR.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (IOException e) {
				createKeytabFlag = false;
				log.error("Excute shell is failed, errMassage:" + e.getMessage());
			}
		}
		return createKeytabFlag;
	}

	/**
	 * 执行shell脚本
	 * 
	 * @param command
	 * @return
	 */
	public boolean exceteShell(String command) {
		boolean createKeytabFlag = true;
		InputStreamReader stdISR = null;
		InputStreamReader errISR = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			String line = null;

			stdISR = new InputStreamReader(process.getInputStream());
			BufferedReader stdBR = new BufferedReader(stdISR);
			while ((line = stdBR.readLine()) != null) {
				log.info(command + " excete info:" + line); // 将执行脚本过程输入到日志中
			}

			errISR = new InputStreamReader(process.getErrorStream());
			BufferedReader errBR = new BufferedReader(errISR);
			while ((line = errBR.readLine()) != null) {
				log.error(command + " excete info:" + line); // 将执行脚本过程中出现的错误与警告输入到日志中
			}
		} catch (Exception e) {
			createKeytabFlag = false;
			log.error(command + " ,Excute shell is failed, errMassage:" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (stdISR != null) {
					stdISR.close();
				}
				if (errISR != null) {
					errISR.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (IOException e) {
				createKeytabFlag = false;
				log.error(command + ",Excute shell is failed, errMassage:" + e.getMessage());
				e.printStackTrace();
			}
		}
		return createKeytabFlag;
	}
	
	
//	 public static void main(String[] args) {
//	
//	 String filter = "objectClass=person";
//	
//	 LdapApiImpl ldapApiImpl = new LdapApiImpl();
//	
//	 // 创建entry
//	// Integer intFlag =
//	 ldapApiImpl.createEntry("ou=people,dc=hadoop,dc=apache,dc=org",
//	 "bms_test411", "");
//	// System.out.println(intFlag);
//	
//	 // 删除entry
//	// Integer deleteflag =
////	 ldapApiImpl.deleteEntry("uid=bms_test6,ou=people,dc=hadoop,dc=apache,dc=org");
//	// System.out.println("deleteEntryFlag is " + deleteflag);
//	
//	 // 查询entry
//	// List<String> entryList =
////	 ldapApiImpl.queryLdap("ou=people,dc=hadoop,dc=apache,dc=org", filter);
//	// for (String entry : entryList) {
//	// System.out.println(entry);
//	// }
//	
//	// ldapApiImpl.queryLdap("ou=people,dc=hadoop,dc=apache,dc=org", filter);
//	 }

}
