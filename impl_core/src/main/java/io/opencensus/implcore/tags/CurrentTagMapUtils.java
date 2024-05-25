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

package io.opencensus.implcore.tags;

import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.unsafe.ContextUtils;

/**
 * Utility methods for accessing the {@link TagContext} contained in the {@link io.grpc.Context}.
 */
final class CurrentTagMapUtils {

  private CurrentTagMapUtils() {}

  /**
   * Returns the {@link TagContext} from the current context.
   *
   * @return the {@code TagContext} from the current context.
   */
  static TagContext getCurrentTagMap() {
    return ContextUtils.getValue(Context.current());
  }

  /**
   * Enters the scope of code where the given {@link TagContext} is in the current context and
   * returns an object that represents that scope. The scope is exited when the returned object is
   * closed.
   *
   * @param tags the {@code TagContext} to be set to the current context.
   * @return an object that defines a scope where the given {@code TagContext} is set to the current
   *     context.
   */
  static Scope withTagMap(TagContext tags) {
    return new WithTagMap(tags);
  }

  private static final class WithTagMap implements Scope {

    private final Context orig;

    /**
     * Constructs a new {@link WithTagMap}.
     *
     * @param tags the {@code TagContext} to be added to the current {@code Context}.
     */
    private WithTagMap(TagContext tags) {
      orig = ContextUtils.withValue(Context.current(), tags).attach();
    }

    @Override
    public void close() {
      Context.current().detach(orig);
    }
  }
}
