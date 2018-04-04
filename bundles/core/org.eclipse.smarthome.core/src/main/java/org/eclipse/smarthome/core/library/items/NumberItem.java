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
package org.eclipse.smarthome.core.library.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.types.util.UnitUtils;

/**
 * A NumberItem has a decimal value and is usually used for all kinds
 * of sensors, like temperature, brightness, wind, etc.
 * It can also be used as a counter or as any other thing that can be expressed
 * as a number.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class NumberItem extends GenericItem {

    private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
    private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<Class<? extends Command>>();

    @Nullable
    private Class<? extends Quantity<?>> dimension;

    static {
        acceptedDataTypes.add(DecimalType.class);
        acceptedDataTypes.add(QuantityType.class);
        acceptedDataTypes.add(UnDefType.class);

        acceptedCommandTypes.add(DecimalType.class);
        acceptedCommandTypes.add(QuantityType.class);
        acceptedCommandTypes.add(RefreshType.class);
    }

    public NumberItem(String name) {
        this(CoreItemFactory.NUMBER, name);
    }

    public NumberItem(String type, String name) {
        super(type, name);

        String itemTypeExtension = ItemUtil.getItemTypeExtension(getType());
        if (itemTypeExtension != null) {
            dimension = UnitUtils.parseDimension(itemTypeExtension);
        }
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
        return Collections.unmodifiableList(acceptedDataTypes);
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        return Collections.unmodifiableList(acceptedCommandTypes);
    }

    public void send(DecimalType command) {
        internalSend(command);
    }

    /**
     * Returns the {@link Dimension} associated with this {@link NumberItem}, may be null.
     *
     * @return the {@link Dimension} associated with this {@link NumberItem}, may be null.
     */
    public @Nullable Class<? extends Quantity<?>> getDimension() {
        return dimension;
    }

    @Override
    public void setState(State state) {
        // DecimalType update for a NumberItem with dimension, convert to QuantityType:
        if (state instanceof DecimalType && dimension != null) {
            Unit<?> unit = getUnit();
            if (unit != null) {
                super.setState(new QuantityType<>(((DecimalType) state).doubleValue(), unit));
                return;
            }
        }

        // QuantityType update, check unit and convert if necessary:
        if (state instanceof QuantityType) {
            Unit<?> itemUnit = getUnit();
            Unit<?> stateUnit = ((QuantityType<?>) state).getUnit();
            if (itemUnit != null && (!stateUnit.getSystemUnit().equals(itemUnit.getSystemUnit())
                    || UnitUtils.isDifferentMeasurementSystem(itemUnit, stateUnit))) {
                QuantityType<?> convertedState = ((QuantityType<?>) state).toUnit(itemUnit);
                if (convertedState != null) {
                    super.setState(convertedState);
                    return;
                }

                // the state could not be converted to an accepted unit.
                return;
            }
        }

        if (isAcceptedState(acceptedDataTypes, state)) {
            super.setState(state);
        } else {
            logSetTypeError(state);
        }
    }

    /**
     * Returns the optional unit symbol for this {@link NumberItem}.
     *
     * @return the optional unit symbol for this {@link NumberItem}.
     */
    public @Nullable String getUnitSymbol() {
        Unit<?> unit = getUnit();
        return unit != null ? unit.toString() : null;
    }

    /**
     * Derive the unit for this item by the following priority:
     * <ul>
     * <li>the unit from the current item state</li>
     * <li>the unit parsed from the state description</li>
     * <li>the default system unit</li>
     * </ul>
     *
     * @return the {@link Unit} for this item if available, {@code null} otherwise.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @Nullable Unit<? extends Quantity<?>> getUnit() {
        StateDescription stateDescription = getStateDescription();
        if (stateDescription != null) {
            Unit<?> stateDescriptionUnit = UnitUtils.parseUnit(stateDescription.getPattern());
            if (stateDescriptionUnit != null) {
                return stateDescriptionUnit;
            }
        }

        if (dimension != null && unitProvider != null) {
            return unitProvider.getUnit((Class<Quantity>) dimension);
        }

        return null;
    }

}
