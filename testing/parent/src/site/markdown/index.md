## wcm.io Testing

Helper tools for supporting Unit Tests, Integration test and test automation in AEM-based projects.


### Overview

* [AEM Mocks](aem-mock/): Mock implementations for running unit tests in AEM context without having to run a real AEM or Sling instance:
* Mock Helper: Helper for setting up Mock contexts for wcm.io subprojects.
    * [Sling](wcm-io-mock/sling/): Helps setting up mock environment for wcm.io Sling Commons and Sling Models Extensions.
    * [Configuration](wcm-io-mock/config/): Helps setting up mock environment for wcm.io Configuration.
    * [Handler](wcm-io-mock/handler/): Helps setting up mock environment for wcm.io Handler.
* [JUnit Commons](junit-commons/): Common extensions of JUnit for supporting the wcm.io JUnit tests.


### Mocking stack

Initially wcm.io provided mocking implementations for JCR, OSGi and Sling. These implementations are now part of the Apache Sling project and maintained by the Sling Community:

* [Sling Mocks](http://sling.apache.org/documentation/development/sling-mock.html)
* [OSGi Mocks](http://sling.apache.org/documentation/development/osgi-mock.html)
* [JCR Mocks](http://sling.apache.org/documentation/development/jcr-mock.html)
