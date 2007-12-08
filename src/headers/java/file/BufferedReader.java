/*
 * Copyright 1996-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * @see FileReader
 * @see InputStreamReader
 * @author      Mark Reinhold
 * @since       JDK1.1
 */
public class BufferedReader extends Reader {
    private Reader in;
    private char cb[];
    private int nChars, nextChar;
    private static final int INVALIDATED = -2;
    private static final int UNMARKED = -1;
    private int markedChar = UNMARKED;
    private int readAheadLimit = 0;
    private boolean skipLF = false;
    private boolean markedSkipLF = false;
    private static int defaultCharBufferSize = 8192;
    private static int defaultExpectedLineLength = 80;
    public BufferedReader(Reader in, int sz) {}
    public BufferedReader(Reader in) {}
    private void ensureOpen() throws IOException {}
    private void fill() throws IOException {}
    public int read() throws IOException {}
    private int read1(char[] cbuf, int off, int len) throws IOException {}
    public int read(char cbuf[], int off, int len) throws IOException {}
    public String readLine() throws IOException {}
    public long skip(long n) throws IOException {}
    public boolean ready() throws IOException {}
    public boolean markSupported() {}
    public void mark(int readAheadLimit) throws IOException {}
    public void reset() throws IOException {}
    public void close() throws IOException {}
}

