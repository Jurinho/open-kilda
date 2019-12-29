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

package org.openkilda.persistence.converters;

import org.openkilda.model.SharedOfFlowCookie;

import org.neo4j.ogm.typeconversion.AttributeConverter;

public class SharedOfFlowCookieConverter implements AttributeConverter<SharedOfFlowCookie, Long> {
    @Override
    public Long toGraphProperty(SharedOfFlowCookie value) {
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    @Override
    public SharedOfFlowCookie toEntityAttribute(Long value) {
        if (value == null) {
            return null;
        }
        return SharedOfFlowCookie.unpackTrusted(value);
    }
}
