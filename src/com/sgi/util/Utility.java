package com.sgi.util;

import java.security.MessageDigest;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.dao.DBConnection;
import com.sun.jersey.core.util.Base64;

public class Utility {
	public static String ConstructJSON(DBConnection db, String tagr,
			boolean statusr, String user_id, Boolean is_faculty, String msg) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(Constants.JSONKeys.TAG, tagr);
			obj.put(Constants.JSONKeys.STATUS, statusr);
			if (statusr) {
				User user = db.getPersonalInfo(user_id, is_faculty);
				System.out.println("in status true");
				
				System.out.print(user);
				
				obj.put(Constants.JSONKeys.FIRST_NAME, user.f_name);
				obj.put(Constants.JSONKeys.LAST_NAME, user.l_name);
				obj.put(Constants.JSONKeys.PROFILE_IMAGE, user.picUrl);
				//obj.put(Constants.JSONKeys.BRANCH, user.branch);
				if (is_faculty) {
					FacultyFull f_user = (FacultyFull) user;
					obj.put(Constants.JSONKeys.BRANCH, f_user.branch);
					
					obj.put(Constants.JSONKeys.CITY, f_user.city);
					obj.put(Constants.JSONKeys.H_MOB, f_user.h_mob);
					obj.put(Constants.JSONKeys.P_MOB, f_user.p_mob);
					obj.put(Constants.JSONKeys.PIN, f_user.pin);
					obj.put(Constants.JSONKeys.STATE, f_user.state);
					obj.put(Constants.JSONKeys.STREET, f_user.street);

				} else {
					StudentFull s_user = (StudentFull) user;
					obj.put(Constants.JSONKeys.SECTION, s_user.section);
					obj.put(Constants.JSONKeys.YEAR, s_user.year);
					
					obj.put(Constants.JSONKeys.ROLL_NO, s_user.u_roll_no);
				}

				obj.put(Constants.JSONKeys.TOKEN, msg);
			} else
				obj.put(Constants.JSONKeys.ERROR, msg);
		} catch (Exception e) {
			return ConstructJSON(db, tagr, false, null, null, e.getMessage());
		}
		System.out.println(obj.toString());
		return obj.toString();
	}

	public static String ConstructJSON(DBConnection db, String tagr,
			boolean statusr, String msg) {
		return ConstructJSON(db, tagr, statusr, null, null, msg);
	}

	public static String ConstructJSONArray(ArrayList<?> data, String metadata)
			throws JSONException {
		// JSONObject obj=new JSONObject();
		JSONArray obja = new JSONArray();

		for (Object str : data) {
			if (metadata.equalsIgnoreCase("student")) {
				Student tmpusr = (Student) str; // studentMin to Student
				JSONObject tmpobj = new JSONObject();

				tmpobj.put(Constants.JSONKeys.FIRST_NAME, tmpusr.f_name);
				tmpobj.put(Constants.JSONKeys.LAST_NAME, tmpusr.l_name);
				tmpobj.put(Constants.JSONKeys.USER_ID, tmpusr.user_id);
				tmpobj.put(Constants.JSONKeys.PROFILE_IMAGE, tmpusr.picUrl);
				//tmpobj.put(Constants.JSONKeys.BRANCH, tmpusr.branch);
				tmpobj.put(Constants.JSONKeys.YEAR, tmpusr.year);
				tmpobj.put(Constants.JSONKeys.SECTION, tmpusr.section);
				//tmpobj.put(Constants.JSONKeys.COURSE, tmpusr.course);

				obja.put(tmpobj);
			} else {
				Faculty tmpusr = (Faculty) str; // FacultyMin to Faculty
				JSONObject tmpobj = new JSONObject();

				tmpobj.put(Constants.JSONKeys.FIRST_NAME, tmpusr.f_name);
				tmpobj.put(Constants.JSONKeys.LAST_NAME, tmpusr.l_name);
				tmpobj.put(Constants.JSONKeys.PROFILE_IMAGE, tmpusr.picUrl);
				tmpobj.put(Constants.JSONKeys.BRANCH, tmpusr.branch);
				//tmpobj.put(Constants.JSONKeys.COURSE, tmpusr.course);
				tmpobj.put(Constants.JSONKeys.USER_ID, tmpusr.user_id);
				obja.put(tmpobj);
			}
		}
		// System.out.println(obja.toString());
		return obja.toString();
	}

	public static String sha1(String input) {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(input.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			return sb.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String decode(String str) {
		return new String(Base64.decode(str)).trim();
	}

	public static String getToken(String input) {
		String token = sha1(input);
		return token;
	}

	public static void debug(Exception e) {
		e.printStackTrace();
	}
}