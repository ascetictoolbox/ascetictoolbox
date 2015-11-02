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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/merger/common/addresses.h $
 | @last_commit: $Date: 2013-09-06 14:48:20 +0200 (vie, 06 sep 2013) $
 | @version:     $Revision: 2099 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef _ADDRESSES_H_
#define _ADDRESSES_H_

struct address_collector_t
{
	UINT64 *addresses;
	int *types;
	unsigned *ptasks;
	unsigned *tasks;
	unsigned count;
	unsigned allocated;
};

void AddressCollector_Initialize (struct address_collector_t *ac);
void AddressCollector_Add (struct address_collector_t *ac, unsigned ptask,
	unsigned task, UINT64 address, int type);
unsigned AddressCollector_Count (struct address_collector_t *ac);
UINT64* AddressCollector_GetAllAddresses (struct address_collector_t *ac);
int* AddressCollector_GetAllTypes (struct address_collector_t *ac);
unsigned* AddressCollector_GetAllPtasks (struct address_collector_t *ac);
unsigned* AddressCollector_GetAllTasks (struct address_collector_t *ac);

#if defined(PARALLEL_MERGE)
void AddressCollector_GatherAddresses (int numtasks, int taskid, struct address_collector_t *ac);
#endif
#endif

