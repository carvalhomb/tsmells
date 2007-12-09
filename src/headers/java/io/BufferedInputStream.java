/*
 * Copyright 1994-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public class BufferedInputStream extends FilterInputStream {
    private static int defaultBufferSize = 8192;
    protected volatile byte buf[];
/*    private static final
        AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater =
        AtomicReferenceFieldUpdater.newUpdater
        (BufferedInputStream.class,  byte[].class, "buf");*/
    protected int count;
    protected int pos;
    protected int markpos = -1;
    protected int marklimit;
    private InputStream getInIfOpen() throws IOException {}
    private byte[] getBufIfOpen() throws IOException {}
    public BufferedInputStream(InputStream in) {}
    public BufferedInputStream(InputStream in, int size) {}
    private void fill() throws IOException {}
    public synchronized int read() throws IOException {}
    private int read1(byte[] b, int off, int len) throws IOException {}
    public synchronized int read(byte b[], int off, int len) {}
    public synchronized long skip(long n) throws IOException {}
    public synchronized int available() throws IOException {}
    public synchronized void mark(int readlimit) {}
    public synchronized void reset() throws IOException {}
    public boolean markSupported() {}
    public void close() throws IOException {}
}

