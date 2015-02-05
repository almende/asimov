package io.asimov.vis.timeline;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.arum.model.events.PersonEvent;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.log.LogUtil;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * {@link VisJSTimelineUtil}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public class VisJSTimelineUtil
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger(VisJSTimelineUtil.class);

	/** */
	private static File outputFile;

	/** */
	private static File styleSheetFile;
	

	public static List<Color> pick(int num)
	{
		List<Color> colors = new ArrayList<Color>();
		if (num < 2)
			return colors;
		float dx = 1.0f / (float) (num - 1);
		for (int i = 0; i < num; i++)
		{
			colors.add(get(i * dx));
		}
		return colors;
	}

	public static Color get(float x)
	{
		float r = 0.0f;
		float g = 0.0f;
		float b = 1.0f;
		if (x >= 0.0f && x < 0.2f)
		{
			x = x / 0.2f;
			r = 0.0f;
			g = x;
			b = 1.0f;
		} else if (x >= 0.2f && x < 0.4f)
		{
			x = (x - 0.2f) / 0.2f;
			r = 0.0f;
			g = 1.0f;
			b = 1.0f - x;
		} else if (x >= 0.4f && x < 0.6f)
		{
			x = (x - 0.4f) / 0.2f;
			r = x;
			g = 1.0f;
			b = 0.0f;
		} else if (x >= 0.6f && x < 0.8f)
		{
			x = (x - 0.6f) / 0.2f;
			r = 1.0f;
			g = 1.0f - x;
			b = 0.0f;
		} else if (x >= 0.8f && x <= 1.0f)
		{
			x = (x - 0.8f) / 0.2f;
			r = 1.0f;
			g = 0.0f;
			b = x;
		}
		return new Color(r, g, b);
	}

	public synchronized static VisJSTimelineGroup getGroupWithName(
			final String name, VisJSTimeline timeline)
	{
		if (name == null)
			return getGroupWithName("world",timeline);
		VisJSTimelineGroup result = null;
		for (VisJSTimelineGroup group : timeline.getGroups())
			if (group.getContent().equals(name))
			{
				result = group;
				break;
			}
		if (result == null)
		{
			result = new VisJSTimelineGroup(name);
			timeline.addGroup(result);
		}
		return result;
	}

	/**
	 * Returns the nearest item that
	 * 
	 * @param item
	 * @return
	 */
	public synchronized static boolean storePairedItem(VisJSTimelineItem item, VisJSTimeline timeline)
	{
		boolean paired = false;
		for (VisJSTimelineItem i : timeline.getItems())
			if (i.getEnd() == null && item.getStart().after(i.getStart())
					&& item.getContent().equals(i.getContent())
					&& ((item == null) ? i == null : item.getTitle().equals(i.getTitle()))	
					&& item.getClassName().equals(i.getClassName())
					&& item.getGroup().equals(i.getGroup()))
			{
				i.setEnd(item.getStart());
				paired = true;
				break;
			}
		if (!paired)
		{
			timeline.addItem(item);
		}
		return paired;
	}
	
	
	/**
	 * @param processTypes
	 * @throws Exception
	 */
	public static void writeTimelineData(final List<PersonEvent<?>> list)
			throws Exception
	{
		outputFile = new File(
				"src/test/resources/resource-usage-vis/usage.json");
		styleSheetFile = new File(
				"src/test/resources/resource-usage-vis/style.css");
		FileWriter fw = new FileWriter(outputFile);
		FileWriter fws = new FileWriter(styleSheetFile);
		writeTimelineData(list, fw, fws);
	}

	/**
	 * @param processTypes
	 * @throws Exception
	 */
	public static void writeTimelineData(final List<PersonEvent<?>> list, Writer outputWriter, Writer styleWriter)
			throws Exception
	{
		
		final VisJSTimeline timeline = new VisJSTimeline();

		
		if (list == null || list.size() == 0)
			throw new IllegalStateException("Got no events!!!!");
		
		HashSet<String> classNames = new HashSet<String>();

		VisJSTimelineItem item;
		String processInstanceName = null;
		for (PersonEvent<?> personEvent : list)
		{
			Event<?> event = (Event<?>) personEvent;
			if (event.getProcessInstanceID() != null)
			{
				processInstanceName = event.getProcessInstanceID();
				classNames.add("c" + processInstanceName);
			} else
			{
				LOG.error("Could not find the process instance for this event");
			}
			item = new VisJSTimelineItem();
			item.setClassName("c"+processInstanceName);
			item.setStart(event.getExecutionTime().getIsoTime());
			if (event instanceof MovementEvent)
			{
				// if (event.hasType(EventType.ENTER)){
				// String resource = ((MovementEvent)
				// event).toXML().getAssemblyLineRef();
				// LOG.info("Visualizing assemblyLine entrance in timeline item.");
				// item.setGroup(getGroupWithName(resource));
				// item.setContent(((PersonEvent<?>)
				// event).getPerson().getName());
				// timeline.addItem(item);
				// } else {
				// LOG.info("Ignoring assemblyLine leave event for in timeline items.");
				// }
				continue;
			} else if (event instanceof MaterialEvent)
			{
				LOG.info("Visualizing material event in timeline item.");
				MaterialEvent e = (MaterialEvent) event;
				EventRecord er = e.toXML();
				String resource = er.getAssemblyLineRef() + ": "
						+ er.getMaterialRef();
				item.setGroup(getGroupWithName(resource, timeline));
				item.setContent(e.getPerson().getName());
				item.setTitle(
						e.getPerson().getType().getName() + " : "
						+ e.getPerson().getName() + "\n:"
						+ er.getProcessRef() + "\ninstance: "
								+ processInstanceName + "\nactivity:"
						+ er.getActivityRef() + "\ninterval: {interval}");
				
			} else if (event instanceof ActivityEvent)
			{
				LOG.info("Visualizing activity as assemblyLine usage event in timeline item.");
				ActivityEvent e = (ActivityEvent) event;
				EventRecord er = ((ActivityEvent) event).toXML();
				String resource = er.getAssemblyLineRef();
				item.setGroup(getGroupWithName(resource, timeline));
				item.setContent(e.getPerson().getName());
				item.setTitle(e.getPerson().getType().getName() + " : "
						+ e.getPerson().getName() + "\n"
						+ er.getProcessRef() + "\ninstance: "
								+ processInstanceName + "\nactivity: "
										+ er.getActivityRef() + "\ninterval: {interval}");
				

			} else
			{
				LOG.warn("Unknown event type for event " + event);
				continue;
			}
			LOG.info("Processing: " + item);
			if (storePairedItem(item, timeline))
				LOG.info("event " + item + " got paired for timeline");
			else
				LOG.warn("new event " + item + " got created for timeline");

		}

		HashMap<String, String> classNameToStyle = new HashMap<String, String>();
		List<Color> colors = pick(classNames.size());
		int i = 0;
		for (String styleName : classNames)
		{
			String rgb = Integer.toHexString(colors.get(i).getRGB());
			rgb = rgb.substring(2, rgb.length());
			classNameToStyle.put(styleName, "{background-color: #" + rgb + "}");
			i++;
		}
		
		String data = "var data = " + timeline.toJSON() + ";\n";
		outputWriter.write(data);
		outputWriter.close();
		LOG.info("wrote:" + data);
		String styleData = "";
		
		for (String key : classNameToStyle.keySet())
			styleData += ".vis.timeline .item." + key + " "
					+ classNameToStyle.get(key) + "\n";
		styleWriter.write(styleData);
		styleWriter.close();
		LOG.info("wrote:" + styleData);
	}
}
