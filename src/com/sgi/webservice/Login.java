package com.sgi.webservice;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.core.util.Base64;

@Path("/login")
public class Login {
	@GET
	@Path("/dologin")
	@Produces(MediaType.APPLICATION_JSON)
	public String doLogin(@QueryParam("username") String uname, @QueryParam("password") String pwd){
		String response="";
		System.out.println(" received Username="+uname+" password="+pwd);
		String d_uname,d_pwd;
		d_uname=new String(Base64.decode(uname)).trim();
		d_pwd=new String(Base64.decode(pwd)).trim();
		System.out.println("decoded Username="+d_uname+" password="+d_pwd);
		
		if(checkCredentials(d_uname,d_pwd)){
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
