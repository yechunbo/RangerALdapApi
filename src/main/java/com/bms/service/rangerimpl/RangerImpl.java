package com.bms.service.rangerimpl;

import org.apache.commons.lang.StringUtils;
import org.apache.ranger.admin.client.datatype.RESTResponse;
import org.apache.ranger.plugin.model.RangerPolicy;
import org.apache.ranger.plugin.util.RangerRESTUtils;
import org.apache.ranger.plugin.util.ServicePolicies;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bms.entity.CreatePoliceReq;
import com.bms.entity.UpdatePoliceReq;
import com.bms.service.Ranger;
import com.bms.utils.PropertyUtil;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * @author YeChunBo
 * @time 2017年7月24日
 *
 *       类说明：Ranger rest api 操作实现类
 */

public class RangerImpl implements Ranger {

	private static Logger log = Logger.getLogger(RangerImpl.class);

	private static final String EXPECTED_MIME_TYPE = PropertyUtil.getProperty("expected_mime_type");
	private static String rangerBaseUrl = PropertyUtil.getProperty("rangerBaseUrl");
	private static String hiveService = PropertyUtil.getProperty("service"); // hive
																				// 的服务名
	private static String hdfsService = PropertyUtil.getProperty("hdfsService"); // hive
																					// 的服务名
	private static String hbaseService = PropertyUtil.getProperty("hbaseService"); // hive
																					// 的服务名
	private static String adminUser = PropertyUtil.getProperty("adminUser");
	private static String adminPwd = PropertyUtil.getProperty("adminPwd"); // ranger自己的登录密码（不是通过单点登录的密码）

	public String getAllValidPolice() {

		String url = rangerBaseUrl + "/service/plugins/policies/download/" + hiveService;

		log.info("getAllValidPolice, reqUrl=" + url);

		ClientResponse response = null;
		Client client = null;
		String allPolice = null;
		try {
			client = Client.create();
			WebResource webResource = client.resource(url)
					.queryParam(RangerRESTUtils.REST_PARAM_LAST_KNOWN_POLICY_VERSION, Long.toString(68));
			response = webResource.accept(RangerRESTUtils.REST_MIME_TYPE_JSON).get(ClientResponse.class);

			if (response != null && response.getStatus() == 200) {
				ServicePolicies ret = response.getEntity(ServicePolicies.class);

				Gson gson = new Gson();
				allPolice = gson.toJson(ret);
				log.info("getAllValidPolice is success , the resp=" + gson.toJson(ret));

			} else {
				RESTResponse resp = RESTResponse.fromClientResponse(response);
				log.warn("getAllValidPolice is fail," + resp.toString());
			}

		} catch (Exception e) {

			log.error("getAllValidPolice is fail, errMassge=" + e.getMessage());
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return allPolice;
	}

	public String getPolicyByName(String policyName, String policyType) {
		String url = null;
		if (policyType.equals("hdfs"))
			url = rangerBaseUrl + "/service/public/v2/api/service/" + hdfsService + "/policy/" + policyName;
		else if (policyType.equals("hbase"))
			url = rangerBaseUrl + "/service/public/v2/api/service/" + hbaseService + "/policy/" + policyName;
		else
			url = rangerBaseUrl + "/service/public/v2/api/service/" + hiveService + "/policy/" + policyName;
		log.info("getPolicyByName, reqUrl=" + url);

		Client client = null;
		ClientResponse response = null;
		String jsonString = null;

		try {
			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));
			WebResource webResource = client.resource(url);
			response = webResource.accept(EXPECTED_MIME_TYPE).get(ClientResponse.class);

			if (response.getStatus() == 200) {

				jsonString = response.getEntity(String.class);
				log.info("getPolicyByName is success, the response message is :" + jsonString);

			} else {

				RESTResponse resp = RESTResponse.fromClientResponse(response);
				jsonString = resp.toJson();
				log.warn("getPolicyByName is fail, the response message is :" + resp.toString());
			}

		} catch (Exception e) {

			RESTResponse resp = RESTResponse.fromClientResponse(response);
			jsonString = resp.toJson();
			log.error("getPolicyByName is fail, the error message is :" + e.getMessage()
					+ " and the response message is : " + jsonString);
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return jsonString;
	}

