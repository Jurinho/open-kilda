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

package org.openkilda.server42.messaging;

//TODO(nmarchenko): remove that and use org.openkilda.messaging.model.FlowDirection instead
public enum FlowDirection {
    FORWARD,
    REVERSE;

    public static boolean toBoolean(FlowDirection d) {
        return d == REVERSE;
    }

    /**
     * Get direction from protobuf format to java.
     */
    public static FlowDirection fromBoolean(Boolean d) {
        if (d) {
            return REVERSE;
        }
        return FORWARD;
    }
}
