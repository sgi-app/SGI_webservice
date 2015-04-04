package com.sgi.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;

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
				System.out.println("sending students list");
				str = db.send_student_list(year, branch, course, section);
			} else {
				System.out.println("sending Faculty list");
				str = db.send_faculty_list(branch, course);
			}
		} else {
			// wrong user
			System.out.println("Somting went wrong");
		}
		db.closeConnection();
		System.out.println(str + "\n\n");
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
		System.out.println("sending details\n" + str + "\n\n");
		return str;// return an JsonObject Telling user is invalid
	}

	@POST
	@Path("/get_full_user_info")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserInfo(InputStream inputStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		DBConnection db = new DBConnection();
		String str = null;
		try {
			String userid = br.readLine();
			String token = br.readLine();
			if (db.authorizeUser(userid, token)) {
				StringBuilder strb = new StringBuilder();
				while ((str = br.readLine()) != null) {
					strb.append(str);
				}
				JSONArray ids = new JSONArray(strb.toString());
				str = db.getUsersDetail(ids).toString();
			}
		} catch (IOException e) {
			Utility.debug(e);
		} catch (JSONException e) {
			Utility.debug(e);
		} finally {
			db.closeConnection();
		}
		System.out.println("sending details\n" + str + "\n\n");
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
			obj.put(Constants.JSONKEYS.TAG,
					Constants.JSONKEYS.TAG_MSGS.UPLOADING_NOTIFICATIONS);
			if (db.authorizeUser(userid, token)) {
				String d_userid = new String(Base64.decode(userid));
				Notification noti = new Notification(subject, body, time,
						d_userid, course, branch, section, year);
				// System.out.println("new notification from " + d_userid);
				db.fillNotification(noti);
				obj.put(Constants.JSONKEYS.STATUS, true);
			} else {
				// return an JsonObject Telling user is invalid
				obj.put(Constants.JSONKEYS.STATUS, false);
				obj.put(Constants.JSONKEYS.ERROR, "Invalid user");
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
			obj.put(Constants.JSONKEYS.TAG,
					Constants.JSONKEYS.TAG_MSGS.UPLOADING_MESSAGES);
			if (db.authorizeUser(userid, token)) {
				// System.out.println(msgs + " ");
				// insert into db
				if (db.fillMessage(msgs)) {
					obj.put(Constants.JSONKEYS.STATUS, true);
				} else {
					obj.put(Constants.JSONKEYS.STATUS, false);
					obj.put(Constants.JSONKEYS.ERROR, "Insertion error");
				}
			} else {
				obj.put(Constants.JSONKEYS.STATUS, false);
				obj.put(Constants.JSONKEYS.ERROR, "User Invalid");
			}
		} catch (JSONException e) {
			Utility.debug(e);
		} finally {
			db.closeConnection();
		}
		// System.out.println("new message ->" + obj.toString() + "\n\n");
		return obj.toString(); // return result sucess or failure
	}

	@GET
	@Path("/give_me_messages")
	@Produces(MediaType.APPLICATION_JSON)
	public String giveMeMessage(
			@QueryParam(Constants.QueryParameters.USERNAME) String userid,
			@QueryParam(Constants.QueryParameters.TOKEN) String token) {
		// insert into db buffered ready to send to target user
		DBConnection db = new DBConnection();
		JSONArray messages = db.getMessagesFromDb(Utility.decode(userid));
		db.closeConnection();
		// System.out.println("sending messages\n" + messages.toString() +
		// "\n\n");
		return messages.toString();
	}

	@POST
	@Path("/receive_ack")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String receive_ack(InputStream inputStream) {
		DBConnection db = new DBConnection();
		JSONObject result = new JSONObject();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		try {
			String userid = br.readLine();
			String token = br.readLine();
			if (db.authorizeUser(userid, token)) {
				StringBuilder strb = new StringBuilder();
				String str;
				while ((str = br.readLine()) != null) {
					strb.append(str);
				}
				System.out.println("ack data received" + strb.toString());
				JSONObject ids = new JSONObject(strb.toString());
				if (ids.has(Constants.JSONKEYS.MESSAGES.ACK)) {
					db.updateMessageState(ids
							.getJSONArray(Constants.JSONKEYS.MESSAGES.ACK));
				}
				if (ids.has(Constants.JSONKEYS.NOTIFICATIONS.ACK)) {
					db.updateNotificationState(
							ids.getJSONArray(Constants.JSONKEYS.NOTIFICATIONS.ACK),
							userid);
				}
				result.put(Constants.JSONKEYS.TAG,
						Constants.JSONKEYS.TAG_MSGS.ACKS);
				result.put(Constants.JSONKEYS.STATUS, true);
			} else {
				result.put(Constants.JSONKEYS.TAG,
						Constants.JSONKEYS.TAG_MSGS.ACKS);
				result.put(Constants.JSONKEYS.STATUS, false);
				result.put(Constants.JSONKEYS.ERROR, "invalid User");
			}
		} catch (Exception e) {
			Utility.debug(e);
		} finally {
			db.closeConnection();
		}
		System.out.println("ack data sending" + result.toString() + "\n\n");
		return result.toString();
	}

	/**
	 * receive data(messages and notifications) from client insert it into
	 * server db get any data for the perticular client send them to client as
	 * JSON
	 * 
	 * @param inputStream
	 * @return
	 */
	@POST
	@Path("/sync")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String sync(InputStream inputStream) {
		DBConnection db = new DBConnection();
		StringBuilder strb = new StringBuilder();
		String userid, token;
		boolean is_faculty;
		JSONObject new_data = new JSONObject(); // data to send
		JSONObject data = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));
			userid = br.readLine();
			token = br.readLine();
			is_faculty = Boolean.parseBoolean(br.readLine());
			if (db.authorizeUser(userid, token)) {

				// sender of the messages
				String d_userid = Utility.decode(userid);

				String str;
				while ((str = br.readLine()) != null) {
					strb.append(str);
				}
				JSONArray msg_ack_ids, noti_ack_ids, new_messages, new_notifications, messages, notifications;
				msg_ack_ids = noti_ack_ids = null;

				// have problem if there is no data
				if (strb.toString().trim().length() > 0) {
					data = new JSONObject(strb.toString());
					System.out.println("sync data received" + data.toString());

					// insert the data into database if there is data
					if (data.has(Constants.JSONKEYS.MESSAGES.MESSAGES)) {
						messages = data
								.getJSONArray(Constants.JSONKEYS.MESSAGES.MESSAGES);
						if (messages.length() > 0)
							msg_ack_ids = db.fillMessages(messages, d_userid);
					}
					if (is_faculty) {
						if (data.has(Constants.JSONKEYS.NOTIFICATIONS.NOTIFICATIONS)) {
							notifications = data
									.getJSONArray(Constants.JSONKEYS.NOTIFICATIONS.NOTIFICATIONS);

							if (notifications.length() > 0)
								noti_ack_ids = db.fillNotifications(
										notifications, d_userid);
						}
					}
				} else {
					// to avoid null error later in
					data = new JSONObject();
					System.out
							.println("sync no data only credentials received");
				}

				// get data from db for this user
				new_messages = db.getMessagesFromDb(d_userid);
				new_notifications = db.getNotificationsFromDb(d_userid,
						is_faculty);
				// set data to send
				if (new_messages.length() > 0)
					new_data.put(Constants.JSONKEYS.MESSAGES.MESSAGES,
							new_messages);
				if (new_notifications.length() > 0)
					new_data.put(
							Constants.JSONKEYS.NOTIFICATIONS.NOTIFICATIONS,
							new_notifications);

				if (msg_ack_ids != null && msg_ack_ids.length() > 0)
					new_data.put(Constants.JSONKEYS.MESSAGES.ACK, msg_ack_ids);
				if (noti_ack_ids != null && noti_ack_ids.length() > 0)
					new_data.put(Constants.JSONKEYS.NOTIFICATIONS.ACK,
							noti_ack_ids);

			} else {
				System.out.println("User not valid");
			}
		} catch (Exception e) {
			Utility.debug(e);
		} finally {
			db.closeConnection();
		}
		if (new_data.length() > 0)
			System.out.println("Sending this" + new_data.toString());
		else
			System.out.println("Sending nothing");

		return new_data.toString();
	}

	@POST
	@Path("/upload_file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public String getFile(MultiPart multipart) throws IOException
	// @FormDataParam("file") FormDataContentDisposition fileDetail,
	// @QueryParam(Constants.QueryParameters.USERNAME) String userid,
	// @QueryParam(Constants.QueryParameters.TOKEN) String token,
	// @QueryParam(Constants.QueryParameters.FILE_ID) JSONArray attachments)
	{
		DBConnection db = new DBConnection();
		JSONObject jobj = new JSONObject();
		JSONArray jsonarr = new JSONArray();
		OutputStream out = null;
		BufferedReader br = null;
		String fileName = null;
		String str = null;
		StringBuilder strb = null;
		BodyPart bp = null;
		InputStream inputStream = null;
		try {		
			System.out.println("got ya");
			// ResultSet rs = db.fill_files(attachments, userid);
			List<BodyPart> bpl = multipart.getBodyParts();
			int files = bpl.size();
			byte[] bytes = new byte[1024];
			bp = bpl.get(0);
			strb = new StringBuilder();
			inputStream = ((BodyPartEntity) bp.getEntity()).getInputStream();
			br = new BufferedReader(new InputStreamReader(inputStream));
			try {
				while ((str = br.readLine()) != null) {
					strb.append(str);
				}
			} catch (IllegalStateException e) {
				System.out.println("Reached end of stream");
				// Utility.debug(e);
			} finally {
				br.close();
			}
			String userid = new String(strb.toString());
			System.out.println(userid.toString());
			bp = bpl.get(1);
			strb = new StringBuilder();
			inputStream = ((BodyPartEntity) bp.getEntity()).getInputStream();
			br = new BufferedReader(new InputStreamReader(inputStream));
			try {
				while ((str = br.readLine()) != null) {
					strb.append(str);
				}
			} catch (IllegalStateException e) {
				System.out.println("Reached end of stream");
				// Utility.debug(e);
			} finally {
				br.close();
			}
			JSONArray ahgsfdjs = new JSONArray(strb.toString());
			System.out.println(ahgsfdjs.toString());
			ResultSet rs = db.fill_files(ahgsfdjs, userid);
	//		while (rs.next()) {
	//			System.out.println(rs.getInt(1));
	//		}
			for (int i = 2; i < files; i++) {
				rs.next();
				bp = bpl.get(i);
				inputStream = ((BodyPartEntity) bp.getEntity()) 
						.getInputStream();
				// MediaType exten = bp.getHeaders()
				// System.out.println(exten.toString());
				fileName = Utility.getFileName(bp);				
				fileName = Utility.setFileName(fileName,rs.getInt(1));
				System.out.println("got filename=" + fileName);
				// fileName = "_" + i + ".txt";
				int read = 0;
				out = new FileOutputStream(new File("D://new/" + fileName));
				try {
					while ((read = inputStream.read(bytes)) != -1) {
						out.write(bytes, 0, read);
					}
				} catch (Exception e) {
					Utility.debug(e);
				}
				out.flush();
				out.close();
				inputStream.close();
			}
			jobj.put(Constants.JSONKEYS.FILES.ID, jsonarr);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
		return jobj.toString();
	}

}