	public boolean createPolice(CreatePoliceReq req) {

		boolean flag = false;
		String url = rangerBaseUrl + "/service/public/v2/api/policy";

		log.info("CreatePolice of reqUrl=" + url);

		// 添加多个用户时将分割符逗号替换成下划线，用来生成新的策略名称
		String newPoliceUser = req.getPoliceUser();
		if (req.getPoliceUser().contains(",")) {
			newPoliceUser = req.getPoliceUser().replace(",", "_");
		}

		String PoliceName = newPoliceUser + "_police";

		ClientResponse response = null;
		Client client = null;

		try {

			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));
			WebResource webResource = client.resource(url);
			Gson gson = new Gson();

			RangerPolicy createOfPolicy = SupportRangerImpl.createOfPolicy(PoliceName, req.getPoliceUser(),
					req.getDbName(), req.getTableName(), req.getPermissionsType());

			response = webResource.accept(RangerRESTUtils.REST_EXPECTED_MIME_TYPE)
					.type(RangerRESTUtils.REST_EXPECTED_MIME_TYPE)
					.post(ClientResponse.class, gson.toJson(createOfPolicy));

			if (response != null && response.getStatus() == 200) {

				RangerPolicy rangerPolicy = response.getEntity(RangerPolicy.class);
				log.info("Create Police is success, the police message is=" + rangerPolicy);
				flag = true;

			} else {
				log.warn("Create Police is fail, the warn message is=" + response.toString());
			}

		} catch (Exception e) {

			log.error("Create Police is fail, the error message is=" + e.getMessage());
			flag = false;

		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return flag;
	}

	public boolean deletePoliceByPoliceName(String policeName) {

		boolean flag = false;
		String url = rangerBaseUrl + "/service/public/v2/api/policy?servicename=" + hiveService + "&policyname="
				+ policeName;
		log.info("DeletePoliceByPoliceName of requrl " + url);

		ClientResponse response = null;
		Client client = null;

		try {

			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));

			WebResource webResource = client.resource(url);
			webResource.accept(RangerRESTUtils.REST_EXPECTED_MIME_TYPE).delete();
			flag = true;
			log.info("DeletePoliceByPoliceName is success.");

		} catch (Exception e) {

			log.error("DeletePoliceByPoliceName is fail. the errMassage is " + e.getMessage());
			flag = false;
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return flag;
	}

	public boolean updatePolicyById(UpdatePoliceReq req) {

		boolean flag = false;

		String url = rangerBaseUrl + "/service/public/v2/api/policy/" + req.getPoliceId();
		log.info("UpdatePolicyById of reqUrl=" + url);

		String policyType = "hive";
		RangerPolicy rangerPolicy = SupportRangerImpl.updateOfPolicy(req.getPoliceName(), req.getDbName(),
				req.getPermissionsType(), req.getPoliceUser(), req.getColPermissionsType(), req.getPoliceIsEnabled(),
				req.getTableName(), policyType);

		ClientResponse response = null;
		Client client = null;

		try {
			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));

			WebResource webResource = client.resource(url);

			Gson gson = new Gson();

			response = webResource.accept(RangerRESTUtils.REST_EXPECTED_MIME_TYPE)
					.type(RangerRESTUtils.REST_EXPECTED_MIME_TYPE).put(ClientResponse.class, gson.toJson(rangerPolicy));

			if (response != null && response.getStatus() == 200) {

				RangerPolicy policy = response.getEntity(RangerPolicy.class);
				flag = true;
				log.info("UpdatePolicyById is success, the police message is=" + policy);

			} else {
				log.warn("UpdatePolicyById is fail, the fail message is=" + response.toString());
			}

		} catch (Exception e) {

			log.error("UpdatePolicyById is fail, the error message is=" + e.getMessage());
			flag = false;
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return flag;
	}

	/**
	 * 通过策略名修改策略
	 * 
	 * @param req
	 * @param policyType
	 *            策略类型，分别为 hive/hbase/hdfs
	 * @return
	 */
	public boolean updatePolicyByName(UpdatePoliceReq req, String policyType) {

		boolean flag = false;

		String resourceName = null;
		String service = null;
		if ("hdfs".equalsIgnoreCase(policyType)) {
			resourceName = req.getHdfsResourcePath();
			service = hdfsService;
		} else if ("hbase".equalsIgnoreCase(policyType)) {
			resourceName = req.getHbaseTableName();
			service = hbaseService;
		} else {// 默认为hive
			resourceName = req.getDbName();
			service = hiveService;
		}

		String url = rangerBaseUrl + "/service/public/v2/api/service/" + service + "/policy/"
				+ req.getPoliceName();

		log.info("UpdatePolicyById of reqUrl=" + url);
		
		RangerPolicy rangerPolicy = SupportRangerImpl.updateOfPolicy(req.getPoliceName(), resourceName, req.getPermissionsType(),
				req.getPoliceUser(), req.getColPermissionsType(), req.getPoliceIsEnabled(), req.getTableName(),
				policyType);

		ClientResponse response = null;
		Client client = null;

		try {
			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));

			WebResource webResource = client.resource(url);

			Gson gson = new Gson();

			response = webResource.accept(RangerRESTUtils.REST_EXPECTED_MIME_TYPE)
					.type(RangerRESTUtils.REST_EXPECTED_MIME_TYPE).put(ClientResponse.class, gson.toJson(rangerPolicy));

			if (response != null && response.getStatus() == 200) {

				RangerPolicy policy = response.getEntity(RangerPolicy.class);
				flag = true;
				log.info("UpdatePolicyByName is success, the police message is=" + policy);

			} else {
				log.warn("UpdatePolicyByName is fail, the fail message is=" + response.toString());
			}

		} catch (Exception e) {

			log.error("UpdatePolicyByName is fail, the error message is=" + e.getMessage());
			flag = false;
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return flag;
	}

	public boolean deletePoliceByPoliceId(String policeId) {

		boolean flag = false;
		String url = rangerBaseUrl + "/service/public/v2/api/policy/" + policeId;
		log.info("DeletePoliceByPoliceId of reqUrl=" + url);

		ClientResponse response = null;
		Client client = null;

		try {

			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));

			WebResource webResource = client.resource(url);

			webResource.accept(RangerRESTUtils.REST_EXPECTED_MIME_TYPE).delete();
			flag = true;

		} catch (Exception e) {

			log.error("DeletePoliceByPoliceId is fail, the error Massage is=" + e.getMessage());
			flag = false;
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return flag;
	}

	/**
	 * 这里的删除只是把用户设为不可见，不可见之后在配置策略时，这个用户就变成不可选，但是原先这个用户所拥有的策略还是存在的。真正删除这个用户后，
	 * 其所拥有的策略才不存在。
	 * 
	 * @param UserName
	 * @return
	 */
	public boolean deleteUserByUserName(String UserName) {

		boolean flag = false;
		String url = rangerBaseUrl + "/service/xusers/users/userName/" + UserName;
		// service/xusers/secure/users/delete?forceDelete=true&
		log.info("deleteUserByUserName of reqUrl=" + url);

		ClientResponse response = null;
		Client client = null;

		try {

			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));

			WebResource webResource = client.resource(url);

			webResource.accept(RangerRESTUtils.REST_EXPECTED_MIME_TYPE).delete();
			flag = true;

		} catch (Exception e) {

			log.error("DeletePoliceByPoliceId is fail, the error Massage is=" + e.getMessage());
			flag = false;
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return flag;
	}

	/**
	 * 获取用户访问记录
	 * 
	 * @param userName
	 * @return
	 */
	public String getUserVisitInfo(String userName, String startDate) {

		String url = null;
		if (StringUtils.isBlank(startDate)) {
			url = rangerBaseUrl + "/service/assets/accessAudit?requestUser=" + userName;
		} else {
			url = rangerBaseUrl + "/service/assets/accessAudit?requestUser=" + userName + "&startDate=" + startDate;
		}

		log.info("getUserVisitInfo, reqUrl=" + url);

		Client client = null;
		ClientResponse response = null;
		String jsonString = null;

		try {
			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, adminPwd));
			WebResource webResource = client.resource(url);
			response = webResource.accept(EXPECTED_MIME_TYPE).get(ClientResponse.class);

			if (response.getStatus() == 200) {
				jsonString = response.getEntity(String.class);
				// log.info("getUserVisitInfo is success, the response message
				// is :" + jsonString);
			} else {
				RESTResponse resp = RESTResponse.fromClientResponse(response);
				jsonString = resp.toJson();
				log.warn("getUserVisitInfo is fail, the response message is :" + resp.toString());
			}

		} catch (Exception e) {
			RESTResponse resp = RESTResponse.fromClientResponse(response);
			jsonString = resp.toJson();
			log.error("getUserVisitInfo is fail, the error message is :" + e.getMessage()
					+ " and the response message is : " + jsonString);
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}
		return jsonString;
	}

	/**
	 * 获取集群CPU使用情况
	 * 
	 * @return
	 */
	public String getClusterCPUInfo() {

		String url = PropertyUtil.getProperty("getCPUInfoUrl") + System.currentTimeMillis();

		log.info("getClusterCPUInfo, reqUrl=" + url);

		Client client = null;
		ClientResponse response = null;
		String jsonString = null;

		try {
			client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(adminUser, PropertyUtil.getProperty("ldapPassword")));
			WebResource webResource = client.resource(url);
			response = webResource.accept("text/plain").get(ClientResponse.class);

			if (response.getStatus() == 200) {
				jsonString = response.getEntity(String.class);
				JSONObject jo = new JSONObject(jsonString);
				JSONObject metrics = jo.getJSONObject("metrics");
				jsonString = metrics.getJSONObject("cpu").get("cpu_idle").toString();

			} else {
				RESTResponse resp = RESTResponse.fromClientResponse(response);
				jsonString = resp.toJson();
				log.warn("getUserVisitInfo is fail, the response message is :" + resp.toString());
			}

		} catch (Exception e) {
			RESTResponse resp = RESTResponse.fromClientResponse(response);
			jsonString = resp.toJson();
			log.error("getUserVisitInfo is fail, the error message is :" + e.getMessage()
					+ " and the response message is : " + jsonString);
		} finally {

			if (response != null) {
				response.close();
			}

			if (client != null) {
				client.destroy();
			}
		}

		return jsonString;

	}

	/**
	 * 获取待更新策略原先存在的策略并封装成java对象
	 * 
	 * @param jsonStr
	 * @param policyType
	 *            策略类型
	 * @return
	 */
	public static UpdatePoliceReq transJsonToObject(String jsonStr, String policyType) {

		UpdatePoliceReq req = new UpdatePoliceReq();
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray ja = jo.getJSONArray("policyItems");// 获取权限相关的

		req.setPoliceId(jo.get("id").toString());
		req.setPoliceName(jo.get("name").toString());
		req.setService(jo.get("service").toString());

		Object resObj = jo.get("resources"); // 获取资源相关的
		StringBuffer dbName = new StringBuffer();
		StringBuffer tableName = new StringBuffer();
		StringBuffer policeUser = new StringBuffer();
		StringBuffer permissionsType = new StringBuffer();

		StringBuffer hdfsPathName = new StringBuffer(); // hdfs 对应的可访问路径
		StringBuffer hbaseTableName = new StringBuffer(); // hbase 对应的可访问路径

		if ("hive".equals(policyType)) { // 对hive 类型策略进行封装
			if (resObj != null) {
				String dbValues = ((JSONObject) resObj).optString("database");
				JSONArray jsonArray = new JSONObject(dbValues).getJSONArray("values");

				for (int i = 0; i < jsonArray.length(); i++) { // 获取该策略所拥有的数据库权限
					dbName.append(jsonArray.getString(i)).append(",");
				}
				req.setDbName(dbName.toString().substring(0, dbName.lastIndexOf(",")));// 设置所拥有的数据库权限

				String tableValues = ((JSONObject) resObj).optString("table");
				JSONArray tableJsonArray = new JSONObject(tableValues).getJSONArray("values");

				for (int i = 0; i < tableJsonArray.length(); i++) { // 获取该策略所拥有的数据库权限
					tableName.append(tableJsonArray.getString(i)).append(",");
				}

				req.setTableName(tableName.toString().substring(0, tableName.lastIndexOf(",")));// 设置所拥有的数据库权限
			}
		}

		if ("hdfs".equals(policyType)) { // 对hdfs 类型策略进行封装
			if (resObj != null) {
				String dbValues = ((JSONObject) resObj).optString("path");
				JSONArray pathArray = new JSONObject(dbValues).getJSONArray("values");

				for (int i = 0; i < pathArray.length(); i++) { // 获取该策略所拥有的数据库权限
					hdfsPathName.append(pathArray.getString(i)).append(",");
				}
				req.setHdfsResourcePath(hdfsPathName.toString().substring(0, hdfsPathName.lastIndexOf(",")));// 设置所拥有的访问hdfs路径权限

			}
		}

		if ("hbase".equals(policyType)) { // 对hbase 类型策略进行封装
			if (resObj != null) {
				String dbValues = ((JSONObject) resObj).optString("table");
				JSONArray jsonArray = new JSONObject(dbValues).getJSONArray("values");

				for (int i = 0; i < jsonArray.length(); i++) { // 获取该策略所拥有的数据库权限
					hbaseTableName.append(jsonArray.getString(i)).append(",");
				}
				req.setHbaseTableName(hbaseTableName.toString().substring(0, hbaseTableName.lastIndexOf(",")));// 设置所拥有的数据表的权限
			}
		}

		if (ja != null) {
			JSONArray jsonUsersArray = ja.getJSONObject(0).getJSONArray("users");// 获取该策略所包括的用户
			for (int i = 0; i < jsonUsersArray.length(); i++) {
				policeUser.append(jsonUsersArray.getString(i)).append(",");
			}

			req.setPoliceUser(policeUser.toString().substring(0, policeUser.lastIndexOf(",")));// 设置所拥有的用户

			JSONArray permissionsTypeArr = ja.getJSONObject(0).getJSONArray("accesses");

			for (int i = 0; i < permissionsTypeArr.length(); i++) {// 获取该策略所包括的权限
				permissionsType.append(permissionsTypeArr.getJSONObject(i).get("type")).append(",");
			}
			req.setPermissionsType(permissionsType.toString().substring(0, permissionsType.lastIndexOf(",")));
		}
		return req;
	}

	public static void main(String[] args) {
		RangerImpl rangerImpl = new RangerImpl();
		UpdatePoliceReq hive = transJsonToObject(rangerImpl.getPolicyByName("12", "hive"), "hive");
		UpdatePoliceReq hbase = transJsonToObject(rangerImpl.getPolicyByName("testHbase", "hbase"), "hbase");
		UpdatePoliceReq hdfs = transJsonToObject(rangerImpl.getPolicyByName("bmsoft_test", "hdfs"), "hdfs");

		System.out.println(hive.toString());
		System.out.println("-------hive---------");
		System.out.println(hbase.toString());
		System.out.println("-------hbase--------");
		System.out.println(hdfs.toString());
		System.out.println("-------hdfs---------");

//		hive.setDbName("test,hivetest");
//		hive.setPermissionsType("select,update");
//		rangerImpl.updatePolicyByName(hive, "hive");

		 hbase.setPermissionsType("read,admin");
		 hbase.setPoliceUser("hive,admin");
		 rangerImpl.updatePolicyByName(hbase, "hbase");

		// hdfs.setPoliceUser("admin,hive");
		// hdfs.setPermissionsType("read");
		// rangerImpl.updatePolicyByName(hdfs, "hdfs");
		// 获取所有有效的策略
		// String allValidPolice = rangerImpl.getAllValidPolice();
		// System.out.println("system out: " + allValidPolice);

		// // 根据策略名获取
		// String response = rangerImpl.getPolicyByName("12");
		// System.out.println(response);
		//
		// UpdatePoliceReq oldPolice = transJsonToObject(response);
		// System.out.println(oldPolice.toString());
		// 删除策略（根据策略名称）
		// boolean flag =
		// rangerImpl.deletePoliceByPoliceName("bmsoft_test_police");
		// System.out.println(flag);

		// 删除策略（根据策略ID）
		// boolean flag = rangerImpl.deletePoliceByPoliceId("28");
		// System.out.println(flag);

		// 创建策略
		// CreatePoliceReq createPoliceReq = new CreatePoliceReq();
		// createPoliceReq.setPoliceUser("hive,hbase");
		// createPoliceReq.setDbName("test1");
		// //createPoliceReq.setTableName("test2");
		// createPoliceReq.setPermissionsType("select,update");
		// boolean createPoliceFlag = rangerImpl.createPolice(createPoliceReq);
		// System.out.println(createPoliceFlag);

		// 更新策略
		// UpdatePoliceReq updatePoliceReq = new UpdatePoliceReq();
		// updatePoliceReq.setPoliceName("12tUpdate13");
		// updatePoliceReq.setPoliceId("36");
		// updatePoliceReq.setDbName("test1");
		// updatePoliceReq.setTableName("test,test2");
		// updatePoliceReq.setPoliceUser("hive,hbase");
		// updatePoliceReq.setPermissionsType("update");
		// updatePoliceReq.setPoliceIsEnabled("0");
		// boolean flag = rangerImpl.updatePolicyById(updatePoliceReq);
		// System.out.println(flag);

		// 更新策略
		// UpdatePoliceReq updatePoliceReq = new UpdatePoliceReq();
		// updatePoliceReq.setPoliceName("12");
		//// updatePoliceReq.setPoliceId("36");
		// updatePoliceReq.setDbName("test1");
		// updatePoliceReq.setTableName("test,test2");
		// updatePoliceReq.setPoliceUser("hive,hbase");
		// updatePoliceReq.setPermissionsType("select,update");
		// updatePoliceReq.setPoliceIsEnabled("1");
		// boolean flag = rangerImpl.updatePolicyByName(updatePoliceReq);
		// System.out.println(flag);

		// boolean deleteFlag =
		// rangerImpl.deleteUserByUserName("hive_hdfs_police");
		// System.out.println(deleteFlag);

		// rangerImpl.getClusterCPUInfo();
	}

}
