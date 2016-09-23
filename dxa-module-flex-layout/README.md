Flex Layout DXA Module
========================

This DXA module provides flexible layouts, which can be managed through SDL Web Experience Manager.
It is based on the DXA Container Framework. The module contains layout containers for:

* Tabs
* Accordions
* Carousels
* Column layouts


Usage
------

Install the CMS content porter package from the '/cms'-directory. It will install the needed schemas, templates and XPM content types.

Install the DXA Container Framework + Flex Layout DXA module in your local Maven repository by go into each module and do:

```
mvn install
```

Add the DXA module as Maven dependency to your web application POM file:

```
    <dependencies> `
        ...
        <dependency>
            <groupId>com.sdl.modules4dxa</groupId>
            <artifactId>dxa-module-flex-layout</artifactId>
            <version>1.0.1</version>
        </dependency>
        
    </dependencies>

``

