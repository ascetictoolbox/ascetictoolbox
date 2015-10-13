package es.bsc.amon.mq;

import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.jms.Queue;

public interface CommandDispatcher {
	String FIELD_COMMAND = "Command";
	void onCommand(ObjectNode msgBody);
}
