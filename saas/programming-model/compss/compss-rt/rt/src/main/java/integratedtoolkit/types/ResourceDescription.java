/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.types;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;

/**
 *
 * @author flordan
 */
public class ResourceDescription {

    public static final ResourceDescription EMPTY = new ResourceDescription();
    private String name;
    //Resource Description
    private String type;
    private CloudImageDescription image;
    //Capabilities
    LinkedList<String> hostQueue;
    String processorArchitecture = "[unassigned]";
    int processorCPUCount = 0;
    int processorCoreCount = 1;
    float processorSpeed = 0.0f;	// in GHz
    float memoryPhysicalSize = 0.0f;	// in GB
    float memoryVirtualSize = 0.0f;        // in GB
    float memoryAccessTime = 0.0f;      	// in ns
    float memorySTR = 0.0f;        // in GB/s
    float storageElemSize = 0.0f;	// in GB
    float storageElemAccessTime = 0.0f;	// in ms
    float storageElemSTR = 0.0f;	// in MB/s
    String operatingSystemType = "[unassigned]";
    LinkedList<String> appSoftware;
    //Extra fields
    int slots = 0;
    private Float value;

    public ResourceDescription() {
        hostQueue = new LinkedList<String>();
        appSoftware = new LinkedList<String>();
    }

    public ResourceDescription(ResourceDescription clone) {
    	name = clone.name;
        type = clone.type;
        image = clone.image;
        hostQueue = new LinkedList<String>();
        for (int i = 0; i < clone.hostQueue.size(); i++) {
            hostQueue.add(clone.hostQueue.get(i));
        }
        processorArchitecture = clone.processorArchitecture;
        processorCPUCount = clone.processorCPUCount;
        processorCoreCount = clone.processorCoreCount;
        processorSpeed = clone.processorSpeed;
        memoryPhysicalSize = clone.memoryPhysicalSize;
        memoryVirtualSize = clone.memoryVirtualSize;
        memoryAccessTime = clone.memoryAccessTime;
        memorySTR = clone.memorySTR;
        storageElemSize = clone.storageElemSize;
        storageElemAccessTime = clone.storageElemAccessTime;
        storageElemSTR = clone.storageElemSTR;
        appSoftware = new LinkedList<String>();
        for (int i = 0; i < clone.appSoftware.size(); i++) {
            appSoftware.add(clone.appSoftware.get(i));
        }
        slots = clone.slots;
        value = clone.value;
    }

