package org.eclipse.smarthome.binding.forcomfort.internal;

import org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants;
import org.eclipse.smarthome.binding.forcomfort.handler.ThingListener;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractElement {

    public static final String IS_ON_JSON = "isOn";
    public static final String ADDRESS_MODULE_JSON = ForcomfortBindingConstants.ADDRESS_MODULE;
    public static final String ELEMENT_POSITION_JSON = ForcomfortBindingConstants.ELEMENT_POSITION;

    protected int moduleAddress;
    protected int elementPosition;
    protected ThingListener listener;
    protected Logger logger = LoggerFactory.getLogger(AbstractElement.class);

    public enum ElementType {
        SwitchElement,
        RGBlightElement,
        DimmableLight
    };

    public static ThingTypeUID getThingTypeUID(ElementType type) {
        switch (type) {
            case DimmableLight:
                return ForcomfortBindingConstants.THING_TYPE_DIMMABLE_LIGHT;

            case RGBlightElement:
                return ForcomfortBindingConstants.THING_TYPE_RGB_LIGHT;

            case SwitchElement:
                return ForcomfortBindingConstants.THING_TYPE_SWITCH_ELEMENT;
        }
        return null;
    }

    public enum ElementParam {
        Brightness,
        IsOn,
        Color
    }

    public AbstractElement(ThingListener listener, int moduleAddress, int elementPosition) {
        super();
        this.moduleAddress = moduleAddress;
        this.elementPosition = elementPosition;
        this.listener = listener;
    }

    public String commandToJson(Command command) {
        if (command instanceof HSBType)
            return commandToJson((HSBType) command);
        if (command instanceof OnOffType)
            return commandToJson((OnOffType) command);
        if (command instanceof IncreaseDecreaseType)
            return commandToJson((IncreaseDecreaseType) command);
        if (command instanceof PercentType)
            return commandToJson((PercentType) command);
        if (command instanceof RefreshType)
            return commandToJson((RefreshType) command);
        return null;
    }

    protected abstract String commandToJson(OnOffType cmd);

    protected abstract String commandToJson(PercentType cmd);

    protected abstract String commandToJson(IncreaseDecreaseType cmd);

    protected abstract String commandToJson(HSBType cmd);

    private String commandToJson(RefreshType cmd) {
        JSONObject json = new JSONObject();
        try {
            json.put("elementType", "Refresh");
            json.put(ADDRESS_MODULE_JSON, moduleAddress);
            json.put(ELEMENT_POSITION_JSON, elementPosition);
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
            return "";
        }

        return json.toString();
    }

    protected abstract ElementType getType();

    protected boolean isOnCommand(OnOffType cmd) {
        return OnOffType.ON.equals(cmd);
    }

    protected JSONObject createHeader() {
        JSONObject json = new JSONObject();
        try {
            json.put("elementType", getType());
            json.put(ADDRESS_MODULE_JSON, moduleAddress);
            json.put(ELEMENT_POSITION_JSON, elementPosition);
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
        }

        return json;
    }

    public int getModuleAddress() {
        return moduleAddress;
    }

    public void setModuleAddress(int moduleAddress) {
        this.moduleAddress = moduleAddress;
    }

    public int getElementPosition() {
        return elementPosition;
    }

    public void setElementPosition(int elementPosition) {
        this.elementPosition = elementPosition;
    }

    public abstract void stateUpdate(ElementParam param, Object o);
}
