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

package org.openkilda.floodlight.command.flow.ingress.of;

import org.openkilda.floodlight.command.flow.ingress.OneSwitchFlowCommand;
import org.openkilda.floodlight.utils.OfFlowModBuilderFactory;
import org.openkilda.model.SwitchFeature;

import net.floodlightcontroller.core.IOFSwitch;

import java.util.Set;

public class OneSwitchFlowInstallSingleTableFlowModFactory extends OneSwitchFlowInstallFlowModFactory {
    public OneSwitchFlowInstallSingleTableFlowModFactory(
            OneSwitchFlowCommand command, IOFSwitch sw, Set<SwitchFeature> features) {
        super(OfFlowModBuilderFactory.makeFactory().multiTable(false), command, sw, features);
    }
}
