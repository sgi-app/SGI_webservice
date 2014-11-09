package com.sgi.webservice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.dao.DBConnection;
import com.sgi.dao.DbConstants;
import com.sgi.dao.DbStructure;
import com.sgi.util.Faculty.FacultyMin;
import com.sgi.util.Student.StudentMin;
import com.sgi.util.Utility;


@Path("/query")
public class QueryTypeHandler {
	
	@GET
	@Path("/type_resolver")
	@Produces(MediaType.APPLICATION_JSON)
	public String typeResolver(@QueryParam(Constants.PARAMETER_USERNAME) String userid,@QueryParam(Constants.PARAMETER_TOKEN) String token,@QueryParam(Constants.PARAMETER_USER_TYPE) boolean student,@QueryParam(Constants.PARAMETER_DEPARTMENT) String department,@QueryParam(Constants.PARAMETER_YEAR) int year,@QueryParam(Constants.PARAMETER_SECTION) String section,@QueryParam(Constants.PARAMETER_COURSE) String course){
		if(DBConnection.authorizeUser(userid, token)){
			if(student){
				return send_student_list(year,department,course,section);
				
			}
			else{
				return send_faculty_list(department,course);
			}
		}
		else{
			//wrong user
			System.out.println("Somting went wrong");
			return null;
		}
	}
	
