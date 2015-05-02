package com.sgi.constants;

public interface Constants {

	// elephant Notation

	// public static String DB_LOGIN_TABLE="login";

	public interface QueryParameters {
		String PASSWORD = "password";
		String USERNAME = "username";
		// String QUERY_ID = "query_id";
		String TOKEN = "token";
		String USER_TYPE = "user_type";
		String DEPARTMENT = "department";
		String BRANCH = "branch";
		String COURSE = "course";
		String SECTION = "section";
		String YEAR = "year";
		String LOGIN_ID = "login_id";
		String GET_DETAILS_OF_USER_ID = "get_details_of_user";
		String IS_FACULTY = "is_faculty";
		String MSGIDS = "message_ids";
		String MESSAGES = "messages";
		String FILE_NAME = "file_name";
		String FILE_ID = "file_id";
		String FILE = "file";
		String NEW_PWD = "new_pwd";
		public interface Notification {
			String SUBJECT = "subject";
			String BODY = "body";
			String TIME = "time";
		}

		public static interface FILES {
			String NAME = "file_name";
			String INPUT_STREAM = "file_input_stream";
			String DESC = "desc";
		}
	}

	public interface FOR_FACULTY {
		int YES = 0;
		int NO = 1;
	}

	/**
	 * states of messages or notifications on sever send pending or to send
	 * 
	 * @author Zeeshan Khan
	 * 
	 */
	public interface STATE {
		/**
		 * this means the message or notification has been send to the target
		 * user or group but no acknowledgment is received
		 */
		int SENT = 1;
		/**
		 * this means the message or notification is pending i.e., not yet send
		 * to the target user
		 */
		int PENDING = 0;
		/**
		 * received from the target user of group of users
		 */
		int ACK_RECEIVED = 3;
		/**
		 * received from source and acknowledged the source
		 */
		int ACK_SENT = 4;
	}

	public interface IS_GROUP_MSG {
		int YES = 0;
		int NO = 1;
	}

	public interface JSONKEYS {

		String STUDENT = "Student";
		String FACULTY = "Faculty";

		String FIRST_NAME = "FirstName";
		String LAST_NAME = "LastName";
		String PROFILE_IMAGE = "ProfileImage";
		String L_ID = "LoginId";
		String USER_ID = "UserId";
		String BRANCH = "Branch";
		String STATE = "State";
		String YEAR = "Year";
		String SECTION = "Section";
		String COURSE = "Course";
		String ROLL_NO = "RollNo";
		String CITY = "City";
		String PIN = "PIN";
		String P_MOB = "PMob";
		String H_MOB = "HMob";
		String STREET = "Street";
		String ERROR = "Error";
		String INITIAL_DATA="initial_data";
		String USER_DATA="user_data";
		
		String COURSES = "Courses";
		String BRANCHES = "Branches";
		String YEARS = "Years";
		String SECTIONS = "Sections";
		
		String TOKEN = "Token";
		String STATUS = "Status";
		String TAG = "Tag";
		String PSWD = "pswd";
		String REG_ID = "reg_id";

		public interface TAG_MSGS {
			String LOGIN = "Login";
			String UPLOADING_MESSAGES = "UploadingMessages";
			String MSG_ACK = "MessageAcknoledgement";
			String NOT_ACK = "NotificationAcknoledgement";
			String ACKS = "MsgAndNotiAck";
			String PWD_CHANGE="PswdChange";
			String UPLOADING_NOTIFICATIONS = "UploadingNotifications";
		}

		public interface MESSAGES {
			String ID = "Id";
			String MESSAGES = "Messages";
			String RECEIVER = "Receiver";
			String TEXT = "Text";
			String SENDER = "Sender";
			String IS_GROUP_MESSAGE = "is_group_msg";
			String TIME = "Time";
			String ACK = "Msg_ack";
		}

		public interface NOTIFICATIONS {
			String FOR_FACULTY = "For_Faculty";
			String NOTIFICATIONS = "Notifications";
			String ID = "Id";
			String TEXT = "Text";
			String SUBJECT = "Subject";
			String TIME = "Time";
			String COURSE = "Course";
			String BRANCH = "Branch";
			String YEAR = "Year";
			String SECTION = "Section";
			String SENDER = "Sender";
			String ACK = "Noti_ack";
			String ATTACHMENTS = "attachments";
		}
		public interface FILES{
			String NAME = "name";
			String ID = "Id";
			String URL = "url";	
			String SIZE = "size";
		}
	}

}
