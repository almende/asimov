package io.asimov.vis.timeline;

import io.asimov.model.AbstractNamed;

import java.util.Date;

import com.eaio.uuid.UUID;

/**
 * {@link VisJSTimelineItem}
 * 
 * @date $Date: 2014-07-14 12:38:32 +0200 (ma, 14 jul 2014) $
 * @version $Revision: 988 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class VisJSTimelineItem extends AbstractNamed<VisJSTimelineItem>
{

	/** */
	private static final long serialVersionUID = 1L;
		
	private String content;
	
	private String title;
	
	private VisJSTimelineGroup group;
	
	private String className;
	
	private Date start;
	
	private Date end;
	
	
	public VisJSTimelineItem(){
		// Bean zero-argument constructor
		super.setValue(new UUID().toString());
	}

	public VisJSTimelineItem(final String content){
		this();
		this.content = content;
	}
	
	
	
	/**
	 * @return the content
	 */
	public String getContent()
	{
		return this.content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content)
	{
		this.content = content;
	}

	/**
	 * @return the group
	 */
	public VisJSTimelineGroup getGroup()
	{
		return this.group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(VisJSTimelineGroup group)
	{
		this.group = group;
	}

	/**
	 * @return the className
	 */
	public String getClassName()
	{
		return this.className;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
	this.className = className;}
	

	/**
	 * @return the start
	 */
	public Date getStart()
	{
		return this.start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
	this.start = start;}
	

	/**
	 * @return the end
	 */
	public Date getEnd()
	{
		return this.end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(Date end) {
	this.end = end;}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	
}
