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

package io.opencensus.contrib.http;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.trace.Span;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class provides storage per request context on http client and server.
 *
 * @since 0.19
 */
@ExperimentalApi
public class HttpRequestContext {
  @VisibleForTesting static final long INVALID_STARTTIME = -1;

  static final TagMetadata METADATA_NO_PROPAGATION = TagMetadata.create(TagTtl.NO_PROPAGATION);

  @VisibleForTesting final long requestStartTime;
  @VisibleForTesting final Span span;
  @VisibleForTesting AtomicLong sentMessageSize = new AtomicLong();
  @VisibleForTesting AtomicLong receiveMessageSize = new AtomicLong();
  @VisibleForTesting AtomicLong sentSeqId = new AtomicLong();
  @VisibleForTesting AtomicLong receviedSeqId = new AtomicLong();
  @VisibleForTesting final TagContext tagContext;

  HttpRequestContext(Span span, TagContext tagContext) {
    checkNotNull(span, "span");
    checkNotNull(tagContext, "tagContext");
    this.span = span;
    this.tagContext = tagContext;
    requestStartTime = System.nanoTime();
  }
}
