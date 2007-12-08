/*
 * Copyright 1994-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.io;

import java.nio.channels.FileChannel;
import sun.nio.ch.FileChannelImpl;


/**
 * @author  Arthur van Hoff
 * @version 1.78, 06/13/07
 * @see     java.io.File
 * @see     java.io.FileDescriptor
 * @see	    java.io.FileOutputStream
 * @since   JDK1.0
 */
public class FileInputStream extends InputStream
{
    private FileDescriptor fd;
    private FileChannel channel = null;
    private Object closeLock = new Object();
    private volatile boolean closed = false;
    //private static ThreadLocal<Boolean> runningFinalize =
    private static boolean isRunningFinalize() {}
    public FileInputStream(String name) throws FileNotFoundException {}
    public FileInputStream(File file) throws FileNotFoundException {}
    public FileInputStream(FileDescriptor fdObj) {}
    private native void open(String name) throws FileNotFoundException;
    public native int read() throws IOException;
    private native int readBytes(byte b[], int off, int len) throws IOException;
    public int read(byte b[]) throws IOException {}
    public int read(byte b[], int off, int len) throws IOException {}
    public native long skip(long n) throws IOException;
    public native int available() throws IOException;
    public void close() throws IOException {}
    public final FileDescriptor getFD() throws IOException {}
    public FileChannel getChannel() {}
    private static native void initIDs();
    private native void close0() throws IOException;
    protected void finalize() throws IOException {}
}

