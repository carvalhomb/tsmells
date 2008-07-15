//
// EntityReference.h
//
// $Id: //poco/svn/XML/include/Poco/DOM/EntityReference.h#2 $
//
// Library: XML
// Package: DOM
// Module:  DOM
//
// Definition of the DOM EntityReference class.
//
// Copyright (c) 2004-2006, Applied Informatics Software Engineering GmbH.
// and Contributors.
//
// Permission is hereby granted, free of charge, to any person or organization
// obtaining a copy of the software and accompanying documentation covered by
// this license (the "Software") to use, reproduce, display, distribute,
// execute, and transmit the Software, and to prepare derivative works of the
// Software, and to permit third-parties to whom the Software is furnished to
// do so, all subject to the following:
// 
// The copyright notices in the Software and this entire statement, including
// the above license grant, this restriction and the following disclaimer,
// must be included in all copies of the Software, in whole or in part, and
// all derivative works of the Software, unless such copies or derivative
// works are solely in the form of machine-executable object code generated by
// a source language processor.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
// SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
// FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//


#ifndef DOM_EntityReference_INCLUDED
#define DOM_EntityReference_INCLUDED


#include "Poco/XML/XML.h"
#include "Poco/DOM/AbstractNode.h"
#include "Poco/XML/XMLString.h"


namespace Poco {
namespace XML {


class XML_API EntityReference: public AbstractNode
	/// EntityReference objects may be inserted into the structure model when an
	/// entity reference is in the source document, or when the user wishes to insert
	/// an entity reference. Note that character references and references to predefined
	/// entities are considered to be expanded by the HTML or XML processor so that
	/// characters are represented by their Unicode equivalent rather than by an
	/// entity reference. Moreover, the XML processor may completely expand references
	/// to entities while building the structure model, instead of providing EntityReference
	/// objects. If it does provide such objects, then for a given EntityReference
	/// node, it may be that there is no Entity node representing the referenced
	/// entity. If such an Entity exists, then the child list of the EntityReference
	/// node is the same as that of the Entity node.
	/// 
	/// As for Entity nodes, EntityReference nodes and all their descendants are
	/// readonly.
	/// 
	/// The resolution of the children of the EntityReference (the replacement value
	/// of the referenced Entity) may be lazily evaluated; actions by the user (such
	/// as calling the childNodes method on the EntityReference node) are assumed
	/// to trigger the evaluation.
{
public:
	// Node
	const XMLString& nodeName() const;
	unsigned short nodeType() const;

protected:
	EntityReference(Document* pOwnerDocument, const XMLString& name);
	EntityReference(Document* pOwnerDocument, const EntityReference& ref);
	~EntityReference();

	Node* copyNode(bool deep, Document* pOwnerDocument) const;

private:
	XMLString _name;
	
	friend class Document;
};


} } // namespace Poco::XML


#endif // DOM_EntityReference_INCLUDED
