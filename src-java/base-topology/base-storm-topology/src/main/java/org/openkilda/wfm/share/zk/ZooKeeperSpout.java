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


package org.openkilda.wfm.share.zk;

import org.openkilda.bluegreen.BuildVersionObserver;
import org.openkilda.bluegreen.LifeCycleObserver;
import org.openkilda.bluegreen.LifecycleEvent;
import org.openkilda.bluegreen.Signal;
import org.openkilda.bluegreen.ZkWatchDog;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.CommandContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class ZooKeeperSpout extends BaseRichSpout implements LifeCycleObserver, BuildVersionObserver {
    public static final String BOLT_ID = "zookeeper.spout";
    public static final String FIELD_ID_LIFECYCLE_EVENT = "lifecycle.event";

    public static final String FIELD_ID_CONTEXT = AbstractBolt.FIELD_ID_CONTEXT;
    private String id;
    private String serviceName;
    private int apiVersion;
    private String connectionString;

    private ZkWatchDog watchDog;
    private SpoutOutputCollector collector;
    private LifecycleEvent event;
    private boolean newEvent = false;
    private long messageId = 0;


    public ZooKeeperSpout(String id, String serviceName, int apiVersion, String connectionString) {
        this.id = id;
        this.serviceName = serviceName;
        this.apiVersion = apiVersion;
        this.connectionString = connectionString;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        try {
            this.watchDog = ZkWatchDog.builder().id(id).serviceName(serviceName).apiVersion(apiVersion)
                    .connectionString(connectionString).build();
            watchDog.subscribe((LifeCycleObserver) this);
            watchDog.subscribe((BuildVersionObserver) this);
        } catch (IOException e) {
            log.error("Failed to init ZooKeeper with connection string: %s, received: %s ", connectionString,
                    e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void nextTuple() {
        if (event != null && newEvent) {
            collector.emit(new Values(event, new CommandContext()), messageId++);
            newEvent = false;
        } else {
            org.apache.storm.utils.Utils.sleep(1000L);
        }
    }

    @Override
    public void ack(Object msgId) {
        super.ack(msgId);
        event = null;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(FIELD_ID_LIFECYCLE_EVENT, FIELD_ID_CONTEXT));

    }

    @Override
    public void handle(Signal signal) {
        LifecycleEvent event = LifecycleEvent.builder()
                .signal(signal)
                .uuid(UUID.randomUUID())
                .messageId(messageId++).build();
        this.event = event;
        this.newEvent = true;
    }

    @Override
    public void handle(String buildVersion) {
        this.event = LifecycleEvent.builder()
                .buildVersion(buildVersion)
                .uuid(UUID.randomUUID())
                .messageId(messageId++).build();
        this.newEvent = true;
    }
}
