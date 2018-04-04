/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.forcomfort.handler;

import static org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants.*;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.smarthome.binding.forcomfort.internal.AbstractElement;
import org.eclipse.smarthome.binding.forcomfort.internal.DimmableLight;
import org.eclipse.smarthome.binding.forcomfort.internal.RGBLight;
import org.eclipse.smarthome.binding.forcomfort.internal.SwitchElement;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ForcomfortThingHandler extends BaseThingHandler implements ThingListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_DIMMABLE_LIGHT,
            THING_TYPE_SWITCH_ELEMENT, THING_TYPE_RGB_LIGHT, THING_TYPE_JSON_INPUT);

    private Logger logger = LoggerFactory.getLogger(ForcomfortThingHandler.class);

    private AbstractElement element;

    private ForcomfortBridgeHandler bridgeHandler;

    public ForcomfortThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thing: " + thing.getThingTypeUID());
        final BigDecimal moduleAddress = (BigDecimal) getConfig().get(ADDRESS_MODULE);
        if (moduleAddress != null) {
            if (THING_TYPE_SWITCH_ELEMENT.equals(thing.getThingTypeUID())) {

                final BigDecimal position = (BigDecimal) getConfig().get(ELEMENT_POSITION);

                if (position != null)
                    element = new SwitchElement(this, moduleAddress.intValue(), position.intValue());

            } else if (THING_TYPE_DIMMABLE_LIGHT.equals(thing.getThingTypeUID())) {

                final BigDecimal position = (BigDecimal) getConfig().get(ELEMENT_POSITION);

                if (position != null)
                    element = new DimmableLight(this, moduleAddress.intValue(), position.intValue());

            } else if (THING_TYPE_RGB_LIGHT.equals(thing.getThingTypeUID())) {

                final BigDecimal R = (BigDecimal) getConfig().get(R_POSITION);
                final BigDecimal G = (BigDecimal) getConfig().get(G_POSITION);
                final BigDecimal B = (BigDecimal) getConfig().get(B_POSITION);

                if (R != null && G != null && B != null)
                    element = new RGBLight(this, moduleAddress.intValue(), R.intValue(), G.intValue(), B.intValue());

            } else {
                logger.warn("That type of Thing is not registred: " + thing.getThingTypeUID().getAsString());
                return;
            }
            checkBridgeStatus();

        } else {
            checkBridgeStatus();
        }
    }

    private void checkBridgeStatus() {
        if (getForcomfortBridgeHandler() != null) {
            if (getBridge().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private synchronized ForcomfortBridgeHandler getForcomfortBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null)
                return null;
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ForcomfortBridgeHandler) {
                bridgeHandler = (ForcomfortBridgeHandler) handler;
                bridgeHandler.registerElement(element);
            } else
                return null;
        }

        return bridgeHandler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String json = null;

        if (command instanceof StringType)
            json = ((StringType) command).toFullString();
        else if (element != null)
            json = element.commandToJson(command);

        if (json != null) {
            bridgeHandler.sendToServer(json);
        } else
            logger.warn("Unknow command: " + command.toFullString());
    }

    @Override
    public void updateStateThing(String channelUID, State state) {
        updateState(channelUID, state);

    }

}