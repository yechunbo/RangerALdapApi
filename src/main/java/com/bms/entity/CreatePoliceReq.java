package com.bms.entity;

/**
 * @author YeChunBo
 * @time 2017年7月25日
 *
 * 类说明
 * 
	 * 创建策略
	 * policeUser 策略对应的用户
	 * dbName :数据库，多个数据库用逗号分割
	 * tableName ：表，多个用逗号分割, 默认拥有操作对应数据库中所有表
	 * permissionsType 表，所对应的权限，多个用逗号分割, 默认为拥有所有权限, 值为 all
	 * colPermissionsType = "*"; // 默认是所有列都可以访问
 */

public class CreatePoliceReq {

	private String policeUser;
	private String dbName;
	private String tableName = "*";
	private String permissionsType = "all";
	private String colPermissionsType = "*"; // 默认是所有列都可以访问

	public String getPoliceUser() {
		return policeUser;
	}

	public void setPoliceUser(String policeUser) {
		this.policeUser = policeUser;
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

	public String getColPermissionsType() {
		return colPermissionsType;
	}

	public void setColPermissionsType(String colPermissionsType) {
		this.colPermissionsType = colPermissionsType;
	}

	@Override
	public String toString() {
		return "CreatePoliceReq [policeUser=" + policeUser + ", dbName=" + dbName + ", tableName=" + tableName
				+ ", permissionsType=" + permissionsType + ", colPermissionsType=" + colPermissionsType + "]";
	}
	
}
