SmartTarget Module
========================

This is a DXA module for SmartTarget 2014 SP1 and DXA 1.3.
It has the following functionality:

* 

The big difference comparing to the standard SmartTarget module is that SmartTarget is made totally implicit, i.e. no additional views etc is needed.
Any region and any entity can be become SmartTarget enabled. It is just configuration from the SDL Tridion side.

## Setup

1. Install the CMS module by importing the Content Porter package in the 'cms' directory.
2. Setup your webapp with needed configuration (smarttarget_conf.xml, cd_ambient_conf.xml etc). Please refer to the standard [SmartTarget online documentation](http://docs.sdl.com/LiveContent/web/pub.xql?c=t&action=home&pub=SDL_SmartTarget_2014_SP1-v1&lang=en-US) for further information.

## Templating

To create dynamic component presentations to be served by SmartTarget you have to add the following building blocks to your component template (in below order):

* Render Component Content
* Add to SmartTarget
* Default Component Template Finish Actions
* Generate Component Presentation

See the example component template '/Building Blocks/Modules/SmartTarget/Editor/Templates/PromoBanner [Hero]' for a working example.

The SmartTarget regions are implicit, i.e. no additional templating coding is required to make a specific TRI region to a SmartTarget region.
You only have to specify what regions that are managed by SmartTarget in the page template metadata field 'SmartTarget Regions'. In this embedded schema field you can specify the following per region:

* region name
* max items in the region
* allow duplicates between regions

To enable region configuration capabilities you have to use the metadata schema 'SmartTarget Page Template Metadata' instead of the standard DXA one.

See the example page template '/Building Blocks/Modules/SmartTarget/Editor/Templates/SmartTarget Home Page' for a working example.

## Design

The SmartTarget module hooks in a custom page builder in the model pipeline. It will populate configured DXA regions with SmartTarget content on pages instead of using page component presentations.
If the SmartTarget query return an empty list the fallback content is used (the content using templates marked for current region) instead.
What regions that are managed by SmartTarget is specified by the the page template metadata field 'smartTargetRegions'.
Only results having component templates defined in the XPM region data are used in the region.
