/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */



package utils;

/**
 * The class of <code>Constant</code> includes all the constants that needed by optimization processes.
 * 
 */
public class Constant {
    public final static String $cup_architecture = "cpu_architecture";
    public final static String $memory_redundancy = "memory_redundancy";


    // for resource of request
    public final static String startTtime = "START_TIME_VAR";
    public final static String endTime = "END_TIME_VAR";
    public final static String Memory = "Memory";
    public final static String CPU = "CPU";
    public final static String Harddisk = "Harddisk";
    public final static String Bandwidth = "Bandwidth";

    public final static float CPU_Max_Speed_VM0 = 4.0f;
    public final static float CPU_Max_Speed_VM1 = 4.0f;
    public final static float CPU_Mini_Speed_VM0 = 1.0f;
    public final static float CPU_Mini_Speed_VM1 = 1.0f;
    public final static double CPU_Core_Max_Nr_VM0 = 16;
    public final static double CPU_Core_Max_Nr_VM1 = 16;
    public final static double CPU_Core_Mini_Nr_VM0 = 1;
    public final static double CPU_Core_Mini_Nr_VM1 = 1;
    public final static double Memory_Max_Size = 2048;
    public final static double Memory_Mini_Size = 128;
    // the price is the basic price of 128M
    public final static double Memory_Basic_Price = 2;
    // the price is the basic price of 1G
    public final static double Harddisk_Basic_Price = 2;
    // the price is the basic price of 1mb/s
    public final static double Bandwidth_Basic_Price = 2;
    // the price is the basic price of 1GHz
    public final static double CPU_Basic_Price = 50;
    // the increased price per 0.1GHz
    public final static double CPU_Price_Increasement = 5;

    /*
     * General Config
     */
    public final static double maxAvailability = 99;
    public final static double basicAvailability = 95;
    public final static double innerProfitRate = 0.3;
    public final static double externalProfitRate = 0.05;
    public final static double maxFailureRate = 0.15;
    public final static double minimalH = 0.01;
    public final static double maxBeta = 0.2;

    // Main Provider
    public final static String Main_Provider = "Main_Provider";

    // Client Type , we assume the main provider is a gold customer to its sub-providers
    public final static String Client_Type_Gold = "gold";
    public final static String Client_Type_Silver = "silver";
    public final static String Client_Type_Bronze = "bronze";

}
