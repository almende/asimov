package io.arum.model.resource.person;

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
 * {@link PersonRole}
 * 
 * @date $Date: 2014-07-10 08:44:48 +0200 (do, 10 jul 2014) $
 * @version $Revision: 977 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class PersonRole extends AbstractNamed<PersonRole> implements
		ResourceSubtype
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public static final PersonRole WORKER = new PersonRole()
			.withValue("Worker");
	
	public static final PersonRole TESTER = new PersonRole()
	.withValue("RAO");
	
	/** */
	public static final PersonRole MINOR_MANAGER = new PersonRole()
			.withValue("MinNC");

	/** */
	public static final PersonRole MAJOR_MANAGER = new PersonRole()
			.withValue("MajNC");

	public static class JsonDeserializer extends
			StdDeserializer<PersonRole>
	{
		/** */
		private static final long serialVersionUID = 3562344402828686313L;

		public JsonDeserializer()
		{
			super(PersonRole.class);
		}

		@Override
		public PersonRole deserialize(JsonParser jsonParser,
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
