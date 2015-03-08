package com.sgi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.util.Faculty;
import com.sgi.util.FacultyFull;
import com.sgi.util.InitialData;
import com.sgi.util.InitialData.Branches;
import com.sgi.util.InitialData.Courses;
import com.sgi.util.InitialData.Sections;
import com.sgi.util.InitialData.Year;
import com.sgi.util.MapperEntry;
import com.sgi.util.Notification;
import com.sgi.util.Student;
import com.sgi.util.StudentFull;
import com.sgi.util.User;
import com.sgi.util.Utility;
import com.sgi.webservice.Login;

public class DBConnection {
	private Connection conn;

	public DBConnection() {
		setConnection();
	}

	public void setConnection() {

		try {
			if (conn == null || conn.isClosed()) {
				Class.forName(DbStructure.DB_CLASS);
				conn = DriverManager.getConnection(DbStructure.DB_URL);
				System.out.println("new connection");
			}

		} catch (Exception e) {
			Utility.debug(e);

		}
	}

	public void closeConnection() {

		try {
			if (conn != null) {
				conn.close();
				System.out.println("connection closed");
			}
		} catch (Exception e) {
			System.out.println("Eroor closing connections");
		}
	}

	public void fillNewNotification(Notification noti) {

		try {
			MapperEntry mapper_e = new MapperEntry();
			mapper_e.COURSE = noti.course;
			mapper_e.BRANCH = noti.branch;
			mapper_e.YEAR = noti.year;
			mapper_e.SECTION = noti.section;
			if (createMapperEntry(mapper_e)) {
				String query = "select LAST_INSERT_ID()";
				Statement stm = conn.createStatement();
				ResultSet rs = stm.executeQuery(query);
				int target_id = -1;
				if (rs.next())
					target_id = rs.getInt(1);
				query = "insert into notification(faculty_id,text,time,title,target) values((select id from login where user_id='"
						+ noti.sid.trim()
						+ "') ,'"
						+ noti.text
						+ "', '"
						+ new Date(noti.time).toString()
						+ "', '"
						+ noti.subject + "', '" + target_id + "')";
				System.out.println(query);
				stm.executeUpdate(query);
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
	}

	private boolean createMapperEntry(MapperEntry mapper_e) {
		// check for valid entries and enter in database
		String query = "insert into user_mapper(course,branch,year,section) values('"
				+ mapper_e.COURSE
				+ "','"
				+ mapper_e.BRANCH
				+ "','"
				+ mapper_e.YEAR + "','" + mapper_e.SECTION + "')";

		try {
			Statement stm = conn.createStatement();
			stm.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			Utility.debug(e);
			return false;
		}

	}

	public boolean fillMessage(JSONObject msgs) {
		try {
			String query = "insert into messages(sender,text,time,receiver) values((select id from login where user_id='"
					+ msgs.getString(Constants.JSONMessageKeys.SENDER)
					+ "'),'"
					+ msgs.getString(Constants.JSONMessageKeys.TEXT)
					+ "','"
					+ msgs.getLong(Constants.JSONMessageKeys.TIME)
					+ "',(select id from login where user_id='"
					+ msgs.getString(Constants.JSONMessageKeys.RECEIVER)
					+ "'))";
			System.out.println(query);
			Statement stm = conn.createStatement();
			stm.executeUpdate(query);
			return true;
		} catch (Exception e) {
			Utility.debug(e);
			return false;
		}

	}

	public JSONArray fetchMessages(String userid) {

		JSONArray result = new JSONArray();
		try {
			String query = "select login.user_id,text,time,is_group_msg,messages.id from messages join login on sender=login.id where receiver=(select id from login where user_id='"
					+ userid + "') and state=" + Constants.MsgState.TO_SEND;
			System.out.println(query);
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			JSONObject obj;
			while (rs.next()) {
				obj = new JSONObject();
				obj.put(Constants.JSONMessageKeys.SENDER, rs.getString(1));
				obj.put(Constants.JSONMessageKeys.TEXT, rs.getString(2));
				obj.put(Constants.JSONMessageKeys.TIME, rs.getLong(3));
				obj.put(Constants.JSONMessageKeys.IS_GROUP_MESSAGE, rs
						.getString(4).equalsIgnoreCase("N") ? 0 : 1);
				obj.put(Constants.JSONMessageKeys.ID, rs.getInt(5));
				result.put(obj);
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
		System.out.println(result.toString());
		return result;
	}

	public boolean authorizeUser(String userid, String token) {
		System.out.print("authorizing user " + userid + " with token " + token);
		userid = Utility.decode(userid);
		token = Utility.decode(token);
		try {

			String query = DbConstants.SELECT + "count(*)" + DbConstants.FROM
					+ DbStructure.LOGIN.TABLE_NAME + DbConstants.WHERE
					+ DbStructure.LOGIN.COLUMN_USER_ID + "='" + userid
					+ "' and " + DbStructure.LOGIN.COLUMN_TOKEN + "='" + token
					+ "';";
			System.out.println(query);
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			if (rs.next()) {
				if (rs.getInt(1) == 1) {
					System.out.println(" successful");
					return true;
				}
			}
			System.out.println(" failed");
			return false;

		} catch (Exception e) {
			Utility.debug(e);
			return false;
		}
	}

	public InitialData getInitialData() {

		try {
			InitialData idata = new InitialData();

			Statement stm = conn.createStatement();
			String query = "select * from " + DbStructure.COURSES.TABLE_NAME;
			ResultSet rs = stm.executeQuery(query);
			Courses course;
			while (rs.next()) {
				course = new Courses();
				course.id = rs.getInt(DbStructure.COURSES.COLUMN_ID);
				course.name = rs.getString(DbStructure.COURSES.COLUMN_NAME);
				course.duration = rs
						.getInt(DbStructure.COURSES.COLUMN_DURATION);
				idata.courses.add(course);
			}
			query = "select * from " + DbStructure.BRANCHES.TABLE_NAME;
			rs = stm.executeQuery(query);
			Branches branch;
			while (rs.next()) {
				branch = new Branches();
				branch.id = rs.getInt(DbStructure.BRANCHES.COLUMN_ID);
				branch.course_id = rs
						.getInt(DbStructure.BRANCHES.COLUMN_COURSE_ID);
				branch.name = rs.getString(DbStructure.BRANCHES.COLUMN_NAME);

				idata.branches.add(branch);
			}
			query = "select * from " + DbStructure.SECTIONS.TABLE_NAME;
			rs = stm.executeQuery(query);
			Sections section = new Sections();
			while (rs.next()) {
				section = new Sections();
				section.year_id = rs
						.getInt(DbStructure.SECTIONS.COLUMN_YEAR_ID);
				section.id = rs.getInt(DbStructure.SECTIONS.COLUMN_ID);
				section.name = rs.getString(DbStructure.SECTIONS.COLUMN_NAME);
				idata.sections.add(section);
			}
			query = "select * from " + DbStructure.YEAR.TABLE_NAME;
			rs = stm.executeQuery(query);
			Year year = new Year();
			while (rs.next()) {
				year = new Year();
				year.branch_id = rs.getInt(DbStructure.YEAR.COLUMN_BRANCH_ID);
				year.id = rs.getInt(DbStructure.YEAR.COLUMN_ID);
				year.year = rs.getInt(DbStructure.YEAR.COLUMN_YEAR);
				idata.years.add(year);
			}

			System.out.println("initial data set");
			return idata;

		} catch (Exception e) {
			Utility.debug(e);
			return null;
		}
	}

	public void updateMessageState(JSONArray msgids) {

		int len = msgids.length();
		try {

			String query = "update messages set state="
					+ Constants.MsgState.SENT_SUCESSFULLY + " where id IN (";
			for (int i = 0; i < len; i++)
				query += msgids.getInt(i) + (i == len - 1 ? "" : ",");
			query += ")";

			Statement stm = conn.createStatement();
			stm.executeUpdate(query);

		} catch (Exception e) {
			Utility.debug(e);
		}
	}

	public boolean checkLogin(String user, String pwd, boolean is_faculty) {

		try {
			Statement stm = conn.createStatement();
			String query = "Select " + DbStructure.LOGIN.COLUMN_PASSWORD
					+ " from " + DbStructure.LOGIN.TABLE_NAME + " where "
					+ DbStructure.LOGIN.COLUMN_USER_ID + "='"
					+ user.toUpperCase() + "' and "
					+ DbStructure.LOGIN.COLUMN_IS_FACULTY + "='"
					+ (is_faculty ? 'Y' : 'N') + "';";
			System.out.println(query);
			System.out.println("matching\n" + pwd);
			ResultSet rs = stm.executeQuery(query);
			if (rs.next()) {
				System.out.println(Utility.sha1(rs.getString(1)));
				if (Utility.sha1(rs.getString(1)).equals(pwd)) {
					query = "Update " + DbStructure.LOGIN.TABLE_NAME
							+ " set token='"
							+ Utility.sha1(pwd + Login.counter) + "' where "
							+ DbStructure.LOGIN.COLUMN_USER_ID + "='" + user
							+ "';";
					System.out.println(query);
					if (stm.executeUpdate(query) == 1)
						return true;
					else
						System.out.println("problem inserting token");
				}
			} else {
				System.out.println("no data matched user input");
			}
			return false;
		} catch (SQLException e) {
			Utility.debug(e);
			return false;
		}
	}

	public User getPersonalInfo(String user_id, Boolean is_faculty) {
		String query;
		User user = null;

		// Personal_info pi = new Personal_info();
		try {

			Statement stm = conn.createStatement();
			// get full details
			if (is_faculty) {
				query = DbConstants.SELECT
						+ DbStructure.FACULTY.COLUMN_F_NAME // 1
						+ DbConstants.COMMA
						+ DbStructure.FACULTY.COLUMN_L_NAME // 2
						+ DbConstants.COMMA
						+ DbStructure.FACULTY.COLUMN_PROFILE_URL // 3
						+ DbConstants.COMMA
						+ DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.BRANCHES.COLUMN_NAME // 4

						+ DbConstants.COMMA
						+ DbStructure.CONTACT_INFO.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_STREET // 5
						+ DbConstants.COMMA
						+ DbStructure.CONTACT_INFO.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_CITY // 6
						+ DbConstants.COMMA
						+ DbStructure.CONTACT_INFO.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_STATE // 7
						+ DbConstants.COMMA
						+ DbStructure.CONTACT_INFO.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_P_MOB // 8
						+ DbConstants.COMMA
						+ DbStructure.CONTACT_INFO.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_H_MOB // 9
						+ DbConstants.COMMA
						+ DbStructure.CONTACT_INFO.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_PIN // 10

						+ DbConstants.FROM + DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.JOIN + DbStructure.FACULTY.TABLE_NAME
						+ DbConstants.ON + DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.DOT + DbStructure.LOGIN.COLUMN_ID
						+ DbConstants.EQUALS + DbStructure.FACULTY.TABLE_NAME
						+ DbConstants.DOT + DbStructure.FACULTY.COLUMN_LOGIN_ID
						+ DbConstants.JOIN + DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.ON + DbStructure.FACULTY.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.FACULTY.COLUMN_BRANCH_ID
						+ DbConstants.EQUALS + DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT + DbStructure.BRANCHES.COLUMN_ID

						+ DbConstants.JOIN + DbStructure.COURSES.TABLE_NAME
						+ DbConstants.ON + DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.BRANCHES.COLUMN_COURSE_ID
						+ DbConstants.EQUALS + DbStructure.COURSES.TABLE_NAME
						+ DbConstants.DOT + DbStructure.COURSES.COLUMN_ID

						+ DbConstants.JOIN
						+ DbStructure.CONTACT_INFO.TABLE_NAME + DbConstants.ON
						+ DbStructure.CONTACT_INFO.TABLE_NAME + DbConstants.DOT
						+ DbStructure.CONTACT_INFO.COLUMN_USER_ID
						+ DbConstants.EQUALS + DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.DOT + DbStructure.LOGIN.COLUMN_ID

						+ DbConstants.WHERE + DbStructure.LOGIN.COLUMN_USER_ID
						+ DbConstants.EQUALS + "'" + user_id + "';";
			} else {
				query = DbConstants.SELECT
						+ DbStructure.STUDENTS.COLUMN_F_NAME // 1
						+ DbConstants.COMMA
						+ DbStructure.STUDENTS.COLUMN_L_NAME // 2
						+ DbConstants.COMMA
						+ DbStructure.STUDENTS.COLUMN_PROFILE // 3
						+ DbConstants.COMMA
						+ DbStructure.STUDENTS.COLUMN_U_ROLL_NO // 4
						+ DbConstants.COMMA
						+ DbStructure.SECTIONS.COLUMN_NAME // 5
						+ DbConstants.COMMA
						+ DbStructure.YEAR.COLUMN_YEAR // 6

						+ DbConstants.FROM + DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.JOIN + DbStructure.STUDENTS.TABLE_NAME
						+ DbConstants.ON + DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.DOT + DbStructure.LOGIN.COLUMN_ID
						+ DbConstants.EQUALS + DbStructure.STUDENTS.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.STUDENTS.COLUMN_LOGIN_ID
						+ DbConstants.JOIN + DbStructure.SECTIONS.TABLE_NAME
						+ DbConstants.ON + DbStructure.STUDENTS.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.STUDENTS.COLUMN_SECTION_ID
						+ DbConstants.EQUALS + DbStructure.SECTIONS.TABLE_NAME
						+ DbConstants.DOT + DbStructure.SECTIONS.COLUMN_ID
						+ DbConstants.JOIN + DbStructure.YEAR.TABLE_NAME
						+ DbConstants.ON + DbStructure.SECTIONS.TABLE_NAME
						+ DbConstants.DOT + DbStructure.SECTIONS.COLUMN_YEAR_ID
						+ DbConstants.EQUALS + DbStructure.YEAR.TABLE_NAME
						+ DbConstants.DOT + DbStructure.YEAR.COLUMN_ID
						+ DbConstants.WHERE + DbStructure.LOGIN.COLUMN_USER_ID
						+ DbConstants.EQUALS + "'" + user_id + "';";
			}
			ResultSet rs = stm.executeQuery(query);
			System.out.println(query);
			String f_name, l_name, picUrl, section, branch, street, city, state, pin, p_mob, h_mob, u_roll;
			int year;

			while (rs.next()) {
				System.out.println(rs.getString(1));
				f_name = rs.getString(1);
				l_name = rs.getString(2);
				picUrl = rs.getString(3);

				if (is_faculty) {
					branch = rs.getString(4);
					street = rs.getString(5);
					city = rs.getString(6);
					state = rs.getString(7);
					p_mob = rs.getString(8);
					h_mob = rs.getString(9);
					pin = rs.getString(10);
					user = new FacultyFull(f_name, l_name, branch, picUrl,
							user_id, street, city, state, pin, p_mob, h_mob);
				} else {
					u_roll = rs.getString(4);
					section = rs.getString(5);
					year = rs.getInt(6);
					user = new StudentFull(f_name, l_name, user_id, picUrl,
							year, section, u_roll);
				}
			}
			return user;
		} catch (SQLException e) {
			Utility.debug(e);
			return null;
		}
	}

	public String send_faculty_list(String department, String course) {

		try {

			String query;
			if (course.equalsIgnoreCase("All"))
				/*
				 * query=
				 * "select f_name,l_name,user_id,branches.name,profile_url,is_online,p_mob from faculty "
				 * +
				 * "join login on l_id=login.id join contact_info on usr_id=login.id "
				 * +
				 * "join branches on faculty.branch_id=branches.id order by f_name;"
				 * ; else if(department.equalsIgnoreCase("All") ){ query=
				 * "select f_name,l_name,user_id,branches.name,profile_url,is_online,p_mob from faculty "
				 * +
				 * "join login on l_id=login.id join contact_info on usr_id=login.id "
				 * +
				 * "join branches on faculty.branch_id=branches.id order by f_name "
				 * + "join courses on branches.course_id=courses.id" +
				 * "where courses.name='"+course+"' order by f_name;"; } else{
				 * query=
				 * "select f_name,l_name,user_id,branches.name,profile_url,is_online,p_mob from faculty "
				 * +
				 * "join login on l_id=login.id join contact_info on usr_id=login.id "
				 * +
				 * "join branches on faculty.branch_id=branches.id order by f_name "
				 * + "join courses on branches.course_id=courses.id" +
				 * "where courses.name='"
				 * +course+"' and branches.name='"+department
				 * +"' order by f_name;"; }
				 */
				query = DbConstants.SELECT
						+ DbStructure.FACULTY.COLUMN_F_NAME
						+ DbConstants.COMMA
						+ DbStructure.FACULTY.COLUMN_L_NAME
						+ DbConstants.COMMA
						+ DbStructure.FACULTY.COLUMN_PROFILE_URL
						+ DbConstants.COMMA
						+ DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.BRANCHES.COLUMN_NAME
						// + DbConstants.COMMA + DbStructure.COURSES.TABLE_NAME
						// + DbConstants.DOT + DbStructure.COURSES.COLUMN_NAME
						+ DbConstants.COMMA + DbStructure.LOGIN.COLUMN_USER_ID
						+ DbConstants.FROM

						+ DbStructure.LOGIN.TABLE_NAME + DbConstants.JOIN
						+ DbStructure.FACULTY.TABLE_NAME + DbConstants.ON

						+ DbStructure.LOGIN.TABLE_NAME + DbConstants.DOT
						+ DbStructure.LOGIN.COLUMN_ID + DbConstants.EQUALS
						+ DbStructure.FACULTY.TABLE_NAME + DbConstants.DOT
						+ DbStructure.FACULTY.COLUMN_LOGIN_ID

						+ DbConstants.JOIN + DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.ON + DbStructure.FACULTY.COLUMN_BRANCH_ID
						+ DbConstants.EQUALS + DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT + DbStructure.BRANCHES.COLUMN_ID
						+ DbConstants.JOIN + DbStructure.COURSES.TABLE_NAME
						+ DbConstants.ON
						+ DbStructure.BRANCHES.COLUMN_COURSE_ID
						+ DbConstants.EQUALS + DbStructure.COURSES.TABLE_NAME
						+ DbConstants.DOT + DbStructure.COURSES.COLUMN_ID
						+ DbConstants.SEMICOLON;
			// query="select f_name,l_name,profile_url,branches.name,courses.name,l_id from faculty join branches on branch_id=branches.id join courses on course_id=courses.id";
			else
				query = DbConstants.SELECT
						+ DbStructure.FACULTY.COLUMN_F_NAME
						+ DbConstants.COMMA
						+ DbStructure.FACULTY.COLUMN_L_NAME
						+ DbConstants.COMMA
						+ DbStructure.FACULTY.COLUMN_PROFILE_URL
						+ DbConstants.COMMA
						+ DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.BRANCHES.COLUMN_NAME
						+ DbConstants.COMMA
						+ DbStructure.COURSES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.COURSES.COLUMN_NAME
						+ DbConstants.COMMA
						+ DbStructure.LOGIN.COLUMN_USER_ID
						+ DbConstants.FROM
						+ DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.JOIN
						+ DbStructure.FACULTY.TABLE_NAME
						+ DbConstants.ON

						+ DbStructure.LOGIN.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.LOGIN.COLUMN_ID
						+ DbConstants.EQUALS
						+ DbStructure.FACULTY.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.FACULTY.COLUMN_LOGIN_ID

						+ DbConstants.JOIN
						+ DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.ON
						+ DbStructure.FACULTY.COLUMN_BRANCH_ID
						+ DbConstants.EQUALS
						+ DbStructure.BRANCHES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.BRANCHES.COLUMN_ID
						+ DbConstants.JOIN
						+ DbStructure.COURSES.TABLE_NAME
						+ DbConstants.ON
						+ DbStructure.BRANCHES.COLUMN_COURSE_ID
						+ DbConstants.EQUALS
						+ DbStructure.COURSES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.COURSES.COLUMN_ID
						+ DbConstants.WHERE
						+ DbStructure.COURSES.TABLE_NAME
						+ DbConstants.DOT
						+ DbStructure.COURSES.COLUMN_NAME
						+ DbConstants.EQUALS
						+ "'"
						+ course
						+ "'"
						+ (department.equalsIgnoreCase("All") ? "" : " and "
								+ DbStructure.BRANCHES.TABLE_NAME
								+ DbConstants.DOT
								+ DbStructure.BRANCHES.COLUMN_NAME + "='"
								+ department + "';");
			// query="select
			// f_name,l_name,profile_url,courses.name,l_id from
			// faculty join branches on branch_id =branches.id join courses on
			// course_id=courses.id
			// "where courses.name='"+course+"' "+(department.equalsIgnoreCase("All")?"":"and branches.name='"+department+"'");
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			System.out.println(query);
			ArrayList<Faculty> faculties = new ArrayList<Faculty>();
			while (rs.next()) {
				faculties.add(new Faculty(rs.getString(1), rs.getString(2), rs
						.getString(3), rs.getString(4), rs.getString(5)));
			}
			System.out.println("returning " + faculties.size() + " faculties\n"
					+ faculties.toString());
			return Utility.ConstructJSONArray(faculties, "faculty");
		} catch (Exception e) {
			Utility.debug(e);
			return null;
		}
	}

	public String send_student_list(int year, String department, String course,
			String section) {
		try {

			String query;
			/*
			 * if(year==0 && department.equalsIgnoreCase("All")) query=
			 * "select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id;"
			 * ; else if(year==0) query=
			 * "select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where branch='"
			 * +department+"';"; else if(department.equalsIgnoreCase("All"))
			 * query=
			 * "select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where year="
			 * +year+";"; else query=
			 * "select f_name,l_name,branch,profile_url,year,user_id,section,is_online from students join login on l_id=login.id where year="
			 * +year+" and branch='"+department+"';";
			 */
			query = "select "
					+ DbStructure.STUDENTS.COLUMN_F_NAME // 1
					+ DbConstants.COMMA
					+ DbStructure.STUDENTS.COLUMN_L_NAME // 2
					+ DbConstants.COMMA
					+ DbStructure.LOGIN.COLUMN_USER_ID // 3
					+ DbConstants.COMMA
					+ DbStructure.STUDENTS.COLUMN_PROFILE // 4
					// + DbConstants.COMMA
					// + DbStructure.BRANCHES.TABLE_NAME
					// + DbConstants.DOT
					// + DbStructure.BRANCHES.COLUMN_NAME
					+ DbConstants.COMMA
					+ DbStructure.YEAR.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.YEAR.COLUMN_YEAR // 5
					+ DbConstants.COMMA
					+ DbStructure.SECTIONS.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.SECTIONS.COLUMN_NAME // 6
					// + DbConstants.COMMA
					// + DbStructure.COURSES.TABLE_NAME
					// + DbConstants.DOT
					// + DbStructure.COURSES.COLUMN_NAME
					+ DbConstants.FROM
					+ DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.JOIN
					+ DbStructure.STUDENTS.TABLE_NAME
					+ DbConstants.ON
					+ DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.LOGIN.COLUMN_ID
					+ DbConstants.EQUALS
					+ DbStructure.STUDENTS.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.STUDENTS.COLUMN_LOGIN_ID
					+ DbConstants.JOIN
					+ DbStructure.SECTIONS.TABLE_NAME
					+ DbConstants.ON
					+ DbStructure.STUDENTS.COLUMN_SECTION_ID
					+ DbConstants.EQUALS
					+ DbStructure.SECTIONS.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.SECTIONS.COLUMN_ID
					+ DbConstants.JOIN
					+ DbStructure.YEAR.TABLE_NAME
					+ DbConstants.ON
					+ DbStructure.SECTIONS.COLUMN_YEAR_ID
					+ DbConstants.EQUALS
					+ DbStructure.YEAR.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.YEAR.COLUMN_ID
					+ DbConstants.JOIN
					+ DbStructure.BRANCHES.TABLE_NAME
					+ DbConstants.ON
					+ DbStructure.YEAR.COLUMN_BRANCH_ID
					+ DbConstants.EQUALS
					+ DbStructure.BRANCHES.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.BRANCHES.COLUMN_ID
					+ DbConstants.JOIN
					+ DbStructure.COURSES.TABLE_NAME
					+ DbConstants.ON
					+ DbStructure.BRANCHES.COLUMN_COURSE_ID
					+ DbConstants.EQUALS
					+ DbStructure.COURSES.TABLE_NAME
					+ DbConstants.DOT
					+ DbStructure.COURSES.COLUMN_ID
					+ ((course.equalsIgnoreCase("All")) ? " "
							: ((DbConstants.WHERE
									+ DbStructure.COURSES.TABLE_NAME
									+ DbConstants.DOT
									+ DbStructure.COURSES.COLUMN_NAME + "='"
									+ course + "'")
									+ ((department.equalsIgnoreCase("All")) ? " "
											: (" and "
													+ DbStructure.BRANCHES.TABLE_NAME
													+ DbConstants.DOT
													+ DbStructure.BRANCHES.COLUMN_NAME
													+ "='" + department + "'"))
									+ ((year == 0 ? " " : (" and "
											+ DbStructure.YEAR.TABLE_NAME
											+ DbConstants.DOT
											+ DbStructure.YEAR.COLUMN_YEAR
											+ "='" + year + "' "))) + ((department
									.equalsIgnoreCase("All") || year == 0) ? " "
									: (section.equalsIgnoreCase("All") ? " "
											: (" and "
													+ DbStructure.SECTIONS.TABLE_NAME
													+ DbConstants.DOT
													+ DbStructure.SECTIONS.COLUMN_NAME
													+ "='" + section + "' ")))));

			/*
			 * query=
			 * "select f_name,l_name,l_id,profile_url,year.year,sections.name from login join"
			 * +
			 * "students on login.id=students.l_id join sections on section_id=sections.id "
			 * +"join year on year_id=year.id "
			 * +"join branches on branch_id=branches.id "
			 * +"join courses on course_id=courses.id "
			 * +((course.equalsIgnoreCase
			 * ("All"))?" ":(("where courses.name='"+course+"'")
			 * +((department.equalsIgnoreCase
			 * ("All"))?" ":(" and branches.name='"+department+"'"))
			 * +((year==0?" ":(" and year.year="+year+" ")))
			 * +((department.equalsIgnoreCase("All") ||
			 * year==0)?" ":(section.equalsIgnoreCase
			 * ("All")?" ":(" and sections.name='"+section+"' ")))));
			 */
			System.out.println(query);
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			ArrayList<Student> students = new ArrayList<Student>();
			Student tmp;
			while (rs.next()) {
				try {
					tmp = new Student(rs.getString(1), rs.getString(2),
							rs.getString(3), rs.getString(4), rs.getInt(5),
							rs.getString(6));
					students.add(tmp);
				} catch (NullPointerException ex) {
					System.out.println("row discarded null value attribute");
				}
			}

			System.out.println("returning " + students.size() + " students \n "
					+ students.toString());

			return Utility.ConstructJSONArray(students, "student");
		} catch (Exception e) {
			Utility.debug(e);
			return null;
		}
	}

	public String getuserInfo(String u_id, boolean is_std) {
		String query;
		if (is_std) {
			query = DbConstants.SELECT + DbStructure.STUDENTS.COLUMN_U_ROLL_NO
					+ DbConstants.FROM + DbStructure.STUDENTS.TABLE_NAME
					+ DbConstants.JOIN + DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.ON + DbStructure.STUDENTS.COLUMN_LOGIN_ID
					+ DbConstants.EQUALS + DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.DOT + DbStructure.LOGIN.COLUMN_ID
					+ DbConstants.WHERE + DbStructure.LOGIN.COLUMN_USER_ID
					+ "='" + u_id + "';";

			// query="select u_roll_no from students join login on l_id=login.id where user_id="+u_id;
		} else {
			query = DbConstants.SELECT + DbStructure.CONTACT_INFO.COLUMN_STREET
					+ DbConstants.COMMA + DbStructure.CONTACT_INFO.COLUMN_CITY
					+ DbConstants.COMMA + DbStructure.CONTACT_INFO.COLUMN_STATE
					+ DbConstants.COMMA + DbStructure.CONTACT_INFO.COLUMN_PIN
					+ DbConstants.COMMA + DbStructure.CONTACT_INFO.COLUMN_P_MOB
					+ DbConstants.COMMA + DbStructure.CONTACT_INFO.COLUMN_H_MOB
					+ DbConstants.FROM + DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.JOIN + DbStructure.CONTACT_INFO.TABLE_NAME
					+ DbConstants.ON + DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.DOT + DbStructure.LOGIN.COLUMN_ID
					+ DbConstants.EQUALS
					+ DbStructure.CONTACT_INFO.COLUMN_USER_ID
					+ DbConstants.WHERE + DbStructure.LOGIN.TABLE_NAME
					+ DbConstants.DOT + DbStructure.LOGIN.COLUMN_USER_ID + "='"
					+ u_id + "';";
			// query="select street,city,state,pin,p_mob,h_mob
			// from login join contact_info on
			// login.id=usr_id
			// where login.user_id="+u_id;
		}
		System.out.println(query);
		Statement stm;
		JSONObject obj;
		try {
			stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			obj = new JSONObject();
			if (rs.next()) {
				if (is_std) {
					obj.put(Constants.JSONKeys.ROLL_NO, rs.getString(1));
				} else {
					obj.put(Constants.JSONKeys.STATE, rs.getString(1));
					obj.put(Constants.JSONKeys.CITY, rs.getString(2));
					obj.put(Constants.JSONKeys.STATE, rs.getString(3));
					obj.put(Constants.JSONKeys.PIN, rs.getString(4));
					obj.put(Constants.JSONKeys.P_MOB, rs.getString(5));
					obj.put(Constants.JSONKeys.H_MOB, rs.getString(6));
				}
			}

		} catch (Exception e) {
			Utility.debug(e);
			obj = new JSONObject();
			try {
				obj.put(Constants.JSONKeys.ERROR, "fail to get or parse data");
			} catch (JSONException e1) {
				Utility.debug(e1);
			}
		}
		return obj.toString();
	}
}
