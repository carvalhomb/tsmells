/*
 * Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.rmi.server;

import java.io.InvalidObjectException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import java.rmi.activation.Activatable;
import java.util.Map;
import java.util.WeakHashMap;
import sun.rmi.server.Util;
import sun.rmi.server.WeakClassHashMap;

/**
 * @author  Ann Wollrath
 * @since   1.5
 */
public class RemoteObjectInvocationHandler extends RemoteObject implements InvocationHandler
{
    private static final long serialVersionUID = 2L;
    private static final MethodToHash_Maps methodToHash_Maps =
    public RemoteObjectInvocationHandler(RemoteRef ref) {}
    public Object invoke(Object proxy, Method method, Object[] args) {}
    private Object invokeObjectMethod(Object proxy,==> COMPLETE MANUALLY
    private Object invokeRemoteMethod(Object proxy,==> COMPLETE MANUALLY
    private String proxyToString(Object proxy) {}
    private void readObjectNoData() throws InvalidObjectException {}
    private static long getMethodHash(Method method) {}
}

