/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.forcomfort;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link forcomfortBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Lukas_L - Initial contribution
 */
public class ForcomfortBindingConstants {

    public static final String BINDING_ID = "forcomfort";

    // List all Thing Type UIDs, related to the Hue Binding

    // bridge
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public final static ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "DimmableLight");
    public final static ThingTypeUID THING_TYPE_SWITCH_ELEMENT = new ThingTypeUID(BINDING_ID, "SwitchElement");
    public final static ThingTypeUID THING_TYPE_SHUTTER_ELEMENT = new ThingTypeUID(BINDING_ID, "ShutterElement");
    public final static ThingTypeUID THING_TYPE_RGB_LIGHT = new ThingTypeUID(BINDING_ID, "RGBlightElement");
    public final static ThingTypeUID THING_TYPE_JSON_INPUT = new ThingTypeUID(BINDING_ID, "JsonInput");

    // List all channels
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SHUTTER = "shutter";
    public static final String CHANNEL_TEXT = "text";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String PORT = "port";

    // Thing config properties
    public static final String ADDRESS_MODULE = "moduleAddress";
    public static final String ELEMENT_POSITION = "elementPosition";
    public static final String ON_OFF_POSITION = "onOffPosition";
    public static final String STATE_POSITION = "statePosition";
    public static final String R_POSITION = "redPosition";
    public static final String G_POSITION = "greenPosition";
    public static final String B_POSITION = "bluePosition";

}
