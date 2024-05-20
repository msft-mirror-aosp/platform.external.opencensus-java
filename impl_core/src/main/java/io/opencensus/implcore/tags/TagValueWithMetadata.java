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

package io.opencensus.implcore.tags;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Internal;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagValue;
import javax.annotation.concurrent.Immutable;

/** Internal helper class that holds a TagValue and a TagMetadata. */
@Immutable
@AutoValue
@Internal
public abstract class TagValueWithMetadata {

  TagValueWithMetadata() {}

  /**
   * Creates a {@link TagValueWithMetadata}.
   *
   * @param tagValue the tag value.
   * @param tagMetadata metadata for the tag.
   * @return a {@code TagValueWithMetadata}.
   */
  public static TagValueWithMetadata create(TagValue tagValue, TagMetadata tagMetadata) {
    return new AutoValue_TagValueWithMetadata(tagValue, tagMetadata);
  }

  /**
   * Returns the {@code TagValue}.
   *
   * @return the {@code TagValue}.
   */
  public abstract TagValue getTagValue();

  abstract TagMetadata getTagMetadata();
}
