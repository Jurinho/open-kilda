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

package org.openkilda.messaging.command.flow;

import org.openkilda.messaging.command.CommandData;
import org.openkilda.messaging.model.DetectConnectedDevicesDto;
import org.openkilda.messaging.payload.flow.FlowEncapsulationType;
import org.openkilda.model.FlowEndpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@JsonNaming(value = SnakeCaseStrategy.class)
@Value
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
public class FlowRequest extends CommandData {

    @NonNull
    String flowId;

    @JsonProperty("source")
    FlowEndpoint source;

    @JsonProperty("destination")
    FlowEndpoint destination;

    long bandwidth;
    boolean ignoreBandwidth;
    boolean periodicPings;
    boolean allocateProtectedPath;
    String description;
    int transitEncapsulationId;
    Long maxLatency;
    Integer priority;
    boolean pinned;
    String diverseFlowId;
    FlowEncapsulationType encapsulationType;
    String pathComputationStrategy;
    Type type;

    @NonNull
    @Builder.Default
    DetectConnectedDevicesDto detectConnectedDevices = new DetectConnectedDevicesDto();

    public enum Type {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }
}
