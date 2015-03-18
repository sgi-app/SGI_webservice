package com.sgi.constants;

public interface Constants {

	// elephant Notation

	// public static String DB_LOGIN_TABLE="login";

	public static interface QueryParameters {
		String PASSWORD = "password";
		String USERNAME = "username";
		String QUERY_ID = "query_id";
		String TOKEN = "token";
		String USER_TYPE = "user_type";
		String DEPARTMENT = "department";
		String BRANCH = "branch";
		String COURSE = "course";
		String SECTION = "section";
		String YEAR = "year";
		String LOGIN_ID = "login_id";
		String GET_DETAILS_OF_USER_ID = "get_details_of_user";
		public static String IS_FACULTY = "is_faculty";
		public static String MSGIDS = "message_ids";
		public static String MESSAGES = "messages";

		public static interface Notification {
			public static String SUBJECT = "subject";
			public static String BODY = "body";
			public static String TIME = "time";
		}


	}

	public static interface MsgState {
		int SENT_SUCESSFULLY = 1;
		int TO_SEND = 0;
	}

	public static interface JSONKeys {
		public static String FIRST_NAME = "FirstName";
		public static String LAST_NAME = "LastName";
		public static String PROFILE_IMAGE = "ProfileImage";
		public static String L_ID = "LoginId";
		public static String USER_ID = "UserId";
		public static String BRANCH = "Branch";
		public static String STATE = "State";
		public static String YEAR = "Year";
		public static String SECTION = "Section";
		public static String COURSE = "Course";
		public static String ROLL_NO = "RollNo";
		public static String CITY = "City";
		public static String PIN = "PIN";
		public static String P_MOB = "PMob";
		public static String H_MOB = "HMob";
		public static String STREET = "Street";
		public static String ERROR = "Error";

		public static String TOKEN = "Token";
		public static String STATUS = "Status";
		public static String TAG = "Tag";

		public static interface TAG_MSGS {
			public static String LOGIN = "Login";
			public static String UPLOADING_MESSAGES = "UploadingMessages";
			public static String MSG_ACK = "MessageAcknoledgement";
			public static String UPLOADING_NOTIFICATIONS = "UploadingNotifications";
		}
	}

	public static interface JSONMessageKeys {
		public static String ID = "Id";
		public static String MESSAGE = "Message";
		public static String RECEIVER = "Receiver";
		public static String TEXT = "Text";
		public static String SENDER = "Sender";
		public static String IS_GROUP_MESSAGE = "is_group_msg";
		public static String TIME = "Time";
		public static String ANY_NEW_MESSAGE= "any_new_message";
	}
}
