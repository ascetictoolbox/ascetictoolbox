package eu.ascetic.paas.applicationmanager.datamodel.convert;

import eu.ascetic.paas.applicationmanager.model.Application;

public class ApplicationConverter {

	public static Application withoutDeployments(Application application) {
		if(application != null) {
			Application applicationReturned = new Application();
			applicationReturned.setDeployments(null);
			applicationReturned.setHref(application.getHref());
			applicationReturned.setId(application.getId());
			applicationReturned.setImages(application.getImages());
			applicationReturned.setLinks(application.getLinks());
			applicationReturned.setName(application.getName());
			
			return applicationReturned;
		} else {
			return null;
		}
	}

}
