/* $Id: Next.java 806 2014-03-28 10:22:14Z suki $
 * $URL: https://redmine.almende.com/svn/a4eesim/trunk/adapt4ee-model/src/main/java/eu/a4ee/model/bean/Next.java $
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
package io.asimov.model.process;

import io.asimov.model.AbstractEntity;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.SL;
import io.coala.jsa.sl.SLConvertible;

import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link Next} describes the sequence of activities within a process
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@NoSql(dataType = "next")
public class Next extends AbstractEntity<Next> implements SLConvertible<Next>,
		XMLConvertible<Object, Next>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private String processID;

	/** */
	private String formerTimeToken;

	/** */
	private String latterTimeToken;
	
	/** */
	private String actorID;
	
	private String chance = null;
	
	
	
	/** @return the actorID */
	public String getActorID()
	{
		return actorID;
	}

	/** @param actorID the actorID to set */
	public void setActorID(String actorID)
	{
		this.actorID = actorID;
	}

	/** @return the processID */
	public String getProcessID()
	{
		return this.processID;
	}

	/** @param processID the processID to set */
	protected void setProcessID(final String processID)
	{
		this.processID = processID;
	}

	/** @param processID the processID to set */
	public Next withProcessID(final String processID)
	{
		setProcessID(processID);
		return this;
	}

	/** @return the formerTimeToken */
	public String getFormerTimeToken()
	{
		return this.formerTimeToken;
	}

	/** @param formerTimeToken the formerTimeToken to set */
	protected void setFormerTimeToken(final String formerTimeToken)
	{
		this.formerTimeToken = formerTimeToken;
	}

	/** @return the latterTimeToken */
	public String getLatterTimeToken()
	{
		return this.latterTimeToken;
	}

	/** @param latterTimeToken the latterTimeToken to set */
	protected void setLatterTimeToken(final String latterTimeToken)
	{
		this.latterTimeToken = latterTimeToken;
	}
	
	/** @return chance the chance cardinality */
	public String getChance()
	{
		return this.chance;
	}

	/** @param chance the chance cardinality to set */
	protected void setChance(final String chance)
	{
		this.chance = chance;
	}

	/** @param latterTimeToken the latterTimeToken to set */
	public Next withTimeTokens(final String formerTimeToken,
			final String latterTimeToken)
	{
		setFormerTimeToken(formerTimeToken);
		setLatterTimeToken(latterTimeToken);
		return this;
	}

	/** */
	public final static String TERM_NAME = "NEXT";
	
	/** */
	public final static String OPTION_TERM_NAME = "NEXT_OPTION";

	/** */
	public static final String FORMER_ACTIVITY_TIME_TOKEN = "FORMER_ACTIVITY_TIME_TOKEN";

	/** */
	public static final String LATTER_ACTIVITY_TIME_TOKEN = "LATTER_ACTIVITY_TIME_TOKEN";

	/** */
	public static final String FORMER_ACTIVITY_NAME = "FORMER_ACTIVITY_NAME";

	/** */
	public static final String LATTER_ACTIVITY_NAME = "LATTER_ACTIVITY_NAME";
	
	/** */
	public static final String PROCESS_AGENT_AID = "PROCESS_AGENT_AID";
	
	/** */
	public static final String ACTOR_AGENT_AID = "ACTOR_AGENT_AID";
	
	/** */
	public static final String CHANCE = "CHANCE";

	/** */
	public static final ASIMOVTerm PATTERN = new ASIMOVTerm().withName(TERM_NAME)
			.instantiate(FORMER_ACTIVITY_TIME_TOKEN, null)
			.instantiate(LATTER_ACTIVITY_TIME_TOKEN, null)
			.instantiate(FORMER_ACTIVITY_NAME, null)
			.instantiate(LATTER_ACTIVITY_NAME, null)
			.instantiate(PROCESS_AGENT_AID, null)
			.instantiate(ACTOR_AGENT_AID, null);
	
	/** */
	public static final ASIMOVTerm OPTION_PATTERN = new ASIMOVTerm().withName(OPTION_TERM_NAME)
			.instantiate(FORMER_ACTIVITY_TIME_TOKEN, null)
			.instantiate(LATTER_ACTIVITY_TIME_TOKEN, null)
			.instantiate(FORMER_ACTIVITY_NAME, null)
			.instantiate(LATTER_ACTIVITY_NAME, null)
			.instantiate(PROCESS_AGENT_AID, null)
			.instantiate(ACTOR_AGENT_AID, null)
			.instantiate(CHANCE, null);
	

	/** @see SLConvertible#toSL() */
	@SuppressWarnings("unchecked")
	@Override
	public ASIMOVTerm toSL()
	{
		// FIXME add next's own id?
		return ((ASIMOVTerm) PATTERN)
				.instantiate(PROCESS_AGENT_AID, SL.string(getProcessID()))
				.instantiate(FORMER_ACTIVITY_TIME_TOKEN,
						SL.string(getFormerTimeToken()))
				.instantiate(LATTER_ACTIVITY_TIME_TOKEN,
						SL.string(getLatterTimeToken()));
	}

	/** @see XMLConvertible#toXML() */
	@Override
	public Object toXML()
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public Next fromXML(final Object xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public <N extends ASIMOVNode<N>> Next fromSL(final N term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

}
