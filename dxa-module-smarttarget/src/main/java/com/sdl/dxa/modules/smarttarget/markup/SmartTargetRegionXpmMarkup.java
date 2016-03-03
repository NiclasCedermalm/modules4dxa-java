package com.sdl.dxa.modules.smarttarget.markup;

import com.sdl.dxa.modules.smarttarget.model.SmartTargetRegion;
import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.model.ViewModel;
import com.sdl.webapp.common.markup.MarkupDecorator;
import com.sdl.webapp.common.markup.html.HtmlCommentNode;
import com.sdl.webapp.common.markup.html.HtmlNode;
import com.sdl.webapp.common.markup.html.builders.HtmlBuilders;

/**
 * SmartTarget Region XPM Markup
 *
 * @author nic
 */
public class SmartTargetRegionXpmMarkup implements MarkupDecorator {

    @Override
    public HtmlNode process(HtmlNode markup, ViewModel model, WebRequestContext webRequestContext) {

        if ( webRequestContext.isPreview() ) {
            if (model instanceof SmartTargetRegion) {
                SmartTargetRegion stRegion = (SmartTargetRegion) model;

                // If SmartTarget is disabled or down -> Ignore to generate targeting XPM markup
                //
                if ( stRegion.getXpmMarkup() == null ) {
                    return markup;
                }

                // Surround with the SmartTarget XPM region markup
                //
                markup =
                        HtmlBuilders.span()
                                .withNode(new HtmlCommentNode(stRegion.getXpmMarkup()))
                                .withNode(markup).build();

                /* TODO: Do we need to have the following markup?
                markup = HtmlBuilders.span()
                        .withTextContent("<!-- Start Promotion Region: {\"RegionID\": \"" + stRegion.getName() + "\"} -->")
                        .withNode(markup).build();
                        */
            }
        }
        return markup;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
