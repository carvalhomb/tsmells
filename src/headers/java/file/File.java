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

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Random;
import java.security.AccessController;
import java.security.AccessControlException;
import sun.security.action.GetPropertyAction;

/**
 * @version 1.148, 08/23/07
 * @author  unascribed
 * @since   JDK1.0
 */
// Removed template Comparable<X> to play nice with Fetch
public class File implements Serializable, Comparable
{
    static private FileSystem fs = FileSystem.getFileSystem();
    private String path;
    private transient int prefixLength;
    public static final char separatorChar = fs.getSeparator();
    public static final String separator = "" + separatorChar;
    public static final char pathSeparatorChar = fs.getPathSeparator();
    public static final String pathSeparator = "" + pathSeparatorChar;
    private File(String pathname, int prefixLength) {}
    private File(String child, File parent) {}
    public File(String pathname) {}
    public File(String parent, String child) {}
    public File(File parent, String child) {}
    public File(URI uri) {}
    public String getName() {}
    public String getParent() {}
    public File getParentFile() {}
    public String getPath() {}
    public boolean isAbsolute() {}
    public String getAbsolutePath() {}
    public File getAbsoluteFile() {}
    public String getCanonicalPath() throws IOException {}
    public File getCanonicalFile() throws IOException {}
    private static String slashify(String path, boolean isDirectory) {}
    public URL toURL() throws MalformedURLException {}
    public URI toURI() {}
    public boolean canRead() {}
    public boolean canWrite() {}
    public boolean exists() {}
    public boolean isDirectory() {}
    public boolean isFile() {}
    public boolean isHidden() {}
    public long lastModified() {}
    public long length() {}
    public boolean createNewFile() throws IOException {}
    public boolean delete() {}
    public void deleteOnExit() {}
    public String[] list() {}
    public String[] list(FilenameFilter filter) {}
    public File[] listFiles() {}
    public File[] listFiles(FilenameFilter filter) {}
    public File[] listFiles(FileFilter filter) {}
    public boolean mkdir() {}
    public boolean mkdirs() {}
    public boolean renameTo(File dest) {}
    public boolean setLastModified(long time) {}
    public boolean setReadOnly() {}
    public boolean setWritable(boolean writable, boolean ownerOnly) {}
    public boolean setWritable(boolean writable) {}
    public boolean setReadable(boolean readable, boolean ownerOnly) {}
    public boolean setReadable(boolean readable) {}
    public boolean setExecutable(boolean executable, boolean ownerOnly) {}
    public boolean setExecutable(boolean executable) {}
    public boolean canExecute() {}
    public static File[] listRoots() {}
    public long getTotalSpace() {}
    public long getFreeSpace() {}
    public long getUsableSpace() {}
    private static final Object tmpFileLock = new Object();
    private static int counter = -1;
    private static File generateFile(String prefix, String suffix, File dir) {}
    private static String tmpdir;
    private static String getTempDir() {}
    private static boolean checkAndCreate(String filename, SecurityManager sm) {}
    public static File createTempFile(String prefix, String suffix,File directory) {}
    public static File createTempFile(String prefix, String suffix) {}
    public int compareTo(File pathname) {}
    public boolean equals(Object obj) {}
    public int hashCode() {}
    public String toString() {}
    private synchronized void writeObject(java.io.ObjectOutputStream s) {}
    private synchronized void readObject(java.io.ObjectInputStream s) {}
    private static final long serialVersionUID = 301077366599181567L;
}

