package com.sgi.webservice;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
@Path("/login")
public class Login {
	@GET
	@Path("/dologin")
	@Produces(MediaType.APPLICATION_JSON)
	public String doLogin(@QueryParam("username") String uname, @QueryParam("password") String pwd){
		String response="";
		System.out.println("Username received="+uname+" passowrd received="+pwd);
		if(checkCredentials(uname,pwd)){
			response=Utility.ConstructJSON("login", true);
			System.out.println("in if");
		}
		else{
			response=Utility.ConstructJSON("login", false, "Credentials not matched");
			System.out.println("in else");
		}
		return response;
	}
	
	public boolean checkCredentials(String username,String pwd){
			return DBConnection.checkLogin(username, pwd);
	}
}
