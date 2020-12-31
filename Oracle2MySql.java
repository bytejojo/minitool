package com.murong.ecp.ams.portal.merchant.web.action.meroprlogin;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;


public class Oracle2MySql {

	static String username = "mysql";
	static FileWriter fileWriter;
	public static void main(String[] args) throws Exception {


		fileWriter = new FileWriter(username + ".sql");
		Connection connection = getConnection("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@//192.168.0.1:1521/orcl",username, "mysql");

		DatabaseMetaData databaseMetaData = connection.getMetaData();
		String[] types = {"TABLE"};
		ResultSet rs = databaseMetaData.getTables(null, username, null, types );

		while( rs.next() ) {
			String tableName = rs.getString("TABLE_NAME");
			String remarks = rs.getString("REMARKS");

			TableEntity tableEntity = new TableEntity();
			tableEntity.name = tableName;
			tableEntity.remarks = remarks;
			processTableName(databaseMetaData, tableEntity);

		}
		rs.close();
		connection.close();
		fileWriter.close();
	}

	static class TableEntity {
		String name;
		String remarks;
	}

	public static void processTableName(DatabaseMetaData databaseMetaData, TableEntity tableEntity) throws SQLException, IOException {
		System.out.println("process table:" + tableEntity.name);
		StringBuffer buf = new StringBuffer();
		buf.append("\n");
		buf.append("drop table if exists " + tableEntity.name + "; \n");
		buf.append("create table " + tableEntity.name + " ( \n");

		ResultSet rs = databaseMetaData.getColumns(null, username, tableEntity.name, null);
		while( rs.next() ) {
			String columnName = rs.getString("COLUMN_NAME");
			String typeName = rs.getString("TYPE_NAME");
			int columnSize = NumberUtils.toInt(rs.getString("COLUMN_SIZE"));
			int decimalDigits = NumberUtils.toInt(rs.getString("DECIMAL_DIGITS"));
			String remarks = rs.getString("REMARKS");
			String nullable = rs.getString("NULLABLE");
			String columnDef = rs.getString("COLUMN_DEF");

			String columnInf = "";

			if( "VARCHAR2".equals(typeName)) {
				typeName = "VARCHAR";
			}

			if( decimalDigits <= 0 ) {
				decimalDigits = 0;
			}
			if( "NUMBER".equals(typeName)) {
				if( decimalDigits == 0  ) {
					typeName = "BIGINT";
					columnSize = 0;
				} else {
					typeName = "DECIMAL";
				}
			}

			if( "NVARCHAR2".equals(typeName)) {
				typeName = "VARCHAR";
			}

			if( "NCLOB".equals(typeName) ) {
				typeName = "VARCHAR";
			}

			if( "CLOB".equals(typeName) ) {
				typeName = "VARCHAR";
			}

			if( columnDef == null ) {
				if( "VARCHAR".equals(typeName)) {
					columnDef = "' '";
				}
			}
			if( columnSize == 0 ) {
				columnInf = String.format("%-20s %-15s", ("`"+columnName+"`"), typeName);
			} else if( columnSize != 0  ){
				if( decimalDigits == 0 ) {
					columnInf = String.format("%-20s %-15s", ("`"+columnName+"`"), (typeName + "(" + columnSize + ")"));
				} else {
					columnInf = String.format("%-20s %-15s", ("`"+columnName+"`"), (typeName + "(" + columnSize + "," + decimalDigits + ")"));
				}
			}

			if( columnDef != null ) {
				columnInf += String.format(" %-15s", ("DEFAULT " + StringUtils.strip(columnDef)));
			} else {
				columnInf += String.format(" %-15s", " ");
			}

			if( !"NULL".equals(nullable)) {
				columnInf +=  String.format(" %-8s", "NOT NULL");
			} else {
				columnInf +=  String.format(" %-8s", " ");
			}

			if( remarks != null ) {
				columnInf += " COMMENT '" + remarks + "'";
			}

			buf.append("   " + columnInf + ",\n");

//			System.out.println(columnName + ":" + typeName + ":" + columnSize + ":" + decimalDigits + ":" + remarks + ":" + nullable + ":" + columnDef);
		}
		rs.close();

		rs = databaseMetaData.getPrimaryKeys(null, username, tableEntity.name);

		TreeMap map = new TreeMap();
		while( rs.next() ) {
			String columnName = rs.getString("COLUMN_NAME");
			int keySeq = NumberUtils.toInt(rs.getString("KEY_SEQ"));
			map.put(keySeq, columnName);
		}
		rs.close();

		if( !map.isEmpty() ) {
			StringBuffer primaryKeyInf = new StringBuffer();
			primaryKeyInf.append("PRIMARY KEY(");

			for (Object key : map.keySet()) {
				primaryKeyInf.append(map.get(key));
				primaryKeyInf.append(",");
			}
			primaryKeyInf.deleteCharAt(primaryKeyInf.length() - 1);
			primaryKeyInf.append(")");
			buf.append("   " + primaryKeyInf + "\n");
		} else {
			buf.delete(buf.length() - 2, buf.length());
			buf.append("\n");
		}

		if( tableEntity.remarks != null ) {
			buf.append(") COMMENT '" + tableEntity.remarks + "';\n");
		} else {
			buf.append(");\n");
		}
		fileWriter.write(buf.toString());


		rs = databaseMetaData.getIndexInfo(null, username, tableEntity.name, false, false);

		Map<String, IndexEntity> indexes = new HashMap<>();
		while( rs.next() ) {
			String indexName = rs.getString("INDEX_NAME");
			if( indexName == null ) {
				continue;
			}
			if( indexName.startsWith("PK_") || indexName.startsWith("SYS_") ) {
				continue;
			}
			IndexEntity indexEntity = null;
			if( indexes.containsKey(indexName) ) {
				indexEntity = indexes.get(indexName);
			} else {
				indexEntity = new IndexEntity();
				indexEntity.name = indexName;
				indexes.put(indexName, indexEntity);
			}

			int ordinalPosition = rs.getInt("ORDINAL_POSITION");
			String columnName = rs.getString("COLUMN_NAME");
			boolean nonUnique = rs.getBoolean("NON_UNIQUE");

			indexEntity.nonUnique = nonUnique;
			String ascOrDesc = rs.getString("ASC_OR_DESC");
			ColumnEntity columnEntity = new ColumnEntity();
			columnEntity.ascOrDesc = ascOrDesc;
			columnEntity.name = columnName;
			indexEntity.columnEntitys.put(ordinalPosition, columnEntity);
		}
		rs.close();


		for(String key : indexes.keySet() ) {
			StringBuffer buf1 = new StringBuffer();
			IndexEntity indexEntity = indexes.get(key);
			if( !indexEntity.nonUnique ) {
				buf1.append("create unique index ");
			} else {
				buf1.append("create index ");
			}
			buf1.append(indexEntity.name + " on " + tableEntity.name + "(");

			for( Object key1 : indexEntity.columnEntitys.keySet() ) {
				ColumnEntity columnEntity = indexEntity.columnEntitys.get(key1);
				buf1.append(columnEntity.name);
				if( columnEntity.ascOrDesc != null ) {
					buf1.append(" ");
					buf1.append(columnEntity.ascOrDesc);
				}
				buf1.append(",");
			}
			buf1.deleteCharAt(buf1.length() - 1);
			buf1.append(");\n");
			fileWriter.write(buf1.toString());
		}

		fileWriter.flush();
	}

	static class IndexEntity {
		String name;
		boolean nonUnique;
		TreeMap<Integer, ColumnEntity> columnEntitys = new TreeMap<Integer, ColumnEntity>();
	}

	static class ColumnEntity {
		String name;
		String ascOrDesc;
	}

	public static Connection getConnection(String driverClass, String url, String username, String password) throws SQLException, ClassNotFoundException {
		Class.forName(driverClass);

		Properties props = new Properties();
		props.setProperty("user", username);
		props.setProperty("password", password);
		props.setProperty("remarks", "true");

		return DriverManager.getConnection(url, props);
	}
}
