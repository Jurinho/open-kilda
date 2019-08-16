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

package org.openkilda.floodlight.api.request;

import static java.util.Objects.requireNonNull;

import org.openkilda.floodlight.api.FlowEndpoint;
import org.openkilda.floodlight.api.MeterConfig;
import org.openkilda.messaging.MessageContext;
import org.openkilda.model.Cookie;
import org.openkilda.model.SwitchId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
abstract class SingleSwitchFlowBlankRequest extends AbstractIngressFlowSegmentRequest {
    @JsonProperty("egress_endpoint")
    protected final FlowEndpoint egressEndpoint;

    SingleSwitchFlowBlankRequest(
            MessageContext context, SwitchId switchId, UUID commandId, String flowId, Cookie cookie,
            FlowEndpoint endpoint, MeterConfig meterConfig, FlowEndpoint egressEndpoint) {
        super(context, switchId, commandId, flowId, cookie, endpoint, meterConfig);

        requireNonNull(egressEndpoint, "Argument egressEndpoint must no be null");

        this.egressEndpoint = egressEndpoint;
    }
}