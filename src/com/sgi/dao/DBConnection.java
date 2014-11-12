package com.sgi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.util.InitialData;
import com.sgi.util.Personal_info;
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
	public static boolean fillMessage(JSONObject msgs){
		Connection conn=null;
		try{
			conn=getConnection();
			String query="insert into messages(sender,text,time,receiver) values((select id from login where user_id='"+msgs.getString(Constants.JSONMessageKeys.SENDER)+"'),'"+msgs.getString(Constants.JSONMessageKeys.TEXT)+"','"+msgs.getLong(Constants.JSONMessageKeys.TIME)+"',(select id from login where user_id='"+msgs.getString(Constants.JSONMessageKeys.RECEIVER)+"'))";
			System.out.println(query);
			Statement stm=conn.createStatement();
			stm.executeUpdate(query);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		finally{
			try{
				conn.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static JSONArray fetchMessages(String userid){
		Connection conn;
		conn=getConnection();
		JSONArray result=new JSONArray();
		try{
			String query="select login.user_id,text,time,is_group_msg,messages.id from messages join login on sender=login.id where receiver=(select id from login where user_id='"+userid+"') and state="+Constants.MsgState.TO_SEND;
			System.out.println(query);
			Statement stm=conn.createStatement();
			ResultSet rs=stm.executeQuery(query);
			JSONObject obj;
			while(rs.next()){
				obj=new JSONObject();
				obj.put(Constants.JSONMessageKeys.SENDER, rs.getString(1));
				obj.put(Constants.JSONMessageKeys.TEXT, rs.getString(2));
				obj.put(Constants.JSONMessageKeys.TIME, rs.getLong(3));
				obj.put(Constants.JSONMessageKeys.IS_GROUP_MESSAGE, rs.getString(4).equalsIgnoreCase("N")?0:1);
				obj.put(Constants.JSONMessageKeys.ID, rs.getInt(5));
				result.put(obj);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				conn.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		System.out.println(result.toString());
		return result;
	}
	public static boolean authorizeUser(String userid,String token){
		Connection conn=null;
		userid=new String(Base64.decode(userid)).trim();
		token=new String(Base64.decode(token)).trim();
		try{
			conn=getConnection();
			String query=DbConstants.SELECT + "count(*)" + DbConstants.FROM 
					+ DbStructure.LOGIN.TABLE_NAME+  DbConstants.WHERE +
					DbStructure.LOGIN.COLUMN_USER_ID+"='"+userid+"' and "+DbStructure.LOGIN.COLUMN_TOKEN+"='"+token+"';";
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
			System.out.println("infinally");
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
	public static void updateMessageState(JSONArray msgids){
		Connection conn=null;
		int len=msgids.length();
		try{
			
			String query="update messages set state="+Constants.MsgState.SENT_SUCESSFULLY+" where id IN (";
			for(int i=0;i<len;i++)
				query+=msgids.getInt(i)+(i==len-1?"":",");
			query+=")";
			conn=getConnection();
			Statement stm=conn.createStatement();
			stm.executeUpdate(query);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
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
			String query="Select "+DbStructure.LOGIN.COLUMN_PASSWORD+" from " + 
					DbStructure.LOGIN.TABLE_NAME + 
						" where "+DbStructure.LOGIN.COLUMN_USER_ID+"='"+user.toUpperCase()+
							"' and "+DbStructure.LOGIN.COLUMN_IS_FACULTY+"='"+(is_faculty?'Y':'N')+"';";
			System.out.println(query);
			System.out.println("matching\n"+pwd);
			ResultSet rs=stm.executeQuery(query);
			if(rs.next()){
				System.out.println(Utility.sha1(rs.getString(1)));
				if(Utility.sha1(rs.getString(1)).equals(pwd)){
					query="Update " + DbStructure.LOGIN.TABLE_NAME+
							" set token='" + Utility.sha1(pwd+Login.counter)+
							"' where " + DbStructure.LOGIN.COLUMN_USER_ID+"='" + user + "';";
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
	
	public static void getPersonalInfo(String user_id,Boolean is_faculty){
		String query;
		Connection con=null;
		try{
			con=DBConnection.getConnection();
			Statement stm=con.createStatement();			
			if(is_faculty) {
				query=DbConstants.SELECT +
						DbStructure.FACULTY.COLUMN_F_NAME + DbConstants.COMMA + 
						DbStructure.FACULTY.COLUMN_L_NAME + DbConstants.COMMA +
						DbStructure.FACULTY.COLUMN_PROFILE_URL + DbConstants.COMMA +
						DbStructure.BRANCHES.COLUMN_NAME + 
						DbConstants.FROM + DbStructure.LOGIN.TABLE_NAME +
						DbConstants.JOIN + DbStructure.FACULTY.TABLE_NAME + DbConstants.ON +
							DbStructure.LOGIN.TABLE_NAME + DbConstants.DOT + DbStructure.LOGIN.COLUMN_ID + DbConstants.EQUALS +
								DbStructure.FACULTY.TABLE_NAME + DbConstants.DOT+DbStructure.FACULTY.COLUMN_LOGIN_ID +
						DbConstants.JOIN + DbStructure.BRANCHES.TABLE_NAME + DbConstants.ON +
							DbStructure.FACULTY.TABLE_NAME + DbConstants.DOT + DbStructure.FACULTY.COLUMN_BRANCH_ID + DbConstants.EQUALS + 
								DbStructure.BRANCHES.TABLE_NAME + DbConstants.DOT + DbStructure.BRANCHES.COLUMN_ID + 
						DbConstants.WHERE + DbStructure.LOGIN.COLUMN_USER_ID + DbConstants.EQUALS +"'" + user_id + "';"; 
			}
			else {
				query=DbConstants.SELECT + 
						DbStructure.STUDENTS.COLUMN_F_NAME + DbConstants.COMMA +
						DbStructure.STUDENTS.COLUMN_L_NAME + DbConstants.COMMA + 
						DbStructure.STUDENTS.COLUMN_PROFILE + DbConstants.COMMA + 
						DbStructure.SECTIONS.COLUMN_NAME + DbConstants.COMMA + 
						DbStructure.YEAR.COLUMN_YEAR +  DbConstants.FROM +  DbStructure.LOGIN.TABLE_NAME + 
						DbConstants.JOIN + DbStructure.STUDENTS.TABLE_NAME + DbConstants.ON + 
							DbStructure.LOGIN.TABLE_NAME+DbConstants.DOT+DbStructure.LOGIN.COLUMN_ID + DbConstants.EQUALS
								+ DbStructure.STUDENTS.TABLE_NAME+DbConstants.DOT+DbStructure.STUDENTS.COLUMN_LOGIN +
						DbConstants.JOIN + DbStructure.SECTIONS.TABLE_NAME + DbConstants.ON + 
							DbStructure.STUDENTS.TABLE_NAME+DbConstants.DOT+DbStructure.STUDENTS.COLUMN_SECTION_ID + DbConstants.EQUALS
								+ DbStructure.SECTIONS.TABLE_NAME+DbConstants.DOT+DbStructure.SECTIONS.COLUMN_ID +
						DbConstants.JOIN + DbStructure.YEAR.TABLE_NAME + DbConstants.ON + 
							DbStructure.SECTIONS.TABLE_NAME+DbConstants.DOT+DbStructure.SECTIONS.COLUMN_YEAR_ID + DbConstants.EQUALS
								+ DbStructure.YEAR.TABLE_NAME+DbConstants.DOT+DbStructure.YEAR.COLUMN_ID +
						DbConstants.WHERE + DbStructure.LOGIN.COLUMN_USER_ID + DbConstants.EQUALS +"'" + user_id + "';"; 
				}
			ResultSet rs=stm.executeQuery(query);
			System.out.println(query+" "+user_id);
			while(rs.next()){
				System.out.println(rs.getString(1));
				Personal_info.f_name=rs.getString(1);
				Personal_info.l_name=rs.getString(2);
				Personal_info.profile_url=rs.getString(3);
				if(is_faculty)
					Personal_info.branch=rs.getString(4);
				else
				{
					Personal_info.section=rs.getString(4);
					Personal_info.year=rs.getString(5);
				}
			}
			
			}catch (SQLException e) {
				e.printStackTrace();
			}
			finally{
				try{
					if(!con.isClosed())
						con.close();
				}catch(Exception ex){
						ex.printStackTrace();
					}
				}
	}
	
}
