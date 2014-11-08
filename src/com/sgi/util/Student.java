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
		String user_id;
		String u_roll_no;
		public StudentFull(String ui,String roll){
			user_id=ui;
			u_roll_no=roll;
		}
	}
	
}
