package com.sgi.dao;

public class DbStructure {
	public static interface COURSES{
		String TABLE_NAME="courses";
		
		String COLUMN_ID="id";
		String COLUMN_NAME="name";
		String COLUMN_DURATION="duration";
	}
	public static interface SECTIONS{
		String TABLE_NAME="sections";
		
		String COLUMN_ID="id";
		String COLUMN_NAME="name";
		String COLUMN_DURATION="branch_id";
	}
	public static interface BRANCHES{
		String TABLE_NAME="branches";
		
		String COLUMN_ID="id";
		String COLUMN_NAME="name";
		String COLUMN_YEAR="year";
		String COLUMN_COURSE_ID="course_id";
	}
	public static interface LOGIN{
		String TABLE_NAME="login";
		
		String COLUMN_ID="id";
		String COLUMN_USER_ID="user_id";
		String COLUMN_PASSWORD="pswd";
		String COLUMN_TOKEN="token";
		String COLUMN_IS_FACULTY="is_faculty";
		String COLUMN_IS_ONLINE="is_online";
	}
	public static interface STUDENTS{
		String TABLE_NAME="students";
		 
		String COLUMN_ID="id";
		String COLUMN_F_NAME="f_name";
		String COLUMN_L_NAME="l_name";
		String COLUMN_BRANCH="branch";
		String COLUMN_YEAR="year";
		String COLUMN_U_ROLL_NO="u_roll_no";
		String COLUMN_LOGIN="l_id";
		String COLUMN_SECTION_ID="section_id";
		String COLUMN_PROFILE="profile_url";
		
	}
	public static interface NOTIFICATIONS{
		String TABLE_NAME="notification";
		 
		String COLUMN_ID="id";
		String COLUMN_TEXT="text";
		String COLUMN_FACULTY_ID="faculty_id";
		String COLUMN_TIME="time";
		String COLUMN_TITLE="title";
	}
	public static interface MESSAGES{
		String TABLE_NAME="messages";
		 
		String COLUMN_ID="id";
		String COLUMN_SENDER="sender";
		String COLUMN_RECEIVER="receiver";
		String COLUMN_TEXT="text";
		String COLUMN_TIME="time";
		String COLUMN_STATE="state";
		String COLUMN_IS_GROUP_MSG="is_group_msg";
	}
	public static interface FILES{
		String TABLE_NAME="files";
		 
		String COLUMN_ID="id";
		String COLUMN_URL="url";
		String COLUMN_OWNER="owner";
		String COLUMN_TIME="time";
	}
	public static interface CONTACT_INFO{
		String TABLE_NAME="contact_info";
		 
		String COLUMN_ID="id";
		String COLUMN_USER_ID="url";
		String COLUMN_STREET="url";
		String COLUMN_CITY="owner";
		String COLUMN_STATE="time";
		String COLUMN_PIN="url";
		String COLUMN_P_MOB="url";
		String COLUMN_H_MOB="url";
	}
	public static interface FILE_NOTIFICATION_MAP{
		String TABLE_NAME="file_notification_map";
		 
		String COLUMN_ID="id";
		String COLUMN_NOTIFICATION_ID="notification_id";
		String COLUMN_FILE_ID="file_id";
		
	}
	public static interface MESSAGE_FILE_MAP{
		String TABLE_NAME="message_file_map";
		 
		String COLUMN_MESSAGE_ID="message_id";
		String COLUMN_FILE_ID="file_id";
		
	}
	public static interface USER_NOTIFICATION_MAP{
		String TABLE_NAME="user_notification_map";
		 
		String COLUMN_ID="id";
		String COLUMN_NOTIFICATION_ID="notification_id";
		String COLUMN_USER_ID="user_id";
		String COLUMN_IS_FACULTY="is_faculty";
		
	}
}
