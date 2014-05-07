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
package wattsup.jsdk.data.serialize.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import wattsup.jsdk.core.serialize.Serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerializer<T> implements Serializer
{
    private final Gson gson_ = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 
     */
    private final Class<T> jsonType_;

    /**
     * 
     * @param type
     *            The type to convert JSON to.
     */
    public JsonSerializer(final Class<T> type)
    {
        this.jsonType_ = type;
    }

    @Override
    public int serialize(OutputStream out, Serializable value) throws IOException
    {
        String json = gson_.toJson(value);
        out.write(json.getBytes());

        return json.getBytes().length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Serializable> A deserialize(InputStream in, int available) throws IOException
    {
        byte[] b = new byte[available];
        in.read(b);

        return (A) gson_.fromJson(new String(b), jsonType_);
    }
}
