package com.sgi.util;

public class Faculty {

	public static class FacultyMin{
		String f_name;
		String l_name;
		String picUrl;
		String dep;
		int l_id; //identifier in login table
		public FacultyMin(String f_n,String l_n,String pu,String dp,int li){
			f_name=f_n;
			l_name=l_n;
			picUrl=pu;
			dep=dp;
			l_id=li;
		}
	}
	public static class FacultyFull{
		String f_name;
		String l_name;
		String id;
		int state;
		UserInfo uinfo;
		String dep;
		String picUrl;
		public FacultyFull(String f_n,String l_n,String l_id,String pu,String dp,int sta,UserInfo ui){
			f_name=f_n;
			l_name=l_n;
			id=l_id;
			picUrl=pu;
			dep=dp;
			state=sta;
			uinfo=ui;
		}
	}
}
