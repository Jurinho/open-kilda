/* Copyright 2019 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.wfm.topology.flowhs.fsm.reroute.actions;

import org.openkilda.floodlight.api.request.FlowSegmentRequest;
import org.openkilda.model.Flow;
import org.openkilda.model.FlowEncapsulationType;
import org.openkilda.model.FlowPath;
import org.openkilda.persistence.PersistenceManager;
import org.openkilda.wfm.share.flow.resources.FlowResourcesManager;
import org.openkilda.wfm.topology.flowhs.fsm.common.action.FlowProcessingAction;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteContext;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.Event;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm.State;
import org.openkilda.wfm.topology.flowhs.service.FlowCommandBuilder;
import org.openkilda.wfm.topology.flowhs.service.FlowCommandBuilderFactory;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class RevertNewRulesAction extends
        FlowProcessingAction<FlowRerouteFsm, State, Event, FlowRerouteContext> {

    private final FlowCommandBuilderFactory commandBuilderFactory;

    public RevertNewRulesAction(PersistenceManager persistenceManager, FlowResourcesManager resourcesManager) {
        super(persistenceManager);

        commandBuilderFactory = new FlowCommandBuilderFactory(resourcesManager);
    }

    @Override
    protected void perform(FlowRerouteFsm.State from, FlowRerouteFsm.State to,
                           FlowRerouteFsm.Event event, FlowRerouteContext context, FlowRerouteFsm stateMachine) {
        Flow flow = getFlow(stateMachine.getFlowId());

        FlowEncapsulationType encapsulationType = stateMachine.getNewEncapsulationType() != null
                ? stateMachine.getNewEncapsulationType() : flow.getEncapsulationType();
        FlowCommandBuilder commandBuilder = commandBuilderFactory.getBuilder(encapsulationType);

        Collection<FlowSegmentRequest> installRequests = new ArrayList<>();

        // Reinstall old ingress rules that may be overridden by new ingress.
        if (stateMachine.getOldPrimaryForwardPath() != null && stateMachine.getOldPrimaryReversePath() != null) {
            FlowPath oldForward = getFlowPath(flow, stateMachine.getOldPrimaryForwardPath());
            FlowPath oldReverse = getFlowPath(flow, stateMachine.getOldPrimaryReversePath());
            installRequests.addAll(commandBuilder.buildIngressOnly(
                    stateMachine.getCommandContext(), flow, oldForward, oldReverse));
        }

        stateMachine.setIngressCommands(installRequests.stream()
                .collect(Collectors.toMap(FlowSegmentRequest::getCommandId, Function.identity())));

        Collection<FlowSegmentRequest> removeRequests = new ArrayList<>();

        if (stateMachine.getNewPrimaryForwardPath() != null && stateMachine.getNewPrimaryReversePath() != null) {
            FlowPath newForward = getFlowPath(flow, stateMachine.getNewPrimaryForwardPath());
            FlowPath newReverse = getFlowPath(flow, stateMachine.getNewPrimaryReversePath());
            removeRequests.addAll(commandBuilder.buildRemoveAllExceptIngress(
                    stateMachine.getCommandContext(), flow, newForward, newReverse));
            removeRequests.addAll(commandBuilder.buildRemoveIngressOnly(
                    stateMachine.getCommandContext(), flow, newForward, newReverse));
        }
        if (stateMachine.getNewProtectedForwardPath() != null && stateMachine.getNewProtectedReversePath() != null) {
            FlowPath newForward = getFlowPath(flow, stateMachine.getNewProtectedForwardPath());
            FlowPath newReverse = getFlowPath(flow, stateMachine.getNewProtectedReversePath());
            removeRequests.addAll(commandBuilder.buildRemoveAllExceptIngress(
                    stateMachine.getCommandContext(), flow, newForward, newReverse));
            removeRequests.addAll(commandBuilder.buildRemoveIngressOnly(
                    stateMachine.getCommandContext(), flow, newForward, newReverse));
        }

        stateMachine.setRemoveCommands(removeRequests.stream()
                .collect(Collectors.toMap(FlowSegmentRequest::getCommandId, Function.identity())));

        Set<UUID> commandIds = Stream.concat(installRequests.stream(), removeRequests.stream())
                .peek(command -> stateMachine.getCarrier().sendSpeakerRequest(command))
                .map(FlowSegmentRequest::getCommandId)
                .collect(Collectors.toSet());
        stateMachine.setPendingCommands(commandIds);

        log.debug("Commands for removing rules have been sent for the flow {}", stateMachine.getFlowId());

        saveHistory(stateMachine, stateMachine.getCarrier(), stateMachine.getFlowId(),
                "Remove commands for new rules have been sent.");
    }
}
