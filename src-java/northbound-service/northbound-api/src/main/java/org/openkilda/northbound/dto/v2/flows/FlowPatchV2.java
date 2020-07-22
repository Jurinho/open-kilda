/* Copyright 2020 Telstra Open Source
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

package org.openkilda.northbound.dto.v2.flows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlowPatchV2 {

    @JsonProperty("source")
    private FlowPatchEndpoint source;

    @JsonProperty("destination")
    private FlowPatchEndpoint destination;

    @JsonProperty("max_latency")
    private Long maxLatency;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("periodic_pings")
    private Boolean periodicPings;

    @JsonProperty("target_path_computation_strategy")
    private String targetPathComputationStrategy;

    @JsonProperty("diverse_flow_id")
    private String diverseFlowId;

    @JsonProperty("maximum_bandwidth")
    private Long maximumBandwidth;

    @JsonProperty("allocate_protected_path")
    private Boolean allocateProtectedPath;

    @JsonProperty("pinned")
    private Boolean pinned;

    @JsonProperty("ignore_bandwidth")
    private Boolean ignoreBandwidth;

    @JsonProperty("description")
    private String description;

    @JsonProperty("encapsulation_type")
    private String encapsulationType;

    @JsonProperty("path_computation_strategy")
    private String pathComputationStrategy;

    @JsonCreator
    public FlowPatchV2(@JsonProperty("source") FlowPatchEndpoint source,
                       @JsonProperty("destination") FlowPatchEndpoint destination,
                       @JsonProperty("max_latency") Long maxLatency,
                       @JsonProperty("priority") Integer priority,
                       @JsonProperty("periodic_pings") Boolean periodicPings,
                       @JsonProperty("target_path_computation_strategy") String targetPathComputationStrategy,
                       @JsonProperty("diverse_flow_id") String diverseFlowId,
                       @JsonProperty("maximum_bandwidth") Long maximumBandwidth,
                       @JsonProperty("allocate_protected_path") Boolean allocateProtectedPath,
                       @JsonProperty("pinned") Boolean pinned,
                       @JsonProperty("ignore_bandwidth") Boolean ignoreBandwidth,
                       @JsonProperty("description") String description,
                       @JsonProperty("encapsulation_type") String encapsulationType,
                       @JsonProperty("path_computation_strategy") String pathComputationStrategy) {
        this.maxLatency = maxLatency;
        this.priority = priority;
        this.periodicPings = periodicPings;
        this.targetPathComputationStrategy = targetPathComputationStrategy;
        this.source = source;
        this.destination = destination;
        this.diverseFlowId = diverseFlowId;
        this.maximumBandwidth = maximumBandwidth;
        this.allocateProtectedPath = allocateProtectedPath;
        this.pinned = pinned;
        this.ignoreBandwidth = ignoreBandwidth;
        this.description = description;
        this.encapsulationType = encapsulationType;
        this.pathComputationStrategy = pathComputationStrategy;
    }
}
