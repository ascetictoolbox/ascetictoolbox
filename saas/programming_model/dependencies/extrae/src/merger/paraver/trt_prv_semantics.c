/*****************************************************************************\
 *                        ANALYSIS PERFORMANCE TOOLS                         *
 *                                   Extrae                                  *
 *              Instrumentation package for parallel applications            *
 *****************************************************************************
 *     ___     This library is free software; you can redistribute it and/or *
 *    /  __         modify it under the terms of the GNU LGPL as published   *
 *   /  /  _____    by the Free Software Foundation; either version 2.1      *
 *  /  /  /     \   of the License, or (at your option) any later version.   *
 * (  (  ( B S C )                                                           *
 *  \  \  \_____/   This library is distributed in hope that it will be      *
 *   \  \__         useful but WITHOUT ANY WARRANTY; without even the        *
 *    \___          implied warranty of MERCHANTABILITY or FITNESS FOR A     *
 *                  PARTICULAR PURPOSE. See the GNU LGPL for more details.   *
 *                                                                           *
 * You should have received a copy of the GNU Lesser General Public License  *
 * along with this library; if not, write to the Free Software Foundation,   *
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA          *
 * The GNU LEsser General Public License is contained in the file COPYING.   *
 *                                 ---------                                 *
 *   Barcelona Supercomputing Center - Centro Nacional de Supercomputacion   *
\*****************************************************************************/

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- *\
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/merger/paraver/trt_prv_semantics.c $
 | @last_commit: $Date: 2013-09-06 14:48:20 +0200 (vie, 06 sep 2013) $
 | @version:     $Revision: 2099 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
#include "common.h"

static char UNUSED rcsid[] = "$Id: trt_prv_semantics.c 2099 2013-09-06 12:48:20Z harald $";

#ifdef HAVE_STDLIB_H
# include <stdlib.h>
#endif
#ifdef HAVE_STDIO_H
# include <stdio.h>
#endif

#include "file_set.h"
#include "object_tree.h"
#include "trace_to_prv.h"
#include "trt_prv_events.h"
#include "semantics.h"
#include "paraver_state.h"
#include "paraver_generator.h"
#include "addresses.h"
#include "options.h"

#if USE_HARDWARE_COUNTERS
# include "HardwareCounters.h"
#endif

#ifdef HAVE_BFD
# include "addr2info.h" 
#endif

#include "record.h"
#include "events.h"

/******************************************************************************
 ***  WorkSharing_Event
 ******************************************************************************/

static int TRT_Call (event_t * current_event, unsigned long long current_time,
	unsigned int cpu, unsigned int ptask, unsigned int task,
	unsigned int thread, FileSet_t *fset )
{
	unsigned int EvType, EvValue;
	UNREFERENCED_PARAMETER(fset);

	EvType  = Get_EvEvent (current_event);
	EvValue = Get_EvValue (current_event);

	Switch_State (STATE_OVHD, (EvValue != EVT_END), ptask, task, thread);

	trace_paraver_state (cpu, ptask, task, thread, current_time);
	trace_paraver_event (cpu, ptask, task, thread, current_time, EvType, EvValue);

	return 0;
}

static int TRT_Function_Event (event_t * current_event, 
	unsigned long long current_time, unsigned int cpu, unsigned int ptask,
	unsigned int task, unsigned int thread, FileSet_t *fset )
{
	unsigned int EvValue;
	UNREFERENCED_PARAMETER(fset);

	EvValue = Get_EvValue (current_event);

	Switch_State (STATE_RUNNING, (EvValue != EVT_END), ptask, task, thread);

#if defined(HAVE_BFD)
	if (get_option_merge_SortAddresses())
	{
		AddressCollector_Add (&CollectedAddresses, ptask, task, EvValue, ADDR2OMP_FUNCTION);
		AddressCollector_Add (&CollectedAddresses, ptask, task, EvValue, ADDR2OMP_LINE);
	}
#endif

	trace_paraver_state (cpu, ptask, task, thread, current_time);
	trace_paraver_event (cpu, ptask, task, thread, current_time, PTHREAD_FUNC_EV, EvValue);
	trace_paraver_event (cpu, ptask, task, thread, current_time, PTHREAD_FUNC_LINE_EV, EvValue);

	return 0;
}

SingleEv_Handler_t PRV_TRT_Event_Handlers[] = {
	{ TRT_SPAWN_EV, TRT_Call },
	{ TRT_READ_EV, TRT_Call },
	{ TRT_USRFUNC_EV, TRT_Function_Event },
	{ NULL_EV, NULL }
};

