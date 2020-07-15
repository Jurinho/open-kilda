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

package org.openkilda.floodlight.kafka.discovery;

import java.time.Instant;

public class DiscoveryEmitterImmediateAction extends DiscoveryEmitterAction {
    DiscoveryEmitterImmediateAction(Instant expireTime, DiscoveryHolder discovery) {
        super(expireTime, discovery);
    }

    @Override
    public void perform(NetworkDiscoveryEmitter emitter) {
        emitter.emit(getDiscovery());
    }

    @Override
    public void flush(NetworkDiscoveryEmitter emitter) {
        // nothing to do here - action was immediately performed
    }

    @Override
    protected void suppress(NetworkDiscoveryEmitter emitter) {
        // nothing to do here - action was performed
    }
}
