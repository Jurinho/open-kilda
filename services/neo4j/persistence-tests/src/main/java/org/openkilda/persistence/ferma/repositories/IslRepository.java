/* Copyright 2018 Telstra Open Source
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

package org.openkilda.persistence.ferma.repositories;

import org.openkilda.model.SwitchId;
import org.openkilda.persistence.ferma.model.Isl;

import java.util.Collection;
import java.util.Optional;

public interface IslRepository {
    Collection<Isl> findByEndpoint(SwitchId switchId, int port);

    Collection<Isl> findBySrcEndpoint(SwitchId srcSwitchId, int srcPort);

    Collection<Isl> findByDestEndpoint(SwitchId dstSwitchId, int dstPort);

    Collection<Isl> findBySrcSwitch(SwitchId switchId);

    Collection<Isl> findByDestSwitch(SwitchId switchId);

    Optional<Isl> findByEndpoints(SwitchId srcSwitchId, int srcPort, SwitchId dstSwitchId, int dstPort);

    Isl create(Isl entity);

    void delete(Isl entity);
}
