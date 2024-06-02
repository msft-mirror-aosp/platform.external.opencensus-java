/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.exporter.stats.stackdriver;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration.DEFAULT_DEADLINE;

import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import io.opencensus.common.Duration;
import java.io.IOException;
import java.util.Date;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverStatsExporter}. */
@RunWith(JUnit4.class)
public class StackdriverStatsExporterTest {

  private static final String PROJECT_ID = "projectId";
  private static final Duration ONE_MINUTE = Duration.create(60, 0);
  private static final Duration NEG_ONE_MINUTE = Duration.create(-60, 0);
  private static final Credentials FAKE_CREDENTIALS =
      GoogleCredentials.newBuilder().setAccessToken(new AccessToken("fake", new Date(100))).build();
  private static final StackdriverStatsConfiguration CONFIGURATION =
      StackdriverStatsConfiguration.builder()
          .setCredentials(FAKE_CREDENTIALS)
          .setProjectId("project")
          .build();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void createWithNullStackdriverStatsConfiguration() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("configuration");
    StackdriverStatsExporter.createAndRegister((StackdriverStatsConfiguration) null);
  }

  @Test
  public void createWithNegativeDuration_WithConfiguration() throws IOException {
    StackdriverStatsConfiguration configuration =
        StackdriverStatsConfiguration.builder()
            .setCredentials(FAKE_CREDENTIALS)
            .setProjectId(PROJECT_ID)
            .setExportInterval(NEG_ONE_MINUTE)
            .build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Export interval must be positive");
    StackdriverStatsExporter.createAndRegister(configuration);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void createWithNullCredentials() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("credentials");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        null, PROJECT_ID, ONE_MINUTE);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void createWithNullProjectId() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("projectId");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), null, ONE_MINUTE);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void createWithNullDuration() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("exportInterval");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, null);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void createWithNegativeDuration() throws IOException {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Export interval must be positive");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, NEG_ONE_MINUTE);
  }

  @Test
  public void createExporterTwice() throws IOException {
    StackdriverStatsExporter.createAndRegister(CONFIGURATION);
    try {
      thrown.expect(IllegalStateException.class);
      thrown.expectMessage("Stackdriver stats exporter is already created.");
      StackdriverStatsExporter.createAndRegister(CONFIGURATION);
    } finally {
      StackdriverStatsExporter.unregister();
    }
  }

  @Test
  public void unregister() throws IOException {
    // unregister has no effect if exporter is not yet registered.
    StackdriverStatsExporter.unregister();
    try {
      StackdriverStatsExporter.createAndRegister(CONFIGURATION);
      StackdriverStatsExporter.unregister();
      StackdriverStatsExporter.createAndRegister(CONFIGURATION);
    } finally {
      StackdriverStatsExporter.unregister();
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void createWithNullMonitoredResource() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("monitoredResource");
    StackdriverStatsExporter.createAndRegisterWithMonitoredResource(ONE_MINUTE, null);
  }

  @Test
  public void createMetricServiceClient() throws IOException {
    MetricServiceClient client;
    synchronized (StackdriverStatsExporter.monitor) {
      client =
          StackdriverStatsExporter.createMetricServiceClient(FAKE_CREDENTIALS, DEFAULT_DEADLINE);
    }
    assertThat(client.getSettings().getCredentialsProvider().getCredentials())
        .isEqualTo(FAKE_CREDENTIALS);
    assertThat(client.getSettings().getTransportChannelProvider())
        .isInstanceOf(InstantiatingGrpcChannelProvider.class);
    // There's no way to get HeaderProvider from TransportChannelProvider.
    assertThat(client.getSettings().getTransportChannelProvider().needsHeaders()).isFalse();
  }

  @Test
  public void createMetricServiceClient_WithoutCredentials() {
    try {
      MetricServiceClient client;
      synchronized (StackdriverStatsExporter.monitor) {
        client = StackdriverStatsExporter.createMetricServiceClient(null, DEFAULT_DEADLINE);
      }
      assertThat(client.getSettings().getCredentialsProvider())
          .isInstanceOf(GoogleCredentialsProvider.class);
      assertThat(client.getSettings().getTransportChannelProvider())
          .isInstanceOf(InstantiatingGrpcChannelProvider.class);
      // There's no way to get HeaderProvider from TransportChannelProvider.
      assertThat(client.getSettings().getTransportChannelProvider().needsHeaders()).isFalse();
    } catch (IOException e) {
      // This test depends on the Application Default Credentials settings (environment variable
      // GOOGLE_APPLICATION_CREDENTIALS). Some hosts may not have the expected environment settings
      // and this test should be skipped in that case.
    }
  }
}
