
Slot-aware VM deployment experiments


I have in mind four situations that we could test. We should use 4 different deployments.


Scale-up from 1 CPU to 24 CPUs using slot-aware deployer + going back to 1 CPU.
Scale-up from 1 CPU to 24 CPUs without slot-aware deployer (fixed size 2 cpus) + going back to 1 CPU.
Scale-up from 1 CPU to 24 CPUs without slot-aware deployer (fixed size 3 cpus) + going back to 1 CPU.
Scale-up from 1 CPU to 24 CPUs VMs without slot-aware deployer (fixed size 4 cpus) + going back to 1 CPU.
(Nice to have) Scale-up from 1 CPU to 32 CPUs using slot-aware deployer with other optimizations.


(Nice to have) Show graphically the deployment plans of the 4 experiments and demonstrate why the first experiment is superior to others. Material on D6.2.3 could be reused for this.


Show numbers about the increment of power consumption created by this 4 deployments and show which percentage of energy has been reduced in scenario 1 against other scenarios. This requires to start always in the same situation.


Configuration:
We will use ascetic-stable avoiding other users to use the environment at the same time. This will produce results closer to a real cluster situation. The idea is to measure the increase of power consumption in all the cluster.
Remember that the slot-aware deployment scenario requires to build deployment plans where the host is specified. So we will need a different kind of communication with the Application Manager.


Metrics:
We will show Zabbix metrics of the hosts impacted by the deployments. So for instance if the deployment plan 1 has deployed VMs on host01, host04 and host05 we have to monitor the increase of watts in this three hosts. I would avoid other kind of metrics since we have seen that right now they aren’t 100% reliable.


To discuss:
How to create an SLA breach? 
One option would be to change the SLA term of power_usage_per_app() >= 0.0 to power_usage_per_app() >= (number of watts consumed by 32 CPUs). 
It would be nice to have number_of_cpus_per_app to be more exact with the experiments.
Shall we increase from 1 to 32 in one phase? Or shall we increase from 1 to 4, then 4 to 16, and then 16 to 32?

