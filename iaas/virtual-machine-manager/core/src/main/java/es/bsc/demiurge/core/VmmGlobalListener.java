package es.bsc.demiurge.core;

import es.bsc.demiurge.core.models.scheduling.SelfAdaptationAction;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public interface VmmGlobalListener {
    void onVmmStart();
    void onVmmStop();
    void onVmmSelfAdaptation(SelfAdaptationAction selfAdaptationAction);
}
