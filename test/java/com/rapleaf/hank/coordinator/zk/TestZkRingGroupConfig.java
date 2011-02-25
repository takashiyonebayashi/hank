package com.rapleaf.hank.coordinator.zk;

import com.rapleaf.hank.ZkTestCase;
import com.rapleaf.hank.coordinator.MockDomainGroupConfig;
import com.rapleaf.hank.coordinator.PartDaemonAddress;
import com.rapleaf.hank.coordinator.RingConfig;

public class TestZkRingGroupConfig extends ZkTestCase {

  public TestZkRingGroupConfig() throws Exception {
    super();
  }

  private final String ring_groups = getRoot() + "/ring_groups";
  private final String ring_group = ring_groups + "/myRingGroup";
  private final String dg_root = getRoot() + "/domain_groups";

  public void testLoad() throws Exception {
    create(ring_groups);
    create(ring_group, dg_root + "/myDomainGroup");
    createRing(1);
    createRing(2);
    createRing(3);

    MockDomainGroupConfig dgc = new MockDomainGroupConfig("myDomainGroup");
    ZkRingGroupConfig ringGroupConf = new ZkRingGroupConfig(getZk(), ring_group, dgc);

    assertEquals("ring group name", "myRingGroup", ringGroupConf.getName());
    assertEquals("num rings", 3, ringGroupConf.getRingConfigs().size());
    assertEquals("domain group config", dgc, ringGroupConf.getDomainGroupConfig());

    assertEquals("ring group for localhost:2", 2, ringGroupConf.getRingConfigForHost(new PartDaemonAddress("localhost", 2)).getRingNumber());
    assertEquals("ring group by number", 3, ringGroupConf.getRingConfig(3).getRingNumber());
  }

  private void createRing(int ringNum) throws Exception {
    RingConfig rc = ZkRingConfig.create(getZk(), ring_group, ringNum, null, 1);
    rc.addHost(new PartDaemonAddress("localhost", ringNum));
  }
}