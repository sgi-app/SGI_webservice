package com.sgi.dao;

public class DbStructure {
	public static String DB_CLASS="com.mysql.jdbc.Driver";
	public static String DB_NAME="sgi_app";	
	public static String DB_USER="root";
	public static String DB_PASSWORD="praveen";
	public static String DB_URL="jdbc:mysql://localhost/"+DB_NAME+"?user="+DB_USER+"&password="+DB_PASSWORD;

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
		String COLUMN_YEAR_ID="year_id";
	}
	public static interface BRANCHES{
		String TABLE_NAME="branches";
		
		String COLUMN_ID="id";
		String COLUMN_NAME="name";
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
		String COLUMN_USER_ID="user_id";
		String COLUMN_STREET="street";
		String COLUMN_CITY="city";
		String COLUMN_STATE="state";
		String COLUMN_PIN="pin";
		String COLUMN_P_MOB="p_mob";
		String COLUMN_H_MOB="h_mob";
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
	public static interface YEAR{
		String TABLE_NAME="year";
		
		String COLUMN_ID="id";
		String COLUMN_BRANCH_ID="branch_id";
		String COLUMN_YEAR="year";
	}
	public static interface FACULTY{
		String TABLE_NAME="faculty";
		
		String COLUMN_ID="id";
		String COLUMN_F_NAME="f_name";
		String COLUMN_L_NAME="l_name";
		String COLUMN_PROFILE_URL="profile_url";
		String COLUMN_LOGIN_ID="l_id";
		String COLUMN_BRANCH_ID="branch_id";
	}
}
