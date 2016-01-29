Client for the Ascetic VMM

Examples:

VmManagerClient vmm = new VmManagerClient("http://10.4.0.15:34372/vmmanager");

// Print all the VMs deployed
System.out.println(vmm.getVms());

// Print all the images uploaded to OpenStack
System.out.println(vmm.getImages());

All the available operations are specified in the es.bsc.vmmclient.vmm.VmManager interface.

