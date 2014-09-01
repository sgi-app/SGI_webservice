package com.sgi.webservice;

public interface Constants {
	public static String DB_CLASS="com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static String DB_NAME="college";
	public static String DB_URL="jdbc:sqlserver://localhost;user=sa;password=1234;database=college";
	public static String DB_USER="sa";
	public static String DB_PASSWORD="1234";
	public static String DB_LOGIN_TABLE="students";
	public class Student{
		static String COLUMN_ID="std_id";
		static String COLUMN_NAME="std_name";
		static String COLUMN_PASSWORD="password";
		static String COLUMN_BRANCH="branch";
	}
}

