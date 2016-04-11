SmartTarget Module
========================

This is a DXA module for SmartTarget 2014 SP1 and DXA 1.4.
It has the following functionality:

* Make any DXA region SmartTarget enabled. This is done in metadata configuration of the page templates.
* All needed XPM markup and content experiments link processing is injected runtime meaning that any DXA view can be used as a SmartTarget promotion.
* If no promotion found for current region it will fall back on the static content already residing on the page
* Full support for Content Experiments including view and conversion tracking
* Local DB analytics provider - offer the possibility to have all tracking of content experiments in a local SQL database instead of Google Analytics
* Automatic selection of winner in Content Experiments, i.e. after a winner has been selected that promotion is always shown for all visitors. This is configurable in the SmartTarget configuration.

## Setup

1. Install the CMS module by importing the Content Porter package in the 'cms' directory.
2. Setup your webapp with needed configuration (smarttarget_conf.xml, cd_ambient_conf.xml etc). Please refer to the standard [SmartTarget online documentation](http://docs.sdl.com/LiveContent/web/pub.xql?c=t&action=home&pub=SDL_SmartTarget_2014_SP1-v1&lang=en-US) for further information.


## Templating

To create dynamic component presentations to be served by SmartTarget you have to add the standard SmartTarget TBB 'Add to SmartTarget' to your component template.
See the example component template '/Building Blocks/Modules/SmartTarget/Editor/Templates/PromoBanner [Hero]' for a working example.

The SmartTarget regions are implicit, i.e. no additional templating coding is required to make a specific DXA region to a SmartTarget region.
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

## Local Analytics Provider

The local analytics provider is a fully functional analytics provider for Content Experiments. It can be used instead of Google Analytics.
It can use any JDBC database for storing and quering tracking data. The winner selection algorithm is also configurable. So the following algorithms
are available:

* Configurable Chi Square - A configurable version of standard Chi Square algorithm where minimum conversions per variant can be configured (property name: MinimumConversionsPerVariant)
* Reach Conversion Goal Algorithm - Simple algorithm that just make the variant that first reach a specific number of conversions to winner (property name: ConversionGoal ) 

For test and demos dummy data can be generated for a specific experiment.

Example of configuration in smarttarget_conf.xml:

```
<Analytics implementationClass="com.sdl.dxa.modules.smarttarget.analytics.LocalAnalyticsManager" timeoutMilliseconds="5000" trackingRedirectUrl="/redirect/">
    <Storage url="jdbc:hsqldb:/db/tracking-db/tracking-db;user=user1;password=secret" className="org.hsqldb.jdbcDriver" cacheTime="10000"/>
    <ExperimentWinnerAlgorithmClassName>com.sdl.dxa.modules.smarttarget.analytics.ReachConversionGoalAlgorithm</ExperimentWinnerAlgorithmClassName>
    <ConversionGoal>10</ConversionGoal>
    <TrackingStoreInterval>10000</TrackingStoreInterval>
    <UseDummyData>true</UseDummyData>
    <Dummy experimentId="a9cf5654-c6ab-41ec-a8da-3b13de130c0c">
        <MinViews>230</MinViews>
        <MaxViews>300</MaxViews>
        <MinConversions>0</MinConversions>
        <MaxConversions>70</MaxConversions>
        <Winner>
            <Publication>tcm:0-3-1</Publication>
            <VariantIndex>1</VariantIndex>
            <MinConversions>50</MinConversions>
            <MaxConversions>100</MaxConversions>
        </Winner>
    </Dummy>
</Analytics>
```
