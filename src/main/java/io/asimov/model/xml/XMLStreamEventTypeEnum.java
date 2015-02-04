package io.asimov.model.xml;

/**
 * {@link XMLStreamEventTypeEnum}
 * 
 * @version $Revision: 976 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public enum XMLStreamEventTypeEnum
{
	/**
	 * Indicates an event is a start element
	 * 
	 * @see javax.xml.stream.events.StartElement
	 */
	START_ELEMENT,

	/**
	 * Indicates an event is an end element
	 * 
	 * @see javax.xml.stream.events.EndElement
	 */
	END_ELEMENT,

	/**
	 * Indicates an event is a processing instruction
	 * 
	 * @see javax.xml.stream.events.ProcessingInstruction
	 */
	PROCESSING_INSTRUCTION,

	/**
	 * Indicates an event is characters
	 * 
	 * @see javax.xml.stream.events.Characters
	 */
	CHARACTERS,

	/**
	 * Indicates an event is a comment
	 * 
	 * @see javax.xml.stream.events.Comment
	 */
	COMMENT,

	/**
	 * The characters are white space (see [XML], 2.10 "White Space Handling").
	 * Events are only reported as SPACE if they are ignorable white space.
	 * Otherwise they are reported as CHARACTERS.
	 * 
	 * @see javax.xml.stream.events.Characters
	 */
	SPACE,

	/**
	 * Indicates an event is a start document
	 * 
	 * @see javax.xml.stream.events.StartDocument
	 */
	START_DOCUMENT,

	/**
	 * Indicates an event is an end document
	 * 
	 * @see javax.xml.stream.events.EndDocument
	 */
	END_DOCUMENT,

	/**
	 * Indicates an event is an entity reference
	 * 
	 * @see javax.xml.stream.events.EntityReference
	 */
	ENTITY_REFERENCE,

	/**
	 * Indicates an event is an attribute
	 * 
	 * @see javax.xml.stream.events.Attribute
	 */
	ATTRIBUTE,

	/**
	 * Indicates an event is a DTD
	 * 
	 * @see javax.xml.stream.events.DTD
	 */
	DTD,

	/**
	 * Indicates an event is a CDATA section
	 * 
	 * @see javax.xml.stream.events.Characters
	 */
	CDATA,

	/**
	 * Indicates the event is a namespace declaration
	 * 
	 * @see javax.xml.stream.events.Namespace
	 */
	NAMESPACE,

	/**
	 * Indicates a Notation
	 * 
	 * @see javax.xml.stream.events.NotationDeclaration
	 */
	NOTATION_DECLARATION,

	/**
	 * Indicates a Entity Declaration
	 * 
	 * @see javax.xml.stream.events.NotationDeclaration
	 */
	ENTITY_DECLARATION,

	//
	;
}