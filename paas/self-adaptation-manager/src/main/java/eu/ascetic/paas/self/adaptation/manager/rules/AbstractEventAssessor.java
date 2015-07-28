/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.rules;

import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.EventListener;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.EventData;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The abstract class event assessor provides the generic routines for
 * assessment of adaptation events. These events are assessed by concrete
 * implementations of this class. Adaption is then implemented based upon the
 * outcome of rules associated with the assessor.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractEventAssessor implements EventAssessor {

    private ArrayList<EventListener> listeners = new ArrayList<>();
    private ArrayList<ActuatorInvoker> actuators = new ArrayList<>();
    private List<EventData> sequence;
    private List<Response> adaptations;
    //duration a history item can stay alive
    private final int historyLengthSeconds = (int) TimeUnit.MINUTES.toSeconds(5);
    //The rate at how often history items are checked to be still in date
    private final int pollInterval = 5;
    private Thread historyClearerThread = null;
    private HistoryClearer historyClearer = null;

    /**
     * This launches a new event assessor.
     */
    public AbstractEventAssessor() {
    }

    @Override
    public Response assessEvent(EventData event) {
        //Add the current event into the sequence of all events.
        sequence.add(event);
        //filter event sequence for only relevent data    
        List<EventData> eventData = EventDataAggregator.filterEventData(sequence, event.getSlaUuid(), event.getGuaranteeid());
        //Purge old event map data
        eventData = EventDataAggregator.filterEventDataByTime(eventData, historyLengthSeconds);
        Response answer = assessEvent(event, eventData, adaptations);
        if (answer != null) {
            adaptations.add(answer);
            //TODO Execute the response here
        }
        return answer;
    }

    /**
     * This allows the ability to record adaptations that haven't been performed
     * by this event assessor. It thus prevents the event assessor overturning a
     * change made by another soon after the change has occurred.
     *
     * @param response The response to add into the modeller's history.
     */
    @Override
    public void addRemoteAdaptationEvent(Response response) {
        adaptations.add(response);
        Collections.sort(adaptations);
    }

    /**
     * This gets the event assessors internal list of event listeners
     *
     * @return the list of event listeners the event assessor uses
     */
    @Override
    public ArrayList<EventListener> getListeners() {
        return listeners;
    }

    /**
     * This sets the event assessors internal list of event listeners
     *
     * @param listeners the listeners to set
     */
    @Override
    public void setListeners(ArrayList<EventListener> listeners) {
        this.listeners = listeners;
        for (EventListener listener : listeners) {
            listener.setEventAssessor(this);
            if (listener instanceof Runnable) {
                Thread thread = new Thread((Runnable) listener);
                thread.start();
            }
        }
    }

    /**
     * This adds a listener to the event assessors internal list of event
     * listeners
     *
     * @param listener The listener to add
     */
    @Override
    public void addListeners(EventListener listener) {
        listeners.add(listener);
        listener.setEventAssessor(this);
        if (listener instanceof Runnable) {
            Thread thread = new Thread((Runnable) listener);
            thread.start();
        }
    }

    /**
     * This clears the event assessors internal list of event listeners
     */
    @Override
    public void clearListeners() {
        for (EventListener listener : listeners) {
            listener.stopListening();
        }
        listeners.clear();
    }

    /**
     * This gets the event assessors internal list of actuators
     *
     * @return the list of actuators the event assessor uses
     */
    @Override
    public ArrayList<ActuatorInvoker> getActuators() {
        return actuators;
    }

    /**
     * This sets the event assessors internal list of actuators
     *
     * @param actuators the actuators to set
     */
    @Override
    public void setActuators(ArrayList<ActuatorInvoker> actuators) {
        this.actuators = actuators;
    }

    /**
     * This adds an actuator to the event assessors internal list of actuators
     *
     * @param actuator The actuator to add
     */
    @Override
    public void addActuators(ActuatorInvoker actuator) {
        actuators.add(actuator);
    }

    /**
     * This clears the event assessors internal list of actuators
     */
    @Override
    public void clearActuators() {
        actuators.clear();
    }

    /**
     * This starts the event history maintenance routines in the event assessor.
     */
    @Override
    public void start() {
        historyClearer = new HistoryClearer();
        historyClearerThread = new Thread(historyClearer);
        historyClearerThread.setDaemon(true);
        historyClearerThread.start();
    }

    /**
     * This stops the event history maintenance routines in the event assessor.
     */
    @Override
    public void stop() {
        if (historyClearer != null) {
            historyClearer.stop();
            historyClearer = null;
            historyClearerThread = null;
        }
    }

    /**
     * The history clearer prunes the sequence of events of old redundant data.
     */
    private class HistoryClearer implements Runnable {

        private boolean running = true;

        /**
         * This makes the history clearer go through the historic event list and
         * prune values as required.
         */
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while (running) {
                if (!sequence.isEmpty()) {
                    sequence = EventDataAggregator.filterEventDataByTime(sequence, historyLengthSeconds);
                    adaptations = filterAdaptationHistory();
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(pollInterval));
                } catch (InterruptedException ex) {
                    Logger.getLogger(HistoryClearer.class.getName()).log(Level.WARNING, "History Cleaner: InterruptedException", ex);
                }
            } //While not stopped
        }

        /**
         * This filters the current adaptation history and return a new list
         * that has all of the old entries removed.
         *
         * @return The list of recent adaptations made by the event assessor.
         */
        private List<Response> filterAdaptationHistory() {
            ArrayList<Response> answer = new ArrayList<>();
            long now = System.currentTimeMillis();
            now = now / 1000;
            long filterTime = now - historyLengthSeconds;
            for (Response response : adaptations) {
                if (response.getTime() >= filterTime) {
                    answer.add(response);
                }
            }
            return answer;
        }

        /**
         * This stops the history clearer thread.
         */
        private void stop() {
            running = false;
        }
    }
}
