package io.asimov.agent.resource;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link AbstractEntity}
 * 
 * @date $Date: 2014-03-26 20:36:21 +0100 (Wed, 26 Mar 2014) $
 * @version $Revision: 796 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@UuidGenerator(name = "EMP_ID_GEN")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
@NoSql(dataFormat = DataFormatType.MAPPED)
public abstract class AbstractEntity<T extends AbstractEntity<T>> extends
		AbstractNamed<T>
{

	/** */
	//	private static final Logger LOG = Logger.getLogger(AbstractEntity.class);

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@Id
	@GeneratedValue(generator = "EMP_ID_GEN")
	@Field(name = "_id")
	private String id;

	/** @return the object identifier, managed by the database */
	public UUID getUUID()
	{
		return this.id == null ? null : UUID.fromString(this.id);
	}

}
