package com.sdl.dxa.modules.smarttarget.analytics;

import com.tridion.smarttarget.experiments.statistics.Variants;

/**
 * ExperimentWinnerAlgorithm
 *
 * @author nic
 */
public interface ExperimentWinnerAlgorithm {

    public void process(Variants variants);
}
