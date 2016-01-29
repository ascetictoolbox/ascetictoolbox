package es.bsc.vmm.ascetic.mq;

import com.google.gson.Gson;
import es.bsc.demiurge.core.VmmGlobalListener;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.drivers.VmAction;
import es.bsc.demiurge.core.drivers.VmmListener;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Calendar;

/**
 * Created by mmacias on 19/11/15.
 */
public class MqEventsManager implements VmmListener, VmmGlobalListener, MessageListener {

	private ActiveMqAdapter activeMqAdapter = new ActiveMqAdapter();
	Logger log = LogManager.getLogger(MqEventsManager.class);

	@Override
	public void onVmDeployment(VmDeployed vm) {
		publishMessageVmDeployed(vm);
		if (vm.getSlaId() != null && !"".equals(vm.getSlaId().trim())) {
			try {
				activeMqAdapter.listenToQueue(
						String.format(VIOLATION_QUEUE_NAME, vm.getSlaId(), vm.getId()),
						this);
			} catch (JMSException e) {
				log.error("Cannot subscribe to message queue after vm deployment: " + e.getMessage());
			}
		}
	}

	@Override
	public void onVmDestruction(VmDeployed vm) {
		publishMessageVmDestroyed(vm);
		if(vm.getSlaId() != null) {
			activeMqAdapter.closeQueue(String.format(VIOLATION_QUEUE_NAME, vm.getSlaId(), vm.getId()));
		}
	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {
		publishMessageVmChangedState(vm, action);
	}

	// next methods do nothing intentionally
	@Override public void onVmMigration(VmDeployed vm) {}
	@Override public void onPreVmDeployment(Vm vm) {}
	// ----


	// Listens all queues for all the currently executed VMs
	@Override
	public void onVmmStart() {
		log.debug("Listening SLA queues for all the currently running VMs");
		for(VmDeployed vm : Config.INSTANCE.getVmManager().getAllVms()) {
			if(vm.getSlaId() != null && !"".equals(vm.getSlaId().trim())) {
				String queueId = String.format(VIOLATION_QUEUE_NAME, vm.getSlaId(), vm.getId());
				try {
					activeMqAdapter.listenToQueue(queueId, this);
				} catch(Exception e) {
					log.warn("Cannot listen to queue " + queueId +": " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void onVmmStop() {
		activeMqAdapter.closeAllQueues();
	}

	private long lastSelfAdaptation = 0;
	private static final long MIN_TIME_BETWEEN_SELF_ADAPTATIONS = 5 * 60 * 1000;

	private static final long IGNORE_MESSAGES_OLDER_THAN = 30 * 1000;

	@Override
	public void onMessage(Message message) {
		long now = System.currentTimeMillis();
		try {
			if (message.getJMSTimestamp() + IGNORE_MESSAGES_OLDER_THAN < now) {
				log.debug("Ignoring old message: " + message.getJMSMessageID());
			} else {
				log.debug("received message: " + message.toString());
				if (message instanceof TextMessage) {
					TextMessage tm = (TextMessage) message;
					log.debug(tm.getText());
					if (lastSelfAdaptation + MIN_TIME_BETWEEN_SELF_ADAPTATIONS < System.currentTimeMillis()) {
						// By the moment, only overall self-adaptation is performed (limit: 1 each 5 minutes. TO DO: make it lower for testing and maybe longer for production)
						lastSelfAdaptation = System.currentTimeMillis();
						Config.INSTANCE.getVmManager().executeOnDemandSelfAdaptation();
					} else {
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(lastSelfAdaptation);
						log.warn("Not triggering self-adaptation since last self-adaptation was at " + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/*
	 * Auxiliary methods for enabling communication
	 */
	private final Gson gson = new Gson();
	public void publishMessageVmDeployed(VmDeployed vm) {
		publishMessage( String.format(VM_STATUS_TOPIC_NAME, vm.getId(), "deployed"), vm);
	}

	public void publishMessageVmDestroyed(VmDeployed vm) {
		publishMessage( String.format(VM_STATUS_TOPIC_NAME, vm.getId(), "destroyed"), vm);
	}

	public void publishMessageVmChangedState(VmDeployed vm, VmAction action) {
		publishMessage( String.format(VM_STATUS_TOPIC_NAME, vm.getId(), action.getCamelCase()), vm);
	}

	private void publishMessage(String topic, Object messageObject) {
		String json = gson.toJson(messageObject);
		log.debug(topic+"\n"+json);
		activeMqAdapter.publishMessage(topic, json);
	}

	// iaas-slam.monitoring.<slaId>.<vmId>.violationNotified
	private static final String VIOLATION_QUEUE_NAME = "iaas-slam.monitoring.%s.%s.violationNotified";

	// virtual-machine-manager.vm.<vmId>.<status>
	private static final String VM_STATUS_TOPIC_NAME = "virtual-machine-manager.vm.%s.%s";

}
