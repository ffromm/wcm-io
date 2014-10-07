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
package io.wcm.testing.mock.osgi;

import io.wcm.testing.mock.osgi.OsgiMetadataUtil.Reference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Helper methods to inject dependencies and activate services via reflection.
 */
final class ReflectionServiceUtil {

  private static final Logger log = LoggerFactory.getLogger(ReflectionServiceUtil.class);

  private ReflectionServiceUtil() {
    // static methods only
  }

  /**
   * Simulate activation or deactivation of OSGi service instance.
   * @param target Service instance.
   * @param componentContext Component context
   * @return true if activation method was called. False if such a method did not exist.
   */
  public static boolean activateDeactivate(Object target, ComponentContext componentContext, boolean activate) {
    Class<?> targetClass = target.getClass();

    // get method name for activation/deactivation from osgi metadata
    Document metadata = OsgiMetadataUtil.getMetadata(targetClass);
    String methodName;
    if (activate) {
      methodName = OsgiMetadataUtil.getActivateMethodName(targetClass, metadata);
    }
    else {
      methodName = OsgiMetadataUtil.getDeactivateMethodName(targetClass, metadata);
    }
    if (StringUtils.isEmpty(methodName)) {
      return false;
    }

    // if method is defined try to execute it
    Method method = getMethod(targetClass, methodName, new Class<?>[] {
        ComponentContext.class
    }, activate);
    if (method != null) {
      try {
        method.setAccessible(true);
        method.invoke(target, componentContext);
        return true;
      }
      catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new RuntimeException("Unable to invoke activate/deactivate method for class " + targetClass.getName(), ex);
      }
    }
    log.warn("Method {}(ComponentContext) not found in class {}", methodName, targetClass.getName());
    return false;
  }

  private static Method getMethod(Class clazz, String methodName, Class<?>[] signature, boolean activate) {
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {
      if (StringUtils.equals(method.getName(), methodName) && Arrays.equals(method.getParameterTypes(), signature)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Simulate OSGi service dependency injection. Injects direct references and multiple references.
   * @param target Service instance
   * @param services Services to be injected. If a {@link BundleContext} object is included, all services from the
   *          bundle context are injected.
   * @return true if all dependencies could be injected
   */
  public static boolean injectServices(Object target, Object... services) {

    // collect all declared reference annotations on class and field level
    Class<?> targetClass = target.getClass();
    List<Reference> references = getReferences(targetClass);

    // try to inject services
    boolean allInjected = true;
    for (Reference reference : references) {
      allInjected = allInjected && injectServiceReference(reference, target, services);
    }
    return allInjected;
  }

  private static List<Reference> getReferences(Class clazz) {
    Document metadata = OsgiMetadataUtil.getMetadata(clazz);
    return OsgiMetadataUtil.getReferences(clazz, metadata);
  }

  private static boolean injectServiceReference(Reference reference, Object target, Object... services) {
    Class<?> targetClass = target.getClass();

    // get reference type
    Class<?> type;
    try {
      type = Class.forName(reference.getInterfaceType());
    }
    catch (ClassNotFoundException ex) {
      throw new RuntimeException("Unable to instantiate reference type: " + reference.getInterfaceType(), ex);
    }

    // get matching service references
    List<ServiceInfo> matchingServices = getMatchingServices(type, services);

    // no references found? check if reference was optional
    if (matchingServices.isEmpty()) {
      boolean isOptional = (reference.getCardinality() == ReferenceCardinality.OPTIONAL_UNARY
          || reference.getCardinality() == ReferenceCardinality.OPTIONAL_MULTIPLE);
      if (!isOptional) {
        log.warn("Unable to inject mandatory reference '{}' for class {}", reference.getName(), targetClass.getName());
      }
      return isOptional;
    }

    // multiple references found? check if reference is not multiple
    if (matchingServices.size() > 1
        && (reference.getCardinality() == ReferenceCardinality.MANDATORY_UNARY
        || reference.getCardinality() == ReferenceCardinality.OPTIONAL_UNARY)) {
      log.warn("Multiple matches found for unary reference '{}' for class {}", reference.getName(), targetClass.getName());
      return false;
    }

    // try to invoke bind method
    String bindMethodName = reference.getBind();
    if (StringUtils.isNotEmpty(bindMethodName)) {
      Method bindMethod = getFirstMethodWithNameAndSignature(targetClass, bindMethodName, new Class<?>[] {
          type
      });
      if (bindMethod != null) {
        bindMethod.setAccessible(true);
        for (ServiceInfo matchingService : matchingServices) {
          try {
            bindMethod.invoke(target, matchingService.getServiceInstance());
          }
          catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Unable to invoke method " + bindMethodName + " for class " + targetClass.getName(), ex);
          }
        }
        return true;
      }
      else {
        Method bindMethodWithConfig = getFirstMethodWithNameAndSignature(targetClass, bindMethodName, new Class<?>[] {
            type,
            Map.class
        });
        if (bindMethodWithConfig != null) {
          bindMethodWithConfig.setAccessible(true);
          for (ServiceInfo matchingService : matchingServices) {
            try {
              bindMethodWithConfig.invoke(target, matchingService.getServiceInstance(), matchingService.getServiceConfig());
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              throw new RuntimeException("Unable to invoke method " + bindMethodName + " for class " + targetClass.getName(), ex);
            }
          }
          return true;
        }
      }
    }

    log.warn("Bind method not found for reference '{}' for class {}", reference.getName(), targetClass.getName());
    return false;
  }

  private static Method getFirstMethodWithNameAndSignature(Class<?> clazz, String methodName, Class<?>[] signature) {
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {
      if (StringUtils.equals(method.getName(), methodName) && Arrays.equals(method.getParameterTypes(), signature)) {
        return method;
      }
    }
    // not found? check super classes
    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null && superClass != Object.class) {
      return getFirstMethodWithNameAndSignature(superClass, methodName, signature);
    }
    return null;
  }

  private static List<ServiceInfo> getMatchingServices(Class<?> type, Object... services) {
    List<ServiceInfo> matchingServices = new ArrayList<>();
    for (Object service : services) {
      if (service instanceof BundleContext) {
        BundleContext bundleContext = (BundleContext)service;
        try {
          ServiceReference[] references = bundleContext.getServiceReferences(type.getName(), null);
          if (references != null) {
            for (ServiceReference serviceReference : references) {
              Object serviceInstance = bundleContext.getService(serviceReference);
              Map<String, Object> serviceConfig = new HashMap<>();
              String[] keys = serviceReference.getPropertyKeys();
              for (String key : keys) {
                serviceConfig.put(key, serviceReference.getProperty(key));
              }
              matchingServices.add(new ServiceInfo(serviceInstance, serviceConfig));
            }
          }
        }
        catch (InvalidSyntaxException ex) {
          // ignore
        }
      }
      else if (type.isAssignableFrom(service.getClass())) {
        Class<?> serviceClass = service.getClass();
        Document doc = OsgiMetadataUtil.getMetadata(serviceClass);
        Map<String, Object> serviceConfig = OsgiMetadataUtil.getProperties(serviceClass, doc);
        matchingServices.add(new ServiceInfo(service, serviceConfig));
      }
    }
    return matchingServices;
  }

  private static class ServiceInfo {

    private final Object serviceInstance;
    private final Map<String, Object> serviceConfig;

    public ServiceInfo(Object serviceInstance, Map<String, Object> serviceConfig) {
      this.serviceInstance = serviceInstance;
      this.serviceConfig = serviceConfig;
    }

    public Object getServiceInstance() {
      return this.serviceInstance;
    }

    public Map<String, Object> getServiceConfig() {
      return this.serviceConfig;
    }

  }

}
