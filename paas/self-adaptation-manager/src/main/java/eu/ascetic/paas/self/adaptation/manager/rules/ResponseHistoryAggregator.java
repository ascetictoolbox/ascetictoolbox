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

import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This takes response history data and performs functions on it, with the aim
 * of determining, if the recent adaptation should stop a future call to adapt.
 * i.e. this prevents many adaptations from happening for the same reason in
 * close proximity to one another.
 *
 * @author Richard Kavanagh
 */
public class ResponseHistoryAggregator {

    /**
     * This takes a list of responses and provides a list of responses for a
     * single guarantee of a named SLA.
     *
     * @param responses The list of responses records to filter
     * @param slaUuid The SLA identifier to filter against
     * @param agreementTerm The agreement term to filter against.
     * @param onlyActionable This filters out any responses that were not
     * possible to perform.
     * @return The list of responses associated with a given guarantee of an
     * SLA, in ascending chronological order. i.e. earliest first.
     */
    public static List<Response> filterResponseHistory(List<Response> responses,
            String slaUuid, String agreementTerm, boolean onlyActionable) {
        ArrayList<Response> answer = new ArrayList<>();
        for (Response response : responses) {
            if (response.getCause().getSlaUuid().equals(slaUuid)
                    && response.getCause().getAgreementTerm().equals(agreementTerm)) {
                if (onlyActionable == true && response.isPossibleToAdapt() == false) {
                    continue;
                }
                answer.add(response);
            }
        }
        Collections.sort(answer);
        return answer;
    }

    /**
     * This takes a list of responses and provides a list of responses for a
     * single guarantee of a named SLA.
     *
     * @param responses The list of responses records to filter
     * @param slaUuid The SLA identifier to filter against
     * @param agreementTerm The agreement term to filter against.
     * @return The list of responses associated with a given guarantee of an
     * SLA, in ascending chronological order. i.e. earliest first.
     */
    public static List<Response> filterResponseHistory(List<Response> responses,
            String slaUuid, String agreementTerm) {
        return filterResponseHistory(responses, slaUuid, agreementTerm, false);
    }

    /**
     * This takes a list of responses and provides a list of responses for a
     * single named SLA.
     *
     * @param responses The list of responses records to filter
     * @param slaUuid The SLA identifier to filter against
     * @return The list of responses associated with a given SLA,in ascending
     * chronological order. i.e. earliest first.
     */
    public static List<Response> filterResponseHistory(List<Response> responses,
            String slaUuid) {
        ArrayList<Response> answer = new ArrayList<>();
        for (Response response : responses) {
            if (response.getCause().getSlaUuid().equals(slaUuid)) {
                answer.add(response);
            }
        }
        Collections.sort(answer);
        return answer;
    }

    /**
     * This takes a list of responses and provides a list of responses for a
     * single named type of adaptation.
     *
     * @param responses The list of responses records to filter
     * @param adaptationForm The form of adaptation to filter against
     * @return The list of responses associated with a given type of adaptation
     * in ascending chronological order. i.e. earliest first.
     */
    public static List<Response> filterResponseHistory(List<Response> responses,
            Response.AdaptationType adaptationForm) {
        ArrayList<Response> answer = new ArrayList<>();
        for (Response response : responses) {
            if (response.getActionType().equals(adaptationForm)) {
                answer.add(response);
            }
        }
        Collections.sort(answer);
        return answer;
    }

    /**
     * This indicates if all actions in the response list were inactionable.
     *
     * @param responses
     * @return true only if all actions were inactionable. A zero sized list is
     * false.
     */
    public static boolean responseHistoryWasUnactionable(List<Response> responses) {
        if (responses.isEmpty()) {
            return false;
        }
        for (Response response : responses) {
            if (response.isPossibleToAdapt()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This takes a list of responses and filters out old data.
     *
     * @param responses The list of responses to filter
     * @param ageSeconds The time in seconds to allow data entry points for
     * @return The list of responses in ascending chronological order. i.e.
     * earliest first, that meet the time criteria.
     */
    public static List<Response> filterResponseHistoryByTime(List<Response> responses, int ageSeconds) {
        ArrayList<Response> answer = new ArrayList<>();
        long now = System.currentTimeMillis();
        now = now / 1000;
        long filterTime = now - ageSeconds;
        for (Response response : responses) {
            if (response.getTime() >= filterTime) {
                answer.add(response);
            }
        }
        Collections.sort(answer);
        return answer;
    }

}
