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
package com.rapleaf.hank.coordinator;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface Ring extends Comparable<Ring> {

  public RingGroup getRingGroup();

  public int getRingNumber();

  /**
   * Returns a set of all Part Daemon addresses.
   *
   * @return
   */
  public Set<Host> getHosts();

  public SortedSet<Host> getHostsSorted();

  public Host getHostByAddress(PartitionServerAddress address);

  public Host addHost(PartitionServerAddress address, List<String> hostFlags) throws IOException;

  /**
   * Remove a host from this ring. Returns true if the host was removed, false
   * if there was no such host.
   *
   * @param address
   * @return
   * @throws IOException
   */
  public boolean removeHost(PartitionServerAddress address) throws IOException;
}
