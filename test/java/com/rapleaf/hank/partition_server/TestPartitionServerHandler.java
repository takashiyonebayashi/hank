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
package com.rapleaf.hank.partition_server;

import com.rapleaf.hank.BaseTestCase;
import com.rapleaf.hank.config.PartitionServerConfigurator;
import com.rapleaf.hank.coordinator.*;
import com.rapleaf.hank.coordinator.mock.MockCoordinator;
import com.rapleaf.hank.coordinator.mock.MockDomain;
import com.rapleaf.hank.generated.HankExceptions;
import com.rapleaf.hank.generated.HankResponse;
import com.rapleaf.hank.partitioner.MapPartitioner;
import com.rapleaf.hank.partitioner.Partitioner;
import com.rapleaf.hank.storage.Reader;
import com.rapleaf.hank.storage.mock.MockReader;
import com.rapleaf.hank.storage.mock.MockStorageEngine;
import org.apache.thrift.TException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TestPartitionServerHandler extends BaseTestCase {
  private static final ByteBuffer K1 = bb(1);
  private static final ByteBuffer K2 = bb(2);
  private static final ByteBuffer K3 = bb(3);
  private static final ByteBuffer K4 = bb(4);
  private static final ByteBuffer K5 = bb(5);
  private static final byte[] V1 = new byte[]{9};
  private static final Host mockHostConfig = new MockHost(
      new PartitionServerAddress("localhost", 12345)) {

    @Override
    public HostDomain getDomainById(int domainId) {
      return new AbstractHostDomain() {
        @Override
        public HostDomainPartition addPartition(int partNum, int initialVersion) {
          return null;
        }

        @Override
        public int getDomainId() {
          return 0;
        }

        @Override
        public Set<HostDomainPartition> getPartitions() throws IOException {
          return new HashSet<HostDomainPartition>(Arrays.asList(
              new MockHostDomainPartition(0, 1, 2),
              new MockHostDomainPartition(4, 1, 2)));
        }

        @Override
        public Long getAggregateCount(String countID) throws IOException {
          return null;
        }

        @Override
        public Set<String> getAggregateCountKeys() throws IOException {
          return null;
        }
      };
    }
  };

  public void testSetUpAndServe() throws Exception {
    PartitionServerHandler handler = createHandler(1);

    assertEquals(HankResponse.value(V1), handler.get((byte) 0, K1));
    assertEquals(HankResponse.value(V1), handler.get((byte) 0, K5));

    assertEquals(HankResponse.xception(HankExceptions.wrong_host(true)),
        handler.get((byte) 0, K2));
    assertEquals(HankResponse.xception(HankExceptions.wrong_host(true)),
        handler.get((byte) 0, K3));
    assertEquals(HankResponse.xception(HankExceptions.wrong_host(true)),
        handler.get((byte) 0, K4));
  }

  public void testDontServeNotUpToDatePartition() throws IOException, TException {
    PartitionServerHandler handler = createHandler(0);

    HankResponse response = handler.get((byte) 0, K1);
    assertTrue(response.isSet(HankResponse._Fields.XCEPTION));
    assertTrue(response.get_xception().isSet(HankExceptions._Fields.INTERNAL_ERROR));
  }

  private PartitionServerHandler createHandler(final int readerVersionNumber) throws IOException {
    Partitioner partitioner = new MapPartitioner(K1, 0, K2, 1, K3, 2, K4, 3,
        K5, 4);
    MockStorageEngine storageEngine = new MockStorageEngine() {
      @Override
      public Reader getReader(PartitionServerConfigurator configurator, int partNum)
          throws IOException {
        return new MockReader(configurator, partNum, V1, readerVersionNumber);
      }
    };
    Domain domain = new MockDomain("myDomain", 5, partitioner, storageEngine, null,
        null);
    MockDomainGroupVersionDomainVersion dgvdv = new MockDomainGroupVersionDomainVersion(
        domain, 1);
    final MockDomainGroupVersion dgv = new MockDomainGroupVersion(
        Collections.singleton((DomainGroupVersionDomainVersion) dgvdv), null, 1);

    final MockDomainGroup dg = new MockDomainGroup("myDomainGroup") {
      @Override
      public DomainGroupVersion getLatestVersion() {
        return dgv;
      }

      @Override
      public Integer getDomainId(String domainName) {
        assertEquals("myDomain", domainName);
        return 0;
      }

      @Override
      public DomainGroupVersion getVersionByNumber(int versionNumber) {
        assertEquals(1, versionNumber);
        return dgv;
      }
    };
    final MockRingGroup rg = new MockRingGroup(dg, "myRingGroupName", null);

    final MockRing mockRingConfig = new MockRing(null, rg, 1, RingState.UP) {
      @Override
      public Host getHostByAddress(PartitionServerAddress address) {
        return mockHostConfig;
      }
    };

    Coordinator mockCoordinator = new MockCoordinator() {
      @Override
      public RingGroup getRingGroup(String ringGroupName) {
        assertEquals("myRingGroupName", ringGroupName);
        return new MockRingGroup(dg, "myRingGroupName", null) {
          @Override
          public Ring getRingForHost(PartitionServerAddress hostAddress) {
            return mockRingConfig;
          }
        };
      }
    };
    PartitionServerConfigurator config = new MockPartitionServerConfigurator(12345,
        mockCoordinator, "myRingGroupName", "/tmp/local/data/dir");
    PartitionServerHandler handler = new PartitionServerHandler(new PartitionServerAddress(
        "localhost", 12345), config);
    return handler;
  }

  private static ByteBuffer bb(int i) {
    return ByteBuffer.wrap(new byte[]{(byte) i});
  }
}
