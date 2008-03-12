/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.xml.internal.stream;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.*;
import javax.xml.stream.util.XMLEventAllocator ;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl;
import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLStreamFilterImpl;
import com.sun.org.apache.xerces.internal.impl.Constants;

/** Factory Implementation for XMLInputFactory.
 * @author Neeraj Bajaj Sun Microsystems
 * @author K.Venugopal Sun Microsystems
 */
public class XMLInputFactoryImpl extends javax.xml.stream.XMLInputFactory {
    private PropertyManager fPropertyManager = new PropertyManager(PropertyManager.CONTEXT_READER);
    private static final boolean DEBUG = false;
    private XMLStreamReaderImpl fTempReader = null;
    public XMLInputFactoryImpl() {}
    public XMLEventReader createXMLEventReader(InputStream inputstream) throws XMLStreamException {}
    public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {}
    public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {}
    public XMLEventReader createXMLEventReader(String systemId, InputStream inputstream) throws XMLStreamException {}
    public XMLEventReader createXMLEventReader(java.io.InputStream stream, String encoding) throws XMLStreamException {}
    public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {}
    public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {}
    public XMLStreamReader createXMLStreamReader(InputStream inputstream) throws XMLStreamException {}
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {}
    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {}
    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {}
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream inputstream) throws XMLStreamException {}
    public XMLStreamReader createXMLStreamReader(InputStream inputstream, String encoding) throws XMLStreamException {}
    public XMLEventAllocator getEventAllocator() {}
    public XMLReporter getXMLReporter() {}
    public XMLResolver getXMLResolver() {}
    public void setXMLReporter(XMLReporter xmlreporter) {}
    public void setXMLResolver(XMLResolver xmlresolver) {}
    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {}
    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {}
    public Object getProperty(java.lang.String name) throws java.lang.IllegalArgumentException {}
    public boolean isPropertySupported(String name) {}
    public void setEventAllocator(XMLEventAllocator allocator) {}
    public void setProperty(java.lang.String name, Object value) throws java.lang.IllegalArgumentException {}
}

