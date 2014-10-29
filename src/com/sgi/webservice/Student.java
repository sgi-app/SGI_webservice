package com.sgi.webservice;

public class Student {
	
	public static String FIRST_NAME="first_name";
	public static String LAST_NAME="last_name";
	public static String PROFILE_IMAGE="profile_image";
	public static String ID="id";
	public static String DEPARTMENT="department";
	public static String STATE="state";
	public static String YEAR="year";
	public static String SECTION="section";
	
	
	String f_name;
	String l_name;
	String branch;
	String picUrl;
	int year;
	int section;
	int state;
	String id;
	public Student(String f_n,String l_n,String l_id,String pu,String dp,int y,int sec,int sta){
		f_name=f_n;
		l_name=l_n;
		picUrl=pu;
		id=l_id;
		branch=dp;
		state=sta;
		section=sec;
		year=y;
	}
}
