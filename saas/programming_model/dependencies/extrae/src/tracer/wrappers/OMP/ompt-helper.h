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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/branches/2.5/src/tracer/wrappers/OMP/omp_wrapper.c $
 | @last_commit: $Date: 2014-02-20 16:48:43 +0100 (jue, 20 feb 2014) $
 | @version:     $Revision: 2487 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef OMPT_HELPER_H_INCLUDED
#define OMPT_HELPER_H_INCLUDED

#include "common.h"

#include "ompt-helper-defs.h"

void Extrae_OMPT_register_ompt_parallel_id_pf (ompt_parallel_id_t ompt_pid, void *pf);
void Extrae_OMPT_unregister_ompt_parallel_id_pf (ompt_parallel_id_t ompt_pid);
void * Extrae_OMPT_get_pf_parallel_id (ompt_parallel_id_t ompt_pid);

void Extrae_OMPT_register_ompt_task_id_pf (ompt_task_id_t ompt_tid, void *pf);
void Extrae_OMPT_unregister_ompt_task_id_pf (ompt_task_id_t ompt_tid);
void * Extrae_OMPT_get_pf_task_id (ompt_task_id_t ompt_tid);

void Extrae_OMPT_register_ompt_task_id_tf (ompt_task_id_t ompt_tid, void *tf, int implicit);
void Extrae_OMPT_unregister_ompt_task_id_tf (ompt_task_id_t ompt_tid);
void * Extrae_OMPT_get_tf_task_id (ompt_task_id_t ompt_tid,
	int *is_implicit, long long *taskctr);

#endif /* OMPT_HELPER_H_INCLUDED */

