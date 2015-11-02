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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/branches/2.3/src/tracer/probes/CUDA/cuda_probe.h $
 | @last_commit: $Date: 2011-10-17 16:29:40 +0200 (dl, 17 oct 2011) $
 | @version:     $Revision: 785 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

#ifndef OPENCL_PROBE_H_INCLUDED
#define OPENCL_PROBE_H_INCLUDED

void Extrae_Probe_clCreateBuffer_Enter (void);
void Extrae_Probe_clCreateBuffer_Exit (void);

void Extrae_Probe_clCreateCommandQueue_Enter (void);
void Extrae_Probe_clCreateCommandQueue_Exit (void);

void Extrae_Probe_clCreateContext_Enter (void);
void Extrae_Probe_clCreateContext_Exit (void);

void Extrae_Probe_clCreateContextFromType_Enter (void);
void Extrae_Probe_clCreateContextFromType_Exit (void);

void Extrae_Probe_clCreateSubBuffer_Enter (void);
void Extrae_Probe_clCreateSubBuffer_Exit (void);

void Extrae_Probe_clCreateKernel_Enter (void);
void Extrae_Probe_clCreateKernel_Exit (void);

void Extrae_Probe_clCreateKernelsInProgram_Enter (void);
void Extrae_Probe_clCreateKernelsInProgram_Exit (void);

void Extrae_Probe_clSetKernelArg_Enter (void);
void Extrae_Probe_clSetKernelArg_Exit (void);

void Extrae_Probe_clCreateProgramWithSource_Enter (void);
void Extrae_Probe_clCreateProgramWithSource_Exit (void);

void Extrae_Probe_clCreateProgramWithBinary_Enter (void);
void Extrae_Probe_clCreateProgramWithBinary_Exit (void);

void Extrae_Probe_clCreateProgramWithBuiltInKernels_Enter (void);
void Extrae_Probe_clCreateProgramWithBuiltInKernels_Exit (void);

void Extrae_Probe_clEnqueueFillBuffer_Enter (void);
void Extrae_Probe_clEnqueueFillBuffer_Exit (void);

void Extrae_Probe_clEnqueueCopyBuffer_Enter (void);
void Extrae_Probe_clEnqueueCopyBuffer_Exit (void);

void Extrae_Probe_clEnqueueCopyBufferRect_Enter (void);
void Extrae_Probe_clEnqueueCopyBufferRect_Exit (void);

void Extrae_Probe_clEnqueueNDRangeKernel_Enter (unsigned long long KID);
void Extrae_Probe_clEnqueueNDRangeKernel_Exit (void);

void Extrae_Probe_clEnqueueTask_Enter (unsigned long long KID);
void Extrae_Probe_clEnqueueTask_Exit (void);

void Extrae_Probe_clEnqueueNativeKernel_Enter (void);
void Extrae_Probe_clEnqueueNativeKernel_Exit (void);

void Extrae_Probe_clEnqueueReadBuffer_Enter (int sync, size_t size);
void Extrae_Probe_clEnqueueReadBuffer_Exit (int sync);

void Extrae_Probe_clEnqueueReadBufferRect_Enter (int sync);
void Extrae_Probe_clEnqueueReadBufferRect_Exit (int sync);

void Extrae_Probe_clEnqueueWriteBuffer_Enter (int sync, size_t size);
void Extrae_Probe_clEnqueueWriteBuffer_Exit (int sync);

void Extrae_Probe_clEnqueueWriteBufferRect_Enter (int sync);
void Extrae_Probe_clEnqueueWriteBufferRect_Exit (int sync);

void Extrae_Probe_clBuildProgram_Enter (void);
void Extrae_Probe_clBuildProgram_Exit (void);

void Extrae_Probe_clCompileProgram_Enter (void);
void Extrae_Probe_clCompileProgram_Exit (void);

void Extrae_Probe_clLinkProgram_Enter (void);
void Extrae_Probe_clLinkProgram_Exit (void);

void Extrae_Probe_clFinish_Enter (void);
void Extrae_Probe_clFinish_Exit (void);

void Extrae_Probe_clFlush_Enter (void);
void Extrae_Probe_clFlush_Exit (void);

void Extrae_Probe_clWaitForEvents_Enter (void);
void Extrae_Probe_clWaitForEvents_Exit (void);

void Extrae_Probe_clEnqueueMarkerWithWaitList_Enter (void);
void Extrae_Probe_clEnqueueMarkerWithWaitList_Exit (void);

void Extrae_Probe_clEnqueueBarrierWithWaitList_Enter (void);
void Extrae_Probe_clEnqueueBarrierWithWaitList_Exit (void);

void Extrae_Probe_clEnqueueMarker_Enter (void);
void Extrae_Probe_clEnqueueMarker_Exit (void);

void Extrae_Probe_clEnqueueBarrier_Enter (void);
void Extrae_Probe_clEnqueueBarrier_Exit (void);

void Extrae_Probe_clEnqueueUnmapMemObject_Enter (void);
void Extrae_Probe_clEnqueueUnmapMemObject_Exit(void);

void Extrae_Probe_clEnqueueMapBuffer_Enter (void);
void Extrae_Probe_clEnqueueMapBuffer_Exit (void);

void Extrae_Probe_clEnqueueMigrateMemObjects_Enter (void);
void Extrae_Probe_clEnqueueMigrateMemObjects_Exit (void);

void Extrae_Probe_clRetainCommandQueue_Enter (void);
void Extrae_Probe_clRetainCommandQueue_Exit (void);

void Extrae_Probe_clReleaseCommandQueue_Enter (void);
void Extrae_Probe_clReleaseCommandQueue_Exit (void);

void Extrae_Probe_clRetainContext_Enter (void);
void Extrae_Probe_clRetainContext_Exit (void);

void Extrae_Probe_clReleaseContext_Enter (void);
void Extrae_Probe_clReleaseContext_Exit (void);

void Extrae_Probe_clRetainDevice_Enter (void);
void Extrae_Probe_clRetainDevice_Exit (void);

void Extrae_Probe_clReleaseDevice_Enter (void);
void Extrae_Probe_clReleaseDevice_Exit (void);

void Extrae_Probe_clRetainEvent_Enter (void);
void Extrae_Probe_clRetainEvent_Exit (void);

void Extrae_Probe_clReleaseEvent_Enter (void);
void Extrae_Probe_clReleaseEvent_Exit (void);

void Extrae_Probe_clRetainKernel_Enter (void);
void Extrae_Probe_clRetainKernel_Exit (void);

void Extrae_Probe_clReleaseKernel_Enter (void);
void Extrae_Probe_clReleaseKernel_Exit (void);

void Extrae_Probe_clRetainMemObject_Enter (void);
void Extrae_Probe_clRetainMemObject_Exit (void);

void Extrae_Probe_clReleaseMemObject_Enter (void);
void Extrae_Probe_clReleaseMemObject_Exit (void);

void Extrae_Probe_clRetainProgram_Enter (void);
void Extrae_Probe_clRetainProgram_Exit (void);

void Extrae_Probe_clReleaseProgram_Enter (void);
void Extrae_Probe_clReleaseProgram_Exit (void);

void Extrae_set_trace_OpenCL (int b);
int Extrae_get_trace_OpenCL (void);

#endif /* OPENCL_PROBE_H_INCLUDED */
