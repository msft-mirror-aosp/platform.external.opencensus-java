/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.http.jetty.client;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.Tracing;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jetty.client.api.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OcJettyHttpClientTest}. */
@RunWith(JUnit4.class)
public class OcJettyHttpClientTest {
  private static final String URI_STR = "http://localhost/test/foo";
  private OcJettyHttpClient client;

  @Before
  public void setUp() {
    client = new OcJettyHttpClient();
  }

  @Test
  public void testOcJettyHttpClientDefault() {
    OcJettyHttpClient defaultClient = new OcJettyHttpClient();
    assertThat(defaultClient.handler).isNotNull();
  }

  @Test
  public void testOcJettyHttpClientNonDefault() {
    OcJettyHttpClient defaultClient =
        new OcJettyHttpClient(
            null,
            null,
            new OcJettyHttpClientExtractor(),
            Tracing.getPropagationComponent().getB3Format());
    assertThat(defaultClient.handler).isNotNull();
  }

  @Test
  public void testOcJettyHttpClientNullExtractor() {
    OcJettyHttpClient defaultClient =
        new OcJettyHttpClient(null, null, null, Tracing.getPropagationComponent().getB3Format());
    assertThat(defaultClient.handler).isNotNull();
  }

  @Test
  public void testOcJettyHttpClientNullPropagator() {
    OcJettyHttpClient defaultClient =
        new OcJettyHttpClient(null, null, new OcJettyHttpClientExtractor(), null);
    assertThat(defaultClient.handler).isNotNull();
  }

  @Test
  public void testListerWithUrlString() {
    Request request = client.newRequest(URI_STR);
    assertThat(request).isNotNull();
  }

  @Test
  public void testListerWithUri() throws URISyntaxException {
    URI uri = new URI(URI_STR);
    Request request = client.newRequest(uri);
    assertThat(request).isNotNull();
  }
}
