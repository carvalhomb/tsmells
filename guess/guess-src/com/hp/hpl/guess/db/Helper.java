package com.hp.hpl.guess.db;

import java.util.HashSet;
import java.util.regex.*;

public abstract class Helper {

    private static HashSet stopwords = new HashSet();
 
    private static String[] res = new String[]{
	"ADD",
	"ALL",
	"ALTER",
	"AND",
	"ANY",
	"AS",
	"ASC",
	"AUTOINCREMENT",
	"AVA ",
	"BETWEEN",
	"BINARY",
	"BIT",
	"BOOLEAN",
	"BY CREATE",
	"BYTE",
	"CHAR",
	"CHARACTER",
	"COLUMN",
	"CONSTRAINT",
	"COUNT",
	"COUNTER",
	"CURRENCY",
	"DATABASE",
	"DATE",
	"DATETIME",
	"DELETE",
	"DESC",
	"DISALLOW",
	"DISTINCT",
	"DISTINCTROW",
	"DOUBLE",
	"DROP",
	"EXISTS",
	"FROM",
	"FLOAT",
	"FLOAT4",
	"FLOAT8",
	"FOREIGN",
	"GENERAL",
	"GROUP",
	"GUID",
	"HAVING ",
	"INNER",
	"INSERT",
	"IGNORE",
	"IMP",
	"IN",
	"INDEX",
	"INT",
	"INTEGER",
	"INTEGER1",
	"INTEGER2",
	"INTEGER4",
	"INTO",
	"IS",
	"JOIN",
	"KEY",
	"LEFT",
	"LEVEL",
	"LIKE",
	"LOGICAL",
	"LONG",
	"LONGBINARY",
	"LONGTEXT",
	"MAX",
	"MEMO",
	"MIN",
	"MOD",
	"MONEY",
	"NOT",
	"NULL",
	"NUMBER",
	"NUMERIC",
	"OLEOBJECT",
	"ON PIVOT",
	"OPTION PRIMARY",
	"ORDER",
	"OUTER",
	"OWNERACCESS",
	"PARAMETERS",
	"PERCENT ",
	"REAL",
	"REFERENCES",
	"RIGHT",
	"SELECT",
	"SET",
	"SHORT",
	"SINGLE",
	"SMALLINT",
	"SOME",
	"STDEV",
	"STDEVP",
	"STRING",
	"SUM",
	"TABLE",
	"TABLEID",
	"TEXT",
	"TIME",
	"TIMESTAMP",
	"TOP",
	"TRANSFORM",
	"UNION",
	"UNIQUE",
	"UPDATE",
	"VALUE",
	"VALUES",
	"VAR",
	"VARBINARY",
	"VARCHAR",
	"VARP",
	"WHERE",
	"WITH",
	"YESNO",
	"g",
	"r",
	"vf",
	"v",
	"Node",
	"Edge",
	"db",
	"interp",
	"ui"};

    static {
	for (int i = 0 ; i < res.length ; i++) {
	    stopwords.add(res[i].toLowerCase());
	}
    }
    
    private static Pattern pat = Pattern.compile("[a-zA-Z0-9_]+");
    private static Pattern pat2 = Pattern.compile("[0-9]+");

    public static boolean isBadName(String n) {
	if (com.hp.hpl.guess.Guess.nowarn) {
	    return(false);
	}

	if (stopwords.contains(n.toLowerCase())) {
	    return(true);
	}
	if (!pat.matcher(n).matches()) {
	    return(true);
	}
	return(pat2.matcher(n).matches());
    }
    
    public static void main(String[] args) {
	System.out.println(args[0] + " " + isBadName(args[0]));
    }
}
