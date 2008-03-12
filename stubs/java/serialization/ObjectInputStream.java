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

import java.io.ObjectStreamClass.WeakClassKey;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import static java.io.ObjectStreamClass.processQueue;

/**
 * @author      Mike Warres
 * @author      Roger Riggs
 * @see java.io.DataInput
 * @see java.io.ObjectOutputStream
 * @see java.io.Serializable
 * @see <a href="../../../platform/serialization/spec/input.html"> Object Serialization Specification, Section 3, Object Input Classes</a>
 * @since   JDK1.1
 */
public class ObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants
{
    private static final int NULL_HANDLE = -1;
    private static final Object unsharedMarker = new Object();
    private static final HashMap primClasses = new HashMap(8, 1.0F);
    private final BlockDataInputStream bin;
    private final ValidationList vlist;
    private int depth;
    private boolean closed;
    private final HandleTable handles;
    private int passHandle = NULL_HANDLE;
    private boolean defaultDataEnd = false;
    private byte[] primVals;
    private final boolean enableOverride;
    private boolean enableResolve;
    private CallbackContext curContext;
    public ObjectInputStream(InputStream in) throws IOException {}
    protected ObjectInputStream() throws IOException, SecurityException {}
    public final Object readObject() {}
    protected Object readObjectOverride() {}
    public Object readUnshared() throws IOException, ClassNotFoundException {}
    public void defaultReadObject() {}
    public ObjectInputStream.GetField readFields() {}
    public void registerValidation(ObjectInputValidation obj, int prio) {}
    protected Class<?> resolveClass(ObjectStreamClass desc) {}
    protected Class<?> resolveProxyClass(String[] interfaces) {}
    protected Object resolveObject(Object obj) throws IOException {}
    protected boolean enableResolveObject(boolean enable) {}
    protected void readStreamHeader() {}
    protected ObjectStreamClass readClassDescriptor() {}
    public int read() throws IOException {}
    public int read(byte[] buf, int off, int len) throws IOException {}
    public int available() throws IOException {}
    public void close() throws IOException {}
    public boolean readBoolean() throws IOException {}
    public byte readByte() throws IOException  {}
    public int readUnsignedByte()  throws IOException {}
    public char readChar()  throws IOException {}
    public short readShort()  throws IOException {}
    public int readUnsignedShort() throws IOException {}
    public int readInt()  throws IOException {}
    public long readLong()  throws IOException {}
    public float readFloat() throws IOException {}
    public double readDouble() throws IOException {}
    public void readFully(byte[] buf) throws IOException {}
    public void readFully(byte[] buf, int off, int len) throws IOException {}
    public int skipBytes(int len) throws IOException {}
    public String readLine() throws IOException {}
    public String readUTF() throws IOException {}
    public static abstract class GetField {
        public abstract ObjectStreamClass getObjectStreamClass();
        public abstract boolean defaulted(String name) throws IOException;
        public abstract boolean get(String name, boolean val) {}
        public abstract byte get(String name, byte val) throws IOException;
        public abstract char get(String name, char val) throws IOException;
        public abstract short get(String name, short val) throws IOException;
        public abstract int get(String name, int val) throws IOException;
        public abstract long get(String name, long val) throws IOException;
        public abstract float get(String name, float val) throws IOException;
        public abstract double get(String name, double val) throws IOException;
        public abstract Object get(String name, Object val) throws IOException;
    }
    private void verifySubclass() {}
    private static boolean auditSubclass(final Class subcl) {}
    private void clear() {}
    private Object readObject0(boolean unshared) throws IOException {}
    private Object checkResolve(Object obj) throws IOException {}
    private Object readNull() throws IOException {}
    private Object readHandle(boolean unshared) throws IOException {}
    private Class readClass(boolean unshared) throws IOException {}
    private ObjectStreamClass readClassDesc(boolean unshared) {}
    private ObjectStreamClass readProxyDesc(boolean unshared) {}
    private ObjectStreamClass readNonProxyDesc(boolean unshared) {}
    private String readString(boolean unshared) throws IOException {}
    private Object readArray(boolean unshared) throws IOException {}
    private Enum readEnum(boolean unshared) throws IOException {}
    private Object readOrdinaryObject(boolean unshared) {}
    private void readExternalData(Externalizable obj, ObjectStreamClass desc) {}
    private void readSerialData(Object obj, ObjectStreamClass desc) {}
    private void skipCustomData() throws IOException {}
    private void defaultReadFields(Object obj, ObjectStreamClass desc) {}
    private IOException readFatalException() throws IOException {}
    private void handleReset() throws StreamCorruptedException {}
    private static native void bytesToFloats(byte[] src, int srcpos,==> COMPLETE MANUALLY
    private static native void bytesToDoubles(byte[] src, int srcpos,==> COMPLETE MANUALLY
    private static native ClassLoader latestUserDefinedLoader();
    private class GetFieldImpl extends GetField {
        private final ObjectStreamClass desc;
        private final byte[] primVals;
        private final Object[] objVals;
        private final int[] objHandles;
        public ObjectStreamClass getObjectStreamClass() {}
        public boolean defaulted(String name) throws IOException {}
        public boolean get(String name, boolean val) throws IOException {}
        public byte get(String name, byte val) throws IOException {}
        public char get(String name, char val) throws IOException {}
        public short get(String name, short val) throws IOException {}
        public int get(String name, int val) throws IOException {}
        public float get(String name, float val) throws IOException {}
        public long get(String name, long val) throws IOException {}
        public double get(String name, double val) throws IOException {}
        public Object get(String name, Object val) throws IOException {}
        private int getFieldOffset(String name, Class type) {}
    }
    private static class ValidationList {
        private static class Callback {}
        private Callback list;
        public void clear() {}
    }
    private static class PeekInputStream extends InputStream {
        private final InputStream in;
        private int peekb = -1;
        public int read() throws IOException {}
        public int read(byte[] b, int off, int len) throws IOException {}
        public long skip(long n) throws IOException {}
        public int available() throws IOException {}
        public void close() throws IOException {}
    }
    private class BlockDataInputStream {
        private static final int MAX_BLOCK_SIZE = 1024;
        private static final int MAX_HEADER_SIZE = 5;
        private static final int CHAR_BUF_SIZE = 256;
        private static final int HEADER_BLOCKED = -2;
        private final byte[] buf = new byte[MAX_BLOCK_SIZE];
        private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
        private final char[] cbuf = new char[CHAR_BUF_SIZE];
        private boolean blkmode = false;
        private int pos = 0;
        private int end = -1;
        private int unread = 0;
        private final PeekInputStream in;
        private final DataInputStream din;
        private int readBlockHeader(boolean canBlock) throws IOException {}
        private void refill() throws IOException {}
        public int read() throws IOException {}
        public int read(byte[] b, int off, int len) throws IOException {}
        public long skip(long len) throws IOException {}
        public int available() throws IOException {}
        public void close() throws IOException {}
        public void readFully(byte[] b) throws IOException {}
        public void readFully(byte[] b, int off, int len) throws IOException {}
        public void readFully(byte[] b, int off, int len, boolean copy) {}
        public int skipBytes(int n) throws IOException {}
        public boolean readBoolean() throws IOException {}
        public byte readByte() throws IOException {}
        public int readUnsignedByte() throws IOException {}
        public char readChar() throws IOException {}
        public short readShort() throws IOException {}
        public int readUnsignedShort() throws IOException {}
        public int readInt() throws IOException {}
        public float readFloat() throws IOException {}
        public long readLong() throws IOException {}
        public double readDouble() throws IOException {}
        public String readUTF() throws IOException {}
        public String readLine() throws IOException {}
        private String readUTFBody(long utflen) throws IOException {}
        private long readUTFSpan(StringBuilder sbuf, long utflen) {}
        private int readUTFChar(StringBuilder sbuf, long utflen) {}
    }
    private static class HandleTable { 
        private static final byte STATUS_OK = 1;
        private static final byte STATUS_UNKNOWN = 2;
        private static final byte STATUS_EXCEPTION = 3;
        private void grow() {}
        private static class HandleList {
            private int[] list = new int[4];
            private int size = 0;
            public HandleList() {}
            public void add(int handle) {}
            public int get(int index) {}
            public int size() {}
        }
    }
    private static Object cloneArray(Object array) {}
    private static class CallbackContext {
        private final Object obj;
        private final ObjectStreamClass desc;
        private final AtomicBoolean used = new AtomicBoolean();
        public CallbackContext(Object obj, ObjectStreamClass desc) {}
        public Object getObj() throws NotActiveException {}
        public ObjectStreamClass getDesc() {}
        private void checkAndSetUsed() throws NotActiveException {}
        public void setUsed() {}
    }
}

