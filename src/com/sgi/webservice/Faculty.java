package com.sgi.webservice;

public class Faculty {
	
	public static String FIRST_NAME="first_name";
	public static String LAST_NAME="last_name";
	public static String PROFILE_IMAGE="profile_image";
	public static String ID="id";
	public static String DEPARTMENT="department";
	public static String STATE="state";
	
	public static String MOBILE="mobile";
	
	
	String f_name;
	String l_name;
	String id;
	int state;
	String mob;
	String dep;
	String picUrl;
	public Faculty(String f_n,String l_n,String l_id,String pu,String dp,int sta,String m){
		f_name=f_n;
		l_name=l_n;
		id=l_id;
		picUrl=pu;
		dep=dp;
		state=sta;
		mob=m;
	}
}
