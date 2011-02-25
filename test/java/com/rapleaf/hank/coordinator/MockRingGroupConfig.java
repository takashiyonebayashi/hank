package com.rapleaf.hank.coordinator;

import java.io.IOException;
import java.util.Set;

import com.rapleaf.hank.exception.DataNotFoundException;

public class MockRingGroupConfig implements RingGroupConfig {

  private final DomainGroupConfig dcg;
  private final String name;
  private final Set<RingConfig> ringConfigs;
  public int updateToVersion;

  public MockRingGroupConfig(DomainGroupConfig dcg, String name, Set<RingConfig> ringConfigs) {
    this.dcg = dcg;
    this.name = name;
    this.ringConfigs = ringConfigs;
    
  }

  @Override
  public DomainGroupConfig getDomainGroupConfig() {
    return dcg;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public RingConfig getRingConfig(int ringNumber) throws DataNotFoundException {
    return null;
  }

  @Override
  public RingConfig getRingConfigForHost(PartDaemonAddress hostAddress)
  throws DataNotFoundException {
    return null;
  }

  @Override
  public Set<RingConfig> getRingConfigs() {
    return ringConfigs;
  }

  @Override
  public boolean claimDataDeployer() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void releaseDataDeployer() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Integer getCurrentVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isUpdating() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setUpdatingToVersion(Integer versionNumber) {
    updateToVersion = versionNumber;
  }

  @Override
  public void updateComplete() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setListener(RingGroupChangeListener listener) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Integer getUpdatingToVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RingConfig addRing(int ringNum) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
}
