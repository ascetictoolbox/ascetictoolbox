/*******************************************************************************
 * Copyright 2002-2015, OpenNebula Project (OpenNebula.org), C12G Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.opennebula.client.group;


import org.opennebula.client.Client;
import org.opennebula.client.OneResponse;
import org.opennebula.client.PoolElement;
import org.w3c.dom.Node;

/**
 * This class represents an OpenNebula group.
 * It also offers static XML-RPC call wrappers.
 */
public class Group extends PoolElement{

    private static final String METHOD_PREFIX   = "group.";
    private static final String ALLOCATE        = METHOD_PREFIX + "allocate";
    private static final String INFO            = METHOD_PREFIX + "info";
    private static final String DELETE          = METHOD_PREFIX + "delete";
    private static final String QUOTA           = METHOD_PREFIX + "quota";
    private static final String ADD_PROVIDER    = METHOD_PREFIX + "addprovider";
    private static final String DEL_PROVIDER    = METHOD_PREFIX + "delprovider";
    private static final String UPDATE          = METHOD_PREFIX + "update";

    /**
     * Creates a new Group representation.
     *
     * @param id The group id.
     * @param client XML-RPC Client.
     */
    public Group(int id, Client client)
    {
        super(id, client);
    }

    /**
     * @see PoolElement
     */
    protected Group(Node xmlElement, Client client)
    {
        super(xmlElement, client);
    }


    // =================================
    // Static XML-RPC methods
    // =================================

    /**
     * Allocates a new group in OpenNebula
     *
     * @param client XML-RPC Client.
     * @param name Name for the new group.
     * @return If successful the message contains the associated
     * id generated for this group.
     */
    public static OneResponse allocate(Client client, String name)
    {
        return client.call(ALLOCATE, name);
    }

    /**
     * Retrieves the information of the given group.
     *
     * @param client XML-RPC Client.
     * @param id The group id.
     * @return If successful the message contains the string
     * with the information returned by OpenNebula.
     */
    public static OneResponse info(Client client, int id)
    {
        return client.call(INFO, id);
    }

    /**
     * Deletes a group from OpenNebula.
     *
     * @param client XML-RPC Client.
     * @param id The group id.
     * @return A encapsulated response.
     */
    public static OneResponse delete(Client client, int id)
    {
        return client.call(DELETE, id);
    }

    /**
     * Replaces the group quota template contents.
     *
     * @param client XML-RPC Client.
     * @param id The group id of the target group we want to modify.
     * @param quota_template New quota template contents.
     * @return If successful the message contains the group id.
     */
    public static OneResponse setQuota(Client client, int id, String quota_template)
    {
        return client.call(QUOTA, id, quota_template);
    }

    /**
     * Adds a resource provider to this group
     *
     * @param client XML-RPC Client.
     * @param id The group id.
     * @param zoneId The zone id.
     * @param clusterId The cluster id.
     * @return A encapsulated response.
     */
    public static OneResponse addProvider(Client client, int id,
        int zoneId, int clusterId)
    {
        return client.call(ADD_PROVIDER, id, zoneId, clusterId);
    }

    /**
     * Deletes a resource provider from this group
     *
     * @param client XML-RPC Client.
     * @param id The group id.
     * @param zoneId The zone id.
     * @param clusterId The cluster id.
     * @return A encapsulated response.
     */
    public static OneResponse delProvider(Client client, int id,
        int zoneId, int clusterId)
    {
        return client.call(DEL_PROVIDER, id, zoneId, clusterId);
    }

    /**
     * Replaces the template contents.
     *
     * @param client XML-RPC Client.
     * @param id The group id of the target group we want to modify.
     * @param new_template New template contents
     * @param append True to append new attributes instead of replace the whole template
     * @return If successful the message contains the group id.
     */
    public static OneResponse update(Client client, int id, String new_template,
        boolean append)
    {
        return client.call(UPDATE, id, new_template, append ? 1 : 0);
    }

    // =================================
    // Instanced object XML-RPC methods
    // =================================

    /**
     * Loads the xml representation of the group.
     * The info is also stored internally.
     *
     * @see Group#info(Client, int)
     */
    public OneResponse info()
    {
        OneResponse response = info(client, id);
        super.processInfo(response);
        return response;
    }

    /**
     * Deletes the group from OpenNebula.
     *
     * @see Group#delete(Client, int)
     */
    public OneResponse delete()
    {
        return delete(client, id);
    }

    /**
     * Replaces the group quota template contents.
     *
     * @param quota_template New quota template contents.
     * @return If successful the message contains the group id.
     */
    public OneResponse setQuota(String quota_template)
    {
        return setQuota(client, id, quota_template);
    }

    /**
     * Adds a resource provider to this group
     *
     * @param zoneId The zone id.
     * @param clusterId The cluster id.
     * @return A encapsulated response.
     */
    public OneResponse addProvider(int zoneId, int clusterId)
    {
        return addProvider(client, id, zoneId, clusterId);
    }

    /**
     * Deletes a resource provider from this group
     *
     * @param zoneId The zone id.
     * @param clusterId The cluster id.
     * @return A encapsulated response.
     */
    public OneResponse delProvider(int zoneId, int clusterId)
    {
        return delProvider(client, id, zoneId, clusterId);
    }

    /**
     * Replaces the template contents.
     *
     * @param new_template New template contents
     * @return If successful the message contains the group id.
     */
    public OneResponse update(String new_template)
    {
        return update(new_template, false);
    }

    /**
     * Replaces the template contents.
     *
     * @param new_template New template contents
     * @param append True to append new attributes instead of replace the whole template
     * @return If successful the message contains the group id.
     */
    public OneResponse update(String new_template, boolean append)
    {
        return update(client, id, new_template, append);
    }

    // =================================
    // Helpers
    // =================================

    /**
     * Returns whether or not the user is part of this group
     *
     * @param uid The user ID.
     * @return Whether or not the user is part of this group.
     */
    public boolean contains(int uid)
    {
        String res = xpath("USERS/ID[.="+uid+"]");
        return res != null && res.equals(""+uid);
    }
}
