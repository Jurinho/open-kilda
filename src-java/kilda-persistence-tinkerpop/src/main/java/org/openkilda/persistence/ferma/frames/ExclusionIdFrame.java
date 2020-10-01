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

package org.openkilda.persistence.ferma.frames;

import org.openkilda.model.ExclusionId.ExclusionIdData;

import com.syncleus.ferma.annotations.Property;

public abstract class ExclusionIdFrame extends KildaBaseVertexFrame implements ExclusionIdData {
    public static final String FRAME_LABEL = "exclusion_id";
    public static final String FLOW_ID_PROPERTY = "flow_id";
    public static final String EXCLUSION_ID_PROPERTY = "id";

    @Override
    @Property(FLOW_ID_PROPERTY)
    public abstract String getFlowId();

    @Override
    @Property(FLOW_ID_PROPERTY)
    public abstract void setFlowId(String flowId);

    @Override
    @Property(EXCLUSION_ID_PROPERTY)
    public abstract int getRecordId();

    @Override
    @Property(EXCLUSION_ID_PROPERTY)
    public abstract void setRecordId(int recordId);
}
