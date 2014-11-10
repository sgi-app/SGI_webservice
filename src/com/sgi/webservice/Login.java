package com.sgi.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.dao.DBConnection;
import com.sgi.dao.DbStructure;
import com.sgi.util.InitialData;
import com.sgi.util.Utility;
import com.sun.jersey.core.util.Base64;

@Path("/login")
public class Login {
	public static long counter=0;
	@GET
	@Path("/dologin")
	@Produces(MediaType.APPLICATION_JSON)
	public String doLogin(@QueryParam(Constants.QueryParameters.USERNAME) String uname, @QueryParam(Constants.QueryParameters.PASSWORD) String pwd,@QueryParam(Constants.QueryParameters.IS_FACULTY) boolean is_faculty){
		String response="";
		System.out.println(" received Username="+uname+" password="+pwd);
		String d_uname,d_pwd,token;
		d_uname=new String(Base64.decode(uname)).trim();
		d_pwd=new String(Base64.decode(pwd)).trim();
		System.out.println("decoded Username="+d_uname+" password="+d_pwd);
		
		if(checkCredentials(d_uname,d_pwd,is_faculty)){
			token=Utility.getToken(d_pwd+counter);
			counter++;
			response=Utility.ConstructJSON(Constants.JSONKeys.TAG_MSGS.LOGIN, true,d_uname,is_faculty,token);
			System.out.println("in if");
		}
		else{
			response=Utility.ConstructJSON(Constants.JSONKeys.TAG_MSGS.LOGIN, false, null,null,"Credentials not matched");
			System.out.println("in else");
		}
		return response;
	}
	@GET
	@Path("/getInitial")
	@Produces(MediaType.APPLICATION_JSON)
	public String getInitial(@QueryParam(Constants.QueryParameters.USERNAME) String userid,@QueryParam(Constants.QueryParameters.TOKEN) String token){
		if(DBConnection.authorizeUser(userid, token)){
			InitialData idata=DBConnection.getInitialData();
			//make json data
			
			JSONArray resultobj=new JSONArray();
			try {
				JSONArray tmparry=new JSONArray();
				for(InitialData.Courses c:idata.courses){
					JSONObject obj=new JSONObject();
					obj.put(DbStructure.COURSES.COLUMN_ID,c.id);
					obj.put(DbStructure.COURSES.COLUMN_DURATION,c.duration);
					obj.put(DbStructure.COURSES.COLUMN_NAME,c.name);
					tmparry.put(obj);
				}
				resultobj.put(0,tmparry);
				
				tmparry=new JSONArray();
				for(InitialData.Branches b:idata.branches){
					
					JSONObject obj=new JSONObject();
					obj.put(DbStructure.BRANCHES.COLUMN_COURSE_ID,b.course_id);
					obj.put(DbStructure.BRANCHES.COLUMN_ID,b.id);
					obj.put(DbStructure.BRANCHES.COLUMN_NAME,b.name);
					tmparry.put(obj);
				}
				resultobj.put(1,tmparry);
				
				tmparry=new JSONArray();
				for(InitialData.Sections s:idata.sections){
					
					JSONObject obj=new JSONObject();
					obj.put(DbStructure.SECTIONS.COLUMN_YEAR_ID,s.year_id);
					obj.put(DbStructure.SECTIONS.COLUMN_ID,s.id);
					obj.put(DbStructure.SECTIONS.COLUMN_NAME,s.name);
					tmparry.put(obj);
				}
				resultobj.put(2,tmparry);
				
				tmparry=new JSONArray();
				for(InitialData.Year y:idata.years){
					
					JSONObject obj=new JSONObject();
					obj.put(DbStructure.YEAR.COLUMN_BRANCH_ID,y.branch_id);
					obj.put(DbStructure.YEAR.COLUMN_ID,y.id);
					obj.put(DbStructure.YEAR.COLUMN_YEAR,y.year);
					tmparry.put(obj);
				}
				resultobj.put(3,tmparry);
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println(resultobj.toString());
			return resultobj.toString();
			
		}
		else{
			System.out.println("User not valid");
			return null;
		}
	}
	public boolean checkCredentials(String username,String pwd,boolean is_faculty){
			return DBConnection.checkLogin(username, pwd,is_faculty);
	}
}
