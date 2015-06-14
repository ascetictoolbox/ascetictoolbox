/*  Copyright 2015 Athens University of Economics and Business
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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

public class Time {
	long time;
	double hour;
	 
	 public Time() {
	        this.time = System.currentTimeMillis();
	    //    this.startTime.setTimeInMillis(TimeUnit.SECONDS.toMillis(startTime));
	 }
	 
	// public Time(long hours) {
	//	 this.time = TimeUnit.HOURS.toMillis(hours);
	// }
	 
	 public long getTime(Time T){
		 return T.time;
	 }
	 
	 public long getTime(){
		// System.out.println((time / 1000 / 60 / 60) % 24 + ":" + (time / 1000 / 60) % 60 + ":" + (time / 1000) % 60);
		// System.out.println(this.time);
		 return this.time;
	 }
	 
	 public long difTime(Time oldT){
		 long dif = this.time-oldT.time;
		// System.out.println(time);
		// System.out.println(oldT.getTime());
		// System.out.println(dif);
		// System.out.println(TimeUnit.MILLISECONDS.toHours(dif));
		 return dif;
	 }

	
}