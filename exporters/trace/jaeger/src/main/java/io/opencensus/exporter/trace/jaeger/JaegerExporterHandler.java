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

package io.opencensus.exporter.trace.jaeger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thriftjava.Log;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import io.jaegertracing.thriftjava.SpanRef;
import io.jaegertracing.thriftjava.SpanRefType;
import io.jaegertracing.thriftjava.Tag;
import io.jaegertracing.thriftjava.TagType;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import io.opencensus.exporter.trace.util.TimeLimitedHandler;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.export.SpanData;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
final class JaegerExporterHandler extends TimeLimitedHandler {
  private static final String EXPORT_SPAN_NAME = "ExportJaegerTraces";
  @VisibleForTesting static final String SPAN_KIND = "span.kind";
  private static final Tag SERVER_KIND_TAG = new Tag(SPAN_KIND, TagType.STRING).setVStr("server");
  private static final Tag CLIENT_KIND_TAG = new Tag(SPAN_KIND, TagType.STRING).setVStr("client");
  private static final String DESCRIPTION = "message";
  private static final Tag RECEIVED_MESSAGE_EVENT_TAG =
      new Tag(DESCRIPTION, TagType.STRING).setVStr("received message");
  private static final Tag SENT_MESSAGE_EVENT_TAG =
      new Tag(DESCRIPTION, TagType.STRING).setVStr("sent message");
  private static final String MESSAGE_EVENT_ID = "id";
  private static final String MESSAGE_EVENT_COMPRESSED_SIZE = "compressed_size";
  private static final String MESSAGE_EVENT_UNCOMPRESSED_SIZE = "uncompressed_size";
  @VisibleForTesting static final String STATUS_CODE = "status.code";
  @VisibleForTesting static final String STATUS_MESSAGE = "status.message";

