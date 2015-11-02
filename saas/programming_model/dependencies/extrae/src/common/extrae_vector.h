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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/common/extrae_vector.h $
 | @last_commit: $Date: 2012-09-28 12:05:25 +0200 (vie, 28 sep 2012) $
 | @version:     $Revision: 1205 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef _EXTRAE_VECTOR_H_

#define _EXTRAE_VECTOR_H_

typedef struct
{
	void **data;
	unsigned count;
	unsigned allocated;
} Extrae_Vector_t;

void Extrae_Vector_Init (Extrae_Vector_t *v);
void Extrae_Vector_Destroy (Extrae_Vector_t *v);
void Extrae_Vector_Append (Extrae_Vector_t *v, void *element);
unsigned Extrae_Vector_Count (Extrae_Vector_t *v);
void * Extrae_Vector_Get (Extrae_Vector_t *v, unsigned position);

#endif /* _EXTRAE_VECTOR_H_ */

