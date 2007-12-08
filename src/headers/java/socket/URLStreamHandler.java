/*
 * Copyright 1995-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;
import sun.net.util.IPAddressUtil;
import sun.net.www.ParseUtil;

/**
 * @author  James Gosling
 * @see     java.net.URL#URL(java.lang.String, java.lang.String, int, java.lang.String)
 * @since   JDK1.0
 */
public abstract class URLStreamHandler {
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {}
    protected void parseURL(URL u, String spec, int start, int limit) {}
    protected int getDefaultPort() {}
    protected boolean equals(URL u1, URL u2) {}
    protected int hashCode(URL u) {}
    protected boolean sameFile(URL u1, URL u2) {}
    protected synchronized InetAddress getHostAddress(URL u) {}
    protected boolean hostsEqual(URL u1, URL u2) {}
    protected String toExternalForm(URL u) {}
    protected void setURL(URL u, String protocol, String host, int port,String authority, String userInfo, String path, String query, String ref) {}
    protected void setURL(URL u, String protocol, String host, int port,String file, String ref) {}
}

