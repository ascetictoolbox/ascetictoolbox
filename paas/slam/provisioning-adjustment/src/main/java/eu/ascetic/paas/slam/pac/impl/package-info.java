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


/**
 * Copyright (c) 2008-2010, SLASOI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SLASOI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SLASOI BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author         Beatriz Fuentes - fuentes@tid.es
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

/**
 * This package declares the interfaces to be defined in a module implementing
 * the Provisioning and Adjustment functionality.
 * The ProvisioningAndAdjustment (PAC) component is responsible for executing the plans
 * supplied byPlanningAndOptimisation (<<plan>>).

 * At the SLA Management level, the "execution" of plans is effected by posting "task
 * requests" to the ServiceManager (<<manage_<T>_service>> LINK?), where requests cover:
 *         o provisioning & decommissioning service-instances,
 *         o provisioning & decommissioning of the monitoring infrastructure,
 *         o any adjustments to the above,
 *         o (where relevant) 'pause' and 'resume' of previously requested tasks.
 * The PAC subscribes to the MonitoredEventChannel (<<subscribe_to_event>>) in order to receive notifications of:
 *         o monitored service-instance events (posted by ManageabilityAgents),
 *         o notifications of changes to task status & any other significant events
 *         (e.g. the monitoring system going down) posted by the ServiceManager.
 * The PAC can also query the ServiceManager directly for task status information (<<manage_<T>_service>> LINK?).
 * Based on received event notifications, the PAC determines the current status of
 * 'plan execution', and reports this status back to PlanningAndOptimisation (<<plan>>).
 * If required, the PAC also has <<query>> access to all GenericSLAManager registries.
 * The PAC is abstract and must be implemented by domain/layer specific plug-ins.

 */
package eu.ascetic.paas.slam.pac.impl;

