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

/**
 * @author  Arthur van Hoff
 * @see     java.io.DataOutputStream
 * @since   JDK1.0
 */
public
class DataInputStream extends FilterInputStream implements DataInput {
    public DataInputStream(InputStream in) {}
    private byte bytearr[] = new byte[80];
    private char chararr[] = new char[80];
    public final int read(byte b[]) throws IOException {}
    public final int read(byte b[], int off, int len) throws IOException {}
    public final void readFully(byte b[]) throws IOException {}
    public final void readFully(byte b[], int off, int len) throws IOException {}
    public final int skipBytes(int n) throws IOException {}
    public final boolean readBoolean() throws IOException {}
    public final byte readByte() throws IOException {}
    public final int readUnsignedByte() throws IOException {}
    public final short readShort() throws IOException {}
    public final int readUnsignedShort() throws IOException {}
    public final char readChar() throws IOException {}
    public final int readInt() throws IOException {}
    private byte readBuffer[] = new byte[8];
    public final long readLong() throws IOException {}
    public final float readFloat() throws IOException {}
    public final double readDouble() throws IOException {}
    private char lineBuffer[];
    public final String readLine() throws IOException {}
    public final String readUTF() throws IOException {}
    public final static String readUTF(DataInput in) throws IOException {}
}

