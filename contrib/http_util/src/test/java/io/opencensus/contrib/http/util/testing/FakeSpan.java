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

package io.opencensus.contrib.http.util.testing;

import io.opencensus.common.Internal;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import java.util.EnumSet;
import java.util.Map;

/** A fake {@link Span} which allows user to set {@link SpanContext} upon initialization. */
@Internal
public class FakeSpan extends Span {
  public FakeSpan(SpanContext context, EnumSet<Options> options) {
    super(context, options);
  }

  @Override
  public void putAttribute(String key, AttributeValue value) {}

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {}

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}

  @Override
  public void addAnnotation(Annotation annotation) {}

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {}

  @Override
  public void addLink(Link link) {}

  @Override
  public void setStatus(Status status) {}

  @Override
  public void end(EndSpanOptions options) {}
}
