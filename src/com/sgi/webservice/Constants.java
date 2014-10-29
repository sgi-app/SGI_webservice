package com.sgi.webservice;

public interface Constants {
	public static String DB_CLASS="com.mysql.jdbc.Driver";
	public static String DB_NAME="sgi_app";	
	public static String DB_USER="root";
	public static String DB_PASSWORD="1234";
	public static String DB_LOGIN_TABLE="login";
	public static String DB_URL="jdbc:mysql://localhost/"+DB_NAME+"?user="+DB_USER+"&password="+DB_PASSWORD;
	public class login{
		static String COLUMN_ID="user_id";
		static String COLUMN_TOKEN="token";
		static String COLUMN_PASSWORD="pswd";
		static String COLUMN_ONLINE="online";
		static String COLUMN_ISFACULTY="is_faculty";
	}
	
	public static String PARAMETER_PASSWORD="password";
	public static String PARAMETER_USERNAME="username";
	public static String PARAMETER_QUERY_ID="query_id";
	public static String PARAMETER_TOKEN="token";
	public static String PARAMETER_USER_TYPE="user_type";
	public static String PARAMETER_DEPARTMENT="department";
	public static String PARAMETER_YEAR="year";
	
	
}

