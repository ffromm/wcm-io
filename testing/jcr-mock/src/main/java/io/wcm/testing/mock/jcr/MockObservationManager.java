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
package io.wcm.testing.mock.jcr;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

/**
 * Mock {@link ObservationManager} implementation.
 */
class MockObservationManager implements ObservationManager {

  @Override
  public void addEventListener(final EventListener listener, final int eventTypes, final String absPath,
      final boolean isDeep, final String[] uuid, final String[] nodeTypeName, final boolean noLocal) {
    // do nothing
  }

  @Override
  public void removeEventListener(final EventListener listener) {
    // do nothing
  }

  // --- unsupported operations ---
  @Override
  public EventListenerIterator getRegisteredEventListeners() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setUserData(final String userData) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EventJournal getEventJournal() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public EventJournal getEventJournal(final int eventTypes, final String absPath, final boolean isDeep,
      final String[] uuid, final String[] nodeTypeName) {
    throw new UnsupportedOperationException();
  }

}
