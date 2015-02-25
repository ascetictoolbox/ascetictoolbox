# Clopla

Clopla is a Java library that, given a set of virtual machines and a set of hosts, computes an optimized 
placement for the VMs.

Clopla supports:
* Several construction heuristics: first fit, first fit decreasing...
* Several local search heuristics: simulated annealing, tabu search, hill climbing...
* Several placement policies: consolidate the VMs, distribute the VMs, place the VMs randomly, and group the VMs
by service or application.

## Installation

Add this to your pom.xml:
...

## Usage

Clopla is very easy to use. You just need to define a set of VMs, a set of hosts, and some options for the 
placement engine: the scheduling policy, the maximum running time, a construction heuristic, a local search heuristic, 
and whether some VMs are required to be deployed in specific hosts.

For example, suppose that we want the we want to find an optimized placement using the following options:
* Scheduling policy: consolidation.
* Timeout: 30 seconds.
* Construction heuristic: first fit decreasing.
* Local Search heuristic: hill climbing.
* We are not interested in pinning some VMs to specific hosts.

The java code for finding a placement using those options is:
```java
IClopla clopla = new Clopla();
    VmPlacementConfig vmPlacementConfig = new VmPlacementConfig.Builder(
        Policy.CONSOLIDATION, // Scheduling policy
        30, // Timeout
        ConstructionHeuristic.FIRST_FIT_DECREASING, // Construction heuristic
        new HillClimbing(), // Local Search heuristic
        false) // Deploy VMs in specific hosts?
        .build();
    System.out.println(clopla.getBestSolution(hosts, vms, vmPlacementConfig)); // get placement and print it
```
The only thing missing from the example is knowing how to instantiate a list of VMs and a list of hosts:
 ```java
 // Create a list of VMs that contains a VM with id = 1, cpus = 2, ramMb = 1024, and diskGb = 4
 List<Vm> vms = new ArrayList<>();
 Vm vm = new Vm.Builder((long) 1, 2, 1024, 4).build();
 vms.add(vm);
 // Instantiate a lists of hosts that contains a host with id = 1, hostname = myHost, cpus = 4, ramMb = 8192,
 // diskGb=100, and that is on
 List<Host> hosts = new ArrayList<>();
 Host host = new Host((long) 1, "myHost", 4, 8192, 100, false);
 hosts.add(host);
 ```

Every local search heuristic can be configured with different options. You need to specify those options when
instantiating the local search heuristic. For example, the tabu search heuristic accepts an entity tabu size and an
accepted count limit. All these configuration options are very well explained in the [Optaplanner documentation]
(http://docs.jboss.org/optaplanner/release/6.0.1.Final/optaplanner-docs/html/localSearch.html).

You can find a complete usage example in the examples/ExampleClient.java class.

## License

Code released under [the Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0)