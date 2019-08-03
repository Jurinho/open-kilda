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

package org.openkilda.floodlight.command.flow;

import static org.openkilda.messaging.Utils.ETH_TYPE;
import static org.projectfloodlight.openflow.protocol.OFVersion.OF_15;

import org.openkilda.floodlight.command.MessageWriter;
import org.openkilda.floodlight.command.SessionProxy;
import org.openkilda.floodlight.command.SpeakerCommandV1;
import org.openkilda.floodlight.command.meter.InstallMeterCommand;
import org.openkilda.floodlight.error.SwitchOperationException;
import org.openkilda.floodlight.error.UnsupportedSwitchOperationException;
import org.openkilda.floodlight.service.FeatureDetectorService;
import org.openkilda.floodlight.switchmanager.SwitchManager;
import org.openkilda.messaging.MessageContext;
import org.openkilda.messaging.model.SpeakerSwitchView.Feature;
import org.openkilda.model.Cookie;
import org.openkilda.model.FlowEncapsulationType;
import org.openkilda.model.MeterId;
import org.openkilda.model.OutputVlanType;
import org.openkilda.model.SwitchId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
public class InstallIngressRuleCommand extends InstallTransitRuleCommand {

    private final Long bandwidth;
    private final Integer inputVlanId;
    private final OutputVlanType outputVlanType;
    private final MeterId meterId;

    @JsonCreator
    public InstallIngressRuleCommand(@JsonProperty("command_id") UUID commandId,
                                     @JsonProperty("flowid") String flowId,
                                     @JsonProperty("message_context") MessageContext messageContext,
                                     @JsonProperty("cookie") Cookie cookie,
                                     @JsonProperty("switch_id") SwitchId switchId,
                                     @JsonProperty("input_port") Integer inputPort,
                                     @JsonProperty("output_port") Integer outputPort,
                                     @JsonProperty("bandwidth") Long bandwidth,
                                     @JsonProperty("input_vlan_id") Integer inputVlanId,
                                     @JsonProperty("output_vlan_type") OutputVlanType outputVlanType,
                                     @JsonProperty("meter_id") MeterId meterId,
                                     @JsonProperty("transit_encapsulation_id") Integer transitEncapsulationId,
                                     @JsonProperty("transit_encapsulation_type")
                                             FlowEncapsulationType transitEncapsulationType) {
        super(commandId, flowId, messageContext, cookie, switchId, inputPort, outputPort,
                transitEncapsulationId, transitEncapsulationType);
        this.bandwidth = bandwidth;
        this.inputVlanId = inputVlanId;
        this.outputVlanType = outputVlanType;
        this.meterId = meterId;
    }

    @Override
    public List<SessionProxy> getCommands(IOFSwitch sw, FloodlightModuleContext moduleContext)
            throws SwitchOperationException {
        List<SessionProxy> commands = new ArrayList<>(2);
        FeatureDetectorService featureDetectorService = moduleContext.getServiceImpl(FeatureDetectorService.class);

        getMeterCommand(sw, moduleContext)
                .ifPresent(commands::add);
        OFFlowMod ruleCommand = getInstallRuleCommand(sw, featureDetectorService);
        commands.add(new MessageWriter(ruleCommand));
        return commands;
    }

    List<OFAction> getOutputAction(OFFactory ofFactory) {
        return inputVlanTypeToOfActionList(ofFactory);
    }

    OFPort getOutputPort() {
        return OFPort.of(outputPort);
    }

    final OFFlowMod getInstallRuleCommand(IOFSwitch sw, FeatureDetectorService featureDetectorService) {
        List<OFAction> actionList = new ArrayList<>();
        OFFactory ofFactory = sw.getOFFactory();
        Set<Feature> supportedFeatures = featureDetectorService.detectSwitch(sw);

        // build meter instruction
        OFInstructionMeter meter = getMeterInstructions(supportedFeatures, ofFactory, actionList);

        // output action based on encap scheme
        actionList.addAll(getOutputAction(ofFactory));

        // transmit packet from outgoing port
        actionList.add(setOutputPort(ofFactory));

        // build instruction with action list
        OFInstructionApplyActions actions = applyActions(ofFactory, actionList);

        // build match by input port and input vlan id
        Match match = matchFlow(inputPort, inputVlanId, ofFactory);

        // build FLOW_MOD command with meter
        OFFlowAdd.Builder builder = prepareFlowModBuilder(ofFactory)
                .setInstructions(meter != null ? ImmutableList.of(meter, actions) : ImmutableList.of(actions))
                .setMatch(match)
                .setPriority(inputVlanId == 0 ? SwitchManager.DEFAULT_FLOW_PRIORITY : FLOW_PRIORITY);

        if (supportedFeatures.contains(Feature.RESET_COUNTS_FLAG)) {
            builder.setFlags(ImmutableSet.of(OFFlowModFlags.RESET_COUNTS));
        }

        return builder.build();
    }

    private List<OFAction> inputVlanTypeToOfActionList(OFFactory ofFactory) {
        List<OFAction> actionList = new ArrayList<>(3);
        if (outputVlanType == OutputVlanType.PUSH || outputVlanType == OutputVlanType.NONE) {
            actionList.add(actionPushVlan(ofFactory, ETH_TYPE));
        }
        actionList.add(actionReplaceVlan(ofFactory, transitEncapsulationId));
        return actionList;
    }

    final OFAction actionPushVlan(OFFactory ofFactory, int etherType) {
        OFActions actions = ofFactory.actions();
        return actions.buildPushVlan().setEthertype(EthType.of(etherType)).build();
    }

    OFInstructionMeter getMeterInstructions(Set<Feature> supportedFeatures, OFFactory ofFactory,
                                            List<OFAction> actionList) {
        OFInstructionMeter meterInstruction = null;
        if (meterId != null && supportedFeatures.contains(Feature.METERS)) {
            if (ofFactory.getVersion().compareTo(OF_15) == 0) {
                actionList.add(ofFactory.actions().buildMeter().setMeterId(meterId.getValue()).build());
            } else /* OF_13, OF_14 */ {
                meterInstruction = ofFactory.instructions().buildMeter()
                        .setMeterId(meterId.getValue())
                        .build();
            }
        }

        return meterInstruction;
    }

    private Optional<SessionProxy> getMeterCommand(IOFSwitch sw, FloodlightModuleContext moduleContext)
            throws SwitchOperationException {
        if (meterId == null) {
            log.debug("Skip meter installation. No meter required for flow {}", flowId);
            return Optional.empty();
        }

        try {
            // FIXME
            throw new UnsupportedSwitchOperationException(sw.getId(), "dummy");
/*
            SpeakerCommandV1 meterCommand = new InstallMeterCommand(messageContext, switchId, meterId, bandwidth);
            return meterCommand.getCommands(sw, moduleContext).stream().findFirst();
*/
        } catch (UnsupportedSwitchOperationException e) {
            log.info("Skip meter {} installation for flow {} on switch {}: {}",
                    meterId, flowId, switchId.toString(), e.getMessage());
            return Optional.empty();
        }
    }
}
