package io.asimov.vis.timeline;

import io.asimov.model.AbstractNamed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link VisJSTimeline}
 * 
 * @date $Date: 2014-07-03 18:07:51 +0200 (do, 03 jul 2014) $
 * @version $Revision: 974 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class VisJSTimeline extends AbstractNamed<VisJSTimeline>
{

	/** */
	private static final long serialVersionUID = 1L;
	
	private List<VisJSTimelineGroup> groups = new ArrayList<VisJSTimelineGroup>();
	private List<VisJSTimelineItem> items = new ArrayList<VisJSTimelineItem>();
	
	public VisJSTimeline(){
		// zero argument bean constructor
	}

	/**
	 * @return the groups
	 */
	public List<VisJSTimelineGroup> getGroups()
	{
		return this.groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(List<VisJSTimelineGroup> groups) {
	this.groups = groups;}
	

	/**
	 * @return the items
	 */
	public List<VisJSTimelineItem> getItems()
	{
		return this.items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<VisJSTimelineItem> items) {
	this.items = items;}

	/**
	 * @param group
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addGroup(VisJSTimelineGroup group)
	{
		return groups.add(group);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAllGroups(Collection<? extends VisJSTimelineGroup> c)
	{
		return groups.addAll(c);
	}

	/**
	 * @param item
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addItem(VisJSTimelineItem item)
	{
		return items.add(item);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAllItems(Collection<? extends VisJSTimelineItem> c)
	{
		return items.addAll(c);
	}
	
	

	
}
