/*
 * Copyright 1995-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * @author  unascribed
 * @see     java.net.SocketImpl
 * @see     java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
 * @see     java.nio.channels.ServerSocketChannel
 * @since   JDK1.0
 */
public class ServerSocket implements java.io.Closeable {
    private boolean created = false;
    private boolean bound = false;
    private boolean closed = false;
    private Object closeLock = new Object();
    private SocketImpl impl;
    private boolean oldImpl = false;
    public ServerSocket() throws IOException {}
    public ServerSocket(int port) throws IOException {}
    public ServerSocket(int port, int backlog) throws IOException {}
    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {}
    private void checkOldImpl() {}
    private void setImpl() {}
    public void bind(SocketAddress endpoint) throws IOException {}
    public void bind(SocketAddress endpoint, int backlog) throws IOException {}
    public InetAddress getInetAddress() {}
    public int getLocalPort() {}
    public SocketAddress getLocalSocketAddress() {}
    public Socket accept() throws IOException {}
    protected final void implAccept(Socket s) throws IOException {}
    public void close() throws IOException {}
    public ServerSocketChannel getChannel() {}
    public boolean isBound() {}
    public boolean isClosed() {}
    public synchronized void setSoTimeout(int timeout) throws SocketException {}
    public synchronized int getSoTimeout() throws IOException {}
    public void setReuseAddress(boolean on) throws SocketException {}
    public boolean getReuseAddress() throws SocketException {}
    public String toString() {}
    private static SocketImplFactory factory = null;
    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {}
    public synchronized void setReceiveBufferSize (int size) throws SocketException {}
    public synchronized int getReceiveBufferSize() {}
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {}
}

