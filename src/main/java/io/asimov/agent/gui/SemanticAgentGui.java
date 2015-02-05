package io.asimov.agent.gui;

import io.asimov.messaging.A4EEMessage;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.plan.TimingCapability;
import io.coala.log.LogUtil;
import io.coala.message.Message;
import io.coala.time.SimTime;
import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import rx.functions.Action1;

/**
 * Class that is a GUI base class for some demo agent.
 * 
 * @author Thierry Martinez - France Telecom
 * @version Date: 2005/01/04 17:00:00 Revision: 1.0
 */
public class SemanticAgentGui extends JFrame
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil.getLogger(SemanticAgentGui.class);

	/*********************************************************************/
	/** CONSTRUCTORS **/
	/*********************************************************************/

	private final Binder binder;

	private final KBase knowledgeBase;

	/**
	 * Constructor
	 * 
	 * @param binder
	 * @param file parameter file
	 * @param pbk true iff the read message has to be put back into the message
	 *            queue
	 * @param showkb true iff the {@link KBase} should be displayed
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SemanticAgentGui(final Binder binder, final String file,
			final boolean pbk, final boolean showkb)
	{
		super(binder.getID().getValue());

		this.binder = binder;
		this.knowledgeBase = (KBase) binder.inject(ReasoningCapability.class)
				.getKBase();

		putback = pbk;
		showkbase = showkb;
		binder.inject(ReceivingCapability.class).getIncoming()
				.subscribe(new Action1<Message>()
				{
					@Override
					public void call(Message msg)
					{
						receivedMsgTextArea.append(msg.toString() + "\n");
						receivedMsgTextArea
								.setCaretPosition(receivedMsgTextArea
										.getDocument().getLength());
					}
				});

		// Building all needed panels
		// --------------------------
		JTabbedPane mainPanel = new JTabbedPane();
		JPanel firstPanel = new JPanel(new BorderLayout());
		JPanel secondPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		JPanel centeredPanel = new JPanel(new BorderLayout());
		JPanel southPanel = new JPanel(new BorderLayout());

		if (showkbase)
		{
			getContentPane().add(mainPanel);
			mainPanel.add("GUI", firstPanel);
			mainPanel.add("KBase", secondPanel);
			mainPanel.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					kbaseList.setListData(knowledgeBase.toStrings().toArray());
				}
			});
		} else
		{
			getContentPane().add(firstPanel);
		}

		firstPanel.add(BorderLayout.NORTH, northPanel);
		firstPanel.add(BorderLayout.CENTER, centeredPanel);
		firstPanel.add(BorderLayout.SOUTH, southPanel);
		northPanel.add(customPanel);

		JPanel NorthEastPanel = new JPanel(new BorderLayout());
		northPanel.add(BorderLayout.EAST, NorthEastPanel);

		// Setting the messages list
		// --------------------------------------
		JScrollPane msgScroll = new JScrollPane(messagesList);
		msgScroll.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Messages:"));
		msgScroll.setPreferredSize(new Dimension(220, 120));
		NorthEastPanel.add(BorderLayout.NORTH, msgScroll);
		messagesList.addMouseListener(new ListMouseAdapter());

		// Setting the receiver list
		// --------------------------------------
		JScrollPane receiverScroll = new JScrollPane(receiversList);
		receiverScroll.setPreferredSize(new Dimension(220, 80));
		receiverScroll.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Receivers:"));
		NorthEastPanel.add(BorderLayout.SOUTH, receiverScroll);
		receiversList.addMouseListener(new ListMouseAdapter());

		// Setting the sending area
		// ---------------------------------
		JPanel p = new JPanel(new GridLayout(3, 2));
		contentTextArea.setForeground(Color.BLUE);
		receiverTextArea.setForeground(Color.BLUE);
		performativeTextArea.setForeground(Color.BLUE);
		JScrollPane sp = new JScrollPane(performativeTextArea);
		sp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Performative:",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
				// #DOTNET_EXCLUDE_BEGIN
				sp.getFont().deriveFont(0, 9)));

		sp.setPreferredSize(new Dimension(100, 52));
		p.add(sp);
		sp = new JScrollPane(receiverTextArea);
		sp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Receiver:",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
				// #DOTNET_EXCLUDE_BEGIN
				sp.getFont().deriveFont(0, 9)));
		p.add(sp);
		sp = new JScrollPane(contentTextArea);
		contentTextArea.setEditable(true);
		sp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Content:",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
				// #DOTNET_EXCLUDE_BEGIN
				sp.getFont().deriveFont(0, 9)));
		p.add(sp);
		centeredPanel.add(p);

		// #DOTNET_EXCLUDE_BEGIN
		centeredPanel.add(BorderLayout.SOUTH, new JButton(new AbstractAction(
				"Send this message")
		{

			/** */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				if (performativeTextArea.getText().equals("")
						|| receiverTextArea.getText().equals("")
						|| contentTextArea.getText().equals(""))
				{
					JOptionPane.showMessageDialog(new JFrame(),
							"A parameter is missing.", "Error !",
							JOptionPane.ERROR_MESSAGE);
				} else
				{
					sendMessage(performativeTextArea.getText(),
							receiverTextArea.getText(),
							contentTextArea.getText());
				}
			}
		}));

		// Setting the receiving area (SOUTH)
		// ----------------------------------
		JScrollPane receivedScroll = new JScrollPane(receivedMsgTextArea);
		receivedScroll.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK),
				"Received messages:", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION,
				// #DOTNET_EXCLUDE_BEGIN
				receivedScroll.getFont().deriveFont(0, 9)));

		southPanel.add(receivedScroll);
		// #DOTNET_EXCLUDE_BEGIN
		southPanel.add(BorderLayout.SOUTH, new JButton(new AbstractAction(
				"Clear")
		{

			/** */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				receivedMsgTextArea.setText("");
			}
		}));

		// Setting the kbase viewer
		// ------------------------
		if (showkbase)
		{
			JScrollPane kbaseScroll = new JScrollPane(kbaseList);
			kbaseScroll.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.BLACK), "KBase:"));
			kbaseScroll.setPreferredSize(new Dimension(280, 120));
			secondPanel.add(kbaseScroll);
			secondPanel.add(BorderLayout.SOUTH, p = new JPanel());
			// #DOTNET_EXCLUDE_BEGIN
			final JTextField text = new JTextField(20);
			final JTextArea answer = new JTextArea(5, 100)
			{
				/** */
				private static final long serialVersionUID = 1L;

				/**
				 * {@inheritDoc}
				 * 
				 * @see javax.swing.text.JTextComponent#setText(java.lang.String)
				 */
				@Override
				public void setText(String t)
				{
					String text = "";
					for (int i = 0; i < t.length(); i = i + 100)
					{
						text += t.substring(i, Math.min(i + 100, t.length()))
								+ "\n";
					}
					super.setText(text);
				}
			};
			answer.setWrapStyleWord(true);

			p.add(text);
			p.add(new JButton(new AbstractAction("Assert")
			{

				/** */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent evt)
				{
					Formula result;
					if (text.getText() != null
							&& !text.getText().trim().equals(""))
					{
						result = SL.formula(text.getText());
					} else
					{
						result = null;
					}
					if (result != null)
					{
						knowledgeBase.assertFormula(result);
						// kbaseList.setListData(((SemanticAgent)agent).getSemanticCapabilities().getBeliefBase().toStrings().toArray());
					}

				}
			}));
			p.add(new JButton(new AbstractAction("QueryRef")
			{

				/** */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent evt)
				{
					// if ( kbaseList.getSelectedIndex() != -1 ) {
					// Formula formPattern =
					// SL.formula(kbaseList.getSelectedValue().toString());
					// ((SemanticAgent)agent).getSemanticCapabilities().getBeliefBase().retractFormula(formPattern);
					// //
					// kbaseList.setListData(((SemanticAgent)agent).getSemanticCapabilities().getBeliefBase().toStrings().toArray());
					// }}
					IdentifyingExpression result;
					if (text.getText() != null
							&& !text.getText().trim().equals(""))
					{
						result = (IdentifyingExpression) SL.term(text.getText());
					} else
					{
						result = null;
					}
					if (result != null)
					{
						ArrayList reasons = new ArrayList();
						Term solution = knowledgeBase.queryRef(result, reasons);
						if (solution == null)
						{
							answer.setText("UNKNOWN, because\n" + reasons);
						} else
						{
							answer.setText(solution.toString());
						}
					}

				}
			}));
			p.add(new JButton(new AbstractAction("Query")
			{
				/** */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
				{
					Formula query;
					if (text.getText() != null
							&& !text.getText().trim().equals(""))
					{
						query = SL.formula(text.getText());
					} else
					{
						query = null;
					}
					if (query != null)
					{
						ArrayList reasons = new ArrayList();
						QueryResult result = knowledgeBase
								.query(query, reasons);
						if (result == null)
						{
							answer.setText("UNKNOWN, because\n" + reasons);
						} else
						{
							answer.setText(result.toString());
						}
					}
				}
			}));

			// #DOTNET_EXCLUDE_BEGIN
			p.add(new JButton(new AbstractAction("Refresh")
			{

				/** */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent evt)
				{
					kbaseList.setListData(knowledgeBase.toStrings().toArray());
				}
			}));

			p.add(new JButton(new AbstractAction("Dump KB")
			{

				/** */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent evt)
				{
					Iterator it = knowledgeBase.toStrings().iterator();
					String result = "";
					for (int i = 0; it.hasNext(); i++)
					{
						result = result
								.concat((i == 0) ? binder.getID()
										+ "  dumps knowledge base after user command:\n"
										: "\n").concat(it.next().toString());
					}
					LOG.info(result);
				}
			}));
			p.add(BorderLayout.SOUTH, answer);

			// ((SemanticAgent)agent).getSemanticCapabilities().getBeliefBase().addListener(
			// new BeliefBase.BeliefBaseListener() {
			// /**
			// * {@inheritDoc}
			// * @see
			// jade.semantics2.bbase.BeliefBase.BeliefBaseListener#updated()
			// */
			// public void updated() {
			// kbaseList.setListData(((SemanticAgent)agent).getSemanticCapabilities().getBeliefBase().toStrings().toArray());
			// }
			// });
		}

		// Reading the configuration file
		// ------------------------------
		String line = null;
		Vector<String> messages = new Vector<String>();
		Vector<String> receivers = new Vector<String>();
		if (file != null)
		{
			try
			{
				@SuppressWarnings("resource")
				java.io.BufferedReader reader = new BufferedReader(
						new FileReader(file));
				while ((line = reader.readLine()) != null)
				{
					if (!line.equals(""))
					{
						// #DOTNET_EXCLUDE_BEGIN
						String fields[] = line.split("#");
						// #DOTNET_EXCLUDE_END
						/*#DOTNET_INCLUDE_BEGIN
						String fields[] = line.Split(new char[] {'#'});
						#DOTNET_INCLUDE_END*/
						if (line.startsWith("image#"))
						{
							customPanel.add(new JLabel(getIcon(binder.getID()
									+ " GUI " + fields[1].trim())));
						} else if (line.startsWith("receiver#"))
						{
							receivers.add(fields[1].trim());
						} else if (line.startsWith("message#"))
						{
							String msgLabel = fields[1].trim();
							messages.add(msgLabel);
							msgMap.put(msgLabel, fields[2].trim());
						}
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// Setting lists data
		// ------------------
		messagesList.setListData(messages);
		receiversList.setListData(receivers);

		// Showing the frame
		// -----------------
		pack();
		setSize(400, 500);
		setVisible(true);
	}

	/*********************************************************************/
	/** METHODS **/
	/*********************************************************************/
	/**
	 * Returns the tempPanel to customize the GUI
	 * 
	 * @return a panel
	 */
	JPanel getCustomPanel()
	{
		return customPanel;
	}

	/*********************************************************************/
	/** INNER CLASS **/
	/*********************************************************************/

	/**
	 * Mouse adapter to handle mouse click within the messages or receivers list
	 */
	class ListMouseAdapter extends MouseAdapter
	{
		/**
		 * @param e a MouseEvent
		 */
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getSource() == messagesList
					&& messagesList.getSelectedIndex() != -1)
			{
				String msg = (String) msgMap.get(messagesList
						.getSelectedValue().toString());
				performativeTextArea
						.setText(msg.substring(0, msg.indexOf(" ")));
				contentTextArea.setText(msg.substring(msg.indexOf(" ") + 1));
			} else if (e.getSource() == receiversList
					&& receiversList.getSelectedIndex() != -1)
			{
				receiverTextArea.setText(receiversList.getSelectedValue()
						.toString());
			}
		}
	}

	/*********************************************************************/
	/** INTERNALS **/
	/*********************************************************************/

	/***
	 * Text area
	 */
	JTextArea receivedMsgTextArea = new JTextArea(5, 10);

	/**
	 * Text area describing message to send
	 */
	JTextArea contentTextArea = new JTextArea();
	/**
    *
    */
	JTextArea receiverTextArea = new JTextArea();
	/**
    *
    */
	JTextArea performativeTextArea = new JTextArea();

	/**
	 * JPanel to received a particular component to customise this frame
	 */
	JPanel customPanel = new JPanel(new BorderLayout());

	/**
	 * Map that makes a link between a label and the effective message
	 */
	HashMap msgMap = new HashMap();

	/**
	 * The jade agent associated with this GUI
	 */
	// Agent agent = null;

	/**
	 * A boolean to tell if the read message has to be put back into the message
	 * queue
	 */
	boolean putback = false;

	/**
	 * A boolean to tell if the kbase should be displayed
	 */
	boolean showkbase = false;

	/**
	 * The messages and receivers lists
	 */
	@SuppressWarnings("rawtypes")
	JList messagesList = new JList();
	/**
    *
    */
	@SuppressWarnings("rawtypes")
	JList receiversList = new JList();

	/**
	 * The kbase list
	 */
	@SuppressWarnings("rawtypes")
	JList kbaseList = new JList();

	/**
	 * HashMap containing agent and clothing representations.
	 */
	static final HashMap images = new HashMap();

	/**
	 * Returns the GIF image icon the name is the given one
	 * 
	 * @param name the name of the GIF image icon to return (with no extension)
	 * @return an image icon
	 */
	static protected ImageIcon getIcon(String name)
	{
		ImageIcon result = (ImageIcon) images.get(name);
		LOG.trace("image=" + name);
		if (result == null)
		{
			// #DOTNET_EXCLUDE_BEGIN
			java.net.URL url = Thread.currentThread().getContextClassLoader()
					.getResource(name + ".gif");
			LOG.trace("url=" + url);
			if (url != null)
			{
				result = new ImageIcon(url);
				images.put(name, result);
			}
		}
		return result;
	} // End of getIcon/1

	/**
	 * Sends a message
	 * 
	 * @param permormative name of the performative
	 * @param receiver receiver of the message
	 * @param content content of the message
	 */
	protected void sendMessage(String permormative, String receiver,
			String content)
	{
		/*
		int perf = -1;
		if (permormative.equals("request-whenever") || permormative.equals("REQUEST-WHENEVER")) {perf = ACLMessage.REQUEST_WHENEVER;}
		else if (permormative.toLowerCase().equals("request-when")) {perf = ACLMessage.REQUEST_WHEN;}
		else if (permormative.toLowerCase().equals("not-understood")) {perf = ACLMessage.NOT_UNDERSTOOD;}
		else if (permormative.toLowerCase().equals("reject-proposal")) {perf = ACLMessage.REJECT_PROPOSAL;}
		else if (permormative.toLowerCase().equals("accept-proposal")) {perf = ACLMessage.ACCEPT_PROPOSAL;}
		else if (permormative.toLowerCase().equals("inform-if")) {perf = ACLMessage.INFORM_IF;}
		else if (permormative.toLowerCase().equals("inform-ref")) {perf = ACLMessage.INFORM_REF;}
		else if (permormative.toLowerCase().equals("query-if")) {perf = ACLMessage.QUERY_IF;}
		else if (permormative.toLowerCase().equals("query-ref")) {perf = ACLMessage.QUERY_REF;}
		else if (permormative.toLowerCase().equals("inform")) {perf = ACLMessage.INFORM;}
		else if (permormative.toLowerCase().equals("request")) {perf = ACLMessage.REQUEST;}
		else if (permormative.toLowerCase().equals("failure")) {perf = ACLMessage.FAILURE;}
		else if (permormative.toLowerCase().equals("confirm")) {perf = ACLMessage.CONFIRM;}
		else if (permormative.toLowerCase().equals("propose")) {perf = ACLMessage.PROPOSE;}
		else if (permormative.toLowerCase().equals("cancel")) {perf = ACLMessage.CANCEL;}
		else if (permormative.toLowerCase().equals("disconfirm")) {perf = ACLMessage.DISCONFIRM;}
		else if (permormative.toLowerCase().equals("agree")) {perf = ACLMessage.AGREE;}
		else if (permormative.toLowerCase().equals("refuse")) {perf = ACLMessage.REFUSE;}
		else if (permormative.toLowerCase().equals("subscribe")) {perf = ACLMessage.SUBSCRIBE;}
		else if (permormative.toLowerCase().equals("cfp")) {perf = ACLMessage.CFP;}
		*/
		// FIXME Add performative
		A4EEMessage msg = new A4EEMessage((SimTime) binder.inject(
				TimingCapability.class).getTime(), binder.getID(), new AgentID(
				binder.getID().getModelID(), receiver), content);
		// msg.setLanguage("fipa-sl");
		// msg.setContent(content);
		try
		{
			binder.inject(SendingCapability.class).send(msg);
		} catch (Exception e)
		{
			System.err.println("Error: [" + msg + "] was not sent!");
			e.printStackTrace();
		}
		System.err.println("[" + msg + "] was just sent");
	}

	@Override
	public void setVisible(boolean visibility)
	{
		try
		{
			if (!visibility)
				binder.inject(DestroyingCapability.class).destroy();
			super.setVisible(visibility);
			if (visibility == false)
				super.dispose();
		} catch (Exception e)
		{
			LOG.error("Failed to kill agent " + binder.getID(), e);
		}
	}

	/**
    *
    */
	@SuppressWarnings("unchecked")
	public void refresh()
	{
		kbaseList.setListData(this.knowledgeBase.toStrings().toArray());
	};

}
