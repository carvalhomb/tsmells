/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.sql;

/**
 * @since 1.2
 */
public interface SQLInput {
    String readString() throws SQLException;
    boolean readBoolean() throws SQLException;
    byte readByte() throws SQLException;
    short readShort() throws SQLException;
    int readInt() throws SQLException;
    long readLong() throws SQLException;
    float readFloat() throws SQLException;
    double readDouble() throws SQLException;
    java.math.BigDecimal readBigDecimal() throws SQLException;
    byte[] readBytes() throws SQLException;
    java.sql.Date readDate() throws SQLException;
    java.sql.Time readTime() throws SQLException;
    java.sql.Timestamp readTimestamp() throws SQLException;
    java.io.Reader readCharacterStream() throws SQLException;
    java.io.InputStream readAsciiStream() throws SQLException;
    java.io.InputStream readBinaryStream() throws SQLException;
    Object readObject() throws SQLException;
    Ref readRef() throws SQLException;
    Blob readBlob() throws SQLException;
    Clob readClob() throws SQLException;
    Array readArray() throws SQLException;
    boolean wasNull() throws SQLException;
    java.net.URL readURL() throws SQLException;
    NClob readNClob() throws SQLException;
    String readNString() throws SQLException;
    SQLXML readSQLXML() throws SQLException;
    RowId readRowId() throws SQLException;
}
d