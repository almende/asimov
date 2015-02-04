package io.arum.model.resource.assemblyline;

import io.arum.model.resource.ResourceSubtype;
import io.asimov.model.AbstractNamed;

import java.io.IOException;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * {@link AssemblyLineType}
 * 
 * @date $Date: 2014-07-10 08:44:48 +0200 (do, 10 jul 2014) $
 * @version $Revision: 977 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class AssemblyLineType extends AbstractNamed<AssemblyLineType> implements
		ResourceSubtype
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public static final AssemblyLineType ASSEMBLY_DESK = new AssemblyLineType()
			.withValue("AssemblyDesk");
	

	public static class JsonDeserializer extends
			StdDeserializer<AssemblyLineType>
	{
		/** */
		private static final long serialVersionUID = 3562344402828686313L;

		public JsonDeserializer()
		{
			super(AssemblyLineType.class);
		}

		@Override
		public AssemblyLineType deserialize(JsonParser jsonParser,
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
				if (className.equals(AssemblyLineType.class.getCanonicalName()))
				{
					return new AssemblyLineType().withName(name);
				} else
				{
					throw deserializationContext
							.mappingException("Expected an OccupantType in the JSON String");
				}
			}

			throw deserializationContext
					.mappingException("Expected JSON String");
		}
	}

}
