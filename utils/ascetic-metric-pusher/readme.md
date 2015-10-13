Monitoring data pusher for Virtual Machines in ASCETiC IaaS layer

To launch jar file in IaaS machine:
nohup java -jar <jar_file> &

The topic format is: vm.<VMid>.item.<itemId> (for example: vm.wally159.item.energy)

JSON message structure:

{              
                “name”:<String>,        //energy or power
                “value”: <double>,
                “units”:<String>,	//W for power and KWh or Wh for energy
                “timestamp”:<long>
}

available metrics:
- cpu
- memory
- energy
- power
- received bytes
- transmitted bytes