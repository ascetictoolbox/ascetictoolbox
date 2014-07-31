package eu.ascetic.iaas.slamanager.poc.slatemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticGenericRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticResourceRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.SharedDisk;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VirtualSystem;

public class AsceticSlaTemplate {
	
	private String uuid = null;

	private String ovfFile = null;

	private Collection<AsceticRequest> asceticRequests = null;

	private SLATemplate slaTemplate = null;

	public AsceticSlaTemplate(Builder builder) {
		uuid = builder._uuid;
		ovfFile = builder._ovfFile;
		asceticRequests = builder._asceticRequests;
		slaTemplate = builder._slaTemplate;
	}

	public void addAsceticResource(AsceticResourceRequest cr) {
		asceticRequests.add(cr);
	}

	public void removeAsceticResource(AsceticResourceRequest cr) {
		asceticRequests.remove(cr);
	}

	public String getOvfFile() {
		return ovfFile;
	}

	public Collection<AsceticRequest> getAsceticRequests() {
		return asceticRequests;
	}

	public SLATemplate getSlaTemplate() {
		return slaTemplate;
	}

	public Collection<VirtualSystem> getVirtualSystems() {
		Collection<VirtualSystem> virtualSystems = new HashSet<VirtualSystem>();
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof VirtualSystem) {
				virtualSystems.add((VirtualSystem) cr);
			}
		}
		return virtualSystems;
	}

	public Collection<SharedDisk> getSharedDisks() {
		Collection<SharedDisk> sharedDisks = new HashSet<SharedDisk>();
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof SharedDisk) {
				sharedDisks.add((SharedDisk) cr);
			}
		}
		return sharedDisks;
	}

	public Collection<AsceticGenericRequest> getGenericRequests() {
		Collection<AsceticGenericRequest> genericRequests = new HashSet<AsceticGenericRequest>();
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof AsceticGenericRequest) {
				genericRequests.add((AsceticGenericRequest) cr);
			}
		}
		return genericRequests;
	}

	@Override
	public String toString() {
		String s = "Ascetic Sla Template:\n";
		s += "UUID: " + uuid + "\n";
		for (AsceticRequest cr : asceticRequests) {
			s += cr.toString() + "\n";
		}
		return s;
	}

	public static class Builder {

		private String _uuid = null;
		private String _ovfFile = null;
		private Collection<AsceticRequest> _asceticRequests = null;
		private SLATemplate _slaTemplate = null;

		public Builder setUUID(String uuid) {
			_uuid = uuid;
			return this;
		}

		public Builder setOvfFile(String ovfFile) {
			_ovfFile = ovfFile;
			return this;
		}

		public Builder setAsceticRequests(Collection<AsceticRequest> c) {
			_asceticRequests = c;
			return this;
		}

		public Builder setSlaTemplate(SLATemplate s) {
			_slaTemplate = s;
			return this;
		}

		public Builder() {

		}

		public AsceticSlaTemplate build() {
			return new AsceticSlaTemplate(this);
		}
	}

	public void removeVirtualSystem(String ovfID) {
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof VirtualSystem && cr.getOvfId().equals(ovfID)) {
				asceticRequests.remove(cr);
				break;
			}
		}
	}

	public void removeSharedDisk(String ovfID) {
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof SharedDisk && cr.getOvfId().equals(ovfID)) {
				asceticRequests.remove(cr);
				break;
			}
		}
	}

	public void removeAllSharedDisks() {
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof SharedDisk) {
				asceticRequests.remove(cr);
			}
		}
	}

	public VirtualSystem getVirtualSystem(String ovfID) {
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof VirtualSystem && cr.getOvfId().equals(ovfID)) {
				return (VirtualSystem) cr;
			}
		}
		return null;
	}

	public void removeAllOvfResourceRequests() {
		for (AsceticRequest cr : asceticRequests) {
			if (cr instanceof VirtualSystem) {
				/*CopyOnWriteArraySet<org.ow2.Ascetic.provider.poc.slatemplate.request.guarantee.Guarantee> cpaGuarantees=new CopyOnWriteArraySet<org.ow2.Ascetic.provider.poc.slatemplate.request.guarantee.Guarantee>(cr.getGuarantees());
				Iterator<org.ow2.Ascetic.provider.poc.slatemplate.request.guarantee.Guarantee> it=cpaGuarantees.iterator();
				while(it.hasNext()){
					org.ow2.Ascetic.provider.poc.slatemplate.request.guarantee.Guarantee g=it.next();
					if(g instanceof OvfResourceGuarantee){
						cr.removeGuarantee(g);
					}
				}*/
			}
		}
	}


}
