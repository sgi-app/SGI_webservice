package com.sgi.webservice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("/query")
public class QueryTypeHandler {
	
	@GET
	@Path("/type_resolver")
	@Produces(MediaType.APPLICATION_JSON)
	public String type_resolver(@QueryParam(Constants.PARAMETER_USERNAME) String userid,@QueryParam(Constants.PARAMETER_TOKEN) String token,@QueryParam(Constants.PARAMETER_QUERY_ID) int query_id){
		System.out.println("Someone Called me");
		if(DBConnection.authorizeUser(userid, token)){
			switch(query_id){
			case 0:
				System.out.println("get 0");
				return send_student_list();
			case 1:
				System.out.println("get 1");
				return send_faculty_list();
			}
			return null;
		}
		else{
			//wrong user
			System.out.println("Somting went wrong");
			return null;
		}
	}
	

	public String send_faculty_list(){
		try{
		Connection conn=DBConnection.getConnection();
		String query="select f_name,l_name,department,profile_url from faculty;";
		Statement stm=conn.createStatement();
		ResultSet rs=stm.executeQuery(query);
		ArrayList<Faculty> faculties=new ArrayList<Faculty>();
		while(rs.next()){
			faculties.add(new Faculty(rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(4)));
		}
		return Utility.ConstructJSONArray(faculties, "faculty");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String send_student_list(){
		try{
		Connection conn=DBConnection.getConnection();
		String query="select f_name,l_name,branch,profile_url from students;";
		Statement stm=conn.createStatement();
		ResultSet rs=stm.executeQuery(query);
		ArrayList<Student> students=new ArrayList<Student>();
		while(rs.next()){
			students.add(new Student(rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(4)));
		}
		return Utility.ConstructJSONArray(students, "student");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
