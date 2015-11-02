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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/include/spu_trace.h $
 | @last_commit: $Date: 2011-06-29 10:44:44 +0200 (mié, 29 jun 2011) $
 | @version:     $Revision: 687 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef SPU_INCLUDED_H
#define SPU_INCLUDED_H

#include "extrae_version.h"

#ifdef __cplusplus
extern "C" {
#endif

int SPUtrace_init (void);
int Extrae_init (void);

void SPUtrace_fini (void);
void Extrae_fini (void);

void SPUtrace_event (unsigned int type, unsigned int value);
void Extrae_event (unsigned int type, unsigned int value);

void SPUtrace_Nevent(int count, unsigned int *tipus, unsigned int *valors);
void Extrae_Nevent(int count, unsigned int *tipus, unsigned int *valors);

void SPUtrace_shutdown (void);
void Extrae_shutdown (void);

void SPUtrace_restart (void);
void Extrae_restart (void);

#ifdef __cplusplus
}
#endif

#endif /* PPU_INCLUDED_H */
