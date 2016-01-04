package com.sdl.webapp.smarttarget;

import com.google.common.base.Strings;
import com.sdl.webapp.common.api.content.*;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.*;
import com.sdl.webapp.common.api.model.entity.AbstractEntityModel;
import com.sdl.webapp.common.api.model.region.RegionModelImpl;
import com.sdl.webapp.common.api.model.region.RegionModelSetImpl;
import com.sdl.webapp.common.api.xpm.ComponentType;
import com.sdl.webapp.common.api.xpm.XpmRegion;
import com.sdl.webapp.common.api.xpm.XpmRegionConfig;
import com.sdl.webapp.common.exceptions.DxaException;
import com.sdl.webapp.smarttarget.model.SmartTargetComponentPresentation;
import com.sdl.webapp.smarttarget.model.SmartTargetQueryResult;
import com.sdl.webapp.smarttarget.model.SmartTargetRegion;
import com.sdl.webapp.smarttarget.model.SmartTargetRegionMvcData;
import com.sdl.webapp.tridion.ModelBuilderPipeline;
import com.sdl.webapp.tridion.PageBuilder;
import com.tridion.smarttarget.SmartTargetException;
import org.dd4t.contentmodel.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final ModelBuilderPipeline modelBuilderPipeline;

    @Value("${smarttarget.enabled}")
    private boolean enabled = true;

    @Autowired
    public SmartTargetPageBuilder(SmartTargetService smartTargetService,
                                  XpmRegionConfig xpmRegionConfig,
                                  ContentProvider contentProvider,
                                  ModelBuilderPipeline modelBuilderPipeline) {
        this.smartTargetService = smartTargetService;
        this.xpmRegionConfig = xpmRegionConfig;
        this.contentProvider = contentProvider;
        this.modelBuilderPipeline = modelBuilderPipeline;
        this.modelBuilderPipeline.addPageBuilderHandler(this);
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

    private void populateSmartTargetRegions(PageModel page, Localization localization) throws ContentProviderException, DxaException {

        List<SmartTargetRegionConfig> smartTargetRegionConfigList = this.getSmartTargetRegionConfiguration(page);
        if ( smartTargetRegionConfigList == null ) return;

        for (SmartTargetRegionConfig regionConfig : smartTargetRegionConfigList) {
            SmartTargetRegion stRegion = new SmartTargetRegion(regionConfig.getRegionName());
            stRegion.setName(regionConfig.getRegionName());
            stRegion.setMvcData(new SmartTargetRegionMvcData(regionConfig.getRegionName()));

            XpmRegion xpmRegion = xpmRegionConfig.getXpmRegion(regionConfig.getRegionName(), localization);
            try {

                SmartTargetQueryResult queryResult =
                    this.smartTargetService.query(page.getId(),
                                                  regionConfig,
                                                  this.getComponentTemplates(xpmRegion));

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

                }

            }
            catch ( SmartTargetException e ) {
                LOG.error("Could not populate SmartTarget region '" + regionConfig.getRegionName() + "'", e);
            }
        }

    }

    private void enrichEntityWithSmartTargetData(EntityModel entity, SmartTargetComponentPresentation stComponentPresentation) {
        if ( entity instanceof AbstractEntityModel) {
            HashMap<String, String> entityData = new HashMap<>();
            entityData.putAll(entity.getXpmMetadata());
            entityData.put("PromotionID", stComponentPresentation.getPromotionId());
            entityData.put("RegionID", stComponentPresentation.getRegionName());
            entityData.put("IsExperiment", Boolean.toString(stComponentPresentation.isExperiment()));
            ((AbstractEntityModel) entity).setXpmMetadata(entityData);
        }
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
