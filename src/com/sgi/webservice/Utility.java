package com.sgi.webservice;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
public class Utility {
	public static String ConstructJSON(String tagr,boolean statusr,String msg){ 
		JSONObject obj=new JSONObject();
		try{
			obj.put("tag",tagr);
			obj.put("status",statusr);
			if(statusr)
				obj.put("token",msg);
			else
				obj.put("error",msg);
		}
		catch(Exception e){
			return ConstructJSON(tagr,false,e.getMessage());
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
				
				tmpobj.put(Student.FIRST_NAME, tmpusr.f_name);
				tmpobj.put(Student.LAST_NAME,tmpusr.l_name);
				tmpobj.put(Student.PROFILE_IMAGE, tmpusr.picUrl);
				tmpobj.put(Student.DEPARTMENT,tmpusr.branch);
				tmpobj.put(Student.YEAR, tmpusr.year);
				tmpobj.put(Student.ID, tmpusr.id);
				tmpobj.put(Student.SECTION, tmpusr.section);
				tmpobj.put(Student.STATE, tmpusr.state);
				
				obja.put(tmpobj);
			}
			else{
				Faculty tmpusr=(Faculty)str;
				JSONObject tmpobj=new JSONObject();
		
				tmpobj.put(Faculty.FIRST_NAME, tmpusr.f_name);
				tmpobj.put(Faculty.LAST_NAME,tmpusr.l_name);
				tmpobj.put(Faculty.PROFILE_IMAGE, tmpusr.picUrl);
				tmpobj.put(Faculty.DEPARTMENT,tmpusr.dep);
				tmpobj.put(Faculty.ID, tmpusr.id);
				tmpobj.put(Faculty.STATE, tmpusr.state);
				tmpobj.put(Faculty.MOBILE, tmpusr.mob);
				obja.put(tmpobj);
			}
		}
//		System.out.println(obja.toString());
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
	public static String getToken(String input){
		String token=sha1(input);
		return token;
	}
}