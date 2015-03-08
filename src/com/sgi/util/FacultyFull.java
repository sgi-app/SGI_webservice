package com.sgi.util;

public class FacultyFull extends Faculty {
	String street;
	String city;
	String state;
	String pin;
	String p_mob;
	String h_mob;

	public FacultyFull(String f_name_, String l_name_, String branch_,
			String picUrl_, String user_id_, String street_, String city_,
			String state_, String pin_, String p_mob_, String h_mob_) {
		super(f_name_, l_name_, picUrl_, branch_, user_id_);
		street = street_;
		city = city_;
		state = state_;
		pin = pin_;
		p_mob = p_mob_;
		h_mob = h_mob_;
	}

	@Override
	public String toString() {
		String COMMA = ",";

		return "[" + street + COMMA + city + COMMA + state + COMMA + pin
				+ COMMA + p_mob + COMMA + h_mob + super.toString() + "]";
	}
}
