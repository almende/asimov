#Simple Example

## A simplified MD's office

![Situation](ASIMOV_Simple_MD_Office_Example.png)

The drawing above provides an archetypical example of a MD’s office being modelled as use case in ASIMOV.
Where just a simple process of the agent visiting the MD practice and optionally schedule a next appointment afterwards is described. 

The resources are observable and thus a-priori assets of the investigation.

The context holds (meta-)physical constraints. In this case this is the building (rooms, doors and devices) and the fact that the people fulfilling the roles are starting outside of the building.

> Please note that a resource named "world" *must* be specified in the context.

The process references to the types of resources required given that the constraints from the context are and will be met.

If we specify as input the xml below:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<p1:SimulationFile xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns:asimovcim="http://io.asimov.almende.org/2015/schema/cim/"
 xmlns:asimovevent="http://io.asimov.almende.org/2015/schema/event/"
 xmlns:p1="http://io.asimov.almende.org/2015/schema/cim/"
 xmlns:asimovunit="http://io.asimov.almende.org/2015/schema/units/"
 xmlns:asimovbpm="http://io.asimov.almende.org/2015/schema/bpm/">
   <id>Simple example</id>
   <simulations>
       <simulationCase>
           <id>Simple example MD office</id>
                <usecase>
                    <id>MD Office</id>
                    <context>
                    <!-- Specify the building -->
                    <resource>
                            <resourceId>world</resourceId>
                            <resourceType>Model</resourceType>
                            <resourceSubType>Front door</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>true</infrastructural> 
                            <connectedResourceId timeDistance="P00Y000DT00H00M10S">
                                <connectedResourceId>MedicalCenter East Waitingroom</connectedResourceId>
                                <transportsResourceWithType>Person</transportsResourceWithType>
                            </connectedResourceId>
                        </resource>
                        <resource>
                            <resourceId>MedicalCenter East Waitingroom</resourceId>
                            <resourceType>Room</resourceType>
                            <resourceSubType>WaitingRoom</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>true</infrastructural> 
                            <connectedResourceId timeDistance="P00Y000DT00H00M10S">
                                <connectedResourceId>Office of Dokter D.</connectedResourceId>
                                <transportsResourceWithType>Person</transportsResourceWithType>
                            </connectedResourceId>
                            <connectedResourceId timeDistance="P00Y000DT00H00M10S">
                                <connectedResourceId>MD Dokter D. and B. Nurses Desk</connectedResourceId>
                                <transportsResourceWithType>Person</transportsResourceWithType>
                            </connectedResourceId>
                            <connectedResourceId timeDistance="P00Y000DT00H00M10S">
                                <connectedResourceId>world</connectedResourceId>
                                <transportsResourceWithType>Person</transportsResourceWithType>
                            </connectedResourceId>
                        </resource>
                        <resource>
                            <resourceId>Office of Dokter D.</resourceId>
                            <resourceType>Room</resourceType>
                            <resourceSubType>Consultation Office</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>true</infrastructural> 
                            <connectedResourceId timeDistance="P00Y000DT00H00M10S">
                                <connectedResourceId>MedicalCenter East Waitingroom</connectedResourceId>
                                <transportsResourceWithType>Person</transportsResourceWithType>
                            </connectedResourceId>
                        </resource>
                        <resource>
                            <resourceId>MD Dokter D. and B. Nurses Desk</resourceId>
                            <resourceType>Room</resourceType>
                            <resourceSubType>Nurses Desk</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>true</infrastructural> 
                            <connectedResourceId timeDistance="P00Y000DT00H00M10S">
                                <connectedResourceId>MedicalCenter East Waitingroom</connectedResourceId>
                                <transportsResourceWithType>Person</transportsResourceWithType>
                            </connectedResourceId>
                        </resource>
                    <!-- Specify the devices that are in the building -->
                        <resource>
                            <resourceId>Administration PC</resourceId>
                            <resourceType>Device</resourceType>
                            <resourceSubType>Computer</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>MD Dokter D. and B. Nurses Desk</connectedResourceId>
                            </containerResourceId>
                        </resource>
                         <resource>
                            <resourceId>Laser printer B120</resourceId>
                            <resourceType>Device</resourceType>
                            <resourceSubType>Printer</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>MD Dokter D. and B. Nurses Desk</connectedResourceId>
                            </containerResourceId>
                        </resource>
                        <resource>
                            <resourceId>Lighting 1</resourceId>
                            <resourceType>Device</resourceType>
                            <resourceSubType>Lighting</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>MD Dokter D. and B. Nurses Desk</connectedResourceId>
                            </containerResourceId>
                        </resource>
                         <resource>
                            <resourceId>Lighting 2</resourceId>
                            <resourceType>Device</resourceType>
                            <resourceSubType>Lighting</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>MedicalCenter East Waitingroom</connectedResourceId>
                            </containerResourceId>
                        </resource>
                         <resource>
                            <resourceId>Medical examining station</resourceId>
                            <resourceType>Device</resourceType>
                            <resourceSubType>General Medical Equipment</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>Office of dokter D.</connectedResourceId>
                            </containerResourceId>
                        </resource>
                        <resource>
                            <resourceId>Lighting 3</resourceId>
                            <resourceType>Device</resourceType>
                            <resourceSubType>Lighting</resourceSubType>
                            <stationary>true</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>Office of dokter D.</connectedResourceId>
                            </containerResourceId>
                        </resource>
					<!-- Specify the occupants that can enter the building -->
                        <resource>
                            <resourceId>Patient 1</resourceId>
                            <resourceType>Person</resourceType>
                            <resourceSubType>Patient</resourceSubType>
                            <stationary>false</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>world</connectedResourceId>
                            </containerResourceId>
                        </resource>
                        <resource>
                            <resourceId>Doctor D.</resourceId>
                            <resourceType>Person</resourceType>
                            <resourceSubType>Doctor</resourceSubType>
                            <stationary>false</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>world</connectedResourceId>
                            </containerResourceId>
                        </resource>
                        <resource>
                            <resourceId>Nurse A.</resourceId>
                            <resourceType>Person</resourceType>
                            <resourceSubType>Nurse</resourceSubType>
                            <stationary>false</stationary> 
                            <infrastructural>false</infrastructural> 
                            <containerResourceId>
                                <connectedResourceId>world</connectedResourceId>
                            </containerResourceId>
                        </resource>
                    </context>
                    <!-- Specify the process that are to be performed in the context -->
                    <process>
                        <id>Consultation process</id>
                        <name>Doctors Consult for patient with optional follow-up</name>
                        <activity>
                            <id>INTAKE</id>
                            <name>Intake</name>
                            <usedResource>
                                <resourceTypeRef>Room</resourceTypeRef>
                                <resourceSubTypeRef>Nurses Desk</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Device</resourceTypeRef>
                                <resourceSubTypeRef>Computer</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                                <timeOfUse>P0Y0M0DT0H2M</timeOfUse>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Device</resourceTypeRef>
                                <resourceSubTypeRef>Printer</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                                <timeOfUse>P0Y0M0DT0H2M</timeOfUse>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Person</resourceTypeRef>
                                <resourceSubTypeRef>Patient</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Person</resourceTypeRef>
                                <resourceSubTypeRef>Nurse</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <interruptable>true</interruptable>
                            <waitingTime>P00Y000DT00H00M00S</waitingTime>
                            <executionTime>P00Y000DT00H05M00S</executionTime>
                            <transportTime>P00Y000DT00H00M00S</transportTime>
                            <isBusinessActivity>true</isBusinessActivity>
                            <nextActivityRef likelihood="2">CONSULT</nextActivityRef>
                        </activity>
                        <activity>
                            <id>CONSULT</id>
                            <name>Consult</name>
                            <usedResource>
                                <resourceTypeRef>Room</resourceTypeRef>
                                <resourceSubTypeRef>Consultation Office</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                             <usedResource>
                                <resourceTypeRef>Device</resourceTypeRef>
                                <resourceSubTypeRef>General Medical Equipment</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Person</resourceTypeRef>
                                <resourceSubTypeRef>Patient</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Person</resourceTypeRef>
                                <resourceSubTypeRef>Doctor</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <interruptable>true</interruptable>
                            <waitingTime>P00Y000DT00H00M00S</waitingTime>
                            <executionTime>P00Y000DT00H20M00S</executionTime>
                            <transportTime>P00Y000DT00H00M00S</transportTime>
                            <isBusinessActivity>true</isBusinessActivity>
                            <previousActivityRef likelihood="2">INTAKE</previousActivityRef>
                            <nextActivityRef likelihood="1">FOLLOW-UP</nextActivityRef>
                        </activity>
                        <activity>
                            <id>FOLLOW-UP</id>
                            <name>Follow-Up</name>
                            <usedResource>
                                <resourceTypeRef>Room</resourceTypeRef>
                                <resourceSubTypeRef>Nurses Desk</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Device</resourceTypeRef>
                                <resourceSubTypeRef>Computer</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                                <timeOfUse>P0Y0M0DT0H4M</timeOfUse>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Device</resourceTypeRef>
                                <resourceSubTypeRef>Printer</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                                <timeOfUse>P0Y0M0DT0H2M</timeOfUse>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Person</resourceTypeRef>
                                <resourceSubTypeRef>Patient</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <usedResource>
                                <resourceTypeRef>Person</resourceTypeRef>
                                <resourceSubTypeRef>Nurse</resourceSubTypeRef>
                                <numberOf>1</numberOf>
                            </usedResource>
                            <interruptable>true</interruptable>
                            <waitingTime>P00Y000DT00H00M00S</waitingTime>
                            <executionTime>P00Y000DT00H05M00S</executionTime>
                            <transportTime>P00Y000DT00H00M00S</transportTime>
                            <isBusinessActivity>true</isBusinessActivity>
                            <previousActivityRef likelihood="1">CONSULT</previousActivityRef>                            
                        </activity>
                     </process>
                </usecase>
           <simulationResult>
               <simulationID>MD Office</simulationID>
               <eventTrace>
               </eventTrace>
           </simulationResult>
       </simulationCase>
   </simulations>
</p1:SimulationFile>
```
We can run a simulation that shows how many of the consultation processes can be performed in this MD office with 1 patient, 1 dokter and 1 nurse for a period of 2 days.

You can run this script from the asimov/dist directory by running the following command from your favorite shell:

```sh
$ java -jar ASIMOV.jar ../examples/simple/usecase.xml 2 ../examples/simple/output
``` 

After ASIMOV has ran the output will be stored in asimov/examples/simple/output

> Please note that you have to use JDK 1.7 and have to have MongoDB running.

More examples and detailed descriptions will be added shortly.
