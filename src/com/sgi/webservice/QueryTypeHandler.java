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
	public String type_resolver(@QueryParam(Constants.PARAMETER_USERNAME) String userid,@QueryParam(Constants.PARAMETER_TOKEN) String token,@QueryParam(Constants.PARAMETER_USER_TYPE) boolean student,@QueryParam(Constants.PARAMETER_DEPARTMENT) String department,@QueryParam(Constants.PARAMETER_YEAR) int year){
		if(DBConnection.authorizeUser(userid, token)){
			/*
			switch(query_id){
			case 0:
				System.out.println("get 0");
				return send_student_list();
			case 1:
				System.out.println("get 1");
				return send_faculty_list();
			}
			*/
			if(student){
				
				return send_student_list(year,department);
				
			}
			else{
				return send_faculty_list(department);
			}
		}
		else{
			//wrong user
			System.out.println("Somting went wrong");
			return null;
		}
	}
	
	@GET
	@Path("/run")
	public int runn(){
		Thread th=new Thread(new looper());
		th.run();
		return 1;
	}

	public String send_faculty_list(String department){
		try{
		Connection conn=DBConnection.getConnection();
		String query;
		if(department.equalsIgnoreCase("All"))
			query="select f_name,l_name,user_id,department,profile_url,is_online,p_mob from faculty join login on l_id=login.id join contact_info on usr_id=login.id order by f_name;";
		else
			query="select f_name,l_name,user_id,department,profile_url,is_online,p_mob from faculty join login on l_id=login.id join contact_info on usr_id=login.id where department='"+department+"' order by f_name;";
		
		Statement stm=conn.createStatement();
		ResultSet rs=stm.executeQuery(query);
		System.out.println(query);
		ArrayList<Faculty> faculties=new ArrayList<Faculty>();
		while(rs.next()){
			faculties.add(new Faculty(rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(5),rs.getString(4),rs.getString(6).equalsIgnoreCase("y")?1:0,rs.getString(7)));
		}
		System.out.println("returning "+faculties.size()+" faculties");
		return Utility.ConstructJSONArray(faculties, "faculty");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	class looper implements Runnable{
		public void run() {
			while(true){
				System.out.println("Looping");
				
			}
			
		}
	}
	public String send_student_list(int year,String department){
		try{
		Connection conn=DBConnection.getConnection();
		String query;
		if(year==0 && department.equalsIgnoreCase("All"))
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id;";
		else if(year==0)
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where branch='"+department+"';";
		else if(department.equalsIgnoreCase("All"))
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where year="+year+";";
		else
			query="select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where year="+year+" and branch='"+department+"';";
		System.out.println(query);
		Statement stm=conn.createStatement();
		ResultSet rs=stm.executeQuery(query);
		ArrayList<Student> students=new ArrayList<Student>();
		Student tmp;
		while(rs.next()){
			try{
			tmp=new Student(rs.getString(1), rs.getString(2), rs.getString(6),rs.getString(4),rs.getString(3),rs.getInt(5),rs.getInt(7),rs.getString(8).equalsIgnoreCase("y")?1:0);
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
