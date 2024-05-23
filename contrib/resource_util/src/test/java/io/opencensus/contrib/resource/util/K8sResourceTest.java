/*
 * Copyright 2019, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.contrib.resource.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link K8sResource}. */
@RunWith(JUnit4.class)
public class K8sResourceTest {
  private static final String K8S_CLUSTER_NAME = "cluster";
  private static final String K8S_NAMESPACE_NAME = "namespace";
  private static final String K8S_POD_NAME = "deployment-replica-pod";
  private static final String K8S_DEPLOYMENT_NAME = "deployment";

  @Test
  public void create_K8sContainerResourceTest_Deprecated() {
    Resource resource = K8sResource.create(K8S_CLUSTER_NAME, K8S_NAMESPACE_NAME, K8S_POD_NAME);
    assertThat(resource.getType()).isEqualTo(K8sResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            K8sResource.CLUSTER_NAME_KEY,
            K8S_CLUSTER_NAME,
            K8sResource.NAMESPACE_NAME_KEY,
            K8S_NAMESPACE_NAME,
            K8sResource.POD_NAME_KEY,
            K8S_POD_NAME,
            K8sResource.DEPLOYMENT_NAME_KEY,
            "");
  }

  @Test
  public void create_K8sContainerResourceTest() {
    Resource resource =
        K8sResource.create(K8S_CLUSTER_NAME, K8S_NAMESPACE_NAME, K8S_POD_NAME, K8S_DEPLOYMENT_NAME);
    assertThat(resource.getType()).isEqualTo(K8sResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            K8sResource.CLUSTER_NAME_KEY,
            K8S_CLUSTER_NAME,
            K8sResource.NAMESPACE_NAME_KEY,
            K8S_NAMESPACE_NAME,
            K8sResource.POD_NAME_KEY,
            K8S_POD_NAME,
            K8sResource.DEPLOYMENT_NAME_KEY,
            K8S_DEPLOYMENT_NAME);
  }

  @Test
  public void getDeploymentNameFromPodName() {
    assertThat(K8sResource.getDeploymentNameFromPodName(K8S_POD_NAME))
        .isEqualTo(K8S_DEPLOYMENT_NAME);
    assertThat(K8sResource.getDeploymentNameFromPodName("")).isEqualTo("");
    assertThat(K8sResource.getDeploymentNameFromPodName("simple-name")).isEqualTo("");
    assertThat(K8sResource.getDeploymentNameFromPodName("deployment-name-replica-pod"))
        .isEqualTo("deployment-name");
  }
}
