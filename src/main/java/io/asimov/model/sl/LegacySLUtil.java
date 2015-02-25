/* $Id: LegacySLUtil.java 1078 2014-09-25 13:25:37Z suki $
 * $URL: https://redmine.almende.com/svn/a4eesim/trunk/adapt4ee-model/src/main/java/eu/a4ee/model/jsa/util/LegacySLUtil.java $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2010-2013 Almende B.V. 
 */
package io.asimov.model.sl;

import io.arum.model.resource.ARUMResourceType;
import io.asimov.model.Resource;
import io.asimov.model.ResourceAllocation;
import io.asimov.model.ResourceRequirement;
import io.asimov.model.ResourceType;
import io.asimov.model.Time;
import io.asimov.model.process.Activity;
import io.asimov.model.process.Next;
import io.asimov.model.process.Process;
import io.asimov.model.process.Task;
import io.asimov.model.process.Transition;
import io.asimov.model.sl.SLConvertible.FOLToSLHelper;
import io.coala.agent.AgentID;
import io.coala.capability.embody.Percept;
import io.coala.config.CoalaPropertyGetter;
import io.coala.jsa.JSAReasoningCapability;
import io.coala.jsa.sl.SLParsableSerializable;
import io.coala.log.LogUtil;
import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.AnyNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfFormula;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.SomeNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequenceNode;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * {@link LegacySLUtil}
 * 
 * @date $Date: 2014-09-25 15:25:37 +0200 (do, 25 sep 2014) $
 * @version $Revision: 1078 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class LegacySLUtil implements ASIMOVAgents
{

	/** */
	private static Logger LOG = LogUtil.getLogger(LegacySLUtil.class);

	final static Set<AgentID> rulesAddedToAgentSet = new HashSet<AgentID>();

	private static final String EXCLUSIVE_RESOURCE_ID_PROPERTY = "exclusiveResources";

	public static void makeObservation(final Observer<Percept> result,
			final Process cProcess, final AgentID agentID)
	{
		// load rules
		for (NodePercept rule : getRules(cProcess.getName(), agentID))
			result.onNext(rule);

		String pInstanceName = agentID.getValue();

		result.onNext(new NodePercept(new NotNode(getBelongsToProcessFormula(
				pInstanceName, Activity.PATTERN))));
		result.onNext(new NodePercept(new NotNode(getBelongsToProcessFormula(
				pInstanceName, Next.PATTERN))));
		result.onNext(new NodePercept(new NotNode(getBelongsToProcessFormula(
				pInstanceName, Next.OPTION_PATTERN))));

		// Add tasks to KBase
		for (Task task : cProcess.getTasks())
			result.onNext(new NodePercept(getBelongsToProcessFormula(
					pInstanceName, task.toSL())));

		// Add transitions to KBase
		for (Transition transition : cProcess.getTransitions())
			result.onNext(new NodePercept(getBelongsToProcessFormula(
					pInstanceName, transition.toSL())));

		// Add resources to KBase
		for (ResourceRequirement resource : cProcess.getRequiredResources()
				.values())
			result.onNext(new NodePercept(getBelongsToProcessFormula(
					pInstanceName, resource.toSL())));

		result.onCompleted();
	}

	public static Serializable convertAgentID(final AgentID agentID,
			final Serializable forumla)
	{
		Formula f;
		try
		{
			f = JSAReasoningCapability.getSLForObject(Formula.class,
					new SLParsableSerializable(forumla.toString()));
		} catch (Exception e)
		{
			LOG.error("Failed to parse Serializable to formula", e);
			return null;
		}
		Term resourceMatchPattern = ResourceRequirement.TASK_RESOURCE_PATTERN
				.instantiate(ResourceRequirement.TASK_RESOURCE,
						Resource.RESOURCE_PATTERN
				// .instantiate(Resource.RESOURCE_NAME,agentName)
				// .instantiate(Resource.RESOURCE_SUB_TYPE,
				// SL.string(requirement.getResource().getSubTypeID().getName()))
				// .instantiate(Resource.RESOURCE_TYPE,SL.string(requirement.getResource().getTypeID().getName()))
				)
				.instantiate(ResourceRequirement.TASK_RESOURCE_AMOUNT,
						SL.integer(1))
				.instantiate(ResourceRequirement.TASK_RESOURCE_DURATION,
						new Time().withMillisecond(0).toSL());
		Formula resourceFormula = SLConvertible.ASIMOV_PROPERTY_SET_FORMULA
				.instantiate(SLConvertible.sASIMOV_PROPERTY,
						SL.string(ResourceRequirement.TASK_RESOURCE))
				.instantiate(SLConvertible.sASIMOV_KEY,
						SL.string(ResourceRequirement.TASK_RESOURCE_TERM_NAME))
				.instantiate(SLConvertible.sASIMOV_VALUE, resourceMatchPattern);
		ListOfNodes formulas = new ListOfNodes();
		if (f.childrenOfKind(Formula.class, formulas))
		{
			Iterator<?> formulaIterator = formulas.iterator();
			MatchResult matchedResourceResult = null;
			while (matchedResourceResult == null && formulaIterator.hasNext())
			{
				Formula matching = (Formula) formulaIterator.next();
				matchedResourceResult = resourceFormula.match(matching);
			}

			ListOfFormula resultList = new ListOfFormula();

			Formula result = BELONGS_TO_PROCESS_FORMULA.instantiate(
					PROCESS_PROPERTY,
					ResourceAllocation.PATTERN.instantiate(
							ResourceAllocation.RESOURCE_REQUIREMENT_ID,
							SL.string(matchedResourceResult
									.term(Resource.RESOURCE_SUB_TYPE)
									.toString().replace("\"", ""))))
					.instantiate(PROCESS_NAME, SL.string(agentID.getValue()));
			resultList.add(result);

			while (formulaIterator.hasNext())
			{
				Formula matching = (Formula) formulaIterator.next();
				MatchResult matchedEquipmentResult = resourceFormula
						.match(matching);
				if (matchedEquipmentResult != null)
					resultList.add(BELONGS_TO_PROCESS_FORMULA.instantiate(
							PROCESS_PROPERTY,
							ResourceAllocation.PATTERN.instantiate(
									ResourceAllocation.RESOURCE_REQUIREMENT_ID,
									matchedEquipmentResult
											.term(Resource.RESOURCE_SUB_TYPE)))
							.instantiate(PROCESS_NAME,
									SL.string(agentID.getValue())));
			}
			if (resultList.size() < 2)
				return new SLParsableSerializable(result.toString());
			String r = "(and ";
			Iterator<?> it = resultList.iterator();
			while (it.hasNext())
			{
				Formula ff = (Formula) it.next();
				r = r + ff + ((it.hasNext()) ? " " : ")");
			}
			return new SLParsableSerializable(r);
		}
		throw new IllegalStateException("Formula not well formatted.");
	}

	public static Formula getBelongsToProcessFormula(final String processName,
			Term property)
	{
		return getStaticBelongsToProcessFormula(property).instantiate(
				PROCESS_NAME, SL.string(processName));
	}

	@Deprecated
	public static Formula getStaticBelongsToProcessFormula(Term property)
	{
		return (Formula) SL.instantiate(BELONGS_TO_PROCESS_FORMULA,
				PROCESS_PROPERTY, property);
	}

	public static class NodePercept implements Percept,
			SLConvertible<NodePercept>
	{

		/** */
		private static final long serialVersionUID = 1L;

		private Node node;

		/** zero argument constructor */
		public NodePercept()
		{
			super();
		}

		public NodePercept(final Node node)
		{
			fromSL(node);
		}

		/** @see io.coala.jsa.sl.SLConvertible#toSL() */
		@SuppressWarnings("unchecked")
		@Override
		public <N extends Node> N toSL()
		{
			return (N) node;
		}

		/** @see io.coala.jsa.sl.SLConvertible#fromSL(jade.semantics.lang.sl.grammar.Node) */
		@Override
		public <N extends Node> NodePercept fromSL(N node)
		{
			this.node = node;
			return this;
		}

		@Override
		public String toString()
		{
			return (this.node == null) ? super.toString() : this.node
					.toString();
		}

	}

	private static java.util.ArrayList<NodePercept> getRules(
			final String processTypeID, final AgentID requestor)
	{
		final java.util.ArrayList<NodePercept> rules = new java.util.ArrayList<NodePercept>();
		if (rulesAddedToAgentSet.contains(requestor))
			return rules;
		rulesAddedToAgentSet.add(requestor);
		VariableNode a = new VariableNode("?aid");
		VariableNode formerActivityName = new VariableNode("?former_activity");
		VariableNode latterActivityName = new VariableNode("?latter_activity");
		VariableNode r = new VariableNode("?resource_reservation");
		VariableNode rs = new VariableNode("resource_reservations_set");
		VariableNode p = new VariableNode("?process_id");
		VariableNode tl = new VariableNode("?latter_time_id");
		VariableNode tf = new VariableNode("?former_time_id");
		VariableNode tc = new VariableNode("transition_cases");
		VariableNode ntc = new VariableNode("?transition_cases");
		VariableNode tcar = new VariableNode("?the_cardinality");
		VariableNode it = new VariableNode("?input_tasks_set");

		final String tpfx = "t";
		Formula transition_t = FOLToSLHelper
				.pfx(getStaticBelongsToProcessFormula(Transition.PATTERN), tpfx)
				.instantiate(FOLToSLHelper.pfx(Transition.TRANSITION_ID, tpfx),
						tl)
				.instantiate(FOLToSLHelper.pfx(PROCESS_NAME, tpfx), p);

		Formula activity = getStaticBelongsToProcessFormula(Activity.PATTERN)
				.instantiate(Activity.ACTIVITY_TIME_TOKEN, tf)
				.instantiate(PROCESS_NAME, p)
				.instantiate(Activity.ACTIVITY_NAME, formerActivityName);

		Formula resourceAllocation = getStaticBelongsToProcessFormula(
				ResourceAllocation.PATTERN).instantiate(PROCESS_NAME, p);

		Formula resourceReservation = getStaticBelongsToProcessFormula(
				ResourceRequirement.TASK_RESOURCE_PATTERN).instantiate(
				ResourceRequirement.TASK_RESOURCE, Resource.RESOURCE_PATTERN)
				.instantiate(PROCESS_NAME, p);

		Formula nextOption = getStaticBelongsToProcessFormula(
				Next.OPTION_PATTERN).instantiate(PROCESS_NAME, p);

		Formula anyResource = new AndNode(
				resourceAllocation.instantiate(
						ResourceAllocation.ALLOCATED_AGENT_AID, a).instantiate(
						ResourceAllocation.RESOURCE_REQUIREMENT_ID, r),
				new AndNode(
						resourceReservation.instantiate(Resource.RESOURCE_NAME,
								r),
						SL.formula("(member ??member ??set)")
								.instantiate("member", r)
								.instantiate(
										"set",
										new AnyNode(
												rs,
												activity.instantiate(
														Task.TASK_RESOURCE_RESERVATION_SET,
														rs)))));

		Formula transition_t_cardinality = new AndNode(new AndNode(transition_t
				.instantiate(FOLToSLHelper.pfx(Transition.CASE_IDS, tpfx), ntc)
				.instantiate(
						FOLToSLHelper.pfx(Transition.INPUT_TASK_NAMES, tpfx),
						it), SL.formula("(card " + ntc + " " + tcar + ")")),
				SL.formula("(member "
						+ ntc
						+ " "
						+ new SomeNode(tc, new AndNode(SL.formula("(member "
								+ formerActivityName + " " + it + ")"),
								transition_t.instantiate(
										FOLToSLHelper.pfx(Transition.CASE_IDS,
												tpfx), tc).instantiate(
										FOLToSLHelper.pfx(
												Transition.INPUT_TASK_NAMES,
												tpfx), it))) + ")"));

		Formula nextForAIDQuery = new AndNode(anyResource, activity);

		Formula nextOptions = FOLToSLHelper.generateImpliesNode(
				new AndNode(nextForAIDQuery, new AndNode(
						getStaticBelongsToProcessFormula(Activity.PATTERN)
								.instantiate(Activity.ACTIVITY_TIME_TOKEN, tl)
								.instantiate(PROCESS_NAME, p)
								.instantiate(Activity.ACTIVITY_NAME,
										latterActivityName),
						transition_t_cardinality)),
				nextOption
						.instantiate(Next.LATTER_ACTIVITY_TIME_TOKEN, tl)
						.instantiate(Next.FORMER_ACTIVITY_TIME_TOKEN, tf)
						.instantiate(Next.FORMER_ACTIVITY_NAME,
								formerActivityName)
						.instantiate(Next.LATTER_ACTIVITY_NAME,
								latterActivityName)
						.instantiate(Next.ACTOR_AGENT_AID, a)
						.instantiate(Next.CHANCE, tcar));

		rules.add(new NodePercept(nextOptions));

		VariableNode x = new VariableNode("?activity");
		VariableNode res = new VariableNode("?resources");
		VariableNode c = new VariableNode("?case_ids");
		VariableNode p_id = new VariableNode("?process_id");
		VariableNode t = new VariableNode("?time_id");
		VariableNode i = new VariableNode("input_task_names");
		VariableNode is = new VariableNode("?input_task_names_set");
		VariableNode o = new VariableNode("output_task_names");
		VariableNode os = new VariableNode("?output_task_names_set");

		Formula transition = getStaticBelongsToProcessFormula(
				Transition.PATTERN).instantiate(Transition.TRANSITION_ID, t)
				.instantiate(PROCESS_NAME, p_id);

		Formula leftFormulaA1 = new AndNode(transition, new AndNode(
				SL.formula("(member " + x + " " + os + ")"),
				SL.formula("(member "
						+ os
						+ " "
						+ new SomeNode(o, transition.instantiate(
								Transition.OUTPUT_TASK_NAMES, o)) + ")")));

		Formula leftFormulaA2 = new AndNode(transition.instantiate(
				Transition.INPUT_TASK_NAMES,
				new TermSequenceNode(new ListOfTerm(new Term[] { SL
						.string(Task.START_OF_PROCESS.getName() + "_"
								+ processTypeID) }))), new AndNode(
				SL.formula("(member " + x + " " + is + ")"),
				SL.formula("(member "
						+ is
						+ " "
						+ new SomeNode(i, transition.instantiate(
								Transition.INPUT_TASK_NAMES, i)) + ")")));

		Formula leftFormulaB = getStaticBelongsToProcessFormula(Task.PATTERN)
				.instantiate(Task.TASK_NAME, x)
				.instantiate(Task.TASK_RESOURCE_RESERVATION_SET, res)
				.instantiate(Task.CASE_IDS, c).instantiate(PROCESS_NAME, p_id);

		Formula rightFormula2 = getStaticBelongsToProcessFormula(
				Activity.PATTERN).instantiate(Activity.ACTIVITY_NAME, x)
				.instantiate(Activity.ACTIVITY_TIME_TOKEN, t)
				.instantiate(Task.TASK_NAME, x)
				.instantiate(Task.TASK_RESOURCE_RESERVATION_SET, res)
				.instantiate(Task.CASE_IDS, c).instantiate(PROCESS_NAME, p_id);

		rules.add(new NodePercept(FOLToSLHelper.generateImpliesNode(
				new AndNode(leftFormulaB, leftFormulaA1), rightFormula2)));

		rules.add(new NodePercept(FOLToSLHelper.generateImpliesNode(
				new AndNode(leftFormulaB, leftFormulaA2), rightFormula2)));

		return rules;
	}
	
	public static Formula countAllocationScore(final String keyName){
		 VariableNode procesName = new VariableNode("procesName");
		 VariableNode processCard = new VariableNode("?"+keyName);
		 return SL.formula("(card "
					+ new SomeNode(procesName, getStaticBelongsToProcessFormula(
							ResourceAllocation.PATTERN).instantiate(PROCESS_NAME,
							procesName)) + " " + processCard + ")");
		 
	}

	public static Formula requestResourceAllocationForRequirement(
			ResourceRequirement requirement)
	{
		Formula result = null;
		VariableNode procesName = new VariableNode("procesName");
		VariableNode processCard = new VariableNode("?processCard");
		// (and (member ??processCard (set 0)) (card (some ?procesName
		// (BELONGS_TO_PROCESS (RESOURCE_ALLOCATION :ALLOCATED_AGENT_AID
		// ??ALLOCATED_AGENT_AID :RESOURCE_REQUIREMENT_ID
		// ??RESOURCE_REQUIREMENT_ID) ?procesName)) ??processCard))
		Formula notAllocated = new AndNode(SL.formula("(member " + processCard
				+ " (set 0))"), SL.formula("(card "
				+ new SomeNode(procesName, getStaticBelongsToProcessFormula(
						ResourceAllocation.PATTERN).instantiate(PROCESS_NAME,
						procesName)) + " " + processCard + ")"));
		Term resourceMatchPattern = ResourceRequirement.TASK_RESOURCE_PATTERN
				.instantiate(
						ResourceRequirement.TASK_RESOURCE,
						Resource.RESOURCE_PATTERN
								// .instantiate(Resource.RESOURCE_NAME,agentName)
								.instantiate(
										Resource.RESOURCE_SUB_TYPE,
										SL.string(requirement.getResource()
												.getSubTypeID().getName()))
								.instantiate(
										Resource.RESOURCE_TYPE,
										SL.string(requirement.getResource()
												.getTypeID().getName())))
				.instantiate(ResourceRequirement.TASK_RESOURCE_AMOUNT,
						SL.integer(1))
				.instantiate(ResourceRequirement.TASK_RESOURCE_DURATION,
						new Time().withMillisecond(0).toSL());
		Formula resourceFormula = SLConvertible.ASIMOV_PROPERTY_SET_FORMULA
				.instantiate(SLConvertible.sASIMOV_PROPERTY,
						SL.string(ResourceRequirement.TASK_RESOURCE))
				.instantiate(SLConvertible.sASIMOV_KEY,
						SL.string(ResourceRequirement.TASK_RESOURCE_TERM_NAME))
				.instantiate(SLConvertible.sASIMOV_VALUE, resourceMatchPattern);

	
//			if (!requirement.getResource().getTypeID().getName()
//					.equalsIgnoreCase(ARUMResourceType.PERSON.name()) || !isExclusive(requirement))
//				result = resourceFormula;
//			else
				result = new AndNode(notAllocated, resourceFormula);
		
		//LOG.error(result);
		return result;
	}

	/**
	 * Check if a resource of a certain type or sub-type is exclusively allocated or not
	 * @param requirement the {@see ResourceRequirement}
	 * @return false if instance can be claimed by multiple allocators, otherwise true.
	 */
	private static boolean isExclusive(ResourceRequirement requirement) {
		CoalaPropertyGetter getter = new CoalaPropertyGetter(EXCLUSIVE_RESOURCE_ID_PROPERTY);
		if (getter != null) {
			String[] result = getter.getJSON(String[].class);
			if (result != null) {
				for (int i = 0 ; i < result.length; i++) {
					if (result[i].equals("*") || requirement.getResource().getTypeID().getName()
							.equalsIgnoreCase(result[i]) || requirement.getResource().getSubTypeID().getName()
						.equalsIgnoreCase(result[i]) 
						)
						return true;
				}
			} 
		}
		return false;
	}
	
	

}
