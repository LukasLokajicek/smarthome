/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.forcomfort.handler;

import static org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants.THING_TYPE_BRIDGE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants;
import org.eclipse.smarthome.binding.forcomfort.discovery.DiscoveryThingService;
import org.eclipse.smarthome.binding.forcomfort.internal.AbstractElement;
import org.eclipse.smarthome.binding.forcomfort.internal.AbstractElement.ElementParam;
import org.eclipse.smarthome.binding.forcomfort.internal.AbstractElement.ElementType;
import org.eclipse.smarthome.binding.forcomfort.internal.DimmableLight;
import org.eclipse.smarthome.binding.forcomfort.internal.RGBLight;
import org.eclipse.smarthome.binding.forcomfort.internal.SwitchElement;
import org.eclipse.smarthome.binding.forcomfort.internal.TCPclient;
import org.eclipse.smarthome.binding.forcomfort.internal.TCPlistener;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ForcomfortBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Lukas_L - Initial contribution
 */
public class ForcomfortBridgeHandler extends ConfigStatusBridgeHandler implements TCPlistener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(ForcomfortBridgeHandler.class);

    private TCPclient tcpClient;

    private Map<Integer, List<AbstractElement>> elements = new HashMap<>();

    public JSONObject discoveryMsg;

    Bridge bridge;

    public ForcomfortBridgeHandler(Bridge bridge) {
        super(bridge);
        this.bridge = bridge;
        logger.debug("Forcomfort bridge was created");
        DiscoveryThingService.setBridgeHandler(this);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not supported
    }

    @Override
    public void initialize() {
        logger.debug("Initializing 4com4t bridge handler.");

        if (getConfig().get(ForcomfortBindingConstants.HOST) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to 4com4t bridge. IP address was not set.");
            return;
        }

        if (getConfig().get(ForcomfortBindingConstants.PORT) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to 4com4t bridge. Port was not set.");
            return;
        }

        String host = (String) getConfig().get(ForcomfortBindingConstants.HOST);
        Integer port = ((BigDecimal) getConfig().get(ForcomfortBindingConstants.PORT)).intValueExact();

        tcpClient = new TCPclient(host, port, this);
        tcpClient.connectToServer();

    }

    public void sendToServer(String msg) {
        tcpClient.sendToserver(msg);
    }

    @Override
    public void dispose() {
        tcpClient.disconnect();
        logger.debug("4com4t bridge handler was disposed.");
        super.dispose();
    }

    @Override
    public void receivedFromServer(String msg) {
        JSONObject json;

        String typeString = "";

        List<AbstractElement> elementGroup;

        try {
            json = new JSONObject(msg);
            typeString = json.getString("elementType");
            if (typeString.equals("init")) {
                discoveryMsg = json;
                return;
            }
            int address = Integer.parseInt(json.getString(AbstractElement.ADDRESS_MODULE_JSON));
            int position = json.getInt(AbstractElement.ELEMENT_POSITION_JSON);

            elementGroup = elements.get(address);
            ElementType type = ElementType.valueOf(typeString);
            AbstractElement element = null;

            if (elementGroup != null) {
                for (AbstractElement el : elementGroup) {
                    if (el.getElementPosition() == position) {
                        element = el;
                        break;
                    }
                }
            } else
                logger.debug(
                        "There is no element with following " + AbstractElement.ADDRESS_MODULE_JSON + " : " + address);

            if (element != null)
                switch (type) {
                    case DimmableLight:
                        Integer brightness = json.getInt(DimmableLight.BRIGHTNESS);
                        element.stateUpdate(ElementParam.Brightness, brightness);

                        break;
                    case RGBlightElement:
                        JSONArray rgbJ = json.getJSONArray(RGBLight.RGB_JSON);
                        int[] rgb = new int[] { rgbJ.getInt(0), rgbJ.getInt(1), rgbJ.getInt(2) };

                        element.stateUpdate(ElementParam.Color, rgb);

                        break;
                    case SwitchElement:
                        Boolean isOn = json.getBoolean(SwitchElement.IS_ON_JSON);
                        element.stateUpdate(ElementParam.IsOn, isOn);
                        break;

                }
            else
                logger.debug("There is no element with following " + AbstractElement.ELEMENT_POSITION_JSON + " : "
                        + position);

        } catch (JSONException e) {
            logger.debug("Couldn't parse Json message: " + msg, e);
        } catch (IllegalArgumentException e) {
            logger.debug("Not known Type: " + typeString, e);
        }

    }

    @Override
    public void changeStatus(ThingStatus status) {
        if (!thing.getStatus().equals(status))
            updateStatus(status);
    }

    public ThingUID getBridgeUID() {
        return bridge.getUID();
    }

    public void registerElement(AbstractElement element) {
        if (element == null)
            return;

        List<AbstractElement> list = elements.get(element.getModuleAddress());

        if (list == null) {
            list = new ArrayList<>();
            elements.put(element.getModuleAddress(), list);
        }

        list.add(element);
    }
}