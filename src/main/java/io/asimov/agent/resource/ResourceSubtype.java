package io.asimov.agent.resource;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * {@link ResourceSubtype}
 * 
 * @date $Date: 2014-07-10 08:44:48 +0200 (do, 10 jul 2014) $
 * @version $Revision: 977 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface ResourceSubtype extends Named
{

	/**
	 * {@link JsonSerializer}
	 * 
	 * @version $Revision: 977 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	class JsonSerializer extends StdSerializer<ResourceSubtype>
	{
		/**
		 * {@link JsonSerializer} constructor
		 */
		public JsonSerializer()
		{
			super(ResourceSubtype.class);
		}

		@Override
		public void serialize(ResourceSubtype value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException
		{
			jgen.writeString(value.getClass().getCanonicalName()
					+ "_classWithValue_" + value.getName());
		}
	}

	/**
	 * {@link JsonDeserializer}
	 * 
	 * @version $Revision: 977 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	@SuppressWarnings("serial")
	class JsonDeserializer extends StdDeserializer<ResourceSubtype>
	{
		/**
		 * {@link JsonDeserializer} constructor
		 */
		public JsonDeserializer()
		{
			super(ResourceSubtype.class);
		}

		@Override
		public ResourceSubtype deserialize(JsonParser jsonParser,
				DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException
		{
			if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING)
			{
				if (!jsonParser.getText().contains("_classWithValue_"))
					throw deserializationContext
							.mappingException("Expected JSON String formated as %s_classWithValue_%s");
				String[] splitResult = jsonParser.getText().split(
						"_classWithValue_");
				String className = splitResult[0];
				String name = splitResult[1];
				if (className.equals(PersonRole.class.getCanonicalName()))
				{
					return new PersonRole().withName(name);
				} else if (className.equals(AssemblyLineType.class.getCanonicalName()))
				{
					return new AssemblyLineType().withName(name);
				} else if (className.equals(SupplyType.class
						.getCanonicalName()))
				{
					return new SupplyType().withName(name);
				} else
				{
					throw deserializationContext
							.mappingException("Expected an implemented ResourceSubtype in the JSON String");
				}
			}

			throw deserializationContext
					.mappingException("Expected JSON String");
		}
	}

	class JsonKeyDeserializer extends KeyDeserializer
	{

		@Override
		public ResourceSubtype deserializeKey(String key,
				DeserializationContext ctxt) throws IOException,
				JsonProcessingException
		{
			String[] keySegments = key.split("{");
			String className = keySegments[0];
			String[] properties = keySegments[1].split(":");
			String name = key;
			if (properties[0].equals("\"name\""))
				name = properties[1].substring(1, properties[1].length() - 1);
			else
				throw ctxt.mappingException("Expected different key String");
			if (className.equals(PersonRole.class.getName()))
			{
				return new PersonRole().withName(name);
			} else if (className.equals(AssemblyLineType.class.getName()))
			{
				return new AssemblyLineType().withName(name);
			} else if (className.equals(SupplyType.class.getName()))
			{
				return new SupplyType().withName(name);
			}
			throw ctxt.mappingException("Expected different key String");
		}
	}

}
