/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.forcomfort.internal;

import java.util.Set;

import org.eclipse.smarthome.binding.forcomfort.handler.ForcomfortBridgeHandler;
import org.eclipse.smarthome.binding.forcomfort.handler.ForcomfortThingHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.google.common.collect.Sets;

/**
 * The {@link ForcomfortHandlerFactory} is responsible for creating things and thing
 * handlers.
 * 
 * @author Lukas_L - Initial contribution
 */
public class ForcomfortHandlerFactory extends BaseThingHandlerFactory {

    //private Logger logger = LoggerFactory.getLogger(ForcomfortHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(ForcomfortBridgeHandler.SUPPORTED_THING_TYPES, ForcomfortThingHandler.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (ForcomfortBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            ForcomfortBridgeHandler handler = new ForcomfortBridgeHandler((Bridge) thing);
            return handler;
        } else if (ForcomfortThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new ForcomfortThingHandler(thing);
        } else
            return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
       if(thingHandler instanceof ForcomfortBridgeHandler) {
           ForcomfortBridgeHandler handler = (ForcomfortBridgeHandler) thingHandler; 
           handler.dispose();           
       }
        super.removeHandler(thingHandler);
    }

}