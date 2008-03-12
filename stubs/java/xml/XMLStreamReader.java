/*
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

/*
 * Copyright (c) 2003 by BEA Systems, Inc. All Rights Reserved.
 */

//////////////////
//  Modified by Manuel Breugelmans <manuel.breugelmans@student.ua.ac.be
//     for use with Fetch
//     removed implementation
//     removed unsupported features, eg templates
//////////////////

package javax.xml.stream;

import java.io.Reader;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @see javax.xml.stream.events.XMLEvent
 * @see XMLInputFactory
 * @see XMLStreamWriter
 * @since 1.6
 */
public interface XMLStreamReader extends XMLStreamConstants {
  public Object getProperty(java.lang.String name) throws java.lang.IllegalArgumentException;
  public int next() throws XMLStreamException;
  public void require(int type, String namespaceURI, String localName) throws XMLStreamException;
  public String getElementText() throws XMLStreamException;
  public int nextTag() throws XMLStreamException;
  public boolean hasNext() throws XMLStreamException;
  public void close() throws XMLStreamException;
  public String getNamespaceURI(String prefix);
  public boolean isStartElement();
  public boolean isEndElement();
  public boolean isCharacters();
  public boolean isWhiteSpace();
  public String getAttributeValue(String namespaceURI, String localName);
  public int getAttributeCount();
  public QName getAttributeName(int index);
  public String getAttributeNamespace(int index);
  public String getAttributeLocalName(int index);
  public String getAttributePrefix(int index);
  public String getAttributeType(int index);
  public String getAttributeValue(int index);
  public boolean isAttributeSpecified(int index);
  public int getNamespaceCount();
  public String getNamespacePrefix(int index);
  public String getNamespaceURI(int index);
  public NamespaceContext getNamespaceContext();
  public int getEventType();
  public String getText();
  public char[] getTextCharacters();
  public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)  throws XMLStreamException;
  public int getTextStart();
  public int getTextLength();
  public String getEncoding();
  public boolean hasText();
  public Location getLocation();
  public QName getName();
  public String getLocalName();
  public boolean hasName();
  public String getNamespaceURI();
  public String getPrefix();
  public String getVersion();
  public boolean isStandalone();
  public boolean standaloneSet();
  public String getCharacterEncodingScheme();
  public String getPITarget();
  public String getPIData();
}


