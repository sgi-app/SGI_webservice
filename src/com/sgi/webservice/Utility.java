package com.sgi.webservice;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.core.util.Base64;
public class Utility {
	public static String ConstructJSON(String tagr,boolean statusr){ 
		JSONObject obj=new JSONObject();
		try{
			obj.put("tag",tagr);
			obj.put("status",statusr);
		}
		catch(Exception e){
			return ConstructJSON(tagr,false,e.getMessage());
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
	
	public static String ConstructJSONArray(ArrayList<?> data,String metadata) throws JSONException{
		//JSONObject obj=new JSONObject();
		JSONArray obja=new JSONArray();
		
		for(Object str:data){
			if(metadata.equalsIgnoreCase("student")){
				Student tmpusr=(Student) str;
				JSONObject tmpobj=new JSONObject();
				tmpobj.put("name", tmpusr.f_name+" "+tmpusr.l_name);
				tmpobj.put("profile_img", tmpusr.pr_url);
				tmpobj.put("department",tmpusr.branch);
				obja.put(tmpobj);
			}
			else{
				Faculty tmpusr=(Faculty)str;
				JSONObject tmpobj=new JSONObject();
				tmpobj.put("name", tmpusr.f_name+" "+tmpusr.l_name);
				tmpobj.put("profile_img", tmpusr.pr_url);
				tmpobj.put("department",tmpusr.dprt);
				obja.put(tmpobj);
			}
		}
		System.out.println(obja.toString());
		return obja.toString();
	}
	
	public static String sha1(String input){
		try{
	        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	        byte[] result = mDigest.digest(input.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        return sb.toString();
        }
		catch(Exception ex){
			ex.printStackTrace();
        	return null;
        }
    }
}