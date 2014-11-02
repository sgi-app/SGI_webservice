package com.sgi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sgi.util.InitialData;
import com.sgi.util.InitialData.Branches;
import com.sgi.util.InitialData.Courses;
import com.sgi.util.InitialData.Sections;
import com.sgi.util.InitialData.Year;
import com.sgi.util.Utility;
import com.sgi.webservice.Login;
import com.sun.jersey.core.util.Base64;

public class DBConnection {
	public static Connection getConnection(){
		Connection con=null;
		try{
			Class.forName(DbStructure.DB_CLASS);
			con=DriverManager.getConnection(DbStructure.DB_URL);
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
		try{
			conn=getConnection();
			String query="select count(*) from login where "+DbStructure.LOGIN.COLUMN_USER_ID+"='"+userid+"' and "+DbStructure.LOGIN.COLUMN_TOKEN+"='"+token+"'";
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
	public static InitialData getInitialData(){
		Connection conn=null;
		try{
			InitialData idata=new InitialData();
			conn=DBConnection.getConnection();
			Statement stm=conn.createStatement();
			String query="select * from "+DbStructure.COURSES.TABLE_NAME;
			ResultSet rs=stm.executeQuery(query);
			Courses course;
			while(rs.next()){
				course=new Courses();
				course.id=rs.getInt(DbStructure.COURSES.COLUMN_ID);
				course.name=rs.getString(DbStructure.COURSES.COLUMN_NAME);
				course.duration=rs.getInt(DbStructure.COURSES.COLUMN_DURATION);
				idata.courses.add(course);
			}
			query="select * from "+DbStructure.BRANCHES.TABLE_NAME;
			rs=stm.executeQuery(query);
			Branches branch;
			while(rs.next()){
				branch=new Branches();
				branch.id=rs.getInt(DbStructure.BRANCHES.COLUMN_ID);
				branch.course_id=rs.getInt(DbStructure.BRANCHES.COLUMN_COURSE_ID);
				branch.name=rs.getString(DbStructure.BRANCHES.COLUMN_NAME);
				
				idata.branches.add(branch);
			}
			query="select * from "+DbStructure.SECTIONS.TABLE_NAME;
			rs=stm.executeQuery(query);
			Sections section=new Sections();
			while(rs.next()){
				section=new Sections();
				section.year_id=rs.getInt(DbStructure.SECTIONS.COLUMN_YEAR_ID);
				section.id=rs.getInt(DbStructure.SECTIONS.COLUMN_ID);
				section.name=rs.getString(DbStructure.SECTIONS.COLUMN_NAME);
				idata.sections.add(section);
			}
			query="select * from "+DbStructure.YEAR.TABLE_NAME;
			rs=stm.executeQuery(query);
			Year year=new Year();
			while(rs.next()){
				year=new Year();
				year.branch_id=rs.getInt(DbStructure.YEAR.COLUMN_BRANCH_ID);
				year.id=rs.getInt(DbStructure.YEAR.COLUMN_ID);
				year.year=rs.getInt(DbStructure.YEAR.COLUMN_YEAR);
				idata.years.add(year);
			}
			
			System.out.println("initial data set");
			return idata;
			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		finally{
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	public static boolean checkLogin(String user,String pwd,boolean is_faculty){
		Connection conn=null;
		try {
			conn=DBConnection.getConnection();
			Statement stm=conn.createStatement();
			String query="Select "+DbStructure.LOGIN.COLUMN_PASSWORD+" from "+DbStructure.LOGIN.TABLE_NAME+" where "+DbStructure.LOGIN.COLUMN_USER_ID+"='"+user.toUpperCase()+"' and "+DbStructure.LOGIN.COLUMN_IS_FACULTY+"='"+(is_faculty?'Y':'N')+"';";
			System.out.println(query);
			System.out.println("matching\n"+pwd);
			ResultSet rs=stm.executeQuery(query);
			if(rs.next()){
				System.out.println(Utility.sha1(rs.getString(1)));
				if(Utility.sha1(rs.getString(1)).equals(pwd)){
					query="Update "+DbStructure.LOGIN.TABLE_NAME+" set token='"+Utility.sha1(pwd+Login.counter)+"' where "+DbStructure.LOGIN.COLUMN_USER_ID+"='"+user+"';";
					System.out.println(query);
					if(stm.executeUpdate(query)==1)
						return true;
					else
						System.out.println("problem inserting token");
				}
			}
			else{
				System.out.println("no data matched user input");
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
