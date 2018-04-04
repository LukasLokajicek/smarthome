/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.forcomfort.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test




/**
 * Tests for {@link forcomfortHandler}.
 *
 * @author Lukas_L - Initial contribution
 */
class forcomfortOSGiTest extends OSGiTest {

	final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("forcomfort", "bridge")

	ManagedThingProvider managedThingProvider
	VolatileStorageService volatileStorageService = new VolatileStorageService()


	@Before
	void setUp() {
		registerService(volatileStorageService)
		managedThingProvider = getService(ThingProvider, ManagedThingProvider)
		assertThat managedThingProvider, is(notNullValue())
	}

	@Test
	void "creation of a forcomfortHandler"() {
		forcomfortHandler handler = getService(ThingHandler,forcomfortBridgeHandler)
		assertThat handler, is(nullValue())
	}

}
