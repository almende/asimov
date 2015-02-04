package io.arum.model.resource.supply;

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
 * {@link SupplyType}
 * 
 * @date $Date: 2014-07-10 08:44:48 +0200 (do, 10 jul 2014) $
 * @version $Revision: 977 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class SupplyType extends AbstractNamed<SupplyType> implements
		ResourceSubtype
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public static final SupplyType FRONT_PANEL = new SupplyType()
			.withValue("FrontPanel");
	
	public static final SupplyType FRAME_WORK = new SupplyType()
	.withValue("RearPanel");
	
	public static final SupplyType ELECTRONICAL_CIRCUIT = new SupplyType()
	.withValue("ElectronicalCircuit");
	
	
	public static class JsonDeserializer extends
			StdDeserializer<SupplyType>
	{
		/** */
		private static final long serialVersionUID = 3562344402828686313L;

		public JsonDeserializer()
		{
			super(SupplyType.class);
		}

		@Override
		public SupplyType deserialize(JsonParser jsonParser,
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
				if (className.equals(SupplyType.class.getCanonicalName()))
				{
					return new SupplyType().withName(name);
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
