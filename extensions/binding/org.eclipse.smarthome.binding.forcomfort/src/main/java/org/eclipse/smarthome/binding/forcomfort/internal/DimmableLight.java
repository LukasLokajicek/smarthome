package org.eclipse.smarthome.binding.forcomfort.internal;

import org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants;
import org.eclipse.smarthome.binding.forcomfort.handler.ThingListener;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.json.JSONException;
import org.json.JSONObject;

public class DimmableLight extends AbstractElement {

    private static final ElementType TYPE = ElementType.DimmableLight;
    private static final int DIM_STEPSIZE = 5;

    public static final String BRIGHTNESS = "brightness";

    private int brightness = 0;

    public DimmableLight(ThingListener listener, int moduleAddress, int elementPosition) {
        super(listener, moduleAddress, elementPosition);
    }

    private String getJson(int val) {
        JSONObject json = createHeader();
        try {
            json.put(BRIGHTNESS, val);
        } catch (JSONException e) {
            logger.warn("Cannoct create json for Dimmable Light 4com4t.");
            logger.debug("Error traces: ", e);
        }

        return json.toString();
    }

    @Override
    public String commandToJson(OnOffType cmd) {

        int val = 0;

        if (isOnCommand(cmd))
            val = 100;

        return getJson(val);
    }

    @Override
    public ElementType getType() {
        return TYPE;
    }

    @Override
    public String commandToJson(PercentType cmd) {
        brightness = cmd.intValue();
        return getJson(brightness);
    }

    @Override
    public String commandToJson(IncreaseDecreaseType cmd) {
        int newBrightness;
        if (cmd == IncreaseDecreaseType.DECREASE) {
            newBrightness = Math.max(brightness - DIM_STEPSIZE, 0);
        } else {
            newBrightness = Math.min(brightness + DIM_STEPSIZE, 100);
        }
        brightness = newBrightness;
        return getJson(newBrightness);
    }

    @Override
    public String commandToJson(HSBType cmd) {
        logger.debug("HSBType is not suported by SwitchElemet!");
        return null;
    }

    @Override
    public void stateUpdate(ElementParam param, Object o) {
        Integer brightness;
        if(o instanceof Integer)
            brightness = (Integer) o;
        else {
            logger.debug("DimmableLight supports only Integer parameter.");
            return;
        }
       
        State state = new PercentType(brightness);
        listener.updateStateThing(ForcomfortBindingConstants.CHANNEL_BRIGHTNESS, state);
    }

}
