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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchElement extends AbstractElement {

    private static final ElementType TYPE = ElementType.SwitchElement;
    private static final Logger logger = LoggerFactory.getLogger(SwitchElement.class);

    public SwitchElement(ThingListener listener, int moduleAddress, int elementPosition) {
        super(listener, moduleAddress, elementPosition);
    }

    @Override
    public String commandToJson(OnOffType onOffType) {
        boolean isOn = isOnCommand(onOffType);
        JSONObject json = createHeader();
        try {
            json.put(IS_ON_JSON, isOn);
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
        }
        return json.toString();
    }

    @Override
    public ElementType getType() {
        return TYPE;
    }

    @Override
    public String commandToJson(PercentType cmd) {
        logger.debug("PercentType is not suported by SwitchElemet!");
        return null;
    }

    @Override
    public String commandToJson(IncreaseDecreaseType cmd) {
        logger.debug("IncreaseDecreaseType is not suported by SwitchElemet!");
        return null;
    }

    @Override
    public String commandToJson(HSBType cmd) {
        logger.debug("HSBType is not suported by SwitchElemet!");
        return null;
    }

    @Override
    public void stateUpdate(ElementParam param, Object o) {
        boolean b;

        if (o instanceof Boolean)
            b = (boolean) o;
        else {
            logger.debug("SwitchElement supports only booelan parameter.");
            return;
        }

        State state = b ? OnOffType.ON : OnOffType.OFF;
        listener.updateStateThing(ForcomfortBindingConstants.CHANNEL_SWITCH, state);
    }
}
