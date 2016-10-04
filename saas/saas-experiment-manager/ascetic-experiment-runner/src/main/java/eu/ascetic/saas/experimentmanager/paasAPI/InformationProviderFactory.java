package eu.ascetic.saas.experimentmanager.paasAPI;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InformationProviderFactory {
	
	private static String _defSource;
	
	public static void configure(String defSource){
		_defSource=defSource;
	}
	
	public static InformationProvider getProvider(){
		ApplicationContext context = new ClassPathXmlApplicationContext(_defSource);
		return (InformationProvider) context.getBean("InformationProvider");
	}
	
	public static InformationProvider getDefaultProvider(String urlToApplicationManager, String urlToApplicationMonitor){
		return new InformationProviderImpl(urlToApplicationManager, urlToApplicationMonitor);
	}

}
