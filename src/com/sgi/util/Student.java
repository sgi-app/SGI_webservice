package com.sgi.util;

public class Student extends User {
	int year;
	String section;

	public Student(String f_name_, String l_name_, String user_id_,
			String picUrl_, int yer_, String section_) {
		super(f_name_, l_name_, picUrl_, user_id_);
		section = section_;
		year = yer_;
	}

	@Override
	public String toString() {
		String COMMA = ",";
		return "[" + year + COMMA + section + COMMA + super.toString() + "]";
	}

}
