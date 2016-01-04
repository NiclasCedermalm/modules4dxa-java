package com.sdl.webapp.smarttarget.markup;

import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.model.ViewModel;
import com.sdl.webapp.common.markup.MarkupDecorator;
import com.sdl.webapp.common.markup.html.HtmlCommentNode;
import com.sdl.webapp.common.markup.html.HtmlNode;
import com.sdl.webapp.common.markup.html.ParsableHtmlNode;
import com.sdl.webapp.common.markup.html.builders.HtmlBuilders;
import com.sdl.webapp.smarttarget.model.SmartTargetRegion;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

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
                                .withContent(new HtmlCommentNode(stRegion.getXpmMarkup()))
                                .withContent(markup).build();

                /* NEEDED???
                markup = HtmlBuilders.span()
                        .withLiteralContent("<!-- Start Promotion Region: {\"RegionID\": \"" + stRegion.getName() + "\"} -->")
                        .withContent(markup).build();
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