    public ResourceDescription(Node n) {
        name = n.getAttributes().getNamedItem("Name").getTextContent();
        hostQueue = new LinkedList<String>();
        appSoftware = new LinkedList<String>();
        type = name;
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            Node capabilities = n.getChildNodes().item(i);
            if (capabilities.getNodeName().compareTo("#text") == 0) {
            } else if (capabilities.getNodeName().compareTo("Capabilities") == 0) {
                for (int j = 0; j < capabilities.getChildNodes().getLength(); j++) {
                    if (capabilities.getChildNodes().item(j).getNodeName().compareTo("#text") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("Host") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("Processor") == 0) {
                        Node processor = capabilities.getChildNodes().item(j);
                        for (int k = 0; k < processor.getChildNodes().getLength(); k++) {
                            if (processor.getChildNodes().item(k).getNodeName().compareTo("#text") == 0) {
                            } else if (processor.getChildNodes().item(k).getNodeName().compareTo("Architecture") == 0) {
                                processorArchitecture = processor.getChildNodes().item(k).getTextContent();
                            } else if (processor.getChildNodes().item(k).getNodeName().compareTo("CPUCount") == 0) {
                                processorCPUCount = Integer.parseInt(processor.getChildNodes().item(k).getTextContent());
                            } else if (processor.getChildNodes().item(k).getNodeName().compareTo("CoreCount") == 0) {
                                processorCoreCount = Integer.parseInt(processor.getChildNodes().item(k).getTextContent());
                            } else if (processor.getChildNodes().item(k).getNodeName().compareTo("Speed") == 0) {
                                processorSpeed = Float.parseFloat(processor.getChildNodes().item(k).getTextContent());
                            } else {
                            }
                        }
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("OS") == 0) {
                        Node OS = capabilities.getChildNodes().item(j);
                        for (int k = 0; k < OS.getChildNodes().getLength(); k++) {
                            if (OS.getChildNodes().item(k).getNodeName().compareTo("#text") == 0) {
                            } else if (OS.getChildNodes().item(k).getNodeName().compareTo("OSType") == 0) {
                                operatingSystemType = OS.getChildNodes().item(k).getTextContent();
                            }
                        }
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("StorageElement") == 0) {
                        Node storageElement = capabilities.getChildNodes().item(j);
                        for (int k = 0; k < storageElement.getChildNodes().getLength(); k++) {
                            if (storageElement.getChildNodes().item(k).getNodeName().compareTo("#text") == 0) {
                            } else if (storageElement.getChildNodes().item(k).getNodeName().compareTo("Size") == 0) {
                                storageElemSize = Float.parseFloat(storageElement.getChildNodes().item(k).getTextContent());
                            } else if (storageElement.getChildNodes().item(k).getNodeName().compareTo("AccessTime") == 0) {
                                storageElemAccessTime = Float.parseFloat(storageElement.getChildNodes().item(k).getTextContent());
                            } else if (storageElement.getChildNodes().item(k).getNodeName().compareTo("STR") == 0) {
                                storageElemSTR = Float.parseFloat(storageElement.getChildNodes().item(k).getTextContent());
                            } else {
                            }
                        }
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("Memory") == 0) {
                        Node memory = capabilities.getChildNodes().item(j);
                        for (int k = 0; k < memory.getChildNodes().getLength(); k++) {
                            if (memory.getChildNodes().item(k).getNodeName().compareTo("#text") == 0) {
                            } else if (memory.getChildNodes().item(k).getNodeName().compareTo("PhysicalSize") == 0) {
                                memoryPhysicalSize = Float.parseFloat(memory.getChildNodes().item(k).getTextContent());
                            } else if (memory.getChildNodes().item(k).getNodeName().compareTo("VirtualSize") == 0) {
                                memoryVirtualSize = Float.parseFloat(memory.getChildNodes().item(k).getTextContent());
                            } else if (memory.getChildNodes().item(k).getNodeName().compareTo("AccessTime") == 0) {
                                memoryAccessTime = Float.parseFloat(memory.getChildNodes().item(k).getTextContent());
                            } else if (memory.getChildNodes().item(k).getNodeName().compareTo("STR") == 0) {
                                memorySTR = Float.parseFloat(memory.getChildNodes().item(k).getTextContent());
                            } else {
                            }
                        }
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("ApplicationSoftware") == 0) {
                        Node apps = capabilities.getChildNodes().item(j);
                        for (int k = 0; k < apps.getChildNodes().getLength(); k++) {
                            if (apps.getChildNodes().item(k).getNodeName().compareTo("#text") == 0) {
                            } else {
                                Node soft = capabilities.getChildNodes().item(j);
                                for (int app = 0; app < soft.getChildNodes().getLength(); app++) {
                                    appSoftware.add(soft.getChildNodes().item(app).getTextContent());
                                }
                            }
                        }
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("Service") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("VO") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("Cluster") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("FileSystem") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("NetworkAdaptor") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("JobPolicy") == 0) {
                    } else if (capabilities.getChildNodes().item(j).getNodeName().compareTo("AccessControlPolicy") == 0) {
                    } else {
                    }
                }
            }
        }
    }

    public void addSlot() {
        slots++;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int qty) {
        slots = qty;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getHostQueue() {
        return hostQueue;
    }

    public void addHostQueue(String queue) {
        this.hostQueue.add(queue);
    }

    public void setProcessorArchitecture(String Architecture) {
        processorArchitecture = Architecture;
    }

    public String getProcessorArchitecture() {
        return processorArchitecture;
    }

    public void setProcessorCPUCount(int count) {
        processorCPUCount = count;
    }

    public int getProcessorCPUCount() {
        return processorCPUCount;
    }
    
    public void setProcessorCoreCount(int count) {
        processorCoreCount = count;
    }


    public int getProcessorCoreCount() {
        return processorCoreCount;
    }


    public void setProcessorSpeed(float GHz) {
        processorSpeed = GHz;
    }

    public float getProcessorSpeed() {
        return processorSpeed;
    }

    public void setMemoryPhysicalSize(float GB) {
        memoryPhysicalSize = GB;
    }

    public float getMemoryPhysicalSize() {
        return memoryPhysicalSize;
    }

    public void setMemoryVirtualSize(float GB) {
        memoryVirtualSize = GB;
    }

    public float getMemoryVirtualSize() {
        return memoryVirtualSize;
    }

    public void setMemoryAccessTime(float ns) {
        memoryAccessTime = ns;
    }

    public float getMemoryAccessTime() {
        return memoryAccessTime;
    }

    public void setMemorySTR(float GBs) {
        memorySTR = GBs;
    }

    public float getMemorySTR() {
        return memorySTR;
    }

    public void setStorageElemSize(float GB) {
        storageElemSize = GB;
    }

    public float getStorageElemSize() {
        return storageElemSize;
    }

    public void setStorageElemAccessTime(float ms) {
        storageElemAccessTime = ms;
    }

    public float getStorageElemAccessTime() {
        return storageElemAccessTime;
    }

    public void setStorageElemSTR(float MBs) {
        storageElemSTR = MBs;
    }

    public float getStorageElemSTR() {
        return storageElemSTR;
    }

    public void setOperatingSystemType(String OSType) {
        operatingSystemType = OSType;
    }

    public String getOperatingSystemType() {
        return operatingSystemType;
    }

    public void resetAppSoftware() {
        appSoftware = new LinkedList<String>();
    }

    public void addAppSoftware(String software) {
        appSoftware.add(software);
    }

    public List<String> getAppSoftware() {
        return appSoftware;
    }

    public void setImage(CloudImageDescription image) {
        this.image = image;
    }

    public CloudImageDescription getImage() {
        return image;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Float getValue() {
        return value;
    }

    public void join(ResourceDescription mr2) {

        this.processorCoreCount = java.lang.Math.max(this.processorCoreCount, mr2.processorCoreCount);
        this.processorSpeed = java.lang.Math.max(this.processorSpeed, mr2.processorSpeed);

        this.memoryPhysicalSize = java.lang.Math.max(this.memoryPhysicalSize, mr2.memoryPhysicalSize);
        this.memoryVirtualSize = java.lang.Math.max(this.memoryVirtualSize, mr2.memoryVirtualSize);
        this.memoryAccessTime = java.lang.Math.max(this.memoryAccessTime, mr2.memoryAccessTime);
        this.memorySTR = java.lang.Math.max(this.memorySTR, mr2.memorySTR);

        this.storageElemSize = java.lang.Math.max(this.storageElemSize, mr2.storageElemSize);
        this.storageElemAccessTime = java.lang.Math.max(this.storageElemAccessTime, mr2.storageElemAccessTime);
        this.storageElemSTR = java.lang.Math.max(this.storageElemSTR, mr2.storageElemSTR);

        if (this.processorArchitecture.compareTo("[unassigned]") == 0) {
            this.processorArchitecture = mr2.processorArchitecture;
        }
        if (this.operatingSystemType.compareTo("[unassigned]") == 0) {
            this.operatingSystemType = mr2.operatingSystemType;
        }

        this.slots += mr2.slots;


        /*----------------------------------------------------
         ----------TODO ----- assignacions de software---------
         ----------TODO ----- assignacions de queue   ---------
         -----------------------------------------------------*/
    }

    public String toString() {

        StringBuilder constrXPath = new StringBuilder().append("/ResourceList/Resource");
        boolean andUsed = false;
        if (processorArchitecture.compareTo("[unassigned]") != 0) {
            constrXPath.append(" [");
            andUsed = true;
            constrXPath.append("Capabilities/Processor/Architecture[text()='" + processorArchitecture + "']");
        }

        if (processorCPUCount > 1) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Processor/CPUCount[text()>='" + processorCPUCount + "']");
        }
        
        if (processorCoreCount > 1) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Processor/CoreCount[text()>='" + processorCoreCount + "']");
        }

        if (processorSpeed > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Processor/Speed[text()>='" + processorSpeed + "']");
        }

        if (memoryPhysicalSize > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Memory/PhysicalSize[text()>='" + memoryPhysicalSize + "']");
        }

        if (memoryVirtualSize > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Memory/PhysicalSize[text()>='" + memoryVirtualSize + "']");
        }

        if (memoryAccessTime > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Memory/AccessTime[text()>='" + memoryAccessTime + "']");
        }

        if (memorySTR > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/Memory/STR[text()>='" + memorySTR + "']");
        }

        if (storageElemSize > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/StorageElement/Size[text()>='" + storageElemSize + "']");
        }

        if (storageElemAccessTime > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [ ");
            }
            andUsed = true;
            constrXPath.append("Capabilities/StorageElement/AccessTime[text()>='" + storageElemAccessTime + "']");
        }

        if (storageElemSTR > 0.0f) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/StorageElement/STR[text()>='" + storageElemSTR + "']");
        }

        if (operatingSystemType.compareTo("[unassigned]") != 0) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/OS/OSType[text()='" + operatingSystemType + "']");
        }

        for (int i = 0; i < appSoftware.size(); i++) {
            if (andUsed) {
                constrXPath.append(" and ");
            } else {
                constrXPath.append(" [");
            }
            andUsed = true;
            constrXPath.append("Capabilities/ApplicationSoftware/Software[text()='" + appSoftware.get(i) + "']");
        }

        if (andUsed) {
            constrXPath.append("]");
        }
        return constrXPath.toString();
    }

    public float difference(ResourceDescription mr2) {
        float processor_dif = this.processorCoreCount - mr2.processorCoreCount;
        float memory_dif = this.memoryPhysicalSize - mr2.memoryPhysicalSize;
        return processor_dif * 10000 + memory_dif;
    }

    public boolean contains(ResourceDescription rc2) {
        boolean into = (this.operatingSystemType.compareTo(rc2.operatingSystemType) == 0 || this.operatingSystemType.compareTo("[unassigned]") == 0 || ("[unassigned]").compareTo(rc2.operatingSystemType) == 0)
                && (this.processorArchitecture.compareTo(rc2.processorArchitecture) == 0 || this.processorArchitecture.compareTo("[unassigned]") == 0 || ("[unassigned]").compareTo(rc2.processorArchitecture) == 0);
        return into
                && !(this.processorSpeed < rc2.processorSpeed
                || this.memoryAccessTime < rc2.memoryAccessTime
                || this.storageElemAccessTime < rc2.storageElemAccessTime
                || this.memorySTR < rc2.memorySTR
                || this.storageElemSTR < rc2.storageElemSTR
                || this.processorCoreCount < rc2.processorCoreCount
                || this.memoryPhysicalSize < rc2.memoryPhysicalSize
                || this.memoryVirtualSize < rc2.memoryVirtualSize
                || this.storageElemSize < rc2.storageElemSize);

    }

        public Integer into(ResourceDescription rc2) {
        boolean into = (this.operatingSystemType.compareTo(rc2.operatingSystemType) == 0 || this.operatingSystemType.compareTo("[unassigned]") == 0 || ("[unassigned]").compareTo(rc2.operatingSystemType) == 0)
                && (this.processorArchitecture.compareTo(rc2.processorArchitecture) == 0 || this.processorArchitecture.compareTo("[unassigned]") == 0 || ("[unassigned]").compareTo(rc2.processorArchitecture) == 0);
//TODO: CHECK SOFTWARE AND QUEUES
        if (!into) {
            return null;
        }

        int bigger = 0;
        if (this.processorSpeed > rc2.processorSpeed) {
            bigger = 1;
        } else {
            if (this.processorSpeed < rc2.processorSpeed) {
                bigger = -1;
            }
        }

        if (this.memoryAccessTime > rc2.memoryAccessTime) {
            if (bigger == -1) {
                return null;
            } else {
                bigger = 1;
            }
        } else {
            if (this.memoryAccessTime < rc2.memoryAccessTime) {
                if (bigger == 1) {
                    return null;
                }
                bigger = -1;
            } else {
                bigger = 0;
            }
        }

        if (this.storageElemAccessTime > rc2.storageElemAccessTime) {
            if (bigger == -1) {
                return null;
            } else {
                bigger = 1;
            }
        } else {
            if (this.storageElemAccessTime < rc2.storageElemAccessTime) {
                if (bigger == 1) {
                    return null;
                }
                bigger = -1;
            } else {
                bigger = 0;
            }
        }

        if (this.memorySTR > rc2.memorySTR) {
            if (bigger == -1) {
                return null;
            } else {
                bigger = 1;
            }
        } else {
            if (this.memorySTR < rc2.memorySTR) {
                if (bigger == 1) {
                    return null;
                }
                bigger = -1;
            } else {
                bigger = 0;
            }
        }

        if (this.storageElemSTR > rc2.storageElemSTR) {
            if (bigger == -1) {
                return null;
            } else {
                bigger = 1;
            }
        } else {
            if (this.storageElemSTR < rc2.storageElemSTR) {
                if (bigger == 1) {
                    return null;
                }
                bigger = -1;
            } else {
                bigger = 0;
            }
        }

        float min = Float.MAX_VALUE;
        float ratio;
        if (rc2.processorCoreCount != 0.0f) {
            ratio = this.processorCoreCount / rc2.processorCoreCount;
            min = ratio;
        }
        if (rc2.memoryPhysicalSize != 0.0f) {
            ratio = this.memoryPhysicalSize / rc2.memoryPhysicalSize;
            min = Math.min(min, ratio);
        }
        if (rc2.memoryVirtualSize != 0.0f) {
            ratio = this.memoryVirtualSize / rc2.memoryVirtualSize;
            min = Math.min(min, ratio);
        }
        if (rc2.storageElemSize != 0.0f) {
            ratio = this.storageElemSize / rc2.storageElemSize;
            min = Math.min(min, ratio);
        }

        if (bigger > 0 && min < 1) {
            return null;
        }
        if (bigger < 0 && min > 1) {
            return null;
        }

        if (bigger < 0 && min == 1) {
            return -1;
        }

        return (int) min;
    }

    public ResourceDescription multiply(int amount) {
        ResourceDescription rd= new ResourceDescription();
        rd.processorCoreCount=this.processorCoreCount*amount;
        rd.processorCPUCount=this.processorCPUCount*amount;
        rd.memoryPhysicalSize = this.memoryPhysicalSize*amount;
        rd.memoryVirtualSize = this.memoryVirtualSize*amount;
        rd.storageElemSize=this.storageElemSize*amount;
        return rd;
    }
}
