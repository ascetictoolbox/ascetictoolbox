/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.power_button_presser.strategies;

public class HoltWintersForecast {
    
    private final double forecast;
    private final double low80;
    private final double high80;
    private final double low95;
    private final double high95;

    public HoltWintersForecast(double forecast, double low80, double high80, double low95, double high95) {
        this.forecast = forecast;
        this.low80 = low80;
        this.high80 = high80;
        this.low95 = low95;
        this.high95 = high95;
    }

    public double getForecast() {
        return forecast;
    }

    public double getLow80() {
        return low80;
    }

    public double getHigh80() {
        return high80;
    }

    public double getLow95() {
        return low95;
    }

    public double getHigh95() {
        return high95;
    }
    
}
