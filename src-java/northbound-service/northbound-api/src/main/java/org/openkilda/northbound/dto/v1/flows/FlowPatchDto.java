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

package org.openkilda.northbound.dto.v1.flows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlowPatchDto {

    @JsonProperty("max-latency")
    private Long maxLatency;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("periodic_pings")
    private Boolean periodicPings;

    @JsonProperty("target_path_computation_strategy")
    private String targetPathComputationStrategy;

    @JsonCreator
    public FlowPatchDto(@JsonProperty("max-latency") Long maxLatency,
                        @JsonProperty("priority") Integer priority,
                        @JsonProperty("periodic_pings") Boolean periodicPings,
                        @JsonProperty("target_path_computation_strategy") String targetPathComputationStrategy) {
        this.maxLatency = maxLatency;
        this.priority = priority;
        this.periodicPings = periodicPings;
        this.targetPathComputationStrategy = targetPathComputationStrategy;
    }
}
