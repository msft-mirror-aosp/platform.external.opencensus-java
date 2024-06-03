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

package io.opencensus.implcore.tags.propagation;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.implcore.tags.TagsTestUtil;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextDeserializationException;
import io.opencensus.tags.propagation.TagContextSerializationException;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link TagContextBinarySerializerImpl}.
 *
 * <p>Thorough serialization/deserialization tests are in {@link TagContextSerializationTest},
 * {@link TagContextDeserializationTest}, and {@link TagContextRoundtripTest}.
 */
@RunWith(JUnit4.class)
public final class TagContextBinarySerializerImplTest {
  private final TagsComponent tagsComponent = new TagsComponentImplBase();
  private final TagContextBinarySerializer serializer =
      tagsComponent.getTagPropagationComponent().getBinarySerializer();

  private static final TagMetadata METADATA_NO_PROPAGATION =
      TagMetadata.create(TagTtl.NO_PROPAGATION);

  private final TagContext tagContext =
      new TagContext() {
        @Override
        public Iterator<Tag> getIterator() {
          return ImmutableSet.<Tag>of(Tag.create(TagKey.create("key"), TagValue.create("value")))
              .iterator();
        }
      };

  private final TagContext tagContextWithNonPropagatingTag =
      new TagContext() {
        @Override
        public Iterator<Tag> getIterator() {
          return ImmutableSet.<Tag>of(
                  Tag.create(
                      TagKey.create("key"), TagValue.create("value"), METADATA_NO_PROPAGATION))
              .iterator();
        }
      };

  @Test
  @SuppressWarnings("deprecation")
  public void toByteArray_TaggingDisabled() throws TagContextSerializationException {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(serializer.toByteArray(tagContext)).isEmpty();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void toByteArray_TaggingReenabled() throws TagContextSerializationException {
    final byte[] serialized = serializer.toByteArray(tagContext);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(serializer.toByteArray(tagContext)).isEmpty();
    tagsComponent.setState(TaggingState.ENABLED);
    assertThat(serializer.toByteArray(tagContext)).isEqualTo(serialized);
  }

  @Test
  public void toByteArray_SkipNonPropagatingTag() throws TagContextSerializationException {
    byte[] versionIdBytes = new byte[] {BinarySerializationUtils.VERSION_ID};
    assertThat(serializer.toByteArray(tagContextWithNonPropagatingTag)).isEqualTo(versionIdBytes);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void fromByteArray_TaggingDisabled()
      throws TagContextDeserializationException, TagContextSerializationException {
    byte[] serialized = serializer.toByteArray(tagContext);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(TagsTestUtil.tagContextToList(serializer.fromByteArray(serialized))).isEmpty();
  }

  @Test
  public void fromByteArray_TaggingReenabled()
      throws TagContextDeserializationException, TagContextSerializationException {
    byte[] serialized = serializer.toByteArray(tagContext);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(TagsTestUtil.tagContextToList(serializer.fromByteArray(serialized))).isEmpty();
    tagsComponent.setState(TaggingState.ENABLED);
    assertThat(serializer.fromByteArray(serialized)).isEqualTo(tagContext);
  }
}
