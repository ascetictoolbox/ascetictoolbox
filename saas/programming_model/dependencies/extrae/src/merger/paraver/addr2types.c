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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/merger/paraver/addr2types.c $
 | @last_commit: $Date: 2015-04-23 17:12:21 +0200 (jue, 23 abr 2015) $
 | @version:     $Revision: 3269 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
#include "common.h"

static char UNUSED rcsid[] = "$Id: addr2types.c 3269 2015-04-23 15:12:21Z harald $";

#ifdef HAVE_STDLIB_H
# include <stdlib.h>
#endif
#ifdef HAVE_STDIO_H
# include <stdio.h>
#endif

#include "addr2types.h"

Extrae_Addr2Type_t * Extrae_Addr2Type_New (int FunctionType,
	unsigned FunctionType_lbl, int LineType, unsigned LineType_lbl)
{
	Extrae_Addr2Type_t *r = (Extrae_Addr2Type_t*) malloc (sizeof(Extrae_Addr2Type_t));

	if (r == NULL)
	{
		fprintf (stderr, "Extrae (%s,%d): Fatal error! Cannot allocate memory for Extrae_Addr2Type_New\n", __FILE__, __LINE__);
		exit (-1);
	}

	r->FunctionType = FunctionType;
	r->FunctionType_lbl = FunctionType_lbl;
	r->LineType = LineType;
	r->LineType_lbl = LineType_lbl;

	return r;	
}

