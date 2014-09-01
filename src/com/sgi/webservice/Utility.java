package com.sgi.webservice;

import org.codehaus.jettison.json.JSONObject;

public class Utility {
	public static String ConstructJSON(String tag,boolean status){
		JSONObject obj=new JSONObject();
		try{
			obj.put("tag",tag);
			obj.put("status",status);
		}
		catch(Exception e){
			return ConstructJSON(tag,false,e.getMessage());
		}
		return obj.toString();
	}
	public static String ConstructJSON(String tag,boolean status,String err_msg){
		JSONObject obj=new JSONObject();
		try{
			obj.put("tag",tag);
			obj.put("status",status);
			obj.put("error",err_msg);
			return obj.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return obj.toString();
	}
}