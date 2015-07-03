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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/tests/overhead/extrae_get_caller1.c $
 | @last_commit: $Date: 2013-07-17 18:39:40 +0200 (mié, 17 jul 2013) $
 | @version:     $Revision: 1944 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
#include <stdio.h>
#include <time.h>

#include "extrae_user_events.h"

extern void * get_caller (int);

int main(int argc, char **argv)
{
	unsigned long long t1, t2;
	struct timespec start, stop;
	int n = 1000000, i;
	Extrae_init();
	clock_gettime (CLOCK_MONOTONIC, &start);
	for (i = 0; i < n; i++)
		get_caller (2);
	clock_gettime (CLOCK_MONOTONIC, &stop);
	t1 = start.tv_nsec;
	t1 += start.tv_sec * 1000000000;
	t2 = stop.tv_nsec;
	t2 += stop.tv_sec * 1000000000;
	printf ("RESULT : Extrae_eventandcounters() %Lu ns\n", (t2 - t1) / n);
	Extrae_fini();
}
