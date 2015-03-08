package com.sgi.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sgi.constants.Constants;

public class Gcm {

	@GET
	@Path("/saveRegistrationId")
	@Produces(MediaType.APPLICATION_JSON)
	public String saveRegistrationId(@QueryParam(Constants.QueryParameters.USERNAME) String uname, 			
			@QueryParam(Constants.QueryParameters.REG_ID) String reg_id){
		
		
		return null;
	}
}
