package com.sgi.webservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.jersey.core.util.Base64;

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
	
	public static boolean authorizeUser(String userid,String token){
		Connection conn=null;
		userid=new String(Base64.decode(userid)).trim();
		token=new String(Base64.decode(token)).trim();
		System.out.println(token);
		try{
			conn=getConnection();
			String query="select count(*) from login where "+Constants.login.COLUMN_ID+"='"+userid+"' and "+Constants.login.COLUMN_TOKEN+"='"+token+"'";
			System.out.println(query);
			Statement stm=conn.createStatement();
			ResultSet rs=stm.executeQuery(query);
			if(rs.next()){
				if(rs.getInt(1)==1)
					return true;
			}
			return false;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		finally{
			try{
				if(!conn.isClosed())
					conn.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public static boolean checkLogin(String user,String pwd,boolean is_faculty){
		Connection conn=null;
		try {
			conn=DBConnection.getConnection();
			Statement stm=conn.createStatement();
			String query="Select "+Constants.login.COLUMN_PASSWORD+" from "+Constants.DB_LOGIN_TABLE+" where "+Constants.login.COLUMN_ID+"='"+user+"' and "+Constants.login.COLUMN_ISFACULTY+"='"+(is_faculty?'Y':'N')+"';";
			System.out.println(query);
			ResultSet rs=stm.executeQuery(query);
			if(rs.next()){
				if(Utility.sha1(rs.getString(1)).equals(pwd)){
					query="Update "+Constants.DB_LOGIN_TABLE+" set token='"+pwd+"' where "+Constants.login.COLUMN_ID+"='"+user+"';";
					if(stm.executeUpdate(query)==1)
						return true;
					else
						System.out.println("problem inserting token");
				}
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally{
			try{
				if(!conn.isClosed())
					conn.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
}
