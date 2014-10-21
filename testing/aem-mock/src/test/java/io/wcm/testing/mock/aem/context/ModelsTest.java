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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.ListResourceBundle;
import java.util.Locale;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.i18n.ResourceBundleProvider;
import org.apache.sling.models.annotations.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.i18n.I18n;

@RunWith(MockitoJUnitRunner.class)
public class ModelsTest {

  @Rule
  public AemContext context = new AemContext();

  @Before
  public void setUp() {
    context.addModelsForPackage("io.wcm.testing.mock.aem.context");
    context.currentPage(context.create().page("/content/sample/en"));
  }

  @Test
  public void testModelWithI18n() {
    ModelWithI18n model = context.request().adaptTo(ModelWithI18n.class);
    assertNotNull(model);
    assertNotNull(model.getI18n());
  }

  @Test
  public void testModelWithI18nWithResourceProvider() {
    ResourceBundleProvider provider = mock(ResourceBundleProvider.class);
    context.registerService(ResourceBundleProvider.class, provider);
    when(provider.getResourceBundle(anyString(), any(Locale.class))).thenReturn(new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][] {
            {
              "key1", "value1"
            }
        };
      }
    });

    ModelWithI18n model = context.request().adaptTo(ModelWithI18n.class);
    assertNotNull(model);
    assertNotNull(model.getI18n());
    assertEquals("value1", model.getI18n().get("key1"));
  }

  @Model(adaptables = SlingHttpServletRequest.class)
  public interface ModelWithI18n {
    @AemObject
    I18n getI18n();
  }

}
