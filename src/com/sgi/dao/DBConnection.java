package com.sgi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
				// System.out.println("new connection");
			}

		} catch (Exception e) {
			Utility.debug(e);

		}
	}

	public void closeConnection() {

		try {
			if (conn != null) {
				conn.close();
				// System.out.println("connection closed");
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
	}

	public void fillNotification(Notification noti) {

		try {
			MapperEntry mapper_e = new MapperEntry(0, noti.course, noti.branch,
					noti.year, noti.section);
			int target_id = -1;
			if ((target_id = createMapperEntry(mapper_e)) != -1) {

				String query = "insert into notification(faculty_id,text,time,title,target) values((select id from login where user_id='"
						+ noti.sid.trim()
						+ "') ,'"
						+ noti.text
						+ "', '"
						+ new Date(noti.time).toString()
						+ "', '"
						+ noti.subject + "', '" + target_id + "')";
				// System.out.println(query);
				Statement stm = conn.createStatement();
				stm.executeUpdate(query);
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
	}

	private int getPKOfUser(String userid) {
		int pk_of_user = -1;
		try {
			String get_id_query = "select id from login where user_id='"
					+ userid + "'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(get_id_query);

			if (rs.next()) {
				pk_of_user = rs.getInt(rs.findColumn("id"));
			} else {
				System.out.println("No such user");
				return pk_of_user;
			}
			rs.close();
		} catch (SQLException e) {
			Utility.debug(e);
			pk_of_user = -1;
		}
		return pk_of_user;
	}

	/**
	 * fill notifications from JSONArray to the database checked and working
	 * 
	 * @param notifications
	 *            {@link JSONArray} of notifications
	 * @param userid
	 *            {@link String} user_id like 'b-11-136'
	 * @return {@link JSONArray} of notification IDs received from client or
	 *         null if exception occurs
	 */
	public JSONArray fillNotifications(JSONArray notifications, String userid) {
		JSONArray noti_ids = new JSONArray();
		try {

			int len = notifications.length();
			if (len > 0) {
				// get the pk of user with userid
				// here user is sender
				int sender_pk = getPKOfUser(userid);

				StringBuilder query = new StringBuilder(
						"insert into notification(faculty_id,text,time,title,target,for_faculty) values");
				String str = "(?,?,?,?,?,?)";

				for (int i = 0; i < len; i++) {
					query.append(str);
					query.append(DbConstants.COMMA);
				}
				query.deleteCharAt(query.length() - 1);

				PreparedStatement stm = conn.prepareStatement(query.toString());
				int j = 1;
				JSONObject notification;

				MapperEntry mapper_e = null;
				ArrayList<MapperEntry> mapper_list = new ArrayList<MapperEntry>();
				int target_id;

				for (int i = 0; i < len; i++) {
					notification = notifications.getJSONObject(i);
					// For user mapper
					mapper_e = new MapperEntry(
							notification
									.getInt(Constants.JSONKEYS.NOTIFICATIONS.FOR_FACULTY),
							notification
									.getString(Constants.JSONKEYS.NOTIFICATIONS.COURSE),
							notification
									.getString(Constants.JSONKEYS.NOTIFICATIONS.BRANCH),
							notification
									.getString(Constants.JSONKEYS.NOTIFICATIONS.YEAR),
							notification
									.getString(Constants.JSONKEYS.NOTIFICATIONS.SECTION));
					mapper_list.add(mapper_e);
					target_id = createMapperEntry(mapper_e);
					// Insert notification into db one by one
					if (target_id != -1) {
						stm.setInt(j, sender_pk);
						stm.setString(
								j + 1,
								notification
										.getString(Constants.JSONKEYS.NOTIFICATIONS.TEXT));
						stm.setLong(j + 2, notification
								.getLong(Constants.JSONKEYS.NOTIFICATIONS.TIME));
						stm.setString(
								j + 3,
								notification
										.getString(Constants.JSONKEYS.NOTIFICATIONS.SUBJECT));
						stm.setInt(j + 4, target_id);
						stm.setInt(
								j + 5,
								notification
										.getInt(Constants.JSONKEYS.NOTIFICATIONS.FOR_FACULTY));
						noti_ids.put(notification
								.getInt(Constants.JSONKEYS.NOTIFICATIONS.ID));
						// fill_files(notification
						// .getJSONArray(Constants.JSONKEYS.NOTIFICATIONS.ATTACHMENTS),
						// sender_pk);

					} else {
						// if here there will be a problem as statement will be
						// containing more ? then the actual values
						// as we skipped some
						// we should reject the whole operations
					}
					j += 6;
				}
				if (stm.executeUpdate() > 0) {
					ResultSet rs = stm.getGeneratedKeys();
					int i = 0;
					while (rs.next()) {
						fill_user_notification_map(mapper_list.get(i),
								rs.getInt(1), sender_pk);
						i++;

					}
				}
				// insert notifications with state(PENDING) set by default in db
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
		return noti_ids;
	}

	public ResultSet fill_files(JSONArray attachments, String user_id) {
		int sender_pk = getPKOfUser(user_id);
		System.out.println("" + user_id + " pk :" + sender_pk);
		StringBuilder query = new StringBuilder(
				"insert into files(url,owner,time) values");
		String str = "(?,?,?)";
		String file_name = null;
		String[] temp = new String[2];
		JSONObject attachment;
		int len_attachments;
		ResultSet rs = null;
		int file_count = 0;
		Statement stmt;
		String query_count = "select count(*) from files";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query_count);
			rs.next();
			file_count = rs.getInt(1);			
			rs=null;
			len_attachments = attachments.length();
			System.out.println("file count : " + file_count+"len_attacdkjd: "+len_attachments);
			if (len_attachments > 0) {
				for (int a = 0; a < len_attachments; a++) {
					query.append(str);
					query.append(DbConstants.COMMA);
				}
				query.deleteCharAt(query.length() - 1);
				PreparedStatement stm = conn.prepareStatement(query.toString());
				int j = 1;
				for (int i = 0; i < len_attachments; i++) {
					attachment = attachments.getJSONObject(i);
			//		temp = attachment.getString(Constants.JSONKEYS.FILES.NAME)
				//			.split("\\.");
					// Eg: filename_id.extension
					file_name = Utility.setFileName(attachment.getString(Constants.JSONKEYS.FILES.NAME), file_count+1);
					stm.setString(j, "D://new/" + file_name);
					stm.setInt(j + 1, sender_pk);
					stm.setLong(j + 2, attachment
							.getLong(Constants.JSONKEYS.NOTIFICATIONS.TIME));
					j += 3;
					file_count++;
				}
				System.out.println(stm.toString());
				//stm.executeUpdate();
				if (stm.executeUpdate() > 0) {
					rs = stm.getGeneratedKeys();

					/*
					 * int i = 0; /= while (rs.next()) {
					 * fill_file_notification_map(); i++;
					 * 
					 * }
					 */
				}

			}
		} catch (Exception e) {
			Utility.debug(e);

		}
		return rs;
	}

	/**
	 * Fill the user_notification_map table with the target users of the
	 * notification
	 * 
	 * @param mapper_e
	 * @param notification_id
	 */

	private void fill_user_notification_map(MapperEntry mapper_e,
			int notification_id, int sender_pk_id) {

		ResultSet rs;
		Statement stm;
		PreparedStatement prep_stm;
		String query_temp = null;
		int rs_size, j = 1;

		StringBuilder query_stud = new StringBuilder(
				"insert into user_notification_map(notification_id,user_id,is_faculty) values");
		StringBuilder query_fac = new StringBuilder(
				"insert into user_notification_map(notification_id,user_id,is_faculty) values");
		String new_values = " (?,?,?)";
		try {

			// adding students
			if (mapper_e.FOR_FACULTY == Constants.FOR_FACULTY.NO) {
				if (mapper_e.COURSE.equalsIgnoreCase("all")) {
					query_temp = " select id,is_faculty from login where is_faculty='N'";

				} else {
					/**
					 * Case 2 : Particular course selected
					 */
					// strb.append("sdawdy")
					query_temp = " select login.id,is_faculty from login"
							+ " join students on login.id=students.l_id"
							+ " join sections on students.section_id=sections.id"
							+ " join year on sections.year_id=year.id"
							+ " join branches on year.branch_id=branches.id"
							+ " join courses on branches.course_id=courses.id"
							+ " where courses.name='" + mapper_e.COURSE + "'";
					if (!mapper_e.BRANCH.equalsIgnoreCase("all")) {
						query_temp += " and branches.name = '"
								+ mapper_e.BRANCH + "'";
					}
					if (!mapper_e.YEAR.equalsIgnoreCase("0")) {
						query_temp += " and year.year= '" + mapper_e.YEAR + "'";
					}
					if (!mapper_e.SECTION.equalsIgnoreCase("all")) {
						query_temp += " and sections.name='" + mapper_e.SECTION
								+ "'";
					}
					System.out.println("" + query_temp);
				}

				stm = conn.createStatement();
				rs = stm.executeQuery(query_temp); // rs contains all the users
				rs.last();
				rs_size = rs.getRow();
				System.out.print(rs_size + " students and ");
				if (rs_size > 0) {
					rs.beforeFirst();

					for (int i = 0; i < rs_size; i++) {
						query_stud.append(new_values);
						query_stud.append(DbConstants.COMMA);
					}
					query_stud.deleteCharAt(query_stud.length() - 1);
					prep_stm = conn.prepareStatement(query_stud.toString());
					while (rs.next()) { // insert all users in rs into u_n_map
										// table
						prep_stm.setInt(j, notification_id);
						prep_stm.setInt(j + 1, rs.getInt(1));
						prep_stm.setString(j + 2, rs.getString(2));
						j += 3;
					}
					prep_stm.executeUpdate();
					rs.close();
				}
			}
			// students added in list..
			// now select target faculty
			j = 1;
			if (mapper_e.COURSE.equalsIgnoreCase("all")) {
				query_temp = " select id,is_faculty from login where is_faculty='Y' and login.id<>"
						+ sender_pk_id;
			} else {
				query_temp = " select l.id,is_faculty from login as l"
						+ " join faculty as f on l.id=f.l_id"
						+ " join branches as b on f.branch_id=b.id"
						+ " join courses as c on b.course_id=c.id"
						+ " where l.id<>" + sender_pk_id + " and c.name='"
						+ mapper_e.COURSE + "'";
				if (!mapper_e.BRANCH.equalsIgnoreCase("all")) {
					query_temp += " and b.name ='" + mapper_e.BRANCH + "'";
				}
			}
			stm = conn.createStatement();
			rs = stm.executeQuery(query_temp); // rs contains all the users
			rs.last();
			rs_size = rs.getRow();
			System.out.println(rs_size
					+ " faculties are targetted for new notification");
			if (rs_size > 0) {
				rs.beforeFirst();
				for (int i = 0; i < rs_size; i++) {
					query_fac.append(new_values);
					query_fac.append(DbConstants.COMMA);
				}
				query_fac.deleteCharAt(query_fac.length() - 1);

				prep_stm = conn.prepareStatement(query_fac.toString());

				while (rs.next()) { // insert all users in rs into u_n_map table
					prep_stm.setInt(j, notification_id);
					prep_stm.setInt(j + 1, rs.getInt(1));
					prep_stm.setString(j + 2, rs.getString(2));
					j += 3;
				}
				prep_stm.executeUpdate();
				rs.close();
			}

		} catch (SQLException e) {
			Utility.debug(e);
		}

	}

	private int createMapperEntry(MapperEntry mapper_e) {
		// check for valid entries and enter in database
		int inserted_id = -1;
		String query = "insert into user_mapper(course,branch,year,section) values(?,?,?,?)";
		try {
			PreparedStatement stm = conn.prepareStatement(query,
					Statement.RETURN_GENERATED_KEYS);
			stm.setString(1, mapper_e.COURSE);
			stm.setString(2, mapper_e.BRANCH);
			stm.setString(3, mapper_e.YEAR);
			stm.setString(4, mapper_e.SECTION);
			if (stm.executeUpdate() > 0) {
				// if inserted successfully get the generated ids
				ResultSet rs = stm.getGeneratedKeys();
				if (rs.next()) {
					inserted_id = rs.getInt(1);
				}
				rs.close();
			} else {

				inserted_id = -1;
			}

		} catch (SQLException e) {
			Utility.debug(e);

			inserted_id = -1;
		}
		return inserted_id;
	}

	public boolean fillMessage(JSONObject msgs) {
		try {
			String query = "insert into messages(sender,text,time,receiver) values((select id from login where user_id='"
					+ msgs.getString(Constants.JSONKEYS.MESSAGES.SENDER)
					+ "'),'"
					+ msgs.getString(Constants.JSONKEYS.MESSAGES.TEXT)
					+ "','"
					+ msgs.getLong(Constants.JSONKEYS.MESSAGES.TIME)
					+ "',(select id from login where user_id='"
					+ msgs.getString(Constants.JSONKEYS.MESSAGES.RECEIVER)
					+ "'))";
			Statement stm = conn.createStatement();
			stm.executeUpdate(query);
			return true;
		} catch (Exception e) {
			Utility.debug(e);
			return false;
		}

	}

	/**
	 * fill {@link JSONArray} containing messages to database checked and its
	 * working
	 * 
	 * @param messages
	 *            {@link JSONArray} of messages received from client
	 * @param userid
	 *            {@link String} user_id like 'b-11-136'
	 * @return {@link JSONArray} of message IDs received from client or null if
	 *         exception occurs
	 */
	public JSONArray fillMessages(JSONArray messages, String userid) {
		JSONArray msg_ids = new JSONArray();
		try {
			int len = messages.length();
			if (len > 0) {
				int sender_pk = getPKOfUser(userid);
				StringBuilder query = new StringBuilder(
						"insert into messages(sender,text,time,receiver) values");
				String new_entry = "(?,?,?,?)";
				for (int i = 0; i < len; i++) {
					query.append(new_entry);
					if (i < len - 1)
						query.append(DbConstants.COMMA);
				}

				PreparedStatement stm = conn.prepareStatement(query.toString());
				JSONObject message;
				int j = 1;
				for (int i = 0; i < len; i++) {
					message = messages.getJSONObject(i);
					stm.setInt(j, sender_pk); // sender
					stm.setString(j + 1,
							message.getString(Constants.JSONKEYS.MESSAGES.TEXT));
					stm.setLong(j + 2,
							message.getLong(Constants.JSONKEYS.MESSAGES.TIME));
					stm.setInt(j + 3, getPKOfUser(message
							.getString(Constants.JSONKEYS.MESSAGES.RECEIVER)));
					j += 4;
					msg_ids.put(message.getInt(Constants.JSONKEYS.MESSAGES.ID));
				}
				stm.execute();
				// inserted messages with pending state default value set in db
				// :P
			}

		} catch (Exception e) {
			Utility.debug(e);
		}
		// System.out.println("message acks" + msg_ids);
		return msg_ids;
	}

	/**
	 * get notifications for a user if any
	 * 
	 * @param userid
	 * @return
	 */
	public JSONArray getNotificationsFromDb(String userid, boolean is_faculty) {
		JSONArray notifications = new JSONArray();

		try {
			String query;
			ResultSet rs;
			Statement stm = conn.createStatement();
			query = "select l.user_id,title,text,time,n.id,um.course,um.section,um.year,um.branch from notification as n join user_mapper as um on n.target=um.id join login as l "
					+ "on n.faculty_id=l.id join user_notification_map as unm on n.id=unm.notification_id where unm.user_id=(select id from login where user_id='"
					+ userid + "')";

			rs = stm.executeQuery(query);
			JSONObject notification;
			while (rs.next()) {

				notification = new JSONObject();
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.SENDER,
						rs.getString(1));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.SUBJECT,
						rs.getString(2));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.TEXT,
						rs.getString(3));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.TIME,
						rs.getString(4));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.ID,
						rs.getString(5));
				// for actual target identification
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.COURSE,
						rs.getString(6));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.SECTION,
						rs.getString(7));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.YEAR,
						rs.getString(8));
				notification.put(Constants.JSONKEYS.NOTIFICATIONS.BRANCH,
						rs.getString(9));
				notifications.put(notification);
			}
			rs.close();
		} catch (Exception e) {
			Utility.debug(e);
		}

		return notifications;
	}

	/**
	 * get messages for a user if any not checked
	 * 
	 * @param userid
	 * @return
	 * 
	 */
	public JSONArray getMessagesFromDb(String userid) {

		JSONArray messages = new JSONArray();
		try {
			int receiver_pk_id = getPKOfUser(userid);

			String query = "select login.user_id,text,time,is_group_msg,messages.id from messages join login on sender=login.id where receiver="
					+ receiver_pk_id + " and state=" + Constants.STATE.PENDING;

			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			JSONObject message;
			while (rs.next()) {
				message = new JSONObject();
				message.put(Constants.JSONKEYS.MESSAGES.SENDER, rs.getString(1));
				message.put(Constants.JSONKEYS.MESSAGES.TEXT, rs.getString(2));
				message.put(Constants.JSONKEYS.MESSAGES.TIME, rs.getLong(3));
				message.put(
						Constants.JSONKEYS.MESSAGES.IS_GROUP_MESSAGE,
						rs.getString(4).equalsIgnoreCase("N") ? Constants.IS_GROUP_MSG.NO
								: Constants.IS_GROUP_MSG.YES);
				message.put(Constants.JSONKEYS.MESSAGES.ID, rs.getInt(5));
				messages.put(message);
			}
		} catch (Exception e) {
			Utility.debug(e);
		}
		return messages;
	}

	public Boolean is_new_message(String userid) {
		try {
			String query = "SELECT sender,text from messages"
					+ " where state = 0 "
					+ "and receiver= (select id from login where user_id = '"
					+ userid + "')";
			System.out.println(query);
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			System.out.println(rs.next());
			return rs.next();
		} catch (Exception e) {
			Utility.debug(e);
			return false;
		}
	}

	public boolean authorizeUser(String userid, String token) {
		// System.out.print("authorizing user " + userid + " with token " +
		// token);
		userid = Utility.decode(userid);
		token = Utility.decode(token);
		try {

			String query = DbConstants.SELECT + "count(*)" + DbConstants.FROM
					+ DbStructure.LOGIN.TABLE_NAME + DbConstants.WHERE
					+ DbStructure.LOGIN.COLUMN_USER_ID + "='" + userid
					+ "' and " + DbStructure.LOGIN.COLUMN_TOKEN + "='" + token
					+ "';";
			// System.out.println(query);
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			if (rs.next()) {
				if (rs.getInt(1) == 1) {
					// System.out.println(" successful");
					return true;
				}
			}
			// System.out.println(" failed");
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

			// System.out.println("initial data set");
			return idata;

		} catch (Exception e) {
			Utility.debug(e);
			return null;
		}
	}

	public void updateMessageState(JSONArray msgids) {

		int len = msgids.length();
		if (len > 0) {
			try {

				StringBuilder query = new StringBuilder(
						"update messages set state=");
				query.append(Constants.STATE.ACK_RECEIVED);
				query.append(" where id IN (");
				for (int i = 0; i < len; i++) {
					query.append(msgids.getInt(i));
					query.append(DbConstants.COMMA);
				}
				query.deleteCharAt(query.length() - 1);
				query.append(DbConstants.PARENTESIS_CLOSE);
				Statement stm = conn.createStatement();

				stm.executeUpdate(query.toString());

			} catch (Exception e) {
				Utility.debug(e);
			}
		}
	}

	public void updateNotificationState(JSONArray noti_ids, String userid) {
		int len = noti_ids.length();
		int id = 0;
		String queryid = null;
		if (len > 0) {
			try {
				/*
				 * StringBuilder query = new StringBuilder(
				 * "update notification set state=");
				 * query.append(Constants.STATE.SENT);
				 * query.append(" where id IN ("); for (int i = 0; i < len; i++)
				 * { query.append(msgids.getInt(i));
				 * query.append(DbConstants.COMMA); }
				 * query.deleteCharAt(query.length() - 1);
				 * query.append(DbConstants.PARENTESIS_CLOSE);
				 */
				Statement stm = conn.createStatement();
				queryid = "select id from login where user_id='"
						+ Utility.decode(userid) + "'";
				ResultSet rs = stm.executeQuery(queryid);
				if (rs.next()) {
					id = rs.getInt(1);
				}
				StringBuilder query = new StringBuilder(
						"DELETE from user_notification_map where (notification_id,user_id) IN (");
				for (int i = 0; i < len; i++) {
					query.append(DbConstants.PARENTESIS_OPEN);
					query.append(noti_ids.getInt(i));
					query.append(DbConstants.COMMA);
					query.append(id);
					query.append(DbConstants.PARENTESIS_CLOSE);
					query.append(DbConstants.COMMA);
				}
				query.deleteCharAt(query.length() - 1);
				query.append(DbConstants.PARENTESIS_CLOSE);
				stm.executeUpdate(query.toString());
			} catch (Exception e) {
				Utility.debug(e);
			}
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
			// System.out.println(query);
			// System.out.println("matching\n" + pwd);
			ResultSet rs = stm.executeQuery(query);
			if (rs.next()) {
				// System.out.println(Utility.sha1(rs.getString(1)));
				if (Utility.sha1(rs.getString(1)).equals(pwd)) {
					query = "Update " + DbStructure.LOGIN.TABLE_NAME
							+ " set token='"
							+ Utility.sha1(pwd + Login.counter) + "' where "
							+ DbStructure.LOGIN.COLUMN_USER_ID + "='" + user
							+ "';";
					// System.out.println(query);
					if (stm.executeUpdate(query) == 1)
						return true;
					else {
						// System.out.println("problem inserting token");
					}
				}
			} else {
				// System.out.println("no data matched user input");
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
			// System.out.println(query);
			String f_name, l_name, picUrl, section, branch, street, city, state, pin, p_mob, h_mob, u_roll;
			int year;

			while (rs.next()) {
				// System.out.println(rs.getString(1));
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
			// System.out.println(query);
			ArrayList<Faculty> faculties = new ArrayList<Faculty>();
			while (rs.next()) {
				faculties.add(new Faculty(rs.getString(1), rs.getString(2), rs
						.getString(3), rs.getString(4), rs.getString(5)));
			}
			// System.out.println("returning " + faculties.size() +
			// " faculties\n" + faculties.toString());
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
			// System.out.println(query);
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

					Utility.debug(ex);
					// System.out.println("row discarded null value attribute");
				}
			}

			// System.out.println("returning " + students.size() +
			// " students \n "+ students.toString());

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
		// System.out.println(query);
		Statement stm;
		JSONObject obj;
		try {
			stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			obj = new JSONObject();
			if (rs.next()) {
				if (is_std) {
					obj.put(Constants.JSONKEYS.ROLL_NO, rs.getString(1));
				} else {
					obj.put(Constants.JSONKEYS.STATE, rs.getString(1));
					obj.put(Constants.JSONKEYS.CITY, rs.getString(2));
					obj.put(Constants.JSONKEYS.STATE, rs.getString(3));
					obj.put(Constants.JSONKEYS.PIN, rs.getString(4));
					obj.put(Constants.JSONKEYS.P_MOB, rs.getString(5));
					obj.put(Constants.JSONKEYS.H_MOB, rs.getString(6));
				}
			}

		} catch (Exception e) {
			Utility.debug(e);
			obj = new JSONObject();
			try {
				obj.put(Constants.JSONKEYS.ERROR, "fail to get or parse data");
			} catch (JSONException e1) {
				Utility.debug(e1);
			}
		}
		return obj.toString();
	}

	/**
	 * return full details of users whos login id is provided in the parameter
	 * 
	 * @param ids
	 *            {@link JSONArray} of Login ids of users whose details are
	 *            required
	 * @return {@link JSONObject} containing two or one depending on input
	 *         parameters data {@link JSONArray} one for {@link FacultyFull}
	 *         other for {@link StudentFull}
	 */
	public JSONObject getUsersDetail(JSONArray ids) {
		int len = ids.length();
		JSONObject result = new JSONObject();

		try {
			if (len > 0) {
				ArrayList<String> student_ids = new ArrayList<String>();
				ArrayList<String> faculty_ids = new ArrayList<String>();
				StringBuilder strb = new StringBuilder();
				ResultSet rs;
				Statement stm;
				String str;
				JSONArray tmp_user_arr;
				JSONObject tmp_user;
				// separate student and faculty IDs
				for (int i = 0; i < len; i++) {
					str = ids.getString(i);
					if (str.startsWith("e") || str.startsWith("E")) {
						faculty_ids.add(str);
					} else {
						student_ids.add(str);
					}
				}

				// get student details
				if (student_ids.size() > 0) {
					strb.append("select l.user_id, f_name, l_name, profile_url,u_roll_no,c.name,b.name,se.name,y.year from login as l join students as s on l.id=s.l_id join sections as se on s.section_id=se.id join year as y on se.year_id=y.id join branches as b on y.branch_id=b.id join courses as c on b.course_id=c.id where l.user_id IN (");
					for (String st : student_ids) {
						strb.append(DbConstants.SINGLE_QUOTE);
						strb.append(st);
						strb.append(DbConstants.SINGLE_QUOTE);
						strb.append(DbConstants.COMMA);
					}
					strb.deleteCharAt(strb.length() - 1);
					strb.append(DbConstants.PARENTESIS_CLOSE);
					// execute it
					stm = conn.createStatement();
					rs = stm.executeQuery(strb.toString());
					tmp_user_arr = new JSONArray();

					while (rs.next()) {
						// get the data
						tmp_user = new JSONObject();
						tmp_user.put(Constants.JSONKEYS.L_ID, rs.getString(1));
						tmp_user.put(Constants.JSONKEYS.FIRST_NAME,
								rs.getString(2));
						tmp_user.put(Constants.JSONKEYS.LAST_NAME,
								rs.getString(3));
						tmp_user.put(Constants.JSONKEYS.PROFILE_IMAGE,
								rs.getString(4));
						tmp_user.put(Constants.JSONKEYS.ROLL_NO,
								rs.getString(5));

						tmp_user.put(Constants.JSONKEYS.COURSE, rs.getString(6));
						tmp_user.put(Constants.JSONKEYS.BRANCH, rs.getString(7));
						tmp_user.put(Constants.JSONKEYS.YEAR, rs.getString(8));
						tmp_user.put(Constants.JSONKEYS.SECTION,
								rs.getString(9));

						tmp_user_arr.put(tmp_user);
					}
					rs.close();
					if (tmp_user_arr.length() > 0)
						result.put(Constants.JSONKEYS.STUDENT, tmp_user_arr);
				}
				// get faculty details
				strb.setLength(0);
				if (faculty_ids.size() > 0) {
					strb.append("select l.user_id, f_name, l_name, profile_url, c.name, b.name, d.street, d.city, d.state, d.pin, d.p_mob, d.h_mob from login as l join faculty as f on l.id=f.l_id join contact_info as d on l.id=d.usr_id join branches as b on f.branch_id=b.id join courses as c on b.course_id=c.id where l.user_id IN (");
					for (String st : faculty_ids) {
						strb.append(DbConstants.SINGLE_QUOTE);
						strb.append(st);
						strb.append(DbConstants.SINGLE_QUOTE);
						strb.append(DbConstants.COMMA);
					}
					strb.deleteCharAt(strb.length() - 1);
					strb.append(DbConstants.PARENTESIS_CLOSE);
					// execute it
					stm = conn.createStatement();
					rs = stm.executeQuery(strb.toString());
					tmp_user_arr = new JSONArray();
					while (rs.next()) {
						// get the data
						tmp_user = new JSONObject();

						tmp_user.put(Constants.JSONKEYS.L_ID, rs.getString(1));
						tmp_user.put(Constants.JSONKEYS.FIRST_NAME,
								rs.getString(2));
						tmp_user.put(Constants.JSONKEYS.LAST_NAME,
								rs.getString(3));
						tmp_user.put(Constants.JSONKEYS.PROFILE_IMAGE,
								rs.getString(4));

						tmp_user.put(Constants.JSONKEYS.COURSE, rs.getString(5));
						tmp_user.put(Constants.JSONKEYS.BRANCH, rs.getString(6));

						tmp_user.put(Constants.JSONKEYS.STREET, rs.getString(7));
						tmp_user.put(Constants.JSONKEYS.CITY, rs.getString(8));
						tmp_user.put(Constants.JSONKEYS.STATE, rs.getString(9));
						tmp_user.put(Constants.JSONKEYS.PIN, rs.getString(10));
						tmp_user.put(Constants.JSONKEYS.P_MOB, rs.getString(11));
						tmp_user.put(Constants.JSONKEYS.H_MOB, rs.getString(12));

						tmp_user_arr.put(tmp_user);
					}
					rs.close();
					if (tmp_user_arr.length() > 0)
						result.put(Constants.JSONKEYS.FACULTY, tmp_user_arr);
				}
			}
		} catch (JSONException e) {
			Utility.debug(e);
		} catch (SQLException e) {
			Utility.debug(e);
		}
		return result;
	}
}
