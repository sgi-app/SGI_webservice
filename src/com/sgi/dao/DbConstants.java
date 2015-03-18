package com.sgi.dao;

public interface DbConstants {

	String SELECT = "SELECT ";
	String JOIN = " JOIN ";
	String ON = " ON ";
	String FROM = " FROM ";
	String WHERE = " WHERE ";

	String EQUALS = " = ";
	String DOT = ".";
	String COMMA = " ,";
	String SINGLE_QUOTE = "'";
	String QUOTES = "\"";
	String PARENTESIS_OPEN = " (";
	String PARENTESIS_CLOSE = ") ";
	String SEMICOLON = ";";
	String QUESTION_MARK = " ?";

	String TYPE_TEXT = " TEXT";
	String TYPE_INT = " INTEGER";
	String TYPE_REAL = " REAL";

	String CONSTRAIN_PRIMARY_KEY = " PRIMARY KEY";
	String UNIQUE = " UNIQUE";

	String ALTER_TABLE = "alter table ";
	String CREATE_TABLE = "create table ";
	String DROP_TABLE = "DROP TABLE IF EXISTS ";
}
