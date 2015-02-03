package io.asimov.agent.resource;

import io.asimov.agent.resource.Named;
import io.coala.json.JSONConvertible;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.name.AbstractIdentifier;
import io.coala.name.Identifier;

import org.apache.log4j.Logger;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.NoSql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * {@link AbstractNamed}
 * 
 * @date $Date: 2014-09-11 15:17:30 +0200 (do, 11 sep 2014) $
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@JsonInclude(Include.NON_NULL)
@NoSql(dataFormat = DataFormatType.MAPPED)
public abstract class AbstractNamed<THIS extends AbstractNamed<THIS>> extends
		AbstractIdentifier<String> implements Named //, JSONConvertible// <T>
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(AbstractNamed.class);

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private String name = null;

	/** @param name the name to set for this {@link THIS} */
	protected void setName(final String name)
	{
		this.name = name;
		super.setValue(name);
	}

	/** @return the name of this {@link THIS} */
	@Override
	public String getName()
	{
		return this.name;
	}

	/**
	 * @param value the (unique) value of this {@link AbstractNamed} object
	 * @return this {@link AbstractNamed} object
	 */
	@SuppressWarnings("unchecked")
	protected THIS withValue(final String value)
	{
		setValue(value);
		return (THIS) this;
	}

	/** @param name the name to set for this {@link THIS} */
	@SuppressWarnings("unchecked")
	public THIS withName(final String name)
	{
		setName(name);
		return (THIS) this;
	}

	/** @see Identifier#setValue(Comparable) */
	@Override
	protected void setValue(final String value)
	{
		setName(value);
	}

	/** @see Identifier#getValue() */
	@Override
	@JsonIgnore
	public String getValue()
	{
		return getName();
	}

	/** @see Identifier#toString() */
	@Override
	public String toString()
	{
		return String.format("%s%s", getClass().getSimpleName(), toJSON());
	}

	/** @see JSONConvertible#toJSON() */
	@Override
	public String toJSON()
	{
		try
		{
			// final JsonNode node = JsonUtil.getJOM().valueToTree(this);
			return JsonUtil.getJOM().writeValueAsString(this);
		} catch (final JsonProcessingException e)
		{
			LOG.warn(
					"Problem marshalling " + getClass().getName() + " to JSON",
					e);
			return String.format("name=\"%s\"", getValue());
		}
	}

}
