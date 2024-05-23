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

/**
 * API for resource information population.
 *
 * <p>The resource library primarily defines a type "Resource" that captures information about the
 * entity for which stats or traces are recorded. For example, metrics exposed by a Kubernetes
 * container can be linked to a resource that specifies the cluster, namespace, pod, and container
 * name.
 *
 * <p>Two environment variables are used to populate resource information:
 *
 * <ul>
 *   <li>OC_RESOURCE_TYPE: A string that describes the type of the resource prefixed by a domain
 *       namespace. Leading and trailing whitespaces are trimmed. e.g. “kubernetes.io/container”.
 *   <li>OC_RESOURCE_LABELS: A comma-separated list of labels describing the source in more detail,
 *       e.g. “key1=val1,key2=val2”. The allowed character set is appropriately constrained.
 * </ul>
 *
 * <p>Type, label keys, and label values MUST contain only printable ASCII (codes between 32 and
 * 126, inclusive) and less than 256 characters. Type and label keys MUST have a length greater than
 * zero. They SHOULD start with a domain name and separate hierarchies with / characters, e.g.
 * k8s.io/namespace/name.
 *
 * <p>WARNING: Currently all the public classes under this package are marked as {@link
 * io.opencensus.common.ExperimentalApi}. DO NOT USE except for experimental purposes.
 *
 * <p>Please see
 * https://github.com/census-instrumentation/opencensus-specs/blob/master/resource/Resource.md for
 * more details.
 */
@io.opencensus.common.ExperimentalApi
package io.opencensus.resource;
