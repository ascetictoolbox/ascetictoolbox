package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.TimerTask;

import eu.ascetic.amqp.client.AmqpBasicListener;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;

public class PaaSPricingMessageHandler extends TimerTask{
	 AmqpBasicListener listener;
	 String previousmsg = null;
	 PaaSPricingModeller provider;
	public PaaSPricingMessageHandler(AmqpBasicListener listener, PaaSPricingModeller provider){
		this.listener = listener;
		this.provider = provider;
	}
	
	@Override
	public void run() {

		try {
			String newmsg = listener.getMessage();
			if (newmsg!=null){
				if (newmsg!=previousmsg){
					System.out.println(newmsg);
					GenericPricingMessage message = readMessage(newmsg);
			//		provider.getProvider(message.getProvider()).setEnergyPrice(message.getEnergyPrice());
						
					System.out.println("IaaS has changed price to "+message.getEnergyPrice());
			//		provider.getBilling().updateVMCharges(provider.getProvider(message.getProvider()).getEnergyPriceForBilling());
					previousmsg = newmsg;
				}
			}
			
		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public GenericPricingMessage readMessage(String msg){
		GenericPricingMessage message = new GenericPricingMessage();
		String[] temp = msg.split("\"");
		message.setProvider(Integer.parseInt(temp[1]));
		message.setEnergyPrice(Double.parseDouble(temp[3]));
		return message;
	}
	
}