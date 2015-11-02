/**
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;

public class VmEstimate {

    private final String id;
    private final double powerEstimate;
    private final double priceEstimate;

    public VmEstimate(String id, double powerEstimate, double priceEstimate) {
        this.id = id;
        this.powerEstimate = powerEstimate;
        this.priceEstimate = priceEstimate;
    }

    public String getId() {
        return id;
    }

    public double getPowerEstimate() {
        return powerEstimate;
    }

    public double getPriceEstimate() {
        return priceEstimate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("powerEstimate", powerEstimate)
                .add("priceEstimate", priceEstimate)
                .toString();
    }

}
