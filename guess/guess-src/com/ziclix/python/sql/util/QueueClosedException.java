
/*
 * Jython Database Specification API 2.0
 *
 * $Id: QueueClosedException.java,v 1.1 2005/10/05 20:19:53 eytanadar Exp $
 *
 * Copyright (c) 2001 brian zimmer <bzimmer@ziclix.com>
 *
 */
package com.ziclix.python.sql.util;

/**
 * This exception is thrown when the queue is closed and an operation is attempted.
 *
 * @author brian zimmer
 * @version $Revision: 1.1 $
 */
public class QueueClosedException extends RuntimeException {

	/**
	 * Constructor QueueClosedException
	 *
	 */
	public QueueClosedException() {
		super();
	}

	/**
	 * Constructor QueueClosedException
	 *
	 * @param String msg
	 *
	 */
	public QueueClosedException(String msg) {
		super(msg);
	}
}
