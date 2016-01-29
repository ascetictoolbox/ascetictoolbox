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

package es.bsc.demiurge.openstackjclouds;

import com.google.common.base.Preconditions;

/**
 * OpenStack credentials.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class OpenStackCredentials {

    private final String openStackIP;
    private final int keyStonePort;
    private final String keyStoneTenant;
    private final String keyStoneUser;
    private final String keyStonePassword;
    private final int glancePort;
    private final String keyStoneTenantId;

    public OpenStackCredentials(String openStackIP, int keyStonePort, String keyStoneTenant, String keyStoneUser,
                                String keyStonePassword, int glancePort, String keyStoneTenantId) {
        validateConstructorParams(openStackIP, keyStonePort, keyStoneTenant, keyStoneUser, keyStonePassword,
                glancePort, keyStoneTenantId);
        this.openStackIP = openStackIP;
        this.keyStonePort = keyStonePort;
        this.keyStoneTenant = keyStoneTenant;
        this.keyStoneUser = keyStoneUser;
        this.keyStonePassword = keyStonePassword;
        this.glancePort = glancePort;
        this.keyStoneTenantId = keyStoneTenantId;
    }

    public String getOpenStackIP() {
        return openStackIP;
    }

    public int getKeyStonePort() {
        return keyStonePort;
    }

    public String getKeyStoneTenant() {
        return keyStoneTenant;
    }

    public String getKeyStoneUser() {
        return keyStoneUser;
    }

    public String getKeyStonePassword() {
        return keyStonePassword;
    }

    public int getGlancePort() {
        return glancePort;
    }

    public String getKeyStoneTenantId() {
        return keyStoneTenantId;
    }

    private void validateConstructorParams(String openStackIP, int keyStonePort, String keyStoneTenant,
                                           String keyStoneUser, String keyStonePassword, int glancePort,
                                           String keyStoneTenantId) {
        Preconditions.checkNotNull(openStackIP);
        Preconditions.checkNotNull(keyStonePort);
        Preconditions.checkNotNull(keyStoneTenant);
        Preconditions.checkNotNull(keyStoneUser);
        Preconditions.checkNotNull(keyStonePassword);
        Preconditions.checkNotNull(glancePort);
        Preconditions.checkNotNull(keyStoneTenantId);
    }

}
