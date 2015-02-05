package io.asimov.vis.timeline;

import io.asimov.model.AbstractNamed;

import com.eaio.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link VisJSTimelineGroup}
 * 
 * @date $Date: 2014-07-03 18:07:51 +0200 (do, 03 jul 2014) $
 * @version $Revision: 974 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class VisJSTimelineGroup extends AbstractNamed<VisJSTimelineGroup>
{

	/** */
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private static int groupCount = 0;

	private int id;
	
	private String content;
	
	public VisJSTimelineGroup(){
		// Bean zero-argument constructor
		super.setValue(new UUID().toString());
	}

	public VisJSTimelineGroup(final String content){
		this();
		this.id = ++groupCount;
		this.content = content;
	}
	
	public VisJSTimelineGroup(int id, final String content){
		this();
		this.id = id;
		groupCount = Math.max(groupCount, this.id);
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
	 * @return the id
	 */
	public int getId()
	{
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
		groupCount = Math.max(groupCount, this.id);
	}
	
	
}
