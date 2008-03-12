/*
 * Copyright 2000-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

// XMLReader.java - read an XML document.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the Public Domain.
// $Id: XMLReader.java,v 1.3 2004/11/03 22:55:32 jsuttor Exp $

//////////////////
//  Modified by Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be
//     for use with Fetch
//     removed implementation
//     removed unsupported features, eg templates
//////////////////

package org.xml.sax;

import java.io.IOException;


/**
 * @since SAX 2.0
 * @author David Megginson
 * @see org.xml.sax.XMLFilter
 * @see org.xml.sax.helpers.ParserAdapter
 * @see org.xml.sax.helpers.XMLReaderAdapter
 */
public interface XMLReader
{
    public boolean getFeature (String name)  throws SAXNotRecognizedException, SAXNotSupportedException;
    public void setFeature (String name, boolean value)  throws SAXNotRecognizedException, SAXNotSupportedException;
    public Object getProperty (String name)  throws SAXNotRecognizedException, SAXNotSupportedException;
    public void setProperty (String name, Object value)  throws SAXNotRecognizedException, SAXNotSupportedException;
    public void setEntityResolver (EntityResolver resolver);
    public EntityResolver getEntityResolver ();
    public void setDTDHandler (DTDHandler handler);
    public DTDHandler getDTDHandler ();
    public void setContentHandler (ContentHandler handler);
    public ContentHandler getContentHandler ();
    public void setErrorHandler (ErrorHandler handler);
    public ErrorHandler getErrorHandler ();
    public void parse (InputSource input)  throws IOException, SAXException;
    public void parse (String systemId)  throws IOException, SAXException;
}