  private static final Function<? super String, Tag> stringAttributeConverter =
      new Function<String, Tag>() {
        @Override
        public Tag apply(final String value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.STRING);
          tag.setVStr(value);
          return tag;
        }
      };

  private static final Function<? super Boolean, Tag> booleanAttributeConverter =
      new Function<Boolean, Tag>() {
        @Override
        public Tag apply(final Boolean value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.BOOL);
          tag.setVBool(value);
          return tag;
        }
      };

  private static final Function<? super Double, Tag> doubleAttributeConverter =
      new Function<Double, Tag>() {
        @Override
        public Tag apply(final Double value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.DOUBLE);
          tag.setVDouble(value);
          return tag;
        }
      };

  private static final Function<? super Long, Tag> longAttributeConverter =
      new Function<Long, Tag>() {
        @Override
        public Tag apply(final Long value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.LONG);
          tag.setVLong(value);
          return tag;
        }
      };

  private static final Function<Object, Tag> defaultAttributeConverter =
      new Function<Object, Tag>() {
        @Override
        public Tag apply(final Object value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.STRING);
          tag.setVStr(value.toString());
          return tag;
        }
      };

  // Re-usable buffers to avoid too much memory allocation during conversions.
  // N.B.: these make instances of this class thread-unsafe, hence the above
  // @NotThreadSafe annotation.
  private final byte[] spanIdBuffer = new byte[SpanId.SIZE];
  private final byte[] traceIdBuffer = new byte[TraceId.SIZE];
  private final byte[] optionsBuffer = new byte[Integer.SIZE / Byte.SIZE];

  private final ThriftSender sender;
  private final Process process;

  JaegerExporterHandler(final ThriftSender sender, final Process process, Duration deadline) {
    super(deadline, EXPORT_SPAN_NAME);
    this.sender = checkNotNull(sender, "Jaeger sender must NOT be null.");
    this.process = checkNotNull(process, "Process sending traces must NOT be null.");
  }

  @Override
  public void timeLimitedExport(final Collection<SpanData> spanDataList) throws SenderException {
    final List<Span> spans = spanDataToJaegerThriftSpans(spanDataList);
    sender.send(process, spans);
  }

  private List<Span> spanDataToJaegerThriftSpans(final Collection<SpanData> spanDataList) {
    final List<Span> spans = Lists.newArrayListWithExpectedSize(spanDataList.size());
    for (final SpanData spanData : spanDataList) {
      spans.add(spanDataToJaegerThriftSpan(spanData));
    }
    return spans;
  }

  private Span spanDataToJaegerThriftSpan(final SpanData spanData) {
    final long startTimeInMicros = timestampToMicros(spanData.getStartTimestamp());
    final long endTimeInMicros = timestampToMicros(spanData.getEndTimestamp());

    final SpanContext context = spanData.getContext();
    copyToBuffer(context.getTraceId());

    List<Tag> tags =
        attributesToTags(
            spanData.getAttributes().getAttributeMap(), spanKindToTag(spanData.getKind()));
    addStatusTags(tags, spanData.getStatus());

    return new io.jaegertracing.thriftjava.Span(
            traceIdLow(),
            traceIdHigh(),
            spanIdToLong(context.getSpanId()),
            spanIdToLong(spanData.getParentSpanId()),
            spanData.getName(),
            optionsToFlags(context.getTraceOptions()),
            startTimeInMicros,
            endTimeInMicros - startTimeInMicros)
        .setReferences(linksToReferences(spanData.getLinks().getLinks()))
        .setTags(tags)
        .setLogs(
            timedEventsToLogs(
                spanData.getAnnotations().getEvents(), spanData.getMessageEvents().getEvents()));
  }

  private void copyToBuffer(final TraceId traceId) {
    // Attempt to minimise allocations, since TraceId#getBytes currently creates a defensive copy:
    traceId.copyBytesTo(traceIdBuffer, 0);
  }

  private long traceIdHigh() {
    return Longs.fromBytes(
        traceIdBuffer[0],
        traceIdBuffer[1],
        traceIdBuffer[2],
        traceIdBuffer[3],
        traceIdBuffer[4],
        traceIdBuffer[5],
        traceIdBuffer[6],
        traceIdBuffer[7]);
  }

  private long traceIdLow() {
    return Longs.fromBytes(
        traceIdBuffer[8],
        traceIdBuffer[9],
        traceIdBuffer[10],
        traceIdBuffer[11],
        traceIdBuffer[12],
        traceIdBuffer[13],
        traceIdBuffer[14],
        traceIdBuffer[15]);
  }

  private long spanIdToLong(final @Nullable SpanId spanId) {
    if (spanId == null) {
      return 0L;
    }
    // Attempt to minimise allocations, since SpanId#getBytes currently creates a defensive copy:
    spanId.copyBytesTo(spanIdBuffer, 0);
    return Longs.fromByteArray(spanIdBuffer);
  }

  private int optionsToFlags(final TraceOptions traceOptions) {
    // Attempt to minimise allocations, since TraceOptions#getBytes currently creates a defensive
    // copy:
    traceOptions.copyBytesTo(optionsBuffer, optionsBuffer.length - 1);
    return Ints.fromByteArray(optionsBuffer);
  }

  private List<SpanRef> linksToReferences(final List<Link> links) {
    final List<SpanRef> spanRefs = Lists.newArrayListWithExpectedSize(links.size());
    for (final Link link : links) {
      copyToBuffer(link.getTraceId());
      spanRefs.add(
          new SpanRef(
              linkTypeToRefType(link.getType()),
              traceIdLow(),
              traceIdHigh(),
              spanIdToLong(link.getSpanId())));
    }
    return spanRefs;
  }

  private static long timestampToMicros(final @Nullable Timestamp timestamp) {
    return (timestamp == null)
        ? 0L
        : SECONDS.toMicros(timestamp.getSeconds()) + NANOSECONDS.toMicros(timestamp.getNanos());
  }

  private static SpanRefType linkTypeToRefType(final Link.Type type) {
    switch (type) {
      case CHILD_LINKED_SPAN:
        return SpanRefType.CHILD_OF;
      case PARENT_LINKED_SPAN:
        return SpanRefType.FOLLOWS_FROM;
    }
    throw new UnsupportedOperationException(
        format("Failed to convert link type [%s] to a Jaeger SpanRefType.", type));
  }

  private static List<Tag> attributesToTags(
      final Map<String, AttributeValue> attributes, @Nullable final Tag extraTag) {
    final List<Tag> tags = Lists.newArrayListWithExpectedSize(attributes.size() + 1);
    for (final Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      final Tag tag =
          entry
              .getValue()
              .match(
                  stringAttributeConverter,
                  booleanAttributeConverter,
                  longAttributeConverter,
                  doubleAttributeConverter,
                  defaultAttributeConverter);
      tag.setKey(entry.getKey());
      tags.add(tag);
    }
    if (extraTag != null) {
      tags.add(extraTag);
    }
    return tags;
  }

  private static List<Log> timedEventsToLogs(
      final List<SpanData.TimedEvent<Annotation>> annotations,
      final List<SpanData.TimedEvent<MessageEvent>> messageEvents) {
    final List<Log> logs =
        Lists.newArrayListWithExpectedSize(annotations.size() + messageEvents.size());
    for (final SpanData.TimedEvent<Annotation> event : annotations) {
      final long timestampsInMicros = timestampToMicros(event.getTimestamp());
      logs.add(
          new Log(
              timestampsInMicros,
              attributesToTags(
                  event.getEvent().getAttributes(),
                  descriptionToTag(event.getEvent().getDescription()))));
    }
    for (final SpanData.TimedEvent<MessageEvent> event : messageEvents) {
      final long timestampsInMicros = timestampToMicros(event.getTimestamp());
      final Tag tagMessageId =
          new Tag(MESSAGE_EVENT_ID, TagType.LONG).setVLong(event.getEvent().getMessageId());
      final Tag tagCompressedSize =
          new Tag(MESSAGE_EVENT_COMPRESSED_SIZE, TagType.LONG)
              .setVLong(event.getEvent().getCompressedMessageSize());
      final Tag tagUncompressedSize =
          new Tag(MESSAGE_EVENT_UNCOMPRESSED_SIZE, TagType.LONG)
              .setVLong(event.getEvent().getUncompressedMessageSize());
      logs.add(
          new Log(
              timestampsInMicros,
              Arrays.asList(
                  event.getEvent().getType() == Type.RECEIVED
                      ? RECEIVED_MESSAGE_EVENT_TAG
                      : SENT_MESSAGE_EVENT_TAG,
                  tagMessageId,
                  tagCompressedSize,
                  tagUncompressedSize)));
    }
    return logs;
  }

  private static Tag descriptionToTag(final String description) {
    final Tag tag = new Tag(DESCRIPTION, TagType.STRING);
    tag.setVStr(description);
    return tag;
  }

  @Nullable
  private static Tag spanKindToTag(@Nullable final io.opencensus.trace.Span.Kind kind) {
    if (kind == null) {
      return null;
    }

    switch (kind) {
      case CLIENT:
        return CLIENT_KIND_TAG;
      case SERVER:
        return SERVER_KIND_TAG;
    }
    return null;
  }

  private static void addStatusTags(List<Tag> tags, @Nullable Status status) {
    if (status == null) {
      return;
    }
    Tag statusTag = new Tag(STATUS_CODE, TagType.LONG).setVLong(status.getCanonicalCode().value());
    tags.add(statusTag);
    if (status.getDescription() != null) {
      tags.add(new Tag(STATUS_MESSAGE, TagType.STRING).setVStr(status.getDescription()));
    }
  }
}
