package io.asimov.test.sim;

import io.asimov.messaging.ASIMOVMessage;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator.ConversionCallback;
import io.asimov.microservice.negotiation.ResourceAllocationRequestor.AllocationCallback;
import io.asimov.microservice.negotiation.ResourceAllocationResponder;
import io.asimov.model.ResourceAllocation;
import io.coala.agent.AgentID;
import io.coala.agent.AgentStatusUpdate;
import io.coala.agent.BasicAgent;
import io.coala.bind.Binder;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.know.ReasoningCapability.Belief;
import io.coala.jsa.JSAReasoningCapability;
import io.coala.jsa.sl.SLParsableSerializable;
import io.coala.lifecycle.ActivationType;
import io.coala.log.InjectLogger;
import jade.semantics.kbase.KBase;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.tools.SL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;
//import eu.a4ee.agent.gui.SemanticAgentGui;

/**
 * {@link NegotiationTestAgent}
 * 
 * @date $Date: 2014-09-23 16:13:57 +0200 (di, 23 sep 2014) $
 * @version $Revision: 1074 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class NegotiationTestAgent extends BasicAgent
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	// public final SemanticAgentGui myGui;

	private ReasoningCapability reasonerService;

	private ResourceAllocationNegotiator negotiator;

	@SuppressWarnings("unused")
	private ResourceAllocationResponder responder;

	private Observer<ASIMOVMessage> messageObserver = new Observer<ASIMOVMessage>()
	{

		@Override
		public void onCompleted()
		{
			LOG.info("All claims where received");

		}

		@Override
		public void onError(Throwable e)
		{
			e.printStackTrace();
		}

		@Override
		public void onNext(ASIMOVMessage args)
		{
			processMessage(args);

		}

	};

	/**
	 * {@link NegotiationTestAgent} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected NegotiationTestAgent(Binder binder)
	{
		super(binder);
		// myGui = new SemanticAgentGui(this.getID().getValue(), null, this,
		// true,
		// true, this.getID() + " GUI");
	}

	@Override
	public ActivationType getActivationType()
	{
		return ActivationType.ACTIVATE_ONCE;
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		System.err.println("I am a real Agent!"+getID());
		LOG.info("Initializing");
		this.reasonerService = getBinder().inject(ReasoningCapability.class);
		getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(ASIMOVMessage.class).subscribe(messageObserver);
		if (getID().getValue().contains("process"))
		{
			this.negotiator = getBinder().inject(
					ResourceAllocationNegotiator.class);
		} else
		{
			this.responder = getBinder()
					.inject(ResourceAllocationResponder.class);
		}
	}

	@Override
	public void activate()
	{
		if (this.negotiator != null)
			doAlloc(getBinder(), this.callback);
		else
			LOG.error("Could not find negotiator");
	}

	private void processMessage(final ASIMOVMessage msg)
	{
		if (msg.getClass().equals(ASIMOVMessage.class))
		{
			LOG.info("Adding " + msg.content + " to the knowledgebase.");
			getBinder().inject(ReasoningCapability.class).addBeliefToKBase(
					getBinder().inject(ReasoningCapability.class).toBelief(
							msg.content));
			LOG.info("STATE_UPDATE("
					+ getBinder().inject(ReasoningCapability.class).getKBase()
					+ "):"
					+ ((KBase) getBinder().inject(ReasoningCapability.class)
							.getKBase()).toStrings());
		}
	}

	private final AllocationCallback callback = new AllocationCallback()
	{

		@Override
		public void done(Map<AgentID, Serializable> resources)
		{
			LOG.info("Allocation done.");
			for (Entry<AgentID, Serializable> entry : resources.entrySet())
			{
				Belief belief = reasonerService.toBelief(entry.getValue(),
						ResourceAllocation.ALLOCATED_AGENT_AID, entry.getKey());
				LOG.info(entry.getKey() + " : " + belief);
				getBinder().inject(ReasoningCapability.class)
						.addBeliefToKBase(belief);
				LOG.info("STATE_UPDATE:"
						+ ((KBase) getBinder().inject(ReasoningCapability.class)
								.getKBase()).toStrings());
			}

			die();
		}

		@Override
		public void failure(Set<AgentID> aids)
		{
			LOG.info("Failed to allocate " + aids);
			die();
		}

		@Override
		public void error(Exception e)
		{
			LOG.error(e.getMessage(), e);
			org.junit.Assert.fail();
		}

		@Override
		public Map<AgentID, Serializable> getAllocatedResources()
		{
			//To be Overridden
			return null;
		}

		@Override
		public Set<AgentID> getUnavailabeResourceIDs()
		{
			//To be Overridden
			return null;
		}

		@Override
		public boolean wasSucces()
		{
			//To be Overridden
			return false;
		}

		@Override
		public AgentID getScenarioAgentID()
		{
			return NegotiationTest.unitTestAgentID;
		}
	};

	public void doAlloc(final Binder binder, final AllocationCallback callback)
	{
		try
		{
			binder.inject(CreatingCapability.class).createAgent(binder.getID())
					.subscribe(new Observer<AgentStatusUpdate>()
					{

						@Override
						public void onCompleted()
						{
							LOG.info("Negotiation completed.");

						}

						@Override
						public void onError(Throwable e)
						{
							e.printStackTrace();
						}

						@Override
						public void onNext(AgentStatusUpdate args)
						{
							if (args.getStatus().isInitializedStatus())
							{
								Map<Serializable, Set<AgentID>> candidateMap = new HashMap<Serializable, Set<AgentID>>();

								Set<AgentID> rooms = new HashSet<AgentID>();
								rooms.add(Platform.getInstance().agents.get(
										"room1").getID());
								rooms.add(Platform.getInstance().agents.get(
										"room2").getID());

								candidateMap
										.put(new SLParsableSerializable(
												SL.formula(
														"(card (some ??PROCESS_NAME (room ??"
																+ ResourceAllocation.ALLOCATED_AGENT_AID
																+ " ??PROCESS_NAME)) 0)")
														.toString()), rooms);

								Set<AgentID> patients = new HashSet<AgentID>();
								patients.add(Platform.getInstance().agents.get(
										"patient1").getID());

								candidateMap
										.put(new SLParsableSerializable(
												SL.formula(
														"(card (some ??PROCESS_NAME (patient ??"
																+ ResourceAllocation.ALLOCATED_AGENT_AID
																+ " ??PROCESS_NAME)) 0)")
														.toString()), patients);
								ConversionCallback conversion = new ConversionCallback()
								{
									Formula lastFormula = null;

									@Override
									public Serializable convert(Serializable f)
									{
										try
										{
											Formula formula = JSAReasoningCapability
													.getFormulaForObject(f);
											ListOfNodes formulas = new ListOfNodes();

											if (formula.childrenOfKind(
													Formula.class, formulas))
											{
												lastFormula = (Formula) formulas
														.get(formulas.size() - 1);
											} else
											{
												lastFormula = formula;
											}
											LOG.info("CONVERSION: IF "
													+ f.toString() + " THEN "
													+ lastFormula);
										} catch (Exception e)
										{
											LOG.error(
													"Failed to convert query",
													e);
											return f;
										}
										return new SLParsableSerializable(
												getBinder()
														.inject(ReasoningCapability.class)
														.toBelief(
																lastFormula,
																"PROCESS_NAME",
																binder.getID()
																		.toString())
														.toString());
									}
								};
								negotiator.negotiate(callback, candidateMap,
										conversion);
							}

						}
					});
			LOG.info("Allocating...");
		} catch (Exception e)
		{
			LOG.error("Error while booting agent", e);
		}

	}

}
