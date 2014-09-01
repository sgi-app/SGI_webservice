package com.sgi.webservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	public static Connection getConnection(){
		Connection con=null;
		try{
			Class.forName(Constants.DB_CLASS);
			con=DriverManager.getConnection(Constants.DB_URL);
			return con;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean checkLogin(String user,String pwd){
		try {
			Connection conn=DBConnection.getConnection();
			Statement stm=conn.createStatement();
			String query="Select count(*) from "+Constants.DB_LOGIN_TABLE+" where "+Constants.Student.COLUMN_ID+"='"+user+"' and "+Constants.Student.COLUMN_PASSWORD+"='"+pwd+"';";
			System.out.println(query);
			ResultSet rs=stm.executeQuery(query);
			while(rs.next()){
				if(rs.getInt(1)==1)
					return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
