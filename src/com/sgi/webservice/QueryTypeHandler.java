package com.sgi.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sgi.constants.Constants;
import com.sgi.dao.DBConnection;
import com.sgi.util.Notification;
import com.sgi.util.Utility;
import com.sun.jersey.core.util.Base64;

@Path("/query")
public class QueryTypeHandler {

	@GET
	@Path("/type_resolver")
	@Produces(MediaType.APPLICATION_JSON)
	public String typeResolver(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token,
			@QueryParam(Constants.QueryParameters.USER_TYPE) boolean student,
			@QueryParam(Constants.QueryParameters.BRANCH) String branch,
			@QueryParam(Constants.QueryParameters.YEAR) int year,
			@QueryParam(Constants.QueryParameters.SECTION) String section,
			@QueryParam(Constants.QueryParameters.COURSE) String course) {
		DBConnection db = new DBConnection();
		String str = null;
		if (db.authorizeUser(userid, token)) {
			if (student) {
				System.out.println("students");
				str = db.send_student_list(year, branch, course, section);
			} else {
				System.out.println("Faculty");
				str = db.send_faculty_list(branch, course);
			}
		} else {
			// wrong user
			System.out.println("Somting went wrong");
		}
		db.closeConnection();
		return str;
	}

	@GET
	@Path("/get_user_info")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserInfo(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token,
			@QueryParam(Constants.QueryParameters.GET_DETAILS_OF_USER_ID) String get_details_of_user_id,
			@QueryParam(Constants.QueryParameters.USER_TYPE) boolean is_std) {
		DBConnection db = new DBConnection();
		String str = null;
		if (db.authorizeUser(userid, token)) {
			str = db.getuserInfo(get_details_of_user_id, is_std);
		}
		db.closeConnection();
		return str;// return an JsonObject Telling user is invalid
	}

	@GET
	@Path("/set_new_notification")
	@Produces(MediaType.APPLICATION_JSON)
	public String setNewNotification(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token,
			@QueryParam(Constants.QueryParameters.Notification.SUBJECT) String subject,
			@QueryParam(Constants.QueryParameters.Notification.BODY) String body,
			@QueryParam(Constants.QueryParameters.Notification.TIME) long time,
			@QueryParam(Constants.QueryParameters.COURSE) String course,
			@QueryParam(Constants.QueryParameters.SECTION) String section,
			@QueryParam(Constants.QueryParameters.YEAR) String year,
			@QueryParam(Constants.QueryParameters.BRANCH) String branch) {
		DBConnection db = new DBConnection();
		JSONObject obj = new JSONObject();
		try {
			obj.put(Constants.JSONKeys.TAG,
					Constants.JSONKeys.TAG_MSGS.UPLOADING_NOTIFICATIONS);
			if (db.authorizeUser(userid, token)) {
				Notification noti = new Notification(subject, body, time,
						new String(Base64.decode(userid)), course, branch,
						section, year);
				System.out.println("new notification from "
						+ new String(Base64.decode(userid)));
				db.fillNewNotification(noti);
				obj.put(Constants.JSONKeys.STATUS, true);
			} else {
				// return an JsonObject Telling user is invalid
				obj.put(Constants.JSONKeys.STATUS, false);
				obj.put(Constants.JSONKeys.ERROR, "Invalid user");
			}
			db.closeConnection();
		} catch (Exception e) {
			Utility.debug(e);
		}
		return obj.toString();
	}

	@GET
	@Path("/upload_message")
	@Produces(MediaType.APPLICATION_JSON)
	public String uploadMessage(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token,
			@QueryParam(Constants.QueryParameters.MESSAGES) JSONObject msgs) {
		// insert into db buffered ready to send to target user
		DBConnection db = new DBConnection();

		JSONObject obj = new JSONObject();

		try {
			obj.put(Constants.JSONKeys.TAG,
					Constants.JSONKeys.TAG_MSGS.UPLOADING_MESSAGES);
			if (db.authorizeUser(userid, token)) {
				System.out.println(msgs + " ");
				// insert into db
				if (db.fillMessage(msgs)) {
					obj.put(Constants.JSONKeys.STATUS, true);
				} else {
					obj.put(Constants.JSONKeys.STATUS, false);
					obj.put(Constants.JSONKeys.ERROR, "Insertion error");
				}

			} else {
				obj.put(Constants.JSONKeys.STATUS, false);
				obj.put(Constants.JSONKeys.ERROR, "User Invalid");
			}
		} catch (JSONException e) {
			Utility.debug(e);
		} finally {
			db.closeConnection();
		}
		return obj.toString(); // return result sucess or failure
	}

	@GET
	@Path("/download_messages")
	@Produces(MediaType.APPLICATION_JSON)
	public String downloadMessage(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token) {
		// insert into db buffered ready to send to target user
		DBConnection db = new DBConnection();
		JSONArray jarr = db.fetchMessages(userid);
		db.closeConnection();
		return jarr.toString();
	}

	@GET
	@Path("/receive_ack")
	@Produces(MediaType.APPLICATION_JSON)
	public String ackMessage(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token,
			@QueryParam(Constants.QueryParameters.MSGIDS) JSONArray ids) {
		DBConnection db = new DBConnection();
		System.out.println(ids.toString());
		if (ids.length() > 0)
			db.updateMessageState(ids);
		JSONObject result = new JSONObject();
		try {
			result.put(Constants.JSONKeys.TAG,
					Constants.JSONKeys.TAG_MSGS.MSG_ACK);
			result.put(Constants.JSONKeys.STATUS, true);
		} catch (JSONException e) {
			Utility.debug(e);
		} finally {
			db.closeConnection();
		}
		return result.toString();
	}

	private String responceGenerator(boolean result, String msg) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(Constants.JSONKeys.STATUS, result);
			obj.put(Constants.JSONKeys.ERROR, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj.toString();

	}

}
