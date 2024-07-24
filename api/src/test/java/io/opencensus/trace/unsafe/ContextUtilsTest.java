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

package io.opencensus.trace.unsafe;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.ContextHandle;
import io.opencensus.trace.Span;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextUtils}. */
@RunWith(JUnit4.class)
public class ContextUtilsTest {

  @Test
  public void testGetCurrentSpan_DefaultContext() {
    Span span = ContextHandleUtils.getValue(ContextHandleUtils.currentContext());
    assertThat(span).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test
  public void testGetCurrentSpan_ContextSetToNull() {
    ContextHandle orig =
        ContextHandleUtils.withValue(ContextHandleUtils.currentContext(), null).attach();
    try {
      Span span = ContextHandleUtils.getValue(ContextHandleUtils.currentContext());
      // ContextUtils.getValue always returns non-null.
      assertThat(span).isEqualTo(BlankSpan.INSTANCE);
    } finally {
      ContextHandleUtils.currentContext().detach(orig);
    }
  }

  @Test
  public void testTryExtractGrpcContext_WillNotThrow() {
    assertThat(ContextHandleUtils.tryExtractGrpcContext(ContextHandleUtils.currentContext()))
        .isNotNull();
  }
}
