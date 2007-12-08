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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedAction;

/**
 * @author  unascribed
 * @see     java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
 * @see     java.net.SocketImpl
 * @see     java.nio.channels.SocketChannel
 * @since   JDK1.0
 */
public class Socket implements java.io.Closeable {
    private boolean created = false;
    private boolean bound = false;
    private boolean connected = false;
    private boolean closed = false;
    private Object closeLock = new Object();
    private boolean shutIn = false;
    private boolean shutOut = false;
    private boolean oldImpl = false;
    public Socket() {}
    public Socket(Proxy proxy) {}
    protected Socket(SocketImpl impl) throws SocketException {}
    public Socket(String host, int port) {}
    public Socket(InetAddress address, int port) throws IOException {}
    public Socket(String host, int port, InetAddress localAddr,==> COMPLETE MANUALLY
    public Socket(InetAddress address, int port, InetAddress localAddr,==> COMPLETE MANUALLY
    public Socket(String host, int port, boolean stream) throws IOException {}
    public Socket(InetAddress host, int port, boolean stream) throws IOException {}
    private Socket(SocketAddress address, SocketAddress localAddr,==> COMPLETE MANUALLY
    private void checkOldImpl() {}
    public void connect(SocketAddress endpoint) throws IOException {}
    public void connect(SocketAddress endpoint, int timeout) throws IOException {}
    public void bind(SocketAddress bindpoint) throws IOException {}
    public InetAddress getInetAddress() {}
    public InetAddress getLocalAddress() {}
    public int getPort() {}
    public int getLocalPort() {}
    public SocketAddress getRemoteSocketAddress() {}
    public SocketAddress getLocalSocketAddress() {}
    public SocketChannel getChannel() {}
    public InputStream getInputStream() throws IOException {}
    public OutputStream getOutputStream() throws IOException {}
    public void setTcpNoDelay(boolean on) throws SocketException {}
    public boolean getTcpNoDelay() throws SocketException {}
    public void setSoLinger(boolean on, int linger) throws SocketException {}
    public int getSoLinger() throws SocketException {}
    public void sendUrgentData (int data) throws IOException  {}
    public void setOOBInline(boolean on) throws SocketException {}
    public boolean getOOBInline() throws SocketException {}
    public synchronized void setSoTimeout(int timeout) throws SocketException {}
    public synchronized int getSoTimeout() throws SocketException {}
    public synchronized void setSendBufferSize(int size) {}
    public synchronized int getSendBufferSize() throws SocketException {}
    public synchronized void setReceiveBufferSize(int size) {}
    public synchronized int getReceiveBufferSize() {}
    public void setKeepAlive(boolean on) throws SocketException {}
    public boolean getKeepAlive() throws SocketException {}
    public void setTrafficClass(int tc) throws SocketException {}
    public int getTrafficClass() throws SocketException {}
    public void setReuseAddress(boolean on) throws SocketException {}
    public boolean getReuseAddress() throws SocketException {}
    public synchronized void close() throws IOException {}
    public void shutdownInput() throws IOException
    public void shutdownOutput() throws IOException
    public String toString() {}
    public boolean isConnected() {}
    public boolean isBound() {}
    public boolean isClosed() {}
    public boolean isInputShutdown() {}
    public boolean isOutputShutdown() {}
    private static SocketImplFactory factory = null;
    public static synchronized void setSocketImplFactory(SocketImplFactory fac) {}
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {}
}

