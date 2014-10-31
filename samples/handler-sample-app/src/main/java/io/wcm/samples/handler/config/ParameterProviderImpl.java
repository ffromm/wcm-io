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
package io.wcm.samples.handler.config;

import io.wcm.config.spi.ParameterProvider;
import io.wcm.config.spi.helpers.AbstractParameterProvider;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Parameter provider
 */
@Component(immediate = true)
@Service(ParameterProvider.class)
public class ParameterProviderImpl extends AbstractParameterProvider {

  /**
   * Provider parameters from Params class
   */
  public ParameterProviderImpl() {
    super(Params.class);
  }

}
