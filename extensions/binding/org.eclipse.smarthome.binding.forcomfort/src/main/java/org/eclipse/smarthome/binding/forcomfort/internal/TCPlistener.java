package org.eclipse.smarthome.binding.forcomfort.internal;

import java.util.EventListener;

import org.eclipse.smarthome.core.thing.ThingStatus;

public interface TCPlistener extends EventListener {

    void receivedFromServer(String message);

    void changeStatus(ThingStatus status);

}
