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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/tracer/online/GremlinsWorker.h $
 | @last_commit: $Date: 2014-01-31 14:13:36 +0100 (vie, 31 ene 2014) $
 | @version:     $Revision: 2459 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef __GREMLINS_WORKER_H__
#define __GREMLINS_WORKER_H__

#include "BackProtocol.h"

class GremlinsWorker : public BackProtocol
{
  public:
    string ID (void) { return "GREMLINS"; } /* ID matches the front-end protocol */
    void Setup(void);
    int  Run  (void);

  private:
    STREAM *stGremlins;
    int NumberOfGremlins;
    int MinGremlins;
    int MaxGremlins;
    int Sweeps;
    int LastSweep;
    int Roundtrip;
    int TargetGremlins;

    void SwitchSome(int GremlinsToChange);
    void SetInitialConditions(void);
};

#endif /* __GREMLINS_WORKER_H__ */
