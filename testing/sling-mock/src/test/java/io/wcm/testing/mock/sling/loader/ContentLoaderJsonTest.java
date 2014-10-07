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
package io.wcm.testing.mock.sling.loader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;

import java.util.Calendar;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

public class ContentLoaderJsonTest {

  private ResourceResolver resourceResolver;

  protected ResourceResolverType getResourceResolverType() {
    return ResourceResolverType.JCR_MOCK;
  }

  protected ResourceResolver newResourceResolver() {
    ResourceResolver resolver = MockSling.newResourceResolver(getResourceResolverType());

    if (getResourceResolverType() == ResourceResolverType.JCR_MOCK) {
      try {
        // dummy namespace registrations to make sure sling JCR resolver does not get mixed up with the prefixes
        NamespaceRegistry namespaceRegistry = resolver.adaptTo(Session.class).getWorkspace().getNamespaceRegistry();
        namespaceRegistry.registerNamespace("sling", "http://mock/sling");
        namespaceRegistry.registerNamespace("cq", "http://mock/cq");
        namespaceRegistry.registerNamespace("dam", "http://mock/dam");
      }
      catch (RepositoryException ex) {
        throw new RuntimeException("Unable to register namespaces.", ex);
      }
    }

    return resolver;
  }

  @Before
  public final void setUp() {
    this.resourceResolver = newResourceResolver();
    ContentLoader contentLoader = new ContentLoader(this.resourceResolver);
    contentLoader.json("/json-import-samples/content.json", "/content/sample/en");
  }

  @Test
  public void testPageResourceType() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en");
    assertEquals("cq:Page", resource.getResourceType());
  }

  @Test
  public void testPageJcrPrimaryType() throws RepositoryException {
    Resource resource = this.resourceResolver.getResource("/content/sample/en");
    assertPrimaryNodeType(resource, "cq:Page");
  }

  @Test
  public void testPageContentResourceType() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/toolbar/profiles/jcr:content");
    assertEquals("sample/components/contentpage", resource.getResourceType());
  }

  @Test
  public void testPageContentJcrPrimaryType() throws RepositoryException {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/toolbar/profiles/jcr:content");
    assertPrimaryNodeType(resource, "cq:PageContent");
  }

  @Test
  public void testPageContentProperties() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/toolbar/profiles/jcr:content");
    ValueMap props = resource.getValueMap();
    assertEquals(true, props.get("hideInNav", Boolean.class));

    Calendar calendar = props.get("cq:lastModified", Calendar.class);
    assertNotNull(calendar);
    assertEquals(2009, calendar.get(Calendar.YEAR));
    assertEquals(10, calendar.get(Calendar.MONTH));
    assertEquals(5, calendar.get(Calendar.DAY_OF_MONTH));

    assertEquals((Long)1234567890123L, props.get("longProp", Long.class));
    assertEquals(1.2345d, props.get("decimalProp", Double.class), 0.00001d);
    assertEquals(true, props.get("booleanProp", Boolean.class));

    assertArrayEquals(new Long[] {
        1234567890123L, 55L
    }, props.get("longPropMulti", Long[].class));
    assertArrayEquals(new Double[] {
        1.2345d, 1.1d
    }, props.get("decimalPropMulti", Double[].class));
    assertArrayEquals(new Boolean[] {
        true, false
    }, props.get("booleanPropMulti", Boolean[].class));
  }

  @Test
  public void testContentResourceType() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/jcr:content/header");
    assertEquals("sample/components/header", resource.getResourceType());
  }

  @Test
  public void testContentJcrPrimaryType() throws RepositoryException {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/jcr:content/header");
    assertPrimaryNodeType(resource, JcrConstants.NT_UNSTRUCTURED);
  }

  @Test
  public void testContentProperties() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/jcr:content/header");
    ValueMap props = resource.getValueMap();
    assertEquals("/content/dam/sample/header.png", props.get("imageReference", String.class));
  }

  private void assertPrimaryNodeType(final Resource resource, final String nodeType) throws RepositoryException {
    Node node = resource.adaptTo(Node.class);
    if (node != null) {
      assertEquals(nodeType, node.getPrimaryNodeType().getName());
    }
    else {
      ValueMap props = resource.getValueMap();
      assertEquals(nodeType, props.get(JcrConstants.JCR_PRIMARYTYPE));
    }
  }

}
