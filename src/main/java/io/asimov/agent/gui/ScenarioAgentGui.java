package io.asimov.agent.gui;

import io.asimov.db.Datasource;
import io.asimov.model.TraceService;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.xml.XmlUtil;
import io.asimov.vis.timeline.VisJSTimelineUtil;
import io.asimov.xml.TEventTrace;
import io.coala.bind.Binder;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.log.LogUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

/**
 * {@link ScenarioAgentGui}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class ScenarioAgentGui extends JFrame
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(ScenarioAgentGui.class);

	/** */
	private static final long serialVersionUID = -5631971473763466432L;
	

	/** */
	private final Binder binder;

	/**
	 * {@link ScenarioAgentGui} constructor
	 */
	public ScenarioAgentGui(final Binder binder, String file, boolean pbk,
			boolean showkb)
	{
		super(binder.getID().getValue());
		this.binder = binder;
		final JPanel panel = new JPanel(new BorderLayout());

		panel.add(new JButton(new AbstractAction("pause simulator")
		{

			/** */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				pause();
			}
		}), BorderLayout.NORTH);
		panel.add(new JButton(new AbstractAction("write XML to "
				+ getOuputXMLName())
		{

			/** */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				final TraceService trace = TraceService.getInstance(binder
						.getID().getModelID().getValue());
				final Datasource ds = binder.inject(Datasource.class);
				final TEventTrace xmlOutput = trace.toXML(ds);
				try
				{
					List<ActivityEvent> events = new ArrayList<ActivityEvent>();
					for (ActivityEvent e : trace.getActivityEvents(ds))
						events.add(e);
					VisJSTimelineUtil.writeTimelineData(events);
				} catch (Exception e1)
				{
					LOG.error("Failed to write timeline data", e1);
				}
//				try
//				{
//					FileWriter fw = new FileWriter(plannerDataOutputFile);
//					String data = "var data = " + binder.inject(ResourceUsagePlanner.class).getVisJSTimeline().toJSON() + ";\n";
//					fw.write(data);
//					fw.close();
//					LOG.warn("wrote:" + data);
//				} catch (IOException e)
//				{
//					LOG.error(e.getMessage());
//				}
				try
				{
					XmlUtil.getCIMMarshaller().marshal(xmlOutput,
							new File(getOuputXMLName()));
					LOG.info("Wrote the following xml to " + getOuputXMLName());
					XmlUtil.getCIMMarshaller().marshal(xmlOutput, System.out);
				} catch (JAXBException e)
				{
					LOG.error("Failed to write xml output", e);
				}
				
				
			}
		}), BorderLayout.CENTER);
		panel.add(new JButton(new AbstractAction("Resume simulator")
		{

			/** */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				start();
			}
		}), BorderLayout.SOUTH);
		this.add(panel);
		pack();
		setSize(400, 500);
		setVisible(true);
	}

	/**
	 * @return
	 */
	private String getOuputXMLName()
	{
		return this.binder.getID().getModelID().getValue() + "_output.xml";
	}

	private void pause()
	{
		final ReplicatingCapability sim = this.binder.inject(ReplicatingCapability.class);
		if (sim.isRunning())
			sim.pause();
	};

	private void start()
	{
		final ReplicatingCapability sim = this.binder.inject(ReplicatingCapability.class);
		if (!sim.isRunning())
			sim.start();
	};

	@Override
	public void setVisible(boolean visibility)
	{
		try
		{
			if (!visibility)
				this.binder.inject(DestroyingCapability.class).destroy();
			super.setVisible(visibility);
			if (visibility == false)
				super.dispose();
		} catch (Exception e)
		{
			LOG.error("Failed to kill agent " + this.binder.getID(), e);
		}
	}

}
