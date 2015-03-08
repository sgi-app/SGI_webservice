package com.sgi.constants;

public interface Constants {

	//elephant Notation
	
//	public static String DB_LOGIN_TABLE="login";
	
	public static interface QueryParameters{
		public static String PASSWORD="password";
		public static String USERNAME="username";
		public static String QUERY_ID="query_id";
		public static String TOKEN="token";
		public static String USER_TYPE="user_type";
		public static String DEPARTMENT="department";
		public static String COURSE="course";
		public static String SECTION="section";
		public static String YEAR="year";
		public static String LOGIN_ID="login_id";
		public static String IS_FACULTY="is_faculty";
		public static String MSGIDS="message_ids";
		public static String MESSAGES="messages";
		public static String REG_ID="reg_id";
		
	}
	
	public static interface MsgState{
		int SENT_SUCESSFULLY=1;
		int TO_SEND=0;
	}
	public static interface JSONKeys{
		public static String FIRST_NAME="FirstName";
		public static String LAST_NAME="LastName";
		public static String PROFILE_IMAGE="ProfileImage";
		public static String L_ID="LoginId";
		public static String USER_ID="UserId";
		public static String BRANCH="Branch";
		public static String STATE="State";
		public static String YEAR="Year";
		public static String SECTION="Section";
		public static String COURSE="Course";
		public static String ROLL_NO="RollNo";
		public static String CITY="City";
		public static String PIN="PIN";
		public static String P_MOB="PMob";
		public static String H_MOB="HMob";
		public static String ERROR="Error";
		
		public static String TOKEN="Token";
		public static String STATUS="Status";
		public static String TAG="Tag";
		public static interface TAG_MSGS{
			public static String LOGIN="Login";
			public static String UPLOADING_MESSAGES="UploadingMessages";
			public static String MSG_ACK="MessageAcknoledgement";
		}
	}
	
	public static interface JSONMessageKeys{
		public static String ID="Id";
		public static String MESSAGE="Message";
		public static String RECEIVER="Receiver";
		public static String TEXT="Text";
		public static String SENDER="Sender";
		public static String IS_GROUP_MESSAGE="is_group_msg";
		public static String TIME="Time";
	}
}

