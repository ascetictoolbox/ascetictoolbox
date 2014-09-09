package integratedtoolkit.types.resources;

import integratedtoolkit.types.resources.jaxb.ApplicationSoftwareType;
import integratedtoolkit.types.resources.jaxb.CapabilitiesType;
import integratedtoolkit.types.resources.jaxb.CloudType;
import integratedtoolkit.types.resources.jaxb.DataNodeType;
import integratedtoolkit.types.resources.jaxb.DiskType;
import integratedtoolkit.types.resources.jaxb.HostType;
import integratedtoolkit.types.resources.jaxb.HostType.TaskCount;
import integratedtoolkit.types.resources.jaxb.ImageListType;
import integratedtoolkit.types.resources.jaxb.ImageType;
import integratedtoolkit.types.resources.jaxb.MemoryType;
import integratedtoolkit.types.resources.jaxb.ObjectFactory;
import integratedtoolkit.types.resources.jaxb.OsType;
import integratedtoolkit.types.resources.jaxb.ProcessorType;
import integratedtoolkit.types.resources.jaxb.ResourceListType;
import integratedtoolkit.types.resources.jaxb.ResourceType;
import integratedtoolkit.types.resources.jaxb.ServiceType;
import integratedtoolkit.types.resources.jaxb.SharedDiskListType;
import integratedtoolkit.types.resources.jaxb.StorageElementType;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class ResourcesFile {
	
	private ResourceListType resources;

	public ResourcesFile(){
		resources = (new ObjectFactory()).createResourceListType();
	}
	
	public ResourcesFile(File file) throws JAXBException{
		Unmarshaller um = getJAXBContext().createUnmarshaller();
		resources = ((JAXBElement<ResourceListType>) um.unmarshal(file)).getValue();
	}
	
	public ResourcesFile(String xmlString) throws JAXBException{
		Unmarshaller um = getJAXBContext().createUnmarshaller();
		resources = ((JAXBElement<ResourceListType>) um.unmarshal(new StringReader(xmlString))).getValue();
	}
	
	public void toFile(File file) throws JAXBException{
		Marshaller m = getJAXBContext().createMarshaller();
		ObjectFactory objFact = new ObjectFactory();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(objFact.createResourceList(resources), file);
	}
	
	public String getString() throws JAXBException{
		Marshaller m = getJAXBContext().createMarshaller();
		ObjectFactory objFact = new ObjectFactory();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(objFact.createResourceList(resources), sw);
		return sw.getBuffer().toString();
	}
	
	private static JAXBContext getJAXBContext() throws JAXBException{
		return JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
	}
	
	public List<ResourceType> getResources(){
		ArrayList<ResourceType> list = new ArrayList<ResourceType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof ResourceType){
					list.add((ResourceType)ob);
				}
			}
		}
		return list;
	}
	
	public HashMap<String,ResourceType> getResourcesHashMap(){
		HashMap<String,ResourceType> list = new HashMap<String, ResourceType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof ResourceType){
					list.put(((ResourceType)ob).getName(), (ResourceType)ob);
				}
			}
		}
		return list;
	}
	
	public void addResource(ResourceType resource){
		resources.getDiskOrDataNodeOrResource().add(resource);
	}
	
	public void updateResourceCapabilities(String name, CapabilitiesType cap) throws Exception{
		ResourceType resource = this.getResource(name);
		resource.setCapabilities(cap);
	}
	
	public ResourceType addResource(String name, String proc_arch, Float proc_speed,
			Integer proc_count, String os_type, Float storage_size,
			Float memory_size, String[] appSoftware, Integer taskCount, String[] queues ) throws Exception{
		ResourceType res = new ResourceType(); 
		if (name!=null)
			res.setName(name);
		else
			throw new Exception("Resource name should be defined");
		CapabilitiesType cap = new CapabilitiesType();
		ProcessorType proc = new ProcessorType();
		if (proc_arch != null)
			proc.setArchitecture(proc_arch);
		if (proc_speed != null) 
			proc.setSpeed(proc_speed);
		if (proc_count != null) 
			proc.setCPUCount(proc_count);
		cap.setProcessor(proc);	
		if (os_type!=null){
			OsType os = new OsType();
			os.setOSType(os_type);
			cap.setOS(os);
		}
		if (storage_size!=null){
			StorageElementType storage = new StorageElementType();
			storage.setSize(storage_size.floatValue());
			cap.setStorageElement(storage);
		}
		if (memory_size!=null){
			MemoryType memory = new MemoryType();
			memory.setPhysicalSize(memory_size.floatValue());
			cap.setMemory(memory);
		}
		if(appSoftware!=null&&appSoftware.length>0){
			ApplicationSoftwareType apps = new ApplicationSoftwareType();
			for (String app:appSoftware)
				apps.getSoftware().add(app);
			cap.setApplicationSoftware(apps);
		}
		HostType host = new HostType();
		if (taskCount!=null){
			TaskCount tc = new TaskCount();
			tc.setValue(taskCount);
			host.setTaskCount(tc);
		}
		if(queues!=null&&queues.length>0){
			for (String q:queues)
				host.getQueue().add(q);
		}
		cap.setHost(host);
		res.setCapabilities(cap);			
		resources.getDiskOrDataNodeOrResource().add(res);
		return res;
	}
	
	
	
	public ResourceType getResource(String name) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof ResourceType){
					if(((ResourceType)ob).getName().equals(name))
							return (ResourceType)ob;
				}
			}
		}
		throw new Exception("Resource "+ name + " not found");
	}
	
	public void deleteResource(String name) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (int i=0; i<objList.size(); i++){
				Object ob = objList.get(i);
				if (ob instanceof ResourceType){
					if(((ResourceType)ob).getName().equals(name)){
							objList.remove(i);
							return;
					}
				}
			}
		}
		throw new Exception("Resource "+ name + " not found");
	}
	
	public static List<String> getResourcesNames(List<ResourceType> resources) {
		ArrayList<String> names = new ArrayList<String>();
		if (resources!= null && resources.size()>0){
			for (ResourceType resource:resources){
				names.add(resource.getName());	
			}
		}
		return names;
	}
	
	public static CapabilitiesType getResourceCapabilities(String name, List<ResourceType> resources) {
		if (resources!= null && resources.size()>0){
			for (ResourceType resource:resources){
				if (resource.getName().equals(name)){
					return resource.getCapabilities();
				}
			}
		}
		return null;
	}
	
	public List<DiskType> getDisks(){
		ArrayList<DiskType> list = new ArrayList<DiskType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof DiskType){
					list.add((DiskType)ob);
				}
			}
		}
		return list;
	}
	
	public List<ResourceType> getResourcesWithDisk(String diskName){
		ArrayList<ResourceType> list = new ArrayList<ResourceType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof ResourceType){
					ResourceType res = (ResourceType)ob;
					SharedDiskListType disks = res.getDisks();
					if (disks!=null){
						for (DiskType d:disks.getDisk()){
							if(d.getName().equals(diskName)){
								list.add(res);
								break;
							}
						}
					}
				}
			}
		}
		return list;
	}

	/** Add a disk. If exists modifiy the mountPoint
	 * @param name Disk name
	 * @param mountPoint Mount point path
	 * @return Disk Element
	 * @throws Exception Name has not been defined
	 */
	public DiskType addDisk(String name, String mountPoint) throws Exception{
		try{
			DiskType res = this.getDisk(name);
			if (mountPoint!=null)
				res.setMountPoint(mountPoint);
			return res;
		}catch(Exception e){
			DiskType res = new DiskType(); 
			if (name!=null)
				res.setName(name);
			else
				throw new Exception("Disk name should be defined");
			if (mountPoint!=null)
				res.setMountPoint(mountPoint);
			resources.getDiskOrDataNodeOrResource().add(res);
			return res;
		}
	}
	
	/**Add a disk to resource. If exists modifiy the mountPoint
	 * @param name Disk name
	 * @param mountPoint Mount point path
	 * @param resource Resource to add the mount point
	 * @throws Exception Name has not been defined
	 */
	public static void addDiskToResource(String name, String mountPoint, ResourceType resource) throws Exception{
		DiskType res = new DiskType(); 
		if (name!=null)
			res.setName(name);
		else
			throw new Exception("Disk name should be defined");
		if (mountPoint!=null)
			res.setMountPoint(mountPoint);
		SharedDiskListType sDisks = resource.getDisks();
		if (sDisks== null){
			sDisks = new SharedDiskListType();
			resource.setDisks(sDisks);
		}
		for(DiskType d:sDisks.getDisk()){
			if (d.getName().equals(name)){
				d.setMountPoint(mountPoint);
				return;
			}
		}
		sDisks.getDisk().add(res);
	}
	
	public static void removeDiskFromResource(String name, ResourceType resource){

		SharedDiskListType sDisks = resource.getDisks();
		if (sDisks== null){
			return;
		}
		for(DiskType d:sDisks.getDisk()){
			if (d.getName().equals(name)){
				sDisks.getDisk().remove(d);
				return;
			}
		}
	}
	
	public static DiskType getDiskFromResource(String name, ResourceType resource) throws Exception{
		SharedDiskListType sDisks = resource.getDisks();
		if (sDisks!= null){
			for (DiskType d : sDisks.getDisk()) {
				if (d.getName().equals(name)) {
					return d;
				}
			}
		}
		throw new Exception("Disk "+ name + " not found");
	}
	
	public DiskType getDisk(String name) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof DiskType){
					if(((DiskType)ob).getName().equals(name))
							return (DiskType)ob;
				}
			}
		}
		throw new Exception("Disk "+ name + " not found");
		
	}
	
	public void removeDisk(String name) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof DiskType){
					if(((DiskType)ob).getName().equals(name)){
						resources.getDiskOrDataNodeOrResource().remove((DiskType)ob);
						return;
					}
				}
			}
		}
		throw new Exception("Disk "+ name + " not found");
		
	}
	
	public List<CloudType> getCloudProviders(){
		ArrayList<CloudType> list = new ArrayList<CloudType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof CloudType){
					list.add((CloudType)ob);
				}
			}
		}
		return list;
	}
	
	public CloudType addCloudProvider(String name) throws Exception{
		CloudType res = new CloudType(); 
		if (name!=null)
			res.setName(name);
		else
			throw new Exception("Provider name should be defined");
		
		resources.getDiskOrDataNodeOrResource().add(res);
		return res;
	}
	
	public CloudType getCloudProvider(String name) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof CloudType){
					if(((CloudType)ob).getName().equals(name))
							return (CloudType)ob;
				}
			}
		}
		throw new Exception("Cloud provider "+ name + " not found");
	}
	
	public ImageType addImageToCloudProvider(String providerName, String imageId,
			Map<String, String> shares) throws Exception {
		CloudType provider;
		try{
			provider = getCloudProvider(providerName);
		}catch (Exception e){
			provider = addCloudProvider(providerName);
		}
		ImageListType imgList = provider.getImageList();
		if (imgList==null){
			imgList = new ImageListType();
			provider.setImageList(imgList);
		}
		ImageType img = new ImageType();
		img.setName(imageId);
		if (shares!=null&&!shares.isEmpty()){
			SharedDiskListType sd = new SharedDiskListType();
			for (Map.Entry<String,String> e:shares.entrySet()){
				DiskType d = new DiskType();
				d.setName(e.getKey());
				d.setMountPoint(e.getValue());
				sd.getDisk().add(d);
			}
			img.setSharedDisks(sd);
		}
		imgList.getImage().add(img);
		return img;
	}
	
	public List<ServiceType> getServices(){
		ArrayList<ServiceType> list = new ArrayList<ServiceType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof ServiceType){
					list.add((ServiceType)ob);
				}
			}
		}
		return list;
	}
	
	public ServiceType addService(String serviceName, String namespace, String port, String wsdlLoc) throws Exception{
		if (serviceName!=null && namespace!=null && wsdlLoc!=null){
			ServiceType res = new ServiceType(); 
			res.setName(serviceName);
			res.setNamespace(namespace);
			res.setWsdl(wsdlLoc);
			if (port!=null){
				res.getPort().add(port);
			}
			resources.getDiskOrDataNodeOrResource().add(res);
			return res;
			
		}else
			throw new Exception("A service parameter is not defined");
	}
	
	public ServiceType getService(String wsdlLoc) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof ServiceType){
					if(((ServiceType)ob).getWsdl().equals(wsdlLoc))
							return (ServiceType)ob;
				}
			}
		}
		throw new Exception("Service "+ wsdlLoc + " not found");
	}
	
	public List<DataNodeType> getDataNodes(){
		ArrayList<DataNodeType> list = new ArrayList<DataNodeType>();
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof DataNodeType){
					list.add((DataNodeType)ob);
				}
			}
		}
		return list;
	}
	
	public DataNodeType addDataNode(String host, String path) throws Exception{
		DataNodeType res = new DataNodeType(); 
		if (host!=null && path!=null){	
			res.setHost(host);
			res.setPath(path);
		}else
			throw new Exception("Data node host should be defined");
		resources.getDiskOrDataNodeOrResource().add(res);
		return res;
	}
	
	public DataNodeType getDataNode(String host) throws Exception{
		List<Object> objList = resources.getDiskOrDataNodeOrResource();
		if (objList!=null && objList.size()>0){
			for (Object ob:objList){
				if (ob instanceof DataNodeType){
					if(((DataNodeType)ob).getHost().equals(host))
							return (DataNodeType)ob;
				}
			}
		}
		throw new Exception("DataNode "+ host + " not found");
	}
	
	public ResourceListType getResourceListType(){
		return resources;
	}
	
}
