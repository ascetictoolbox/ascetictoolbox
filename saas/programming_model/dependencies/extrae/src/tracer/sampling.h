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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/tracer/sampling.h $
 | @last_commit: $Date: 2014-05-28 13:01:35 +0200 (mié, 28 may 2014) $
 | @version:     $Revision: 2706 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef _SAMPLING_H_INCLUDED_
#define _SAMPLING_H_INCLUDED_

enum
{
	SAMPLING_TIMING_REAL,
	SAMPLING_TIMING_VIRTUAL,
	SAMPLING_TIMING_PROF,
	SAMPLING_TIMING_DEFAULT = SAMPLING_TIMING_REAL
};

void setTimeSampling (unsigned long long period, unsigned long long variability, int sampling_type);
void setTimeSampling_postfork (void);
void Extrae_SamplingHandler (void* address);
void Extrae_SamplingHandler_PAPI (void *address);

int isSamplingEnabled(void);
void setSamplingEnabled (int enabled);
void unsetTimeSampling (void);

#endif
