/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.server.namenode;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.hdfs.protocol.ErasureCodingPolicy;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;

import java.util.Map;
import java.util.TreeMap;

/**
 * This manages erasure coding policies predefined and activated in the system.
 * It loads customized policies and syncs with persisted ones in
 * NameNode image.
 *
 * This class is instantiated by the FSNamesystem.
 */
@InterfaceAudience.LimitedPrivate({"HDFS"})
public final class ErasureCodingPolicyManager {

  /**
   * TODO: HDFS-8095
   */
  private static final int DEFAULT_CELLSIZE = 64 * 1024;
  private static final ErasureCodingPolicy SYS_POLICY1 =
      new ErasureCodingPolicy(HdfsConstants.RS_6_3_SCHEMA, DEFAULT_CELLSIZE,
          HdfsConstants.RS_6_3_POLICY_ID);
  private static final ErasureCodingPolicy SYS_POLICY2 =
      new ErasureCodingPolicy(HdfsConstants.RS_3_2_SCHEMA, DEFAULT_CELLSIZE,
          HdfsConstants.RS_3_2_POLICY_ID);

  //We may add more later.
  private static final ErasureCodingPolicy[] SYS_POLICIES =
      new ErasureCodingPolicy[]{SYS_POLICY1, SYS_POLICY2};

  /**
   * All active policies maintained in NN memory for fast querying,
   * identified and sorted by its name.
   */
  private final Map<String, ErasureCodingPolicy> activePoliciesByName;

  ErasureCodingPolicyManager() {

    this.activePoliciesByName = new TreeMap<>();
    for (ErasureCodingPolicy policy : SYS_POLICIES) {
      activePoliciesByName.put(policy.getName(), policy);
    }

    /**
     * TODO: HDFS-7859 persist into NameNode
     * load persistent policies from image and editlog, which is done only once
     * during NameNode startup. This can be done here or in a separate method.
     */
  }

  /**
   * Get system defined policies.
   * @return system policies
   */
  public static ErasureCodingPolicy[] getSystemPolicies() {
    return SYS_POLICIES;
  }

  /**
   * Get system-wide default policy, which can be used by default
   * when no policy is specified for a path.
   * @return ecPolicy
   */
  public static ErasureCodingPolicy getSystemDefaultPolicy() {
    // make this configurable?
    return SYS_POLICY1;
  }

  /**
   * Get all policies that's available to use.
   * @return all policies
   */
  public ErasureCodingPolicy[] getPolicies() {
    ErasureCodingPolicy[] results =
        new ErasureCodingPolicy[activePoliciesByName.size()];
    return activePoliciesByName.values().toArray(results);
  }

  /**
   * Get the policy specified by the policy name.
   */
  public ErasureCodingPolicy getPolicyByName(String name) {
    return activePoliciesByName.get(name);
  }

  /**
   * Get the policy specified by the policy ID.
   */
  public ErasureCodingPolicy getPolicyByID(byte id) {
    for (ErasureCodingPolicy policy : activePoliciesByName.values()) {
      if (policy.getId() == id) {
        return policy;
      }
    }
    return null;
  }

  /**
   * Clear and clean up
   */
  public void clear() {
    activePoliciesByName.clear();
  }
}
