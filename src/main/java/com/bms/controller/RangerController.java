package com.bms.controller;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bms.entity.CreatePoliceReq;
import com.bms.entity.UpdatePoliceReq;
import com.bms.service.rangerimpl.RangerImpl;
import com.bms.service.rangerimpl.SupportRangerImpl;

/**
 * @author YeChunBo
 * @time 2017年8月2日
 *
 *       类说明
 */

@SpringBootApplication
@RestController
public class RangerController {

	RangerImpl rangerImpl = new RangerImpl();
	private static Logger log = Logger.getLogger(RangerController.class);

	/**
	 * 查询所有有效的策略
	 * 
	 * @return
	 */
	@RequestMapping("/query_valid_police")
	@ResponseBody
	String queryValidPolice() {
		log.info("QueryValidPolice......");
		return rangerImpl.getAllValidPolice();
	}

	/**
	 * 创建策略
	 * 
	 * @param policyUser：用户，必填，多个用户之间用逗号分割（英文符）
	 * @param dbname：数据库，必，多个用逗号分割
	 * @param tablename：表，非，多个用户用户英文符逗号分割开,
	 *            eg:hive_test,hbase_test
	 * @param permissionstype：对表的操作权限，非，默认为
	 *            all, eg:select,update,drop...
	 * @return
	 */
	@RequestMapping("/create_policy")
	@ResponseBody
	Boolean createPolice(@RequestParam(value = "policyuser", required = true) String policyUser,
			@RequestParam(value = "dbname", required = true) String dbname,
			@RequestParam(value = "tablename", required = false, defaultValue = "*") String tablename,
			@RequestParam(value = "permissionstype", required = false, defaultValue = "all") String permissionstype) {

		CreatePoliceReq req = new CreatePoliceReq();

		req.setPoliceUser(policyUser);
		req.setDbName(dbname);
		req.setTableName(tablename);
		req.setPermissionsType(permissionstype);
		log.info("CreatePolice of req=" + req.toString());
		return rangerImpl.createPolice(req);
	}

	/**
	 * 更新策略：注，这里的更新会将原先设置的对应属性给覆盖掉，比如，policyUser传为hive。原先如果policyUser为hbase,hdfs
	 * ,当修改完后，这个策略的用户便只有hive。其他属性同理。
	 * 
	 * @param policyid
	 * @param policyUser
	 * @param policyname
	 * @param dbname
	 * @param tablename
	 * @param policeIsEnabled：策略可用性，0：无效，1：有效
	 * @param permissionsType
	 * @return
	 */
	@RequestMapping("/update_policy_by_id")
	@ResponseBody
	Boolean updatePolicyById(@RequestParam(value = "policyId", required = true) String policyId,
			@RequestParam(value = "policyUser", required = true) String policyUser,
			@RequestParam(value = "policyName", required = true) String policyName,
			@RequestParam(value = "dbName", required = true) String dbName,
			@RequestParam(value = "tableName", required = false, defaultValue = "*") String tableName,
			@RequestParam(value = "policeIsEnabled", required = false, defaultValue = "1") String policeIsEnabled,
			@RequestParam(value = "permissionstype", required = false, defaultValue = "all") String permissionsType) {

		UpdatePoliceReq updatePoliceReq = new UpdatePoliceReq();

		updatePoliceReq.setPoliceId(policyId);

		if (StringUtils.isNotBlank(dbName))
			updatePoliceReq.setDbName(dbName);

		if (StringUtils.isNotBlank(tableName))
			updatePoliceReq.setTableName(tableName);

		if (StringUtils.isNotBlank(policyName))
			updatePoliceReq.setPoliceName(policyName);

		if (StringUtils.isNotBlank(policyUser))
			updatePoliceReq.setPoliceUser(policyUser);

		if (StringUtils.isNotBlank(permissionsType))
			updatePoliceReq.setPermissionsType(permissionsType);

		if (StringUtils.isNotBlank(policeIsEnabled))
			updatePoliceReq.setPoliceIsEnabled(policeIsEnabled);

		log.info("UpdatePolicyById of req=" + updatePoliceReq.toString());
		return rangerImpl.updatePolicyById(updatePoliceReq);
	}

