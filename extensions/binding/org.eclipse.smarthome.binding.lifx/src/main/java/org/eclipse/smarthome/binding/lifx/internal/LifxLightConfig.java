/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.net.InetSocketAddress;
import java.time.Duration;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;

/**
 * Configuration class for LIFX lights.
 *
 * @author Wouter Born - Initial contribution
 */
public class LifxLightConfig {

    private String deviceId;
    private String host;
    private long fadetime = 300; // milliseconds

    public MACAddress getMACAddress() {
        return deviceId == null ? null : new MACAddress(deviceId, true);
    }

    public InetSocketAddress getHost() {
        return host == null ? null : new InetSocketAddress(host, LifxBindingConstants.UNICAST_PORT);
    }

    public Duration getFadeTime() {
        return Duration.ofMillis(fadetime);
    }

}
