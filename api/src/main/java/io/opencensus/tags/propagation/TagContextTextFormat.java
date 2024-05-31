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

package io.opencensus.tags.propagation;

import io.opencensus.tags.TagContext;
import java.util.List;
import javax.annotation.Nullable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/**
 * Object for injecting and extracting {@link TagContext} as text into carriers that travel in-band
 * across process boundaries. Tags are often encoded as messaging or RPC request headers.
 *
 * <p>When using http, the carrier of propagated data on both the client (injector) and server
 * (extractor) side is usually an http request. Propagation is usually implemented via library-
 * specific request interceptors, where the client-side injects tags and the server-side extracts
 * them.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tagger tagger = Tags.getTagger();
 * private static final TagContextTextFormat textFormat =
 *     Tags.getPropagationComponent().getCorrelationContextFormat();
 * private static final TagContextTextFormat.Setter setter =
 *     new TagContextTextFormat.Setter<HttpURLConnection>() {
 *       public void put(HttpURLConnection carrier, String key, String value) {
 *         carrier.setRequestProperty(field, value);
 *       }
 *     };
 *
 * void makeHttpRequest() {
 *   TagContext tagContext = tagger.emptyBuilder().put(K, V).build();
 *   try (Scope s = tagger.withTagContext(tagContext)) {
 *     HttpURLConnection connection =
 *         (HttpURLConnection) new URL("http://myserver").openConnection();
 *     textFormat.inject(tagContext, connection, httpURLConnectionSetter);
 *     // Send the request, wait for response and maybe set the status if not ok.
 *   }
 * }
 * }</pre>
 *
 * <p>Example of usage on the server:
 *
 * <pre>{@code
 * private static final Tagger tagger = Tags.getTagger();
 * private static final TagContextTextFormat textFormat =
 *     Tags.getPropagationComponent().getCorrelationContextFormat();
 * private static final TagContextTextFormat.Getter<HttpRequest> getter = ...;
 *
 * void onRequestReceived(HttpRequest request) {
 *   TagContext tagContext = textFormat.extract(request, getter);
 *   try (Scope s = tagger.withTagContext(tagContext)) {
 *     // Handle request and send response back.
 *   }
 * }
 * }</pre>
 *
 * @since 0.21
 */
public abstract class TagContextTextFormat {

  /**
   * The propagation fields defined. If your carrier is reused, you should delete the fields here
   * before calling {@link #inject(TagContext, Object, Setter)}.
   *
   * <p>For example, if the carrier is a single-use or immutable request object, you don't need to
   * clear fields as they couldn't have been set before. If it is a mutable, retryable object,
   * successive calls should clear these fields first.
   *
   * @since 0.21
   */
  // The use cases of this are:
  // * allow pre-allocation of fields, especially in systems like gRPC Metadata
  // * allow a single-pass over an iterator (ex OpenTracing has no getter in TextMap)
  public abstract List<String> fields();

  /**
   * Injects the tag context downstream. For example, as http headers.
   *
   * @param tagContext the tag context.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param setter invoked for each propagation key to add or remove.
   * @throws TagContextSerializationException if the given tag context cannot be serialized.
   * @since 0.21
   */
  public abstract <C /*>>> extends @NonNull Object*/> void inject(
      TagContext tagContext, C carrier, Setter<C> setter) throws TagContextSerializationException;

  /**
   * Class that allows a {@code TagContextTextFormat} to set propagated fields into a carrier.
   *
   * <p>{@code Setter} is stateless and allows to be saved as a constant to avoid runtime
   * allocations.
   *
   * @param <C> carrier of propagation fields, such as an http request
   * @since 0.21
   */
  public abstract static class Setter<C> {

    /**
     * Replaces a propagated field with the given value.
     *
     * <p>For example, a setter for an {@link java.net.HttpURLConnection} would be the method
     * reference {@link java.net.HttpURLConnection#addRequestProperty(String, String)}
     *
     * @param carrier holds propagation fields. For example, an outgoing message or http request.
     * @param key the key of the field.
     * @param value the value of the field.
     * @since 0.21
     */
    public abstract void put(C carrier, String key, String value);
  }

  /**
   * Extracts the tag context from upstream. For example, as http headers.
   *
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param getter invoked for each propagation key to get.
   * @throws TagContextDeserializationException if the input is invalid
   * @since 0.21
   */
  public abstract <C /*>>> extends @NonNull Object*/> TagContext extract(
      C carrier, Getter<C> getter) throws TagContextDeserializationException;

  /**
   * Class that allows a {@code TagContextTextFormat} to read propagated fields from a carrier.
   *
   * <p>{@code Getter} is stateless and allows to be saved as a constant to avoid runtime
   * allocations.
   *
   * @param <C> carrier of propagation fields, such as an http request
   * @since 0.21
   */
  public abstract static class Getter<C> {

    /**
     * Returns the first value of the given propagation {@code key} or returns {@code null}.
     *
     * @param carrier carrier of propagation fields, such as an http request
     * @param key the key of the field.
     * @return the first value of the given propagation {@code key} or returns {@code null}.
     * @since 0.21
     */
    @Nullable
    public abstract String get(C carrier, String key);
  }
}
