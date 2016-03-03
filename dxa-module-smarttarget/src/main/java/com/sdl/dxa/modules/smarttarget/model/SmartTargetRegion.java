package com.sdl.dxa.modules.smarttarget.model;

import com.sdl.webapp.common.api.model.region.RegionModelImpl;
import com.sdl.webapp.common.exceptions.DxaException;

import java.util.Map;

/**
 * SmartTargetRegion
 *
 * @author nic
 */
public class SmartTargetRegion extends RegionModelImpl {

    private boolean containsSmartTargetContent = false;
    private String xpmMarkup;

    public SmartTargetRegion(String name) throws DxaException {
        super(name);
    }

    public boolean containsSmartTargetContent() {
        return containsSmartTargetContent;
    }

    public void setContainsSmartTargetContent(boolean containsSmartTargetContent) {
        this.containsSmartTargetContent = containsSmartTargetContent;
    }

    public String getXpmMarkup() {
        return xpmMarkup;
    }

    public void setXpmMarkup(String xpmMarkup) {
        this.xpmMarkup = xpmMarkup;
    }

}
