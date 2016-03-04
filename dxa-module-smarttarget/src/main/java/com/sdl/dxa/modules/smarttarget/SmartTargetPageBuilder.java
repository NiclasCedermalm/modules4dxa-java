package com.sdl.dxa.modules.smarttarget;

import com.sdl.dxa.modules.smarttarget.model.SmartTargetRegionMvcData;
import com.sdl.webapp.common.api.content.ContentProvider;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.EntityModel;
import com.sdl.webapp.common.api.model.PageModel;
import com.sdl.webapp.common.api.model.RegionModel;
import com.sdl.webapp.common.api.xpm.ComponentType;
import com.sdl.webapp.common.api.xpm.XpmRegion;
import com.sdl.webapp.common.api.xpm.XpmRegionConfig;
import com.sdl.webapp.common.exceptions.DxaException;
import com.sdl.dxa.modules.smarttarget.model.SmartTargetComponentPresentation;
import com.sdl.dxa.modules.smarttarget.model.SmartTargetQueryResult;
import com.sdl.dxa.modules.smarttarget.model.SmartTargetRegion;
import com.sdl.webapp.tridion.mapping.PageBuilder;
import com.tridion.smarttarget.SmartTargetException;
import org.dd4t.contentmodel.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SmartTarget Page Builder
 *
 * @author nic
 */
@Component
public class SmartTargetPageBuilder implements PageBuilder {

    // TODO: Use view name from the predefined regions????

    private static final Logger LOG = LoggerFactory.getLogger(SmartTargetPageBuilder.class);

    private final SmartTargetService smartTargetService;

    private final XpmRegionConfig xpmRegionConfig;

    private final ContentProvider contentProvider;

    @Value("${smarttarget.enabled}")
    private boolean enabled = true;

    @Autowired
    public SmartTargetPageBuilder(SmartTargetService smartTargetService,
                                  XpmRegionConfig xpmRegionConfig,
                                  ContentProvider contentProvider) {
        this.smartTargetService = smartTargetService;
        this.xpmRegionConfig = xpmRegionConfig;
        this.contentProvider = contentProvider;
    }

    @Override
    public PageModel createPage(Page genericPage, PageModel pageModel, Localization localization, ContentProvider contentProvider) throws ContentProviderException {

        try {
            if ( this.enabled ) {
                this.populateSmartTargetRegions(pageModel, localization);
            }
        }
        catch ( DxaException e ) {
            throw new ContentProviderException("Could not populate SmartTarget regions.", e);
        }

        return pageModel;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private void populateSmartTargetRegions(PageModel page, Localization localization) throws ContentProviderException, DxaException {

        List<SmartTargetRegionConfig> smartTargetRegionConfigList = this.getSmartTargetRegionConfiguration(page);
        if ( smartTargetRegionConfigList == null ) return;

        List<String> itemsActiveOnPage = new ArrayList<>();

        for (SmartTargetRegionConfig regionConfig : smartTargetRegionConfigList) {
            SmartTargetRegion stRegion = new SmartTargetRegion(regionConfig.getRegionName());
            stRegion.setName(regionConfig.getRegionName());
            stRegion.setMvcData(new SmartTargetRegionMvcData(regionConfig.getRegionName()));

            XpmRegion xpmRegion = xpmRegionConfig.getXpmRegion(regionConfig.getRegionName(), localization);
            try {

                SmartTargetQueryResult queryResult =
                    this.smartTargetService.query(page.getId(),
                                                  regionConfig,
                                                  this.getComponentTemplates(xpmRegion),
                                                  itemsActiveOnPage);

                stRegion.setXpmMarkup(queryResult.getXpmMarkup());
                if ( queryResult.getComponentPresentations().size() > 0 ) {
                    // Override region on page model
                    //
                    RegionModel pageRegion = page.getRegions().get(stRegion.getName());
                    if ( pageRegion != null ) {
                        page.getRegions().remove(pageRegion);
                    }
                    page.getRegions().add(stRegion);

                }
                for ( SmartTargetComponentPresentation stComponentPresentation : queryResult.getComponentPresentations() ) {

                    String entityId = stComponentPresentation.getComponentUri().split("-")[1] + "-" +
                                      stComponentPresentation.getTemplateUri().split("-")[1];
                    EntityModel entity = contentProvider.getEntityModel(entityId, localization);

                    this.enrichEntityWithSmartTargetData(entity, stComponentPresentation);
                    stRegion.addEntity(entity);
                    itemsActiveOnPage.add(stComponentPresentation.getComponentUri());
                }

            }
            catch ( SmartTargetException e ) {
                LOG.error("Could not populate SmartTarget region '" + regionConfig.getRegionName() + "'", e);
            }
        }

    }

    private void enrichEntityWithSmartTargetData(EntityModel entity, SmartTargetComponentPresentation stComponentPresentation) {
        entity.getXpmMetadata().put("PromotionID", stComponentPresentation.getPromotionId());
        entity.getXpmMetadata().put("RegionID", stComponentPresentation.getRegionName());
        entity.getXpmMetadata().put("IsExperiment", Boolean.toString(stComponentPresentation.isExperiment()));
    }

    private List<SmartTargetRegionConfig> getSmartTargetRegionConfiguration(PageModel page) {
        List<Map<String,String>> smartTargetRegionConfig = (List<Map<String,String>>) page.getMvcData().getMetadata().get("regions");
        if ( smartTargetRegionConfig == null ) return null;

        List<SmartTargetRegionConfig> configList = new ArrayList<>();
        for ( Map<String,String> regionConfig : smartTargetRegionConfig ) {
            configList.add(new SmartTargetRegionConfig(regionConfig));
        }
        return configList;
    }

    private List<String> getComponentTemplates(XpmRegion xpmRegion) {
        List<String> componentTemplates = new ArrayList<>();
        for (ComponentType componentType : xpmRegion.getComponentTypes() ) {
            componentTemplates.add(componentType.getTemplateId());
        }
        return componentTemplates;
    }


}
