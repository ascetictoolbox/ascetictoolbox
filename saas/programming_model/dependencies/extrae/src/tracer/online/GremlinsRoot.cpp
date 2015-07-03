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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/tracer/online/GremlinsRoot.cpp $
 | @last_commit: $Date: 2014-01-31 14:13:36 +0100 (vie, 31 ene 2014) $
 | @version:     $Revision: 2459 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#include "common.h"

static char UNUSED rcsid[] = "$Id: GremlinsRoot.cpp 2459 2014-01-31 13:13:36Z gllort $";

#include "GremlinsRoot.h"

GremlinsRoot::GremlinsRoot()
{

}

/**
 * Register the stream that will perform the signal reduction 
 */
void GremlinsRoot::Setup()
{
  /* stGremlins uses the filter OnlineGremlins */
  stGremlins = Register_Stream(TFILTER_NULL, SFILTER_WAITFORALL);
}

/**
 * Front-end side of the Gremlins analysis.
 *
 * @return 1 if the analysis target is achieved; 0 otherwise.
 */
int GremlinsRoot::Run()
{
  return 0;
}

