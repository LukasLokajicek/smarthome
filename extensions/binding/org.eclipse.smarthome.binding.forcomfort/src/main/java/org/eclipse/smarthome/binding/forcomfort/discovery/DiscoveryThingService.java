package org.eclipse.smarthome.binding.forcomfort.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.forcomfort.ForcomfortBindingConstants;
import org.eclipse.smarthome.binding.forcomfort.handler.ForcomfortBridgeHandler;
import org.eclipse.smarthome.binding.forcomfort.internal.AbstractElement;
import org.eclipse.smarthome.binding.forcomfort.internal.AbstractElement.ElementType;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryThingService extends AbstractDiscoveryService {

    private static final int TIMEOUT = 60;

    private static final Logger l = LoggerFactory.getLogger(DiscoveryThingService.class);

    private static ForcomfortBridgeHandler bridgeHandler;

    public DiscoveryThingService() {
        super(TIMEOUT);
        l.debug("Discovery service was created");
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ForcomfortBridgeHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        l.debug("Discovery service scanning");
        sendDiscoveryRequest();
        if (bridgeHandler != null && bridgeHandler.discoveryMsg != null) {
            l.debug("Found init");
            JSONArray elementsArray = null;
            ThingUID uid = new ThingUID(ForcomfortBindingConstants.THING_TYPE_DIMMABLE_LIGHT, "1");
            ThingUID bridgeUID = bridgeHandler.getBridgeUID();
            String label;
            Map<String, Object> properties = new HashMap<>();
            JSONObject json;
            String s;
            ElementType type;
            int pos,address;
            String addressHexString;
            try {
                elementsArray = bridgeHandler.discoveryMsg.getJSONArray("elements");

                if (elementsArray == null) {
                    return;
                }

                for (int i = 0; i < elementsArray.length(); i++) {
                    json = elementsArray.getJSONObject(i);

                    pos = json.getInt(ForcomfortBindingConstants.ELEMENT_POSITION);
                    addressHexString = json.getString(ForcomfortBindingConstants.ADDRESS_MODULE);
                    address = Integer.parseInt(addressHexString,16);
                    properties.clear();
                    properties.put(ForcomfortBindingConstants.ADDRESS_MODULE, addressHexString);
                    properties.put(ForcomfortBindingConstants.ELEMENT_POSITION, pos);

                    s = json.getString("elementType");
                    type = ElementType.valueOf(s);
                    uid = new ThingUID(AbstractElement.getThingTypeUID(type),
                            String.valueOf(((address & 0xff) << 8 | (pos & 0xff)) & 0xffff));
                    if (type == ElementType.RgbLightElement) {
                        JSONArray ja = json.getJSONArray("positions");
                        properties.put(ForcomfortBindingConstants.R_POSITION, ja.getInt(0));
                        properties.put(ForcomfortBindingConstants.G_POSITION, ja.getInt(1));
                        properties.put(ForcomfortBindingConstants.B_POSITION, ja.getInt(2));
                    }
                    if (type == ElementType.ShutterElement) {
                        properties.put(ForcomfortBindingConstants.ON_OFF_POSITION, json.getInt(ForcomfortBindingConstants.ON_OFF_POSITION));
                        properties.put(ForcomfortBindingConstants.STATE_POSITION, json.getInt(ForcomfortBindingConstants.STATE_POSITION));
                    }

                    label = json.getString("name");

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                            .withLabel(label).withProperties(properties).build();
                    thingDiscovered(discoveryResult);
                }
                stopScan();
            } catch (JSONException e) {
                l.warn("Couldn't parse json array of initial message", e);
                return;
            }

        }

    }

    private void sendDiscoveryRequest() {
        if (bridgeHandler == null) {
            return;
        }
        try {
            bridgeHandler.sendToServer(new JSONObject().put("elementType", "discoveryService").toString());
            for (int i = 0; i < 10; i++) {
                Thread.sleep(500);
                if (bridgeHandler.discoveryMsg != null) {
                    break;
                }
            }

        } catch (JSONException e) {
            l.warn("Couldn't create Json message for discoveryService", e);
        } catch (InterruptedException e) {
            l.warn("Waiting thread in discovery service was interrupted", e);
        }

    }

    public static void setBridgeHandler(ForcomfortBridgeHandler bridgeHandler) {
        DiscoveryThingService.bridgeHandler = bridgeHandler;
    }

}
