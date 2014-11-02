package com.sgi.util;

public class Student {
	public static class StudentMin{
		String f_name;
		String l_name;
		String branch;
		String picUrl;
		int year;
		String section;
		String course;
		int l_id;
		public StudentMin(String f_n,String l_n,int lid,String pu,String dp,int y,String sec,String cou){
			f_name=f_n;
			l_name=l_n;
			picUrl=pu;
			l_id=lid;
			branch=dp;
			course=cou;
			section=sec;
			year=y;
		}
	}
	public class StudentFull{
		String f_name;
		String l_name;
		String branch;
		String picUrl;
		int year;
		String section;
		String course;
		int l_id;
		int state;
		String user_id;
		UserInfo info;
	}
	
}
