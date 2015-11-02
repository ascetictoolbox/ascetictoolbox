/**
 *     Copyright (C) 2013 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package wattsup.jsdk.client;

import wattsup.jsdk.core.serialize.Serializer;
import wattsup.jsdk.core.serialize.csv.CsvWattsUpPacketSerializer;
import wattsup.jsdk.data.serialize.json.JsonSerializer;
import wattsup.jsdk.remote.data.Response;

public enum OutputFormat
{
    /**
     * 
     */
    CSV(new CsvWattsUpPacketSerializer().includeHeader().includeRownum()),

    /**
     * 
     */
    JSON(new JsonSerializer<Response>(Response.class));

    private final Serializer serialize_;

    private OutputFormat(Serializer serializer)
    {
        this.serialize_ = serializer;
    }

    /**
     * @return the serialize
     */
    public Serializer getSerialize()
    {
        return serialize_;
    }
}
