package com.sgi.util;

public class Faculty {

	public static class FacultyMin{
		String f_name;
		String l_name;
		String picUrl;
		String dep;
		String course;
		int l_id; //identifier in login table
		public FacultyMin(String f_n,String l_n,String pu,String dp,String cou,int li){
			f_name=f_n;
			l_name=l_n;
			picUrl=pu;
			dep=dp;
			l_id=li;
			course=cou;
		}
	}
	public static class FacultyFull{
		
		String id;
		UserInfo uinfo;
		public FacultyFull(String uid,UserInfo ui){
			id=uid;
			uinfo=ui;
		}
	}
}
