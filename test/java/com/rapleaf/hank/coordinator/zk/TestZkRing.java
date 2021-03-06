/**
 *  Copyright 2011 Rapleaf
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.rapleaf.hank.coordinator.zk;

import com.rapleaf.hank.ZkTestCase;
import com.rapleaf.hank.coordinator.*;
import com.rapleaf.hank.coordinator.mock.MockCoordinator;
import com.rapleaf.hank.zookeeper.ZkPath;

import java.util.Collections;

public class TestZkRing extends ZkTestCase {

  private static Coordinator coordinator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    create(ZkPath.append(getRoot(), "ring-group-one"));
    TestZkRing.coordinator = new MockCoordinator();
  }

  private static final PartitionServerAddress LOCALHOST = PartitionServerAddress.parse("localhost:1");

  private final String ring_group_root = ZkPath.append(getRoot(), "ring-group-one");
  private final String ring_root = ZkPath.append(getRoot(), "ring-group-one/ring-1");

  public void testCreate() throws Exception {
    ZkRing ring = ZkRing.create(getZk(), coordinator, ring_group_root, 1, null, null);

    assertEquals("ring number", 1, ring.getRingNumber());
    assertEquals("number of hosts", 0, ring.getHosts().size());
    ring.close();
  }

  public void testLoad() throws Exception {
    ZkRing ring = ZkRing.create(getZk(), coordinator, ring_group_root, 1, null, null);
    ring.close();

    ring = new ZkRing(getZk(), ZkPath.append(ring_group_root, "ring-1"), null, coordinator, null);

    assertEquals("ring number", 1, ring.getRingNumber());
    assertEquals("number of hosts", 0, ring.getHosts().size());
    ring.close();
  }

  public void testHosts() throws Exception {
    ZkRing ring = ZkRing.create(getZk(), coordinator, ring_group_root, 1, null, null);
    assertEquals(0, ring.getHosts().size());

    Host host = ring.addHost(LOCALHOST, Collections.<String>emptyList());
    assertEquals(LOCALHOST, host.getAddress());
    for (int i = 0; i < 20; i++) {
      if (!ring.getHosts().isEmpty()) {
        break;
      }
      Thread.sleep(100);
    }
    assertEquals(Collections.singleton(host), ring.getHosts());

    assertEquals(LOCALHOST, ring.getHostByAddress(LOCALHOST).getAddress());
    ring.close();

    // assure that hosts reload well, too
    ring = new ZkRing(getZk(), ring_root, null, coordinator, null);
    assertEquals(1, ring.getHosts().size());

    assertEquals(Collections.singleton(host), ring.getHosts());

    assertEquals(LOCALHOST, ring.getHostByAddress(LOCALHOST).getAddress());

    assertTrue(ring.removeHost(LOCALHOST));
    assertNull(ring.getHostByAddress(LOCALHOST));
    assertFalse(ring.removeHost(LOCALHOST));

    ring.close();
  }

  public void testListenersPreservedWhenHostAdded() throws Exception {
    ZkRing ring = ZkRing.create(getZk(), coordinator, ZkPath.append(getRoot(), "ring-group-one"), 1, null, null);
    Host h1 = ring.addHost(new PartitionServerAddress("localhost", 1), Collections.<String>emptyList());
    MockHostCommandQueueChangeListener l1 = new MockHostCommandQueueChangeListener();
    h1.setCommandQueueChangeListener(l1);
    MockHostStateChangeListener l2 = new MockHostStateChangeListener();
    h1.setStateChangeListener(l2);

    ring.addHost(new PartitionServerAddress("localhost", 2), Collections.<String>emptyList());

    h1.setState(HostState.UPDATING);
    synchronized (l2) {
      l2.wait(WAIT_TIME);
    }
    assertEquals(HostState.UPDATING, l2.calledWith);

    h1.enqueueCommand(HostCommand.EXECUTE_UPDATE);
    l1.waitForNotification();
    assertEquals(h1, l1.calledWith);
  }

  public void testDelete() throws Exception {
    ZkRing ring = ZkRing.create(getZk(), coordinator, ZkPath.append(getRoot(), "ring-group-one"), 1, null, null);
    ring.delete();
    assertTrue(getZk().exists(ZkPath.append(getRoot(), "ring-group-one/ring-1"), null) == null);
  }
}
