package eu.ascetic.amqp.console;

import java.io.IOException;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.amqp.console.client.TopicListener;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * Command Line parser
 */
public class Cli {
	private static final Logger logger = Logger.getLogger(Cli.class.getName());
	private String[] args = null;
	private Options options = new Options();
	private String host = "localhost:5673";
	private String user = "guest";
	private String password = "guest";
	private String topic = null;
	private String queue = null;


	public Cli(String[] args) {
		this.args = args;

		options.addOption("h", "help", false, "Shows help message.");
		options.addOption("u", "user", true, "Specifies the user to connect to the Amqp server. If this option it is not specified user it is set to 'guest'.");
		options.addOption("p", "password", true, "Specifies the password to connect to the Amqp server. If this option it is not specified user it is set to 'password'.");
		options.addOption("s", "server", true, "Specifies the AMQP url address. If this option it is not specified address is set to 'localhost:5673'.");
		options.addOption("t", "topic", true, "Specifies the topic to which subscribe. Wildcards are valid.");
		options.addOption("q", "queue", true, "Specifies the queue to which subscribe. Wildcards are valid.");
	}
	
	public void parse() throws NamingException, JMSException, IOException  {
		CommandLineParser parser = new DefaultParser();;
		CommandLine cmd = null;
		
		if(args == null || args.length == 0) {
			help();
		}

		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h")) {
				help();
			}
			
			if(!cmd.hasOption("t") && !cmd.hasOption("q")) {
				System.out.println("No 'topic' or 'queue' specified");
				System.out.println("One of those parameters is mandatory.");
				System.out.println("Please, check the help: '-h' or '--help' for more information.");
			}
			else {
				if(cmd.hasOption("u")) {
					this.user = cmd.getOptionValue("u");
				}
				if(cmd.hasOption("p")) {
					this.password = cmd.getOptionValue("p");
				}
				if(cmd.hasOption("t")) {
					this.topic = cmd.getOptionValue("t");
				}
				if(cmd.hasOption("q")) {
					this.queue = cmd.getOptionValue("q");
				}
				if(cmd.hasOption("s")) {
					this.host = cmd.getOptionValue("s");
				}
				
				logger.debug("Host: " + this.host + " User: " + this.user + " password: " + password + " topic: " + topic + " queue: " + queue);
				
				listen();
			}


		} catch (ParseException e) {
			logger.warn("Failed to parse comand line properties", e);
			help();
		}

	}
	
	protected void listen() throws NamingException, JMSException, IOException  {
		if(topic != null) {
			AmqpMessageReceiver receiver = new AmqpMessageReceiver(host, user, password,  topic, true);
			TopicListener topicListener = new TopicListener();
			receiver.setMessageConsumer(topicListener);
			
            System.out.println("Receiving messages. Press enter to stop.");
            System.in.read();
 

            System.out.println("Shutting down.");
            receiver.close();
            System.exit(0);
		}
	}

	 private void help() {

		 // This prints out some help
		 HelpFormatter formater = new HelpFormatter();
		 formater.printHelp("ASCETiC AMQP Simple Console Client", options);

		 System.exit(0);
	 }
}
