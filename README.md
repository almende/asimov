# ASIMOV
Agent-based SImulation, MOdeling, and Visualization of processes 

ASIMOV is a loosely coupled distributed inference engine that infers knowledge about the details of a process (e.g., consultation) occurring in a context (e.g., hospital) with the sole generic information about the process itself and measurement about a real-life process of that kind. Such knowledge can then be used to calculate KPIs, visualize analytics, make predictions, validate models, explain processes etc.   

Given a business process described as a flowchart and some measurements about the process, ASIMOV can infer what sequence of events have led to the measurements, thus inferring and abstracting knowledge about the process itself. 

As input ASIMOV receives two types of parameters
  - A Process/Workflow that describes what resources (Actors, Assets, Materials, Places, etc.) are **required** to participate in an activity at the same place and time together, and how these activities are sequenced after eachother expressed by a branching probability.
  - A Context describing the types and other properties of resources required to execute the process/workflow descriptions providing detailed information on how they **constrain** the process/workflow in both space and time dimentions.

ASIMOV produces:
  - Events that explain the execution of the process/workflow descriptions meeting the requirements of the the process/workflow and the constraints of the resources.
