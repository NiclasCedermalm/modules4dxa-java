package com.sdl.dxa.modules.smarttarget.markup;

import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.model.EntityModel;
import com.sdl.webapp.common.api.model.ViewModel;
import com.sdl.webapp.common.markup.MarkupDecorator;
import com.sdl.webapp.common.markup.html.HtmlCommentNode;
import com.sdl.webapp.common.markup.html.HtmlNode;
import com.sdl.webapp.common.markup.html.ParsableHtmlNode;
import com.sdl.webapp.common.markup.html.builders.HtmlBuilders;
import com.sdl.dxa.modules.smarttarget.model.SmartTargetComponentPresentation;
import com.sdl.dxa.modules.smarttarget.SmartTargetService;

/**
 * SmartTarget Promotion XPM Markup
 *
 * @author nic
 */
public class SmartTargetPromotionXpmMarkup implements MarkupDecorator {

    private static final String PROMOTION_PATTERN = "Start Promotion: " +
            "{\"PromotionID\": \"%s\", \"RegionID\": \"%s\"}";

    private SmartTargetService smartTargetService;

    public SmartTargetPromotionXpmMarkup(SmartTargetService smartTargetService) {
        this.smartTargetService = smartTargetService;
    }

    @Override
    public HtmlNode process(HtmlNode markup, ViewModel model, WebRequestContext webRequestContext) {

        if ( webRequestContext.isPreview() ) {

            if ( model instanceof EntityModel && model.getXpmMetadata() != null ) {

                EntityModel entity = (EntityModel) model;
                final String promotionId = (String) entity.getXpmMetadata().get("PromotionID");

                if ( promotionId != null) {

                    SmartTargetComponentPresentation promotion = smartTargetService.getSavedPromotionItem(promotionId, entity.getId());
                    markup = HtmlBuilders.span()
                            .withNode(this.buildXpmMarkup(promotion.getPromotionId(), promotion.getRegionName()))
                            .withNode(markup).build();

                    final boolean isExperiment = Boolean.parseBoolean(entity.getXpmMetadata().get("IsExperiment").toString());
                    if ( isExperiment ) {
                        SmartTargetComponentPresentation stComponentPresentation = this.smartTargetService.getSavedPromotionItem(promotionId, entity.getId());
                        if ( stComponentPresentation != null ) {
                            String processedHtml = this.smartTargetService.postProcessExperimentComponentPresentation(stComponentPresentation, markup.toHtml());
                            if (processedHtml != null) {
                                markup = new ParsableHtmlNode(processedHtml);
                            }
                        }
                    }
                }
            }
        }
        return markup;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    private HtmlNode buildXpmMarkup(String promotionId, String regionId) {
        return new HtmlCommentNode(String.format(PROMOTION_PATTERN, promotionId, regionId));
    }
}
