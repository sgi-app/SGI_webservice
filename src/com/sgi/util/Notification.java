package com.sgi.util;

public class Notification {

	public static interface STATE {
		public static int FETCHED = 0;
		public static int CREATED = 1;
		public static int SENT = 2;
	}

	public String text, subject, branch, section, course;
	public long time;
	public int sender_id;
	public String year;
	public String sid; // sender ki id like EMP-100

	/**
	 * New notification created should be instantiated using this constructor
	 * 
	 * @param subject_
	 *            String Subject of the notification
	 * @param txt
	 *            String Content of notification
	 * @param time_
	 *            Long Time in milisecounds
	 * @param pid
	 *            String Sender id like EMP-100
	 * @param course_
	 *            String Course name
	 * @param branch_
	 *            String branch name
	 * @param section_
	 *            String section name
	 * @param year_
	 *            String year
	 * 
	 */
	public Notification(String subject_, String txt, long time_, String pid,
			String course_, String branch_, String section_, String year_) {
		time =time_;
		subject = subject_;
		text = txt;
		sid = pid;

		// target audience
		course = course_;
		branch = branch_;
		year = year_;
		section = section_;
	}
}
