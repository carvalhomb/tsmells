/*
 * Copyright 1996-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

//////////////////
//  Modified by Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be
//     for use with Fetch
//     removed implementation
//     removed unsupported features, eg templates
//////////////////

package java.net;

import java.io.InputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.Date;

/**
 * @see     java.net.HttpURLConnection#disconnect()
 * @since JDK1.1
 */
abstract public class HttpURLConnection extends URLConnection {
    protected String method = "GET";
    protected int chunkLength = -1;
    protected int fixedContentLength = -1;
    public String getHeaderFieldKey (int n) {}
    public void setFixedLengthStreamingMode (int contentLength) {}
    private static final int DEFAULT_CHUNK_SIZE = 4096;
    public void setChunkedStreamingMode (int chunklen) {}
    public String getHeaderField(int n) {}
    protected int responseCode = -1;
    protected String responseMessage = null;
    private static boolean followRedirects = true;
    protected boolean instanceFollowRedirects = followRedirects;
    private static final String[] methods = {}
    protected HttpURLConnection (URL u) {}
    public static void setFollowRedirects(boolean set) {}
    public static boolean getFollowRedirects() {}
    public void setInstanceFollowRedirects(boolean followRedirects) {}
    public boolean getInstanceFollowRedirects() {}
    public void setRequestMethod(String method) throws ProtocolException {}
    public String getRequestMethod() {}
    public int getResponseCode() throws IOException {}
    public String getResponseMessage() throws IOException {}
    public long getHeaderFieldDate(String name, long Default) {}
    public abstract void disconnect();
    public abstract boolean usingProxy();
    public Permission getPermission() throws IOException {}
    public InputStream getErrorStream() {}
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_RESET = 205;
    public static final int HTTP_PARTIAL = 206;
    public static final int HTTP_MULT_CHOICE = 300;
    public static final int HTTP_MOVED_PERM = 301;
    public static final int HTTP_MOVED_TEMP = 302;
    public static final int HTTP_SEE_OTHER = 303;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_USE_PROXY = 305;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_BAD_METHOD = 405;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_PROXY_AUTH = 407;
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_PRECON_FAILED = 412;
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    public static final int HTTP_REQ_TOO_LONG = 414;
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_UNAVAILABLE = 503;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION = 505;
}

