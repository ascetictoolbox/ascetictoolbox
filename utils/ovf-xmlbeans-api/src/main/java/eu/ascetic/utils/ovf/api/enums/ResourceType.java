/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.utils.ovf.api.enums;

import java.text.MessageFormat;

import eu.ascetic.utils.ovf.api.Item;

/**
 * Enumeration containing predefined constants of virtual resource types used in
 * {@link Item} from CIM_ResourceAllocationSettingData.mof<br>
 * <br>
 * (See <a
 * href="@link http://www.dmtf.org/standards/cim/cim_schema_v2191">http:/
 * /www.dmtf.org/standards/cim/cim_schema_v2191</a>)
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public enum ResourceType {

    /**
     * Enumeration containing all possible virtual machine Resource Types.
     */
    // @formatter:off
	OTHER(1), COMPUTER_SYSTEM(2), PROCESSOR(3), MEMORY(4), IDE_CONTROLLER(5), 
	PARALLEL_SCSI_HBA(6), FC_HBA(7), ISCSI_HBA(8), IB_HCA(9), ETHERNET_ADAPTER(10), 
	OTHER_NETWORK_ADAPTER(11), IO_SLOT(12), IO_DEVICE(13),	FLOPPY_DRIVE(4), 
	CD_DRIVE(15), DVD_DRIVE(16), DISK_DRIVE(17), TAPE_DRIVE(18), STORAGE_EXTENT(19), 
	OTHER_STORAGE_DEVICE(20), SERIAL_PORT(21), PARALLEL_PORT(22), USB_CONTROLLER(23), 
	GRAPHICS_CONTROLLER(24), IEEE_1394_CONTROLLER(25), PARTITIONABLE_UNIT(26), 
	BASE_PARTITIONABLE_UNIT(27), POWER(28), COOLING_CAPACITY(29), 
	ETHERNET_SWITCH_PORT(30), LOGICAL_DISK(31), STORAGE_VOLUME(32), 
	ETHERNET_CONNECTION(33);
	// @formatter:on

    /**
     * Stores the numerical value of this ResourceType.
     */
    private final int number;

    /**
     * Default constructor.
     * 
     * @param number
     *            The numerical representation of the ResourceType
     */
    private ResourceType(int number) {
        this.number = number;
    }

    /**
     * Returns the numerical representation of this ResourceType.
     * 
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Finds the ResourceType object for a given numerical value.
     * 
     * @param number
     *            The resource type number to find
     * @return The ResourceType object
     */
    public static ResourceType findByNumber(Integer number) {
        if (number != null) {
            for (ResourceType rt : ResourceType.values()) {
                if (rt.number == number) {
                    return rt;
                }
            }
        }
        String message = "There is no virtual resource type with number ''{0}'' specified.";
        throw new IllegalArgumentException(
                MessageFormat.format(message, number));
    }
}
