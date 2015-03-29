package com.sgi.webservice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.dao.DBConnection;
import com.sgi.dao.DbStructure;
import com.sgi.util.FacultyFull;
import com.sgi.util.InitialData;
import com.sgi.util.StudentFull;
import com.sgi.util.User;
import com.sgi.util.Utility;

@Path("/login")
public class Login {
	public static long counter = 0;

	private void print(String str) {
		System.out.println(str);
	}

	@POST
	@Path("/dologin")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String doLogin(InputStream in) {
		String str;

		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder strb = new StringBuilder();
		JSONObject input;
		String d_uname, d_pwd, token, regid;
		boolean is_faculty;
		DBConnection db = new DBConnection();
		JSONObject response = new JSONObject();

		try {
			while ((str = br.readLine()) != null) {
				strb.append(str);
			}

			input = new JSONObject(strb.toString());

			print(input.toString());

			d_uname = Utility.decode(input
					.getString(Constants.JSONKEYS.USER_ID));
			d_pwd = Utility.decode(input.getString(Constants.JSONKEYS.PSWD));
			is_faculty = input.getBoolean(Constants.JSONKEYS.FACULTY);
			regid = input.getString(Constants.JSONKEYS.REG_ID);

			/**
			 * if login successfull then send initial data and user data else
			 * say no
			 */

			if (checkCredentials(db, d_uname, d_pwd, is_faculty)) {
				token = Utility.getToken(d_pwd + counter);

				counter++;
				response.put(Constants.JSONKEYS.TAG,
						Constants.JSONKEYS.TAG_MSGS.LOGIN);
				response.put(Constants.JSONKEYS.STATUS, true);
				response.put(Constants.JSONKEYS.TOKEN, token);

				// insert a JSONObject with user details and a JSONArray with
				// initial data

				User user = db.getPersonalInfo(d_uname, is_faculty);
				// System.out.println("in status true");

				// System.out.print(user);
				JSONObject obj = new JSONObject();
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
				response.put(Constants.JSONKEYS.USER_DATA, obj);
				response.put(Constants.JSONKEYS.INITIAL_DATA, getInitial());

				// update device regid
				db.updateRegId(d_uname, regid);
				System.out.println(d_uname + " logged in\n"+response.toString());

			} else {

				response.put(Constants.JSONKEYS.TAG,
						Constants.JSONKEYS.TAG_MSGS.LOGIN);
				response.put(Constants.JSONKEYS.STATUS, false);
				response.put(Constants.JSONKEYS.ERROR,
						"Credentials not matched");

				System.out.println(d_uname
						+ " tried to login but wrong details");
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
		db.closeConnection();
		return response.toString();
	}

	public JSONObject getInitial() {

		DBConnection db = new DBConnection();

		InitialData idata = db.getInitialData();
		// make json data

		JSONObject resultobj = new JSONObject();
		try {
			JSONArray tmparry = new JSONArray();
			for (InitialData.Courses c : idata.courses) {
				JSONObject obj = new JSONObject();
				obj.put(DbStructure.COURSES.COLUMN_ID, c.id);
				obj.put(DbStructure.COURSES.COLUMN_DURATION, c.duration);
				obj.put(DbStructure.COURSES.COLUMN_NAME, c.name);
				tmparry.put(obj);
			}
			resultobj.put(Constants.JSONKEYS.COURSES, tmparry);

			tmparry = new JSONArray();
			for (InitialData.Branches b : idata.branches) {

				JSONObject obj = new JSONObject();
				obj.put(DbStructure.BRANCHES.COLUMN_COURSE_ID, b.course_id);
				obj.put(DbStructure.BRANCHES.COLUMN_ID, b.id);
				obj.put(DbStructure.BRANCHES.COLUMN_NAME, b.name);
				tmparry.put(obj);
			}
			resultobj.put(Constants.JSONKEYS.BRANCHES, tmparry);

			tmparry = new JSONArray();
			for (InitialData.Sections s : idata.sections) {

				JSONObject obj = new JSONObject();
				obj.put(DbStructure.SECTIONS.COLUMN_YEAR_ID, s.year_id);
				obj.put(DbStructure.SECTIONS.COLUMN_ID, s.id);
				obj.put(DbStructure.SECTIONS.COLUMN_NAME, s.name);
				tmparry.put(obj);
			}
			resultobj.put(Constants.JSONKEYS.SECTIONS, tmparry);

			tmparry = new JSONArray();
			for (InitialData.Year y : idata.years) {

				JSONObject obj = new JSONObject();
				obj.put(DbStructure.YEAR.COLUMN_BRANCH_ID, y.branch_id);
				obj.put(DbStructure.YEAR.COLUMN_ID, y.id);
				obj.put(DbStructure.YEAR.COLUMN_YEAR, y.year);
				tmparry.put(obj);
			}
			resultobj.put(Constants.JSONKEYS.YEARS, tmparry);

		} catch (JSONException e) {
			Utility.debug(e);
		}
		db.closeConnection();
		return resultobj;
	}

	public boolean checkCredentials(DBConnection db, String username,
			String pwd, boolean is_faculty) {
		return db.checkLogin(username, pwd, is_faculty);

	}
}