	/**
	 * 更新策略：注，这里的策略如果没有传则保存原先的，会先根据策略名查出该策略的属性，当发现用户请求中也传入该属性慢更新该属性
	 * 
	 * @param policyName
	 * @param policyUser
	 * @param resourceName
	 * @param tableName
	 * @param permissionsType
	 * @param policyType
	 * @param quartFlag
	 * @return
	 */
	@RequestMapping("/update_policy_by_name")
	@ResponseBody
	Boolean updatePolicyByName(@RequestParam(value = "policyName", required = true) String policyName,
			@RequestParam(value = "policyUser", required = false) String policyUser,
			@RequestParam(value = "resourceName", required = false) String resourceName,
			@RequestParam(value = "tableName", required = false) String tableName,
			@RequestParam(value = "permissionsType", required = false) String permissionsType,
			@RequestParam(value = "policyType", required = false, defaultValue = "hive") String policyType,
			@RequestParam(value = "quartFlag", required = false, defaultValue = "1") String quartFlag) {

		String policyJsonStr = getPolicyByName(policyName, policyType);

		// 获取待更新策略原先存在的策略并封装成java对象
		UpdatePoliceReq oldPoliceReq = RangerImpl.transJsonToObject(policyJsonStr, policyType);

		UpdatePoliceReq updatePoliceReq = new UpdatePoliceReq();
		updatePoliceReq.setPoliceName(policyName);

		// 如果是定时任务调用该方法，则quartFlag 参数传非1
		if (!"1".equals(quartFlag)) {
			// 为了防止定时任务多次修改策略，所以当用户磁盘超过配置的使用量后查询出来的策略如果没有写的权限则不进行修改策略的操作
			if (!(oldPoliceReq.getPermissionsType().contains("create")
					|| oldPoliceReq.getPermissionsType().contains("write"))) {
				return true;
			} else {
				SupportRangerImpl.handlePermissionsType(oldPoliceReq, updatePoliceReq, permissionsType);
			}
		} else {
			// 当做外部接口调用
			if (StringUtils.isNotBlank(permissionsType))
				updatePoliceReq.setPermissionsType(permissionsType);
			else {
				updatePoliceReq.setPermissionsType(oldPoliceReq.getPermissionsType());
			}
		}

		if (StringUtils.isNotBlank(resourceName)) {
			if ("hdfs".equalsIgnoreCase(policyType)) {
				updatePoliceReq.setHdfsResourcePath(resourceName);
			} else if ("hbase".equalsIgnoreCase(policyType)) {
				updatePoliceReq.setHbaseTableName(resourceName);
			} else {
				updatePoliceReq.setDbName(resourceName);
			}
		} else {
			if ("hdfs".equalsIgnoreCase(policyType)) {
				updatePoliceReq.setHdfsResourcePath(oldPoliceReq.getHdfsResourcePath());
			} else if ("hbase".equalsIgnoreCase(policyType)) {
				updatePoliceReq.setHbaseTableName(oldPoliceReq.getHbaseTableName());
			} else {
				updatePoliceReq.setDbName(oldPoliceReq.getDbName());
			}
		}

		if (StringUtils.isNotBlank(tableName))
			updatePoliceReq.setTableName(tableName);
		else
			updatePoliceReq.setTableName(oldPoliceReq.getTableName());

		if (StringUtils.isNotBlank(policyUser))
			updatePoliceReq.setPoliceUser(policyUser);
		else
			updatePoliceReq.setPoliceUser(oldPoliceReq.getPoliceUser());

		log.info("UpdatePolicyByName of req=" + updatePoliceReq.toString());
		return rangerImpl.updatePolicyByName(updatePoliceReq, policyType);
	}

	/**
	 * 通过策略名取得该策略，返回的是该策略所有属性，json格式
	 * 
	 * @param policyName
	 * @return
	 */
	@RequestMapping("/get_policy_by_name")
	@ResponseBody
	String getPolicyByName(@RequestParam(value = "policyName", required = true) String policyName,
			@RequestParam(value = "policyType", required = false, defaultValue = "hive") String policyType) {

		log.info("GetPolicyByName of policyName=" + policyName);

		return rangerImpl.getPolicyByName(policyName, policyType);
	}

	/**
	 * 通过策略id 删除策略
	 * 
	 * @param policeId
	 * @return
	 */
	@RequestMapping("/delete_police_by_police_id")
	@ResponseBody
	Boolean deletePoliceByPoliceId(@RequestParam(value = "policeId", required = true) String policeId) {

		log.info("DeletePoliceByPoliceId of policeId=" + policeId);

		return rangerImpl.deletePoliceByPoliceId(policeId);
	}

	/**
	 * 通过策略名 删除策略
	 * 
	 * @param policeName
	 * @return
	 */
	@RequestMapping("/delete_police_by_police_name")
	@ResponseBody
	Boolean deletePoliceByPoliceName(@RequestParam(value = "policeName", required = true) String policeName) {
		log.info("DeletePoliceByPoliceName of policeName=" + policeName);
		return rangerImpl.deletePoliceByPoliceName(policeName);
	}

	/**
	 * 通过用户与起始时间获取其对数据库访问的记录数
	 * 
	 * @param userName
	 * @param startDate
	 *            起始时间，MM/dd/YYYY 08/29/2017
	 * @return
	 */
	@RequestMapping("/get_user_visit_info")
	@ResponseBody
	String getUserVisitInfo(@RequestParam(value = "userName", required = true) String userName,
			@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate) {
		log.info("GetUserVisitInfo of userName=" + userName);

		return rangerImpl.getUserVisitInfo(userName, startDate);
	}

	/**
	 * 获取集群CPU使用情况，返回CPU空闲值（百分数）
	 * 
	 * @return
	 */
	@RequestMapping("/get_cluster_cpu_info")
	@ResponseBody
	String getClusterCPUInfo() {
		log.info("GetClusterCPUInfo ......");
		return rangerImpl.getClusterCPUInfo();
	}

}
