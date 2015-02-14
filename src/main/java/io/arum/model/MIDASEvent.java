package io.arum.model;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.arum.model.events.PersonEvent;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonRole;
import io.asimov.db.Datasource;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TSkeletonActivityType.RoleInvolved;
import io.coala.json.JSONConvertible;
import io.coala.json.JsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bean representing a MIDAS event.
 * 
 * { "jobId": "100", <-- 999 || Mapped from processIntanceId field of ASIMOV
 * Event "time": "2014-09-16T09:25:00.000+02:00", <-- Mapped from timestamp
 * field of ASIMOV Event "performedBy": "Francesco", <-- Mapped from person ref
 * field of ASIMOV Event "type": "worker", <-- global || Mapped from person role
 * ref fields of ASIMOV Event "assignment": "Produce Coffeemaker", <-- global ||
 * Mapped from activity field of ASIMOV Event "productId": "7", <-- Mapped from
 * processId field of ASIMOV Event "operation":
 * "start"|"finish"|"pause"|"resume"|"startOfDay"|"endOfDay" (where start/end of
 * day is added for every first/last event of the day "prerequisites": [ <--
 * null || all unused movement events performed by person before this activity {
 * "type": "Go to NC meeting" } ] }
 *
 * @author suki
 */

public class MIDASEvent implements Serializable, JSONConvertible<MIDASEvent> {

	/**
	 * The serialVersionUID of the MIDASEvent bean.
	 */
	private static final long serialVersionUID = -3493863909092431679L;

	private static final Map<String, Set<MIDASEvent>> personToPrerequisite = new HashMap<String, Set<MIDASEvent>>();

	private static final String GLOBAL_JOB_ID = "999";

	private static final String GLOBAL_IDENTIFIER = "global";

	private static long peopleInFactoryCount = 0;

	public static class MIDASPrerequisite implements Serializable,
			JSONConvertible<MIDASPrerequisite> {

		/**
		 * The serialVersionUID of the MIDASPrerequisite bean.
		 */
		private static final long serialVersionUID = 2422238853921502723L;

		public String type;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Override
		public String toJSON() {
			return JsonUtil.toPrettyJSON(this);
		}

		@Override
		public MIDASPrerequisite fromJSON(String jsonValue) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public MIDASEvent() {
		super();
		// Zero Argument bean constructor;
	}

	public static enum OperationEnum {
		start, finish, pause, resume, startOfDay, endOfDay
	}

	public String jobId = null;
	public String time = null;
	public String performedBy = null;
	public String type = null;
	public String assignment = null;
	public String productId = null;
	public OperationEnum operation;
	public ArrayList<MIDASPrerequisite> prerequisites = null;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getPerformedBy() {
		return performedBy;
	}

	public void setPerformedBy(String performedBy) {
		this.performedBy = performedBy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAssignment() {
		return assignment;
	}

	public void setAssignment(String assignment) {
		this.assignment = assignment;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getOperation() {
		return operation.name();
	}

	public void setOperation(OperationEnum operation) {
		this.operation = operation;
	}

	public ArrayList<MIDASPrerequisite> getPrerequisites() {
		return prerequisites;
	}

	public void setPrerequisites(ArrayList<MIDASPrerequisite> prerequisites) {
		this.prerequisites = prerequisites;
	}

	@Override
	public String toJSON() {
		return JsonUtil.toPrettyJSON(this);
	}

	@Override
	public MIDASEvent fromJSON(String jsonValue) {
		// TODO Autogenerated
		return null;
	}

	public static Map<String, Person> roleMap = new HashMap<String, Person>();

	public static String getTypeForPersonInActivity(final Person person,
			final String activityId, final String processId,
			final String processInstanceId, Datasource ds) {
		final String token = activityId + processInstanceId;
		io.asimov.model.process.Process process = ds.findProcessByID(processId);
		for (TSkeletonActivityType activity : process.toXML().getActivity()) {
			if (activity.getName().equals(activityId)) {
				for (RoleInvolved roleRequired : activity.getRoleInvolved()) {
					Person mappedPerson = roleMap.get(token
							+ roleRequired.getRoleRef());
					if (mappedPerson != null
							&& mappedPerson.getName().equals(person.getName()))
						return roleToTypeMapping(roleRequired.getRoleRef());
				}
				for (RoleInvolved roleRequired : activity.getRoleInvolved()) {
					for (PersonRole role : person.getTypes()) {
						if (!roleMap.containsKey(token + role.getName())
								&& roleRequired.getRoleRef().equals(
										role.getName())) {
							roleMap.put(role.getName() + token, person);
							return roleToTypeMapping(role.getName());
						}
					}
				}
			}
		}
		return person.getName();
	}

	public static String roleToTypeMapping(final String role) {
		if (role.equalsIgnoreCase("Electrical Engineer")
				|| role.equalsIgnoreCase("Worker"))
			return "worker";
		if (role.equalsIgnoreCase("RAO"))
			return "rao";
		if (role.equalsIgnoreCase("Minor Manager"))
			return "pm";
		if (role.equalsIgnoreCase("Major Manager"))
			return "mt";
		return role;
	}

	public static MIDASEvent getGlobalEvent(PersonEvent<?> event) {
		final MIDASEvent result = new MIDASEvent();
		if (event instanceof MovementEvent) {
			MovementEvent movementEvent = (MovementEvent) event;
			result.setAssignment(event.getPerson().getName());
			result.setTime(event.toXML().getTimeStamp().toString());
			if (movementEvent.getType().equals(EventType.ARIVE_AT_ASSEMBLY)
					&& movementEvent.getAssemblyLine() == null) {
				peopleInFactoryCount--;
				if (peopleInFactoryCount == 0) {
					result.setOperation(OperationEnum.endOfDay);
					result.setJobId(GLOBAL_JOB_ID);
					result.setPerformedBy(GLOBAL_IDENTIFIER);
					result.setType(GLOBAL_IDENTIFIER);
					result.setAssignment("");
					result.setProductId("");
					return result;
				}
			} else if (movementEvent.getType().equals(EventType.LEAVE_ASSEMBLY)
					&& movementEvent.getAssemblyLine() == null) {
				peopleInFactoryCount++;
				if (peopleInFactoryCount == 1) {
					result.setOperation(OperationEnum.startOfDay);
					result.setJobId(GLOBAL_JOB_ID);
					result.setPerformedBy(GLOBAL_IDENTIFIER);
					result.setType(GLOBAL_IDENTIFIER);
					result.setAssignment("");
					result.setProductId("");
					return result;
				}
			}

		}
		return null;
	}
	
	public MIDASEvent fromPersonEvent(PersonEvent<?> event, Datasource ds, boolean includeMaterial) {
		this.setAssignment(event.getPerson().getName());
		this.setTime(event.toXML().getTimeStamp().toString());
		this.setAssignment(event.getActivity());
		final String nameOfPersonInMovementEvent = event.getPerson().getName();
		if (!personToPrerequisite.containsKey(nameOfPersonInMovementEvent))
			personToPrerequisite.put(nameOfPersonInMovementEvent,
					new HashSet<MIDASEvent>());
		
		if (event instanceof MovementEvent) {
			MovementEvent movementEvent = (MovementEvent) event;
			this.setJobId(movementEvent.getProcessInstanceID()+" "+nameOfPersonInMovementEvent
					+ " walks before "
					+ event.getActivity());
			// add to mapping of person to midasEvents
			personToPrerequisite.get(nameOfPersonInMovementEvent).add(this);
			final String jobID = 
					movementEvent.getProcessInstanceID()+" "+nameOfPersonInMovementEvent
					+ " walks before "
					+ event.getActivity();
			this.setAssignment(nameOfPersonInMovementEvent
					+ " walks to assemblyLine before "
					+ event.getActivity());
			if (movementEvent.getType().equals(EventType.ARIVE_AT_ASSEMBLY) && movementEvent.getPerson() != null) {
				this.setJobId(jobID);
				this.setPerformedBy(movementEvent.getPerson().getName());
				this.setType(MIDASEvent.getTypeForPersonInActivity(
						movementEvent.getPerson(), movementEvent.getActivity(),
						movementEvent.getProcessID(),
						movementEvent.getProcessInstanceID(), ds));
				this.setProductId(movementEvent.getProcessID());
				this.setOperation(OperationEnum.finish);
				return this;
			}
			if (movementEvent.getType().equals(EventType.LEAVE_ASSEMBLY) && movementEvent.getPerson() != null) {
				this.setJobId(jobID);
				this.setPerformedBy(movementEvent.getPerson().getName());
				this.setType(MIDASEvent.getTypeForPersonInActivity(
						movementEvent.getPerson(), movementEvent.getActivity(),
						movementEvent.getProcessID(),
						movementEvent.getProcessInstanceID(), ds));
				this.setProductId(movementEvent.getProcessID());
				this.setOperation(OperationEnum.finish);
				return this;
			}
			
			
			
		} else if (event instanceof ActivityEvent) {
			ActivityEvent activityEvent = (ActivityEvent) event;
			final String jobID = 
					activityEvent.getProcessInstanceID()+" "+activityEvent.getPerson().getName()
					+ " performs activity "
					+ event.getActivity();
			if (activityEvent.getType().equals(EventType.START_ACTIVITY))
				this.setOperation(OperationEnum.start);
			else
				this.setOperation(OperationEnum.finish);
			this.setJobId(jobID);
			this.setPerformedBy(activityEvent.getPerson().getName());
			this.setType(MIDASEvent.getTypeForPersonInActivity(
					activityEvent.getPerson(), activityEvent.getActivity(),
					activityEvent.getProcessID(),
					activityEvent.getProcessInstanceID(), ds));
			this.setProductId(activityEvent.getProcessID());
			if (this.getOperation().equals(OperationEnum.finish)) {
				for (MIDASEvent e : personToPrerequisite
						.get(nameOfPersonInMovementEvent)) {
					MIDASPrerequisite pr = new MIDASPrerequisite();
					pr.setType(e.getAssignment());
					if (this.getPrerequisites() == null)
						this.setPrerequisites(new ArrayList<MIDASEvent.MIDASPrerequisite>());
				}
				personToPrerequisite.get(nameOfPersonInMovementEvent).clear();
			}
			return this;
		} else if (event instanceof MaterialEvent && includeMaterial) {
			MaterialEvent materialEvent = (MaterialEvent) event;
			final String jobID = 
					materialEvent.getProcessInstanceID()+" "+materialEvent.getPerson().getName()
					+ " uses material "+materialEvent.getMaterial()+" for activity "
					+ event.getActivity();
			if (materialEvent.getType().equals(EventType.START_USE_MATERIAL))
				this.setOperation(OperationEnum.start);
			else
				this.setOperation(OperationEnum.finish);
			this.setJobId(jobID);
			this.setPerformedBy(materialEvent.getPerson().getName());
			this.setAssignment("Assembly with material: "
					+ materialEvent.getMaterial());
			this.setType(MIDASEvent.getTypeForPersonInActivity(
					materialEvent.getPerson(), materialEvent.getActivity(),
					materialEvent.getProcessID(),
					materialEvent.getProcessInstanceID(), ds));
			this.setProductId(materialEvent.getProcessID());
			return this;
		}
		return null;
	}
}