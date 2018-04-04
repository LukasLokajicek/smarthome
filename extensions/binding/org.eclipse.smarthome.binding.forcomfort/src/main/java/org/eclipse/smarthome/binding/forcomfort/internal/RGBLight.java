
package org.eclipse.smarthome.binding.forcomfort.internal;

import org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants;
import org.eclipse.smarthome.binding.forcomfort.handler.ThingListener;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBLight extends AbstractElement {

    private static final ElementType TYPE = ElementType.RGBlightElement;
    private static final Logger logger = LoggerFactory.getLogger(RGBLight.class);
    private static final int DIM_STEPSIZE = 5;

    public static final String POSITIONS_JSON = "positions";
    public static final String RGB_JSON = "rgb";

    private int[] rgbPosition;

    private int[] rgbCurrentBrightness = new int[] { 0, 0, 0 };

    private int[] rgbLastNotZeroBrightness = new int[] { 50, 50, 50 };

    public RGBLight(ThingListener listener, int moduleAddress, int r, int g, int b) {
        super(listener, moduleAddress, r);
        rgbPosition = new int[] { r, g, b };
    }

    public void setBrightness(int[] rgb) {
        boolean black = isBlack(rgb);
        for (int i = 0; i < rgb.length; i++) {
            rgbCurrentBrightness[i] = rgb[i];
            if (!black)
                rgbLastNotZeroBrightness[i] = rgb[i];
        }

    }

    private boolean isBlack(int[] rgb) {
        return rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0;
    }

    @Override
    public String commandToJson(OnOffType cmd) {
        boolean setOn = isOnCommand(cmd);

        if (setOn)
            setBrightness(rgbLastNotZeroBrightness);
        else
            setBrightness(new int[] { 0, 0, 0 });

        return generateJsonString();
    }

    @Override
    public String commandToJson(PercentType cmd) {
        logger.debug("not Supported command:" + cmd);
        return null;
    }

    @Override
    public String commandToJson(IncreaseDecreaseType cmd) {
        int newBrightness[] = new int[3];
        for (int i = 0; i < rgbCurrentBrightness.length; i++) {
            if (cmd == IncreaseDecreaseType.DECREASE) {
                newBrightness[i] = Math.max(rgbCurrentBrightness[i] - DIM_STEPSIZE, 0);
            } else {
                newBrightness[i] = Math.min(rgbCurrentBrightness[i] + DIM_STEPSIZE, 100);
            }
        }

        setBrightness(newBrightness);

        return generateJsonString();
    }

    @Override
    public String commandToJson(HSBType cmd) {
        PercentType[] pts = cmd.toRGB();
        setBrightness(new int[] { pts[0].intValue(), pts[1].intValue(), pts[2].intValue() });

        return generateJsonString();
    }

    private String generateJsonString() {
        JSONObject json = createHeader();
        JSONArray jsonRgbPosition = new JSONArray().put(rgbPosition[0]).put(rgbPosition[1]).put(rgbPosition[2]);
        JSONArray jsonRgbBrightness = new JSONArray().put(rgbCurrentBrightness[0]).put(rgbCurrentBrightness[1])
                .put(rgbCurrentBrightness[2]);
        try {
            json.put(POSITIONS_JSON, jsonRgbPosition);
            json.put(RGB_JSON, jsonRgbBrightness);

        } catch (JSONException e) {
            logger.warn("Cannot create Json Object for " + TYPE);
        }

        return json.toString();
    }

    @Override
    public ElementType getType() {
        return TYPE;
    }

    @Override
    public void stateUpdate(ElementParam param, Object o) {
        int[] rgb;
        if (o instanceof int[])
            rgb = (int[]) o;
        else {
            logger.debug("RGBLight supports only int array parameter.");
            return;
        }

        State state = HSBType.fromRGB(rgb[0], rgb[1], rgb[2]);
        listener.updateStateThing(ForcomfortBindingConstants.CHANNEL_COLOR, state);

    }

}
