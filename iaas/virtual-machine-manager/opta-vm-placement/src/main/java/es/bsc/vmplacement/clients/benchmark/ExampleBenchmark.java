/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmplacement.clients.benchmark;

/**
 * This class executes a benchmark that compares several construction and local search algorithms
 * for the given planning problem.
 * The algorithms to compare amd the details of the benchmark are defined in an xml file.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ExampleBenchmark extends CommonBenchmarkApp {

    private static final String BENCHMARK_CONFIG = "/vmplacementBenchmarkConfig.xml.ftl";

    public static void main(String[] args) {
        new ExampleBenchmark().buildFromTemplateAndBenchmark(BENCHMARK_CONFIG);
    }

}
