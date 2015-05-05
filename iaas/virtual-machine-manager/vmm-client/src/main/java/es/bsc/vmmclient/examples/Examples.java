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

package es.bsc.vmmclient.examples;

import es.bsc.vmmclient.models.ListVmsDeployed;
import es.bsc.vmmclient.models.VmDeployed;
import es.bsc.vmmclient.rest.VmmClient;

public class Examples {

    private static void printVms() {
        ListVmsDeployed vms = VmmClient.getVmmService().getVms();
        for (VmDeployed vm : vms.getVms()) {
            System.out.println(vm);
        }
    }

    public static void main(String[] args) {
        printVms();
        System.out.println(VmmClient.getVmmService().getVm("596b462a-96dc-42cc-adfd-e6427789fe81"));
    }

}
