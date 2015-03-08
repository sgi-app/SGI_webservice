package com.sgi.util;

public class User {
	String f_name;
	String l_name;

	String picUrl;
	String user_id;

	public User(String f_name_, String l_name_, String picUrl_, String user_id_) {
		f_name = f_name_;
		l_name = l_name_;
		picUrl = picUrl_;
		user_id = user_id_;

	}

	@Override
	public String toString() {
		String COMMA = ",";
		return "[" + f_name + COMMA + l_name + COMMA + user_id + "]";
	}
}
