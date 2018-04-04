package org.eclipse.smarthome.binding.forcomfort.handler;

import java.util.EventListener;

import org.eclipse.smarthome.core.types.State;

public interface ThingListener extends EventListener {

    void updateStateThing(String channelUID, State state);

}
