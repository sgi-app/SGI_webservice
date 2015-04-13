package com.sgi.util;

import java.security.MessageDigest;
import java.util.ArrayList;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.dao.DBConnection;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.BodyPart;

public class Utility {
	public static String sender_id;

	private static String DEBUG_FILE = "FILE: ";
	private static String DEBUG_METHOD = " METHOD: ";
	private static String DEBUG_LINE = " LINE: ";
	private static String DEBUG_NEW_LINE = "\n";
	private static String DEBUG_ERROR = "ERROR: ";

	private static Log log = LogFactory.getLog(Utility.class);

	public static JSONObject ConstructJSON(DBConnection db, String tagr,
			boolean statusr, String user_id, Boolean is_faculty, String msg) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(Constants.JSONKEYS.TAG, tagr);
			obj.put(Constants.JSONKEYS.STATUS, statusr);
			if (statusr) {
				User user = db.getPersonalInfo(user_id, is_faculty);
				// LOG("in status true");

				// System.out.print(user);

				obj.put(Constants.JSONKEYS.FIRST_NAME, user.f_name);
				obj.put(Constants.JSONKEYS.LAST_NAME, user.l_name);
				obj.put(Constants.JSONKEYS.PROFILE_IMAGE, user.picUrl);
				// obj.put(Constants.JSONKEYS.BRANCH, user.branch);
				if (is_faculty) {
					FacultyFull f_user = (FacultyFull) user;
					obj.put(Constants.JSONKEYS.BRANCH, f_user.branch);

					obj.put(Constants.JSONKEYS.CITY, f_user.city);
					obj.put(Constants.JSONKEYS.H_MOB, f_user.h_mob);
					obj.put(Constants.JSONKEYS.P_MOB, f_user.p_mob);
					obj.put(Constants.JSONKEYS.PIN, f_user.pin);
					obj.put(Constants.JSONKEYS.STATE, f_user.state);
					obj.put(Constants.JSONKEYS.STREET, f_user.street);

				} else {
					StudentFull s_user = (StudentFull) user;
					obj.put(Constants.JSONKEYS.SECTION, s_user.section);
					obj.put(Constants.JSONKEYS.YEAR, s_user.year);

					obj.put(Constants.JSONKEYS.ROLL_NO, s_user.u_roll_no);
				}

				obj.put(Constants.JSONKEYS.TOKEN, msg);
			} else
				obj.put(Constants.JSONKEYS.ERROR, msg);
		} catch (Exception e) {
			return ConstructJSON(db, tagr, false, null, null, e.getMessage());
		}
		// LOG(obj.toString());
		return obj;
	}

	public static void LOG(String str) {
		log.info(str);
		// System.out.println(str);
	}

	public static JSONObject ConstructJSON(DBConnection db, String tagr,
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

				tmpobj.put(Constants.JSONKEYS.FIRST_NAME, tmpusr.f_name);
				tmpobj.put(Constants.JSONKEYS.LAST_NAME, tmpusr.l_name);
				tmpobj.put(Constants.JSONKEYS.USER_ID, tmpusr.user_id);
				tmpobj.put(Constants.JSONKEYS.PROFILE_IMAGE, tmpusr.picUrl);
				// tmpobj.put(Constants.JSONKEYS.BRANCH, tmpusr.branch);
				tmpobj.put(Constants.JSONKEYS.YEAR, tmpusr.year);
				tmpobj.put(Constants.JSONKEYS.SECTION, tmpusr.section);
				// tmpobj.put(Constants.JSONKEYS.COURSE, tmpusr.course);

				obja.put(tmpobj);
			} else {
				Faculty tmpusr = (Faculty) str; // FacultyMin to Faculty
				JSONObject tmpobj = new JSONObject();

				tmpobj.put(Constants.JSONKEYS.FIRST_NAME, tmpusr.f_name);
				tmpobj.put(Constants.JSONKEYS.LAST_NAME, tmpusr.l_name);
				tmpobj.put(Constants.JSONKEYS.PROFILE_IMAGE, tmpusr.picUrl);
				tmpobj.put(Constants.JSONKEYS.BRANCH, tmpusr.branch);
				// tmpobj.put(Constants.JSONKEYS.COURSE, tmpusr.course);
				tmpobj.put(Constants.JSONKEYS.USER_ID, tmpusr.user_id);
				obja.put(tmpobj);
			}
		}
		// LOG(obja.toString());
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
		StringBuilder strb = new StringBuilder(DEBUG_ERROR + e.getMessage()
				+ DEBUG_NEW_LINE + e.getLocalizedMessage() + DEBUG_NEW_LINE);

		for (StackTraceElement se : e.getStackTrace())
			strb.append(DEBUG_FILE + se.getFileName() + DEBUG_LINE
					+ se.getLineNumber() + DEBUG_METHOD + se.getMethodName()
					+ DEBUG_NEW_LINE);
		log.error(strb.toString());
	}

	public static String getSenderKey() {
		return "AIzaSyDLu3sgf1P5tQXEpFC-y-yR5O0kUuAun44";
		/*
		 * if (sender_id == null) { try { BufferedReader br = new
		 * BufferedReader(new InputStreamReader( Application.class
		 * .getResourceAsStream("/WEB-INF/api.key"))); sender_id =
		 * br.readLine(); br.close(); return sender_id; } catch (Exception e) {
		 * Utility.debug(e); return null; } } else { return sender_id; }
		 */
	}

	/**
	 * get file name from the header of the bodypart
	 * 
	 * @param bp
	 * @return String filename extracted from header
	 */

	public static String getFileName(BodyPart bp) {
		String filename = "";
		MultivaluedMap<String, String> contentDisp = bp.getHeaders();
		System.out.println(contentDisp);
		String[] tokens = contentDisp.get("Content-Disposition").get(0)
				.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				filename = token.substring(token.indexOf("=") + 2,
						token.length() - 1);
				break;
			}
		}
		return filename;
	}

	/**
	 * sets the file name to be saved concatenates index in the file name index
	 * contains file id
	 * 
	 * @param fileName
	 * @param index
	 * @return string filename
	 */

	public static String setFileName(String fileName, int index) {
		int lastindexof = fileName.lastIndexOf(".");
		String extenstion = fileName.substring(lastindexof);
		String name = fileName.substring(0, lastindexof);
		fileName = name + "_" + index + extenstion;
		Utility.LOG("got filename=" + fileName + "\n" + name + "extend:"
				+ extenstion);
		return fileName;
	}

	public static String getFileStoreBase() {
		LOG("path reqrested");
		String path = "";
		try {
			path = System.getenv("OPENSHIFT_DATA_DIR")+"/files/";
			LOG("returning -> " + path);
		} catch (Exception e) {
			LOG("error finding path");
			debug(e);
		}
		return path;
	}

}