	@GET
	@Path("/get_user_info")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserInfo(@QueryParam(Constants.PARAMETER_USERNAME) String userid,@QueryParam(Constants.PARAMETER_TOKEN) String token,@QueryParam(Constants.PARAMETER_LOGIN_ID) int l_id,@QueryParam(Constants.PARAMETER_USER_TYPE) boolean is_std){
		if(DBConnection.authorizeUser(userid, token)){
			return userInfo(l_id,is_std);
		}
		return null;
	}

	
	public String userInfo(int l_id,boolean is_std){
		
		String query;
		if(is_std){
			query = DbConstants.SELECT + 
					DbStructure.LOGIN.COLUMN_USER_ID + DbConstants.COMMA +
					DbStructure.STUDENTS.COLUMN_U_ROLL_NO + DbConstants.FROM +
					DbStructure.STUDENTS.TABLE_NAME + DbConstants.JOIN + DbStructure.LOGIN.TABLE_NAME + DbConstants.ON + 
					DbStructure.STUDENTS.COLUMN_LOGIN + DbConstants.EQUALS + DbStructure.LOGIN.COLUMN_ID + 
					DbConstants.WHERE + DbStructure.STUDENTS.COLUMN_LOGIN + "='" + l_id + "';";
					
		//	query="select user_id,u_roll_no from students join login on l_id=login.id where l_id="+l_id;
		}
		else{
			query = DbConstants.SELECT + 
					DbStructure.LOGIN.COLUMN_USER_ID + DbConstants.COMMA +
					DbStructure.CONTACT_INFO.COLUMN_STREET + DbConstants.COMMA +
					DbStructure.CONTACT_INFO.COLUMN_CITY + DbConstants.COMMA +
					DbStructure.CONTACT_INFO.COLUMN_STATE + DbConstants.COMMA +
					DbStructure.CONTACT_INFO.COLUMN_PIN + DbConstants.COMMA +
					DbStructure.CONTACT_INFO.COLUMN_P_MOB + DbConstants.COMMA +
					DbStructure.CONTACT_INFO.COLUMN_H_MOB + DbConstants.FROM +
					DbStructure.LOGIN.TABLE_NAME + DbConstants.JOIN + DbStructure.CONTACT_INFO.TABLE_NAME + DbConstants.ON + 
					DbStructure.LOGIN.TABLE_NAME+DbConstants.DOT+DbStructure.LOGIN.COLUMN_ID + DbConstants.EQUALS + DbStructure.CONTACT_INFO.COLUMN_USER_ID + 
					DbConstants.WHERE + DbStructure.LOGIN.COLUMN_ID + "='" + l_id + "';";
			//query="select user_id ,street,city,state,pin,p_mob,h_mob from login join contact_info on login.id=usr_id where login.id="+l_id;
		}
		
		Connection con=DBConnection.getConnection();
		Statement stm;
		JSONObject obj;
		try {
			stm = con.createStatement();
			ResultSet rs=stm.executeQuery(query);
			obj=new JSONObject();
			if(rs.next()){
				if(is_std){
					obj.put(Constants.USER_ID, rs.getString(1));
					obj.put(Constants.ROLL_NO, rs.getString(2));
				}
				else{
					obj.put(Constants.USER_ID, rs.getString(1));
					obj.put(Constants.STATE, rs.getString(2));
					obj.put(Constants.CITY, rs.getString(3));
					obj.put(Constants.STATE, rs.getString(4));
					obj.put(Constants.PIN, rs.getString(5));
					obj.put(Constants.P_MOB, rs.getString(6));
					obj.put(Constants.H_MOB, rs.getString(7));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			obj=new JSONObject();
			try {
				obj.put(Constants.Error, "fail to get or parse data");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return obj.toString();
		
		
	}
	
	public String send_faculty_list(String department,String course){

		try{
		Connection conn=DBConnection.getConnection();
		String query;
		if(course.equalsIgnoreCase("All"))
/*
			query="select f_name,l_name,user_id,branches.name,profile_url,is_online,p_mob from faculty "
					+ "join login on l_id=login.id join contact_info on usr_id=login.id "
					+ "join branches on faculty.branch_id=branches.id order by f_name;";
		else if(department.equalsIgnoreCase("All") ){
			query="select f_name,l_name,user_id,branches.name,profile_url,is_online,p_mob from faculty "
					+ "join login on l_id=login.id join contact_info on usr_id=login.id "
					+ "join branches on faculty.branch_id=branches.id order by f_name "
					+ "join courses on branches.course_id=courses.id"
					+ "where courses.name='"+course+"' order by f_name;";
		}
		else{			
			query="select f_name,l_name,user_id,branches.name,profile_url,is_online,p_mob from faculty "
					+ "join login on l_id=login.id join contact_info on usr_id=login.id "
					+ "join branches on faculty.branch_id=branches.id order by f_name "
					+ "join courses on branches.course_id=courses.id"
					+ "where courses.name='"+course+"' and branches.name='"+department +"' order by f_name;";
		}*/
			query = DbConstants.SELECT +
				DbStructure.FACULTY.COLUMN_F_NAME + DbConstants.COMMA +
				DbStructure.FACULTY.COLUMN_L_NAME + DbConstants.COMMA +
				DbStructure.FACULTY.COLUMN_PROFILE_URL + DbConstants.COMMA +
				DbStructure.BRANCHES.TABLE_NAME + DbConstants.DOT + DbStructure.BRANCHES.COLUMN_NAME + DbConstants.COMMA +
				DbStructure.COURSES.TABLE_NAME + DbConstants.DOT + DbStructure.COURSES.COLUMN_NAME + DbConstants.COMMA +
				DbStructure.FACULTY.COLUMN_LOGIN_ID + DbConstants.FROM + 
				DbStructure.FACULTY.TABLE_NAME + 
				DbConstants.JOIN + DbStructure.BRANCHES.TABLE_NAME + DbConstants.ON +
							DbStructure.FACULTY.COLUMN_BRANCH_ID + DbConstants.EQUALS +
								DbStructure.BRANCHES.TABLE_NAME + DbConstants.DOT+DbStructure.BRANCHES.COLUMN_ID +  
				DbConstants.JOIN + DbStructure.COURSES.TABLE_NAME + DbConstants.ON +
							DbStructure.BRANCHES.COLUMN_COURSE_ID + DbConstants.EQUALS +
								DbStructure.COURSES.TABLE_NAME + DbConstants.DOT+DbStructure.COURSES.COLUMN_ID + DbConstants.SEMICOLON;			
		//	query="select f_name,l_name,profile_url,branches.name,courses.name,l_id from faculty join branches on branch_id=branches.id join courses on course_id=courses.id";
		else
			query = DbConstants.SELECT +
			DbStructure.FACULTY.COLUMN_F_NAME + DbConstants.COMMA +
			DbStructure.FACULTY.COLUMN_L_NAME + DbConstants.COMMA +
			DbStructure.FACULTY.COLUMN_PROFILE_URL + DbConstants.COMMA +
			DbStructure.BRANCHES.TABLE_NAME + DbConstants.DOT + DbStructure.BRANCHES.COLUMN_NAME + DbConstants.COMMA +
			DbStructure.COURSES.TABLE_NAME + DbConstants.DOT + DbStructure.COURSES.COLUMN_NAME + DbConstants.COMMA +
			DbStructure.FACULTY.COLUMN_LOGIN_ID + DbConstants.FROM + 
			DbStructure.FACULTY.TABLE_NAME + 
			DbConstants.JOIN + DbStructure.BRANCHES.TABLE_NAME + DbConstants.ON +
						DbStructure.FACULTY.COLUMN_BRANCH_ID + DbConstants.EQUALS +
							DbStructure.BRANCHES.TABLE_NAME + DbConstants.DOT+DbStructure.BRANCHES.COLUMN_ID +  
			DbConstants.JOIN + DbStructure.COURSES.TABLE_NAME + DbConstants.ON +
						DbStructure.BRANCHES.COLUMN_COURSE_ID + DbConstants.EQUALS +
							DbStructure.COURSES.TABLE_NAME + DbConstants.DOT+DbStructure.COURSES.COLUMN_ID +
			DbConstants.WHERE + DbStructure.COURSES.TABLE_NAME + DbConstants.DOT + DbStructure.COURSES.COLUMN_NAME + 
				DbConstants.EQUALS + "'" + course + "'" +
					(department.equalsIgnoreCase("All")?"":"and"+ DbStructure.BRANCHES.COLUMN_NAME+"='"+department+"';");
	//query="select f_name,l_name,profile_url,branches.name,courses.name,l_id from faculty join branches on branch_id	=branches.id join courses on course_id=courses.id 
		//		"where courses.name='"+course+"' "+(department.equalsIgnoreCase("All")?"":"and branches.name='"+department+"'");
		Statement stm=conn.createStatement();
		ResultSet rs=stm.executeQuery(query);
		System.out.println(query);
		ArrayList<FacultyMin> faculties=new ArrayList<FacultyMin>();
		while(rs.next()){
			faculties.add(new FacultyMin(rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(4),rs.getString(5),rs.getInt(6)));
		}
		System.out.println("returning "+faculties.size()+" faculties");
		return Utility.ConstructJSONArray(faculties, "faculty");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String send_student_list(int year,String department,String course,String section){
		try{
		Connection conn=DBConnection.getConnection();
		String query;
/*		if(year==0 && department.equalsIgnoreCase("All"))
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id;";
		else if(year==0)
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where branch='"+department+"';";
		else if(department.equalsIgnoreCase("All"))
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where year="+year+";";
		else
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where year="+year+" and branch='"+department+"';";
*/		
		query="select "+ DbStructure.STUDENTS.COLUMN_F_NAME+ DbConstants.COMMA + 
				DbStructure.STUDENTS.COLUMN_L_NAME + DbConstants.COMMA +
				DbStructure.STUDENTS.COLUMN_LOGIN + DbConstants.COMMA + 
				DbStructure.STUDENTS.COLUMN_PROFILE + DbConstants.COMMA +
				DbStructure.BRANCHES.TABLE_NAME+DbConstants.DOT + DbStructure.BRANCHES.COLUMN_NAME +DbConstants.COMMA +
				DbStructure.YEAR.TABLE_NAME+DbConstants.DOT + DbStructure.YEAR.COLUMN_YEAR + DbConstants.COMMA +
				DbStructure.SECTIONS.TABLE_NAME+DbConstants.DOT + DbStructure.SECTIONS.COLUMN_NAME + DbConstants.COMMA +
				DbStructure.COURSES.TABLE_NAME+DbConstants.DOT + DbStructure.COURSES.COLUMN_NAME + 
				DbConstants.FROM + DbStructure.STUDENTS.TABLE_NAME +
				DbConstants.JOIN + DbStructure.SECTIONS.TABLE_NAME + DbConstants.ON +
							DbStructure.STUDENTS.COLUMN_SECTION_ID + DbConstants.EQUALS +
								DbStructure.SECTIONS.TABLE_NAME + DbConstants.DOT+DbStructure.SECTIONS.COLUMN_ID +
				DbConstants.JOIN + DbStructure.YEAR.TABLE_NAME + DbConstants.ON +
							DbStructure.SECTIONS.COLUMN_YEAR_ID + DbConstants.EQUALS +
								DbStructure.YEAR.TABLE_NAME + DbConstants.DOT+DbStructure.YEAR.COLUMN_ID +
				DbConstants.JOIN + DbStructure.BRANCHES.TABLE_NAME + DbConstants.ON +
							DbStructure.YEAR.COLUMN_BRANCH_ID + DbConstants.EQUALS +
								DbStructure.BRANCHES.TABLE_NAME + DbConstants.DOT+DbStructure.BRANCHES.COLUMN_ID +
				DbConstants.JOIN + DbStructure.COURSES.TABLE_NAME + DbConstants.ON +
							DbStructure.BRANCHES.COLUMN_COURSE_ID + DbConstants.EQUALS +
								DbStructure.COURSES.TABLE_NAME + DbConstants.DOT+DbStructure.COURSES.COLUMN_ID +
				((course.equalsIgnoreCase("All"))?" ":(("where "+DbStructure.COURSES.COLUMN_NAME+"='"+course+"'")+
				((department.equalsIgnoreCase("All"))?" ":(" and "+DbStructure.BRANCHES.COLUMN_NAME+"='"+department+"'"))+
				((year==0?" ":(" and "+DbStructure.YEAR.COLUMN_YEAR+"='"+year+"' ")))+
				((department.equalsIgnoreCase("All") || year==0)?" ":(section.equalsIgnoreCase("All")?" ":(" and "+DbStructure.SECTIONS.COLUMN_NAME+"='"+section+"' ")))));

		/*
		query="select+ f_name,l_name,l_id,profile_url,branches.name,year.year,sections.name,courses.name from "
		+"students join sections on section_id=sections.id "
		+"join year on year_id=year.id "
		+"join branches on branch_id=branches.id "
		+"join courses on course_id=courses.id "
		+((course.equalsIgnoreCase("All"))?" ":(("where courses.name='"+course+"'")
		+((department.equalsIgnoreCase("All"))?" ":(" and branches.name='"+department+"'"))
		+((year==0?" ":(" and year.year="+year+" ")))
		+((department.equalsIgnoreCase("All") || year==0)?" ":(section.equalsIgnoreCase("All")?" ":(" and sections.name='"+section+"' ")))));
		*/
		System.out.println(query);
		Statement stm=conn.createStatement();
		ResultSet rs=stm.executeQuery(query);
		ArrayList<StudentMin> students=new ArrayList<StudentMin>();
		StudentMin tmp;
		while(rs.next()){
			try{
			tmp=new StudentMin(rs.getString(1), rs.getString(2), rs.getInt(3),rs.getString(4),rs.getString(5),rs.getInt(6),rs.getString(7),rs.getString(8));
			students.add(tmp);
			}catch(NullPointerException ex){
				System.out.println("row discarded null value attribute");
			}
		}
		System.out.println("returning "+students.size()+" students");
		return Utility.ConstructJSONArray(students, "student");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
