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
public class File implements Serializable//, Comparable
{
    private File(String pathname, int prefixLength) {}
    private File(String child, File parent) {}
    public File(String pathname) {}
    public File(String parent, String child) {}
    public File(File parent, String child) {}
    public File(URI uri) {}
    public String getName() { return "";}
    public String getParent() { return "";}
    public File getParentFile() { return null; }
    public String getPath() { return ""; }
    public boolean isAbsolute() { return true; }
    public String getAbsolutePath() { return "";}
    public File getAbsoluteFile() { return null; }
    public String getCanonicalPath() throws IOException { return "";}
    public File getCanonicalFile() throws IOException { return null; }
    private static String slashify(String path, boolean isDirectory) { return "";}
    public URL toURL() throws MalformedURLException { return null; }
    public URI toURI() { return null; }
    public boolean canRead() { return true;}
    public boolean canWrite() { return true;}
    public boolean exists() { return true;}
    public boolean isDirectory() { return true;}
    public boolean isFile() { return true;}
    public boolean isHidden() { return true;}
    public long lastModified() { return 0; }
    public long length() { return 0; }
    public boolean createNewFile() throws IOException { return true;}
    public boolean delete() { return true;}
    public void deleteOnExit() {}
    public String[] list() { return null; }
    public String[] list(FilenameFilter filter) { return null; }
    public File[] listFiles() { return null; }
    public File[] listFiles(FilenameFilter filter) { return null; }
    public File[] listFiles(FileFilter filter) { return null; }
    public boolean mkdir() { return true; }
    public boolean mkdirs() { return true; }
    public boolean renameTo(File dest) { return true; }
    public boolean setLastModified(long time) { return true; }
    public boolean setReadOnly() { return true; }
    public boolean setWritable(boolean writable, boolean ownerOnly) { return true; }
    public boolean setWritable(boolean writable) { return true; }
    public boolean setReadable(boolean readable, boolean ownerOnly) { return true; }
    public boolean setReadable(boolean readable) { return true; }
    public boolean setExecutable(boolean executable, boolean ownerOnly) { return true; }
    public boolean setExecutable(boolean executable) { return true; }
    public boolean canExecute() { return true; }
    public static File[] listRoots() { return null; }
    public long getTotalSpace() { return 0; }
    public long getFreeSpace() { return 0; }
    public long getUsableSpace() { return 0; }
    private static final Object tmpFileLock = new Object();
    private static int counter = -1;
    private static File generateFile(String prefix, String suffix, File dir) { return null; }
    private static String tmpdir;
    private static String getTempDir() { return ""; }
    private static boolean checkAndCreate(String filename, SecurityManager sm) { return true; }
    public static File createTempFile(String prefix, String suffix,File directory) { return null; }
    public static File createTempFile(String prefix, String suffix) { return null; }
    public int compareTo(File pathname) { return 0; }
    public boolean equals(Object obj) { return true; }
    public int hashCode() { return 0; }
    public String toString() { return ""; }
    private synchronized void writeObject(java.io.ObjectOutputStream s) {}
    private synchronized void readObject(java.io.ObjectInputStream s) {}
    private static final long serialVersionUID = 301077366599181567L;
}

