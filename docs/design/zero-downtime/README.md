# Zero Downtime Upgrades for Open Kilda

## Rationale
Current deployment process requires time slot with no reactions from contol plane for any network events as long
as no flow/switch ops are available as well. To improve this new deployment procedure is proposed.
It's details are descrivbed in this document.

## Solution

Provide ability to deploy kilda in so called blue/green mode. Which will allow to quickly switch
system between different release versions. However, due to event-driven nature of kilda and a bunch of
limitations from Apache Storm there are changes made in original Blue/Green approach.

### Shared Transport

Kilda uses Kafka as a Message broker and messages in it to communicate between components of the system.
To provide backward compatibility between green and new versions of kilda within the same kafka topic
we need to split messages by some value between new and old ones. For this purpose will be used kafka message
header with run-id encoded in it. Run id should be unique within a single deployment.
Each component will emit messages with run-id header that is valid by the deployment. All receiver parts
should validate message header first and verify that run id matches with its configuration in that case component
will handle the message. Kafka provide API to write custom interceptors for both consumer and producer. This
interceptor will be responsible for verifying deployment id.

### Zookeeper to store the state

Since storm has limitations on lifecycle of its topologies, new mechanism is required to deal with topologies states. 
Also it should be responsible to handle graceful shutdown procedure for topologies. For this role Apache Zookeeper
looks like a good fit.

#### Node structure for Zookeeper

`/kilda/{component_type}/{env_id}` - root for every component process, where:
`component_type` - topology or service name, e.g. `floodlight`, `network`, `nb_worker`
`env_id` - flag of blue or green env

#### Signal, States and Build-Version

Each component root node will have 3 children zNodes:
- signal - input field, can be `START` or `SHUTDOWN`, the way to make component start emittin processing new events
- state - int, number of active subcomponents of a component, when `SHUTDOWN` is emitted, should be `0`, positive otherwise.
- build-version - string field with run id, could be changed on fly, see #Shared transport for details
For the long running task such as hub in hab and spoke topologies, there should be a way to stop receiving new
requests, finishing up existing, and after that decrementing counter by 1 in a state field.

#### Basic Deployment Scenario:

Since floodlifghts could be destinguished by region let's assume, that all odd regions are blue and even are odd
Based on that deployment process should be look like:
- Set `SHUTDOWN` signal for odd floodlights and change it's build-version for the new one
- Redeploy floodlight containers of green color
- Ensure that all green components has a signal `SHUTDOWN` and state of each has 0 active workers.
- Deploy all new topologies for green
- Emit `START` for green floodlight
- Emit `START` for `router`, `flow_hs`, `reroute`, `switch_manager`, `nb_worker` of green
- Verify state is changed, and each has positive number value
- Emit `SHUTDOWN` for the network blue
- Emit `START` for the network green
- Emit `SHUTDOWN` for `router`, `flow_hs`, `reroute`, `switch_manager`, `nb_worker` of blue
- Set build-version to new one for the odd floodlight
- Redeploy odd floodlight 
- Emit `START` for the odd floodlights
