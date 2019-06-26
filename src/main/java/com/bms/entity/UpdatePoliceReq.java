package com.bms.entity;

/**
 * @author YeChunBo
 * @time 2017年7月25日
 *
 *   类说明
	 * 更新策略
	 * policeName
	 * id
	 * dbName
	 * tableName 默认拥有所有表
	 * permissionsType 表的操作权限，eg:select,update...
	 * policeUser
	 * colPermissionsType 列的操作权限，默认拥有所有权限
	 * policeIsEnabled 该策略是否有效，默认有效：1 有效，0 无效
 */

public class UpdatePoliceReq {
	
	private String policeName;
	private String policeId;
	private String dbName;
	private String tableName = "*";
	private String permissionsType;
	private String policeUser;
	private String colPermissionsType = "*";
	private String policeIsEnabled = "1";
	private String service;
	// hfds 相关属性
	private String hdfsResourcePath;

	// hbase 相关属性
	private String hbaseTableName;
	
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getHdfsResourcePath() {
		return hdfsResourcePath;
	}

	public void setHdfsResourcePath(String hdfsResourcePath) {
		this.hdfsResourcePath = hdfsResourcePath;
	}

	public String getPoliceName() {
		return policeName;
	}

	public void setPoliceName(String policeName) {
		this.policeName = policeName;
	}

	public String getPoliceId() {
		return policeId;
	}

	public void setPoliceId(String policeId) {
		this.policeId = policeId;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPermissionsType() {
		return permissionsType;
	}

	public void setPermissionsType(String permissionsType) {
		this.permissionsType = permissionsType;
	}

	public String getPoliceUser() {
		return policeUser;
	}

	public void setPoliceUser(String policeUser) {
		this.policeUser = policeUser;
	}

	public String getColPermissionsType() {
		return colPermissionsType;
	}

	public void setColPermissionsType(String colPermissionsType) {
		this.colPermissionsType = colPermissionsType;
	}

	public String getPoliceIsEnabled() {
		return policeIsEnabled;
	}

	public void setPoliceIsEnabled(String policeIsEnabled) {
		this.policeIsEnabled = policeIsEnabled;
	}

	public String getHbaseTableName() {
		return hbaseTableName;
	}

	public void setHbaseTableName(String hbaseTableName) {
		this.hbaseTableName = hbaseTableName;
	}

	@Override
	public String toString() {
		return "UpdatePoliceReq [policeName=" + policeName + ", policeId=" + policeId + ", dbName=" + dbName
				+ ", tableName=" + tableName + ", permissionsType=" + permissionsType + ", policeUser=" + policeUser
				+ ", colPermissionsType=" + colPermissionsType + ", policeIsEnabled=" + policeIsEnabled + ", service="
				+ service + ", hdfsResourcePath=" + hdfsResourcePath + ", hbaseTableName=" + hbaseTableName + "]";
	}

}
