/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.mock.aem.context;

import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * Create resolve resolver instance and initialize it depending on it's type.
 */
final class AemResourceResolverFactory {

  private AemResourceResolverFactory() {
    // static methods only
  }

  public static ResourceResolver initializeResourceResolver(final ResourceResolverType resourceResolverType) {
    try {
      ResourceResolver resourceResolver = MockSling.newResourceResolver(resourceResolverType);

      switch (resourceResolverType) {
        case JCR_MOCK:
          initializeJcrMock(resourceResolver);
          break;
        case JCR_JACKRABBIT:
          initializeJcrJackrabbit(resourceResolver);
          break;
        case RESOURCERESOLVER_MOCK:
          initializeResourceResolverMock(resourceResolver);
          break;
        default:
          throw new IllegalArgumentException("Invalid resource resolver type: " + resourceResolverType);
      }

      return resourceResolver;
    }
    catch (Throwable ex) {
      throw new RuntimeException("Unable to initialize " + resourceResolverType + " resource resolver.", ex);
    }
  }

  private static void initializeJcrMock(final ResourceResolver resourceResolver) throws RepositoryException {
    // register default namespaces
    NamespaceRegistry namespaceRegistry = resourceResolver.adaptTo(Session.class).getWorkspace().getNamespaceRegistry();
    namespaceRegistry.registerNamespace("sling", "http://sling.apache.org/jcr/sling/1.0");
    namespaceRegistry.registerNamespace("cq", "http://www.day.com/jcr/cq/1.0 ");
    namespaceRegistry.registerNamespace("dam", "http://www.day.com/dam/1.0 ");
  }

  private static void initializeJcrJackrabbit(final ResourceResolver resourceResolver) {
    // TODO: register sling and AEM node types
  }

  private static void initializeResourceResolverMock(final ResourceResolver resourceResolver) {
    // nothing to do
  }

}
