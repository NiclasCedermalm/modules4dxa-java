DXA Container Framework
============================


The DXA Container Framework is framework that provides support for container components in SDL Web & DXA.
The container is a mixture of a region and a component. The container is basically a component that can be drag&dropped onto a page. 
This creates an inner XPM region where other components can be dropped. 
Examples of typical containers:

* Image container with overlays
* Column layout
* Tab
* Accordion
* Carousel

Setup
------

The DXA Container Framework requires you install an extension in SDL Web. It make sure all drag&dropped components in XPM ends up in the correct place.
Compile the C# code in the src/cms/container-gravity-extension directory. Upload the DLL to your SDL Web server and place it somewhere.
Then add the following in your %SDLWEB_HOME%\config\Tridion.ContentManager.config in <extensions> tag:

```
<add assemblyFileName="[PATH TO DLL]\container-gravity-extension.dll"/>
``
