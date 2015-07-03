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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/src/merger/paraver/mpi_prv_events.c $
 | @last_commit: $Date: 2015-02-23 12:32:16 +0100 (lun, 23 feb 2015) $
 | @version:     $Revision: 3120 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
#include "common.h"

static char UNUSED rcsid[] = "$Id: mpi_prv_events.c 3120 2015-02-23 11:32:16Z harald $";

#ifdef HAVE_STDLIB_H
# include <stdlib.h>
#endif

#include "mpi_prv_events.h"
#include "events.h"
#include "labels.h"

struct t_event_mpit2prv
{
  int tipus_mpit;
  int tipus_prv;
  int valor_prv;
  int utilitzada;               /* Boolea que indica si apareix a la trac,a */
};

struct t_prv_type_info
{
  int type;
  char *label;
  int flag_color;
};

struct t_prv_val_label
{
  int value;
  char *label;
};

#define NUM_MPI_PRV_ELEMENTS 153    /* 127 */

static struct t_event_mpit2prv event_mpit2prv[NUM_MPI_PRV_ELEMENTS] = {
	{MPI_ALLGATHER_EV, MPITYPE_COLLECTIVE, MPI_ALLGATHER_VAL, FALSE}, /*   1 */
	{MPI_ALLGATHERV_EV, MPITYPE_COLLECTIVE, MPI_ALLGATHERV_VAL, FALSE},       /*   2 */
	{MPI_ALLREDUCE_EV, MPITYPE_COLLECTIVE, MPI_ALLREDUCE_VAL, FALSE}, /*   3 */
	{MPI_ALLTOALL_EV, MPITYPE_COLLECTIVE, MPI_ALLTOALL_VAL, FALSE},   /*   4 */
	{MPI_ALLTOALLV_EV, MPITYPE_COLLECTIVE, MPI_ALLTOALLV_VAL, FALSE}, /*   5 */
	{MPI_BARRIER_EV, MPITYPE_COLLECTIVE, MPI_BARRIER_VAL, FALSE},     /*   6 */
	{MPI_BCAST_EV, MPITYPE_COLLECTIVE, MPI_BCAST_VAL, FALSE}, /*   7 */
	{MPI_GATHER_EV, MPITYPE_COLLECTIVE, MPI_GATHER_VAL, FALSE},       /*   8 */
	{MPI_GATHERV_EV, MPITYPE_COLLECTIVE, MPI_GATHERV_VAL, FALSE},     /*   9 */
	{-1, MPITYPE_OTHER, MPI_OP_CREATE_VAL, FALSE},        /*  10 */
	{-1, MPITYPE_OTHER, MPI_OP_FREE_VAL, FALSE},  /*  11 */
	{MPI_REDUCESCAT_EV, MPITYPE_COLLECTIVE, MPI_REDUCE_SCATTER_VAL, FALSE},   /*  12 */
	{MPI_REDUCE_EV, MPITYPE_COLLECTIVE, MPI_REDUCE_VAL, FALSE},       /*  13 */
	{MPI_SCAN_EV, MPITYPE_COLLECTIVE, MPI_SCAN_VAL, FALSE},   /*  14 */
	{MPI_SCATTER_EV, MPITYPE_COLLECTIVE, MPI_SCATTER_VAL, FALSE},     /*  15 */
	{MPI_SCATTERV_EV, MPITYPE_COLLECTIVE, MPI_SCATTERV_VAL, FALSE},   /*  16 */
	{-1, MPITYPE_OTHER, MPI_ATTR_DELETE_VAL, FALSE},      /*  17 */
	{-1, MPITYPE_OTHER, MPI_ATTR_GET_VAL, FALSE}, /*  18 */
	{-1, MPITYPE_OTHER, MPI_ATTR_PUT_VAL, FALSE}, /*  19 */
	{MPI_COMM_CREATE_EV, MPITYPE_COMM, MPI_COMM_CREATE_VAL, FALSE},   /*  20 */
	{MPI_COMM_DUP_EV, MPITYPE_COMM, MPI_COMM_DUP_VAL, FALSE}, /*  21 */
	{MPI_COMM_FREE_EV, MPITYPE_COMM, MPI_COMM_FREE_VAL, FALSE}, /*  22 */
	{-1, MPITYPE_COMM, MPI_COMM_GROUP_VAL, FALSE},        /*  23 */
	{MPI_COMM_RANK_EV, MPITYPE_COMM, MPI_COMM_RANK_VAL, FALSE},       /*  24 */
	{-1, MPITYPE_COMM, MPI_COMM_REMOTE_GROUP_VAL, FALSE}, /*  25 */
	{-1, MPITYPE_COMM, MPI_COMM_REMOTE_SIZE_VAL, FALSE},  /*  26 */
	{MPI_COMM_SIZE_EV, MPITYPE_COMM, MPI_COMM_SIZE_VAL, FALSE},       /*  27 */
	{MPI_COMM_SPLIT_EV, MPITYPE_COMM, MPI_COMM_SPLIT_VAL, FALSE},     /*  28 */
	{-1, MPITYPE_COMM, MPI_COMM_TEST_INTER_VAL, FALSE},   /*  29 */
	{-1, MPITYPE_COMM, MPI_COMM_COMPARE_VAL, FALSE},      /*  30 */
	{-1, MPITYPE_GROUP, MPI_GROUP_DIFFERENCE_VAL, FALSE}, /*  31 */
	{-1, MPITYPE_GROUP, MPI_GROUP_EXCL_VAL, FALSE},       /*  32 */
	{-1, MPITYPE_GROUP, MPI_GROUP_FREE_VAL, FALSE},       /*  33 */
	{-1, MPITYPE_GROUP, MPI_GROUP_INCL_VAL, FALSE},       /*  34 */
	{-1, MPITYPE_GROUP, MPI_GROUP_INTERSECTION_VAL, FALSE},       /*  35 */
	{-1, MPITYPE_GROUP, MPI_GROUP_RANK_VAL, FALSE},       /*  36 */
	{-1, MPITYPE_GROUP, MPI_GROUP_RANGE_EXCL_VAL, FALSE}, /*  37 */
	{-1, MPITYPE_GROUP, MPI_GROUP_RANGE_INCL_VAL, FALSE}, /*  38 */
	{-1, MPITYPE_GROUP, MPI_GROUP_SIZE_VAL, FALSE},       /*  39 */
	{-1, MPITYPE_GROUP, MPI_GROUP_TRANSLATE_RANKS_VAL, FALSE},    /*  40 */
	{-1, MPITYPE_GROUP, MPI_GROUP_UNION_VAL, FALSE},      /*  41 */
	{-1, MPITYPE_GROUP, MPI_GROUP_COMPARE_VAL, FALSE},    /*  42 */
	{MPI_INTERCOMM_CREATE_EV, MPITYPE_COMM, MPI_INTERCOMM_CREATE_VAL, FALSE},  /*  43 */
	{MPI_INTERCOMM_CREATE_VAL, MPITYPE_COMM, MPI_INTERCOMM_MERGE_VAL, FALSE},   /*  44 */
	{-1, MPITYPE_OTHER, MPI_KEYVAL_FREE_VAL, FALSE},      /*  45 */
	{-1, MPITYPE_OTHER, MPI_KEYVAL_CREATE_VAL, FALSE},    /*  46 */
	{-1, MPITYPE_OTHER, MPI_ABORT_VAL, FALSE},    /*  47 */
	{-1, MPITYPE_OTHER, MPI_ERROR_CLASS_VAL, FALSE},      /*  48 */
	{-1, MPITYPE_OTHER, MPI_ERRHANDLER_CREATE_VAL, FALSE},        /*  49 */
	{-1, MPITYPE_OTHER, MPI_ERRHANDLER_FREE_VAL, FALSE},  /*  50 */
	{-1, MPITYPE_OTHER, MPI_ERRHANDLER_GET_VAL, FALSE},   /*  51 */
	{-1, MPITYPE_OTHER, MPI_ERROR_STRING_VAL, FALSE},     /*  52 */
	{-1, MPITYPE_OTHER, MPI_ERRHANDLER_SET_VAL, FALSE},   /*  53 */
	{MPI_FINALIZE_EV, MPITYPE_OTHER, MPI_FINALIZE_VAL, FALSE},        /*  54 */
	{-1, MPITYPE_OTHER, MPI_GET_PROCESSOR_NAME_VAL, FALSE},       /*  55 */
	{MPI_INIT_EV, MPITYPE_OTHER, MPI_INIT_VAL, FALSE},     /*  56 */
	{-1, MPITYPE_OTHER, MPI_INITIALIZED_VAL, FALSE},      /*  57 */
	{-1, MPITYPE_OTHER, MPI_WTICK_VAL, FALSE},    /*  58 */
	{-1, MPITYPE_OTHER, MPI_WTIME_VAL, FALSE},    /*  59 */
	{-1, MPITYPE_OTHER, MPI_ADDRESS_VAL, FALSE},  /*  60 */
	{MPI_BSEND_EV, MPITYPE_PTOP, MPI_BSEND_VAL, FALSE},       /*  61 */
	{MPI_BSEND_INIT_EV, MPITYPE_PTOP, MPI_BSEND_INIT_VAL, FALSE},     /*  62 */
	{-1, MPITYPE_OTHER, MPI_BUFFER_ATTACH_VAL, FALSE},    /*  63 */
	{-1, MPITYPE_OTHER, MPI_BUFFER_DETACH_VAL, FALSE},    /*  64 */
	{MPI_CANCEL_EV, MPITYPE_PTOP, MPI_CANCEL_VAL, FALSE},     /*  65 */
	{MPI_REQUEST_FREE_EV, MPITYPE_OTHER, MPI_REQUEST_FREE_VAL, FALSE},        /*  66 */
	{MPI_RECV_INIT_EV, MPITYPE_PTOP, MPI_RECV_INIT_VAL, FALSE},       /*  67 */
	{MPI_SEND_INIT_EV, MPITYPE_PTOP, MPI_SEND_INIT_VAL, FALSE},       /*  68 */
	{-1, MPITYPE_OTHER, MPI_GET_COUNT_VAL, FALSE},        /*  69 */
	{-1, MPITYPE_OTHER, MPI_GET_ELEMENTS_VAL, FALSE},     /*  70 */
	{MPI_IBSEND_EV, MPITYPE_PTOP, MPI_IBSEND_VAL, FALSE},     /*  71 */
	{MPI_IPROBE_EV, MPITYPE_PTOP, MPI_IPROBE_VAL, FALSE},     /*  72 */
	{MPI_IRECV_EV, MPITYPE_PTOP, MPI_IRECV_VAL, FALSE},       /*  73 */
	{MPI_IRSEND_EV, MPITYPE_PTOP, MPI_IRSEND_VAL, FALSE},     /*  74 */
	{MPI_ISEND_EV, MPITYPE_PTOP, MPI_ISEND_VAL, FALSE},       /*  75 */
	{MPI_ISSEND_EV, MPITYPE_PTOP, MPI_ISSEND_VAL, FALSE},     /*  76 */
	{-1, MPITYPE_OTHER, MPI_PACK_VAL, FALSE},     /*  77 */
	{-1, MPITYPE_OTHER, MPI_PACK_SIZE_VAL, FALSE},        /*  78 */
	{MPI_PROBE_EV, MPITYPE_PTOP, MPI_PROBE_VAL, FALSE},       /*  79 */
	{MPI_RECV_EV, MPITYPE_PTOP, MPI_RECV_VAL, FALSE}, /*  80 */
	{MPI_RSEND_EV, MPITYPE_PTOP, MPI_RSEND_VAL, FALSE},       /*  81 */
	{MPI_RSEND_INIT_EV, MPITYPE_PTOP, MPI_RSEND_INIT_VAL, FALSE},     /*  82 */
	{MPI_SEND_EV, MPITYPE_PTOP, MPI_SEND_VAL, FALSE}, /*  83 */
	{MPI_SENDRECV_EV, MPITYPE_PTOP, MPI_SENDRECV_VAL, FALSE},  /*  84 */
	{MPI_SENDRECV_REPLACE_EV, MPITYPE_PTOP, MPI_SENDRECV_REPLACE_VAL, FALSE},  /*  85 */
	{MPI_SSEND_EV, MPITYPE_PTOP, MPI_SSEND_VAL, FALSE},       /*  86 */
	{MPI_SSEND_INIT_EV, MPITYPE_PTOP, MPI_SSEND_INIT_VAL, FALSE},     /*  87 */
	{MPI_START_EV, MPITYPE_OTHER, MPI_START_VAL, FALSE},      /*  88 */
	{MPI_STARTALL_EV, MPITYPE_OTHER, MPI_STARTALL_VAL, FALSE},        /*  89 */
	{MPI_TEST_EV, MPITYPE_OTHER, MPI_TEST_VAL, FALSE},        /*  90 */
	{MPI_TESTALL_EV, MPITYPE_OTHER, MPI_TESTALL_VAL, FALSE},  /*  91 */
	{MPI_TESTANY_EV, MPITYPE_OTHER, MPI_TESTANY_VAL, FALSE},  /*  92 */
	{-1, MPITYPE_OTHER, MPI_TEST_CANCELLED_VAL, FALSE},   /*  93 */
	{MPI_TESTSOME_EV, MPITYPE_OTHER, MPI_TESTSOME_VAL, FALSE}, /*  94 */
	{-1, MPITYPE_TYPE, MPI_TYPE_COMMIT_VAL, FALSE},       /*  95 */
	{-1, MPITYPE_TYPE, MPI_TYPE_CONTIGUOUS_VAL, FALSE},   /*  96 */
	{-1, MPITYPE_TYPE, MPI_TYPE_EXTENT_VAL, FALSE},       /*  97 */
	{-1, MPITYPE_TYPE, MPI_TYPE_FREE_VAL, FALSE}, /*  98 */
	{-1, MPITYPE_TYPE, MPI_TYPE_HINDEXED_VAL, FALSE},     /*  99 */
	{-1, MPITYPE_TYPE, MPI_TYPE_HVECTOR_VAL, FALSE},      /* 100 */
	{-1, MPITYPE_TYPE, MPI_TYPE_INDEXED_VAL, FALSE},      /* 101 */
	{-1, MPITYPE_TYPE, MPI_TYPE_LB_VAL, FALSE},   /* 102 */
	{-1, MPITYPE_TYPE, MPI_TYPE_SIZE_VAL, FALSE}, /* 103 */
	{-1, MPITYPE_TYPE, MPI_TYPE_STRUCT_VAL, FALSE},       /* 104 */
	{-1, MPITYPE_TYPE, MPI_TYPE_UB_VAL, FALSE},   /* 105 */
	{-1, MPITYPE_TYPE, MPI_TYPE_VECTOR_VAL, FALSE},       /* 106 */
	{-1, MPITYPE_OTHER, MPI_UNPACK_VAL, FALSE},   /* 107 */
	{MPI_WAIT_EV, MPITYPE_PTOP, MPI_WAIT_VAL, FALSE}, /* 108 */
	{MPI_WAITALL_EV, MPITYPE_PTOP, MPI_WAITALL_VAL, FALSE},   /* 109 */
	{MPI_WAITANY_EV, MPITYPE_PTOP, MPI_WAITANY_VAL, FALSE},   /* 110 */
	{MPI_WAITSOME_EV, MPITYPE_PTOP, MPI_WAITSOME_VAL, FALSE}, /* 111 */
	{MPI_CART_COORDS_EV, MPITYPE_TOPOLOGIES, MPI_CART_COORDS_VAL, FALSE}, /* 112 */
	{MPI_CART_CREATE_EV, MPITYPE_TOPOLOGIES, MPI_CART_CREATE_VAL, FALSE}, /* 113 */
	{-1, MPITYPE_TOPOLOGIES, MPI_CART_GET_VAL, FALSE},    /* 114 */
	{-1, MPITYPE_TOPOLOGIES, MPI_CART_MAP_VAL, FALSE},    /* 115 */
	{-1, MPITYPE_TOPOLOGIES, MPI_CART_RANK_VAL, FALSE},   /* 116 */
	{-1, MPITYPE_TOPOLOGIES, MPI_CART_SHIFT_VAL, FALSE},  /* 117 */
	{MPI_CART_SUB_EV, MPITYPE_TOPOLOGIES, MPI_CART_SUB_VAL, FALSE},       /* 118 */
	{-1, MPITYPE_TOPOLOGIES, MPI_CARTDIM_GET_VAL, FALSE}, /* 119 */
	{-1, MPITYPE_TOPOLOGIES, MPI_DIMS_CREATE_VAL, FALSE}, /* 120 */
	{-1, MPITYPE_TOPOLOGIES, MPI_GRAPH_GET_VAL, FALSE},   /* 121 */
	{-1, MPITYPE_TOPOLOGIES, MPI_GRAPH_MAP_VAL, FALSE},   /* 122 */
	{-1, MPITYPE_TOPOLOGIES, MPI_GRAPH_NEIGHBORS_VAL, FALSE},     /* 123 */
	{-1, MPITYPE_TOPOLOGIES, MPI_GRAPH_CREATE_VAL, FALSE},        /* 124 */
	{-1, MPITYPE_TOPOLOGIES, MPI_GRAPHDIMS_GET_VAL, FALSE},       /* 125 */
	{-1, MPITYPE_TOPOLOGIES, MPI_GRAPH_NEIGHBORS_COUNT_VAL, FALSE},       /* 126 */
	{-1, MPITYPE_TOPOLOGIES, MPI_TOPO_TEST_VAL, FALSE},   /* 127 */
	{MPI_WIN_CREATE_EV, MPITYPE_RMA, MPI_WIN_CREATE_VAL, FALSE}, /* 128 */
	{MPI_WIN_FREE_EV, MPITYPE_RMA, MPI_WIN_FREE_VAL, FALSE},   /* 129 */
	{MPI_PUT_EV, MPITYPE_RMA, MPI_PUT_VAL, FALSE},        /* 130 */
	{MPI_GET_EV, MPITYPE_RMA, MPI_GET_VAL, FALSE},        /* 131 */
	{-1, MPITYPE_RMA, MPI_ACCUMULATE_VAL, FALSE}, /* 132 */
	{MPI_WIN_FENCE_EV, MPITYPE_RMA, MPI_WIN_FENCE_VAL, FALSE},  /* 133 */
	{MPI_WIN_START_EV, MPITYPE_RMA, MPI_WIN_START_VAL, FALSE},  /* 134 */
	{MPI_WIN_COMPLETE_EV, MPITYPE_RMA, MPI_WIN_COMPLETE_VAL, FALSE},       /* 135 */
	{MPI_WIN_POST_EV, MPITYPE_RMA, MPI_WIN_POST_VAL, FALSE},   /* 136 */
	{MPI_WIN_WAIT_EV, MPITYPE_RMA, MPI_WIN_WAIT_VAL, FALSE},   /* 137 */
	{-1, MPITYPE_RMA, MPI_WIN_TEST_VAL, FALSE},   /* 138 */
	{-1, MPITYPE_RMA, MPI_WIN_LOCK_VAL, FALSE},   /* 139 */
	{-1, MPITYPE_RMA, MPI_WIN_UNLOCK_VAL, FALSE},  /* 140 */
	{MPI_FILE_OPEN_EV, MPITYPE_IO, MPI_FILE_OPEN_VAL, FALSE}, /* 141 */
	{MPI_FILE_CLOSE_EV, MPITYPE_IO, MPI_FILE_CLOSE_VAL, FALSE}, /* 142 */
	{MPI_FILE_READ_EV, MPITYPE_IO, MPI_FILE_READ_VAL, FALSE}, /* 143 */
	{MPI_FILE_READ_ALL_EV, MPITYPE_IO, MPI_FILE_READ_ALL_VAL, FALSE}, /* 144 */
	{MPI_FILE_WRITE_EV, MPITYPE_IO, MPI_FILE_WRITE_VAL, FALSE}, /* 145 */
	{MPI_FILE_WRITE_ALL_EV, MPITYPE_IO, MPI_FILE_WRITE_ALL_VAL, FALSE}, /* 146 */
	{MPI_FILE_READ_AT_EV, MPITYPE_IO, MPI_FILE_READ_AT_VAL, FALSE}, /* 147 */
	{MPI_FILE_READ_AT_ALL_EV, MPITYPE_IO, MPI_FILE_READ_AT_ALL_VAL, FALSE}, /* 148 */
	{MPI_FILE_WRITE_AT_EV, MPITYPE_IO, MPI_FILE_WRITE_AT_VAL, FALSE}, /* 149 */
	{MPI_FILE_WRITE_AT_ALL_EV, MPITYPE_IO, MPI_FILE_WRITE_AT_ALL_VAL, FALSE}, /* 150 */
	{MPI_COMM_SPAWN_EV, MPITYPE_COMM, MPI_COMM_SPAWN_VAL, FALSE}, /* 151 */
	{MPI_COMM_SPAWN_MULTIPLE_EV, MPITYPE_COMM, MPI_COMM_SPAWN_MULTIPLE_VAL, FALSE}, /* 152 */
	{MPI_REQUEST_GET_STATUS_EV, MPITYPE_OTHER, MPI_REQUEST_GET_STATUS_VAL, FALSE}        /*  153 */
};



/* Dels 12, de moment nomes 8 son diferents */
static struct t_prv_type_info prv_block_groups[NUM_MPI_BLOCK_GROUPS] = {
	{MPITYPE_PTOP, MPITYPE_PTOP_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_COLLECTIVE, MPITYPE_COLLECTIVE_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_OTHER, MPITYPE_OTHER_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_RMA, MPITYPE_RMA_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_IO, MPITYPE_IO_LABEL, MPITYPE_FLAG_COLOR}
#if defined(DEAD_CODE)
	{MPITYPE_COMM, MPITYPE_COMM_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_GROUP, MPITYPE_GROUP_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_TOPOLOGIES, MPITYPE_TOPOLOGIES_LABEL, MPITYPE_FLAG_COLOR},
	{MPITYPE_TYPE, MPITYPE_TYPE_LABEL, MPITYPE_FLAG_COLOR},
	{USER_FUNCTION, USER_FUNCTION_LABEL, MPITYPE_FLAG_COLOR},
	{USER_CALL, USER_CALL_LABEL, MPITYPE_FLAG_COLOR},
	{USER_BLOCK, USER_BLOCK_LABEL, MPITYPE_FLAG_COLOR}
#endif
};



static struct t_prv_val_label mpi_prv_val_label[NUM_MPI_PRV_ELEMENTS] = {
	{MPI_SEND_VAL, MPI_SEND_LABEL},
	{MPI_RECV_VAL, MPI_RECV_LABEL},
	{MPI_ISEND_VAL, MPI_ISEND_LABEL},
	{MPI_IRECV_VAL, MPI_IRECV_LABEL},
	{MPI_WAIT_VAL, MPI_WAIT_LABEL},
	{MPI_WAITALL_VAL, MPI_WAITALL_LABEL},
	{MPI_BCAST_VAL, MPI_BCAST_LABEL},
	{MPI_BARRIER_VAL, MPI_BARRIER_LABEL},
	{MPI_REDUCE_VAL, MPI_REDUCE_LABEL},
	{MPI_ALLREDUCE_VAL, MPI_ALLREDUCE_LABEL},
	{MPI_ALLTOALL_VAL, MPI_ALLTOALL_LABEL},
	{MPI_ALLTOALLV_VAL, MPI_ALLTOALLV_LABEL},
	{MPI_GATHER_VAL, MPI_GATHER_LABEL},
	{MPI_GATHERV_VAL, MPI_GATHERV_LABEL},
	{MPI_SCATTER_VAL, MPI_SCATTER_LABEL},
	{MPI_SCATTERV_VAL, MPI_SCATTERV_LABEL},
	{MPI_ALLGATHER_VAL, MPI_ALLGATHER_LABEL},
	{MPI_ALLGATHERV_VAL, MPI_ALLGATHERV_LABEL},
	{MPI_COMM_RANK_VAL, MPI_COMM_RANK_LABEL},
	{MPI_COMM_SIZE_VAL, MPI_COMM_SIZE_LABEL},
	{MPI_COMM_CREATE_VAL, MPI_COMM_CREATE_LABEL},
	{MPI_COMM_DUP_VAL, MPI_COMM_DUP_LABEL},
	{MPI_COMM_SPLIT_VAL, MPI_COMM_SPLIT_LABEL},
	{MPI_COMM_SPAWN_VAL, MPI_COMM_SPAWN_LABEL},
	{MPI_COMM_SPAWN_MULTIPLE_VAL,MPI_COMM_SPAWN_MULTIPLE_LABEL},
	{MPI_COMM_GROUP_VAL, MPI_COMM_GROUP_LABEL},
	{MPI_COMM_FREE_VAL, MPI_COMM_FREE_LABEL},
	{MPI_COMM_REMOTE_GROUP_VAL, MPI_COMM_REMOTE_GROUP_LABEL},
	{MPI_COMM_REMOTE_SIZE_VAL, MPI_COMM_REMOTE_SIZE_LABEL},
	{MPI_COMM_TEST_INTER_VAL, MPI_COMM_TEST_INTER_LABEL},
	{MPI_COMM_COMPARE_VAL, MPI_COMM_COMPARE_LABEL},
	{MPI_SCAN_VAL, MPI_SCAN_LABEL},
	{MPI_INIT_VAL, MPI_INIT_LABEL},
	{MPI_FINALIZE_VAL, MPI_FINALIZE_LABEL},
	{MPI_BSEND_VAL, MPI_BSEND_LABEL},
	{MPI_SSEND_VAL, MPI_SSEND_LABEL},
	{MPI_RSEND_VAL, MPI_RSEND_LABEL},
	{MPI_IBSEND_VAL, MPI_IBSEND_LABEL},
	{MPI_ISSEND_VAL, MPI_ISSEND_LABEL},
	{MPI_IRSEND_VAL, MPI_IRSEND_LABEL},
	{MPI_TEST_VAL, MPI_TEST_LABEL},
	{MPI_CANCEL_VAL, MPI_CANCEL_LABEL},
	{MPI_SENDRECV_VAL, MPI_SENDRECV_LABEL},
	{MPI_SENDRECV_REPLACE_VAL, MPI_SENDRECV_REPLACE_LABEL},
	{MPI_CART_CREATE_VAL, MPI_CART_CREATE_LABEL},
	{MPI_CART_SHIFT_VAL, MPI_CART_SHIFT_LABEL},
	{MPI_CART_COORDS_VAL, MPI_CART_COORDS_LABEL},
	{MPI_CART_GET_VAL, MPI_CART_GET_LABEL},
	{MPI_CART_MAP_VAL, MPI_CART_MAP_LABEL},
	{MPI_CART_RANK_VAL, MPI_CART_RANK_LABEL},
	{MPI_CART_SUB_VAL, MPI_CART_SUB_LABEL},
	{MPI_CARTDIM_GET_VAL, MPI_CARTDIM_GET_LABEL},
	{MPI_DIMS_CREATE_VAL, MPI_DIMS_CREATE_LABEL},
	{MPI_GRAPH_GET_VAL, MPI_GRAPH_GET_LABEL},
	{MPI_GRAPH_MAP_VAL, MPI_GRAPH_MAP_LABEL},
	{MPI_GRAPH_CREATE_VAL, MPI_GRAPH_CREATE_LABEL},
	{MPI_GRAPH_NEIGHBORS_VAL, MPI_GRAPH_NEIGHBORS_LABEL},
	{MPI_GRAPHDIMS_GET_VAL, MPI_GRAPHDIMS_GET_LABEL},
	{MPI_GRAPH_NEIGHBORS_COUNT_VAL, MPI_GRAPH_NEIGHBORS_COUNT_LABEL},
	{MPI_TOPO_TEST_VAL, MPI_TOPO_TEST_LABEL},
	{MPI_WAITANY_VAL, MPI_WAITANY_LABEL},
	{MPI_WAITSOME_VAL, MPI_WAITSOME_LABEL},
	{MPI_PROBE_VAL, MPI_PROBE_LABEL},
	{MPI_REQUEST_GET_STATUS_VAL, MPI_REQUEST_GET_STATUS_LABEL},
	{MPI_IPROBE_VAL, MPI_IPROBE_LABEL},
	{MPI_WIN_CREATE_VAL, MPI_WIN_CREATE_LABEL},
	{MPI_WIN_FREE_VAL, MPI_WIN_FREE_LABEL},
	{MPI_PUT_VAL, MPI_PUT_LABEL},
	{MPI_GET_VAL, MPI_GET_LABEL},
	{MPI_ACCUMULATE_VAL, MPI_ACCUMULATE_LABEL},
	{MPI_WIN_FENCE_VAL, MPI_WIN_FENCE_LABEL},
	{MPI_WIN_START_VAL, MPI_WIN_START_LABEL},
	{MPI_WIN_COMPLETE_VAL, MPI_WIN_COMPLETE_LABEL},
	{MPI_WIN_POST_VAL, MPI_WIN_POST_LABEL},
	{MPI_WIN_WAIT_VAL, MPI_WIN_WAIT_LABEL},
	{MPI_WIN_TEST_VAL, MPI_WIN_TEST_LABEL},
	{MPI_WIN_LOCK_VAL, MPI_WIN_LOCK_LABEL},
	{MPI_WIN_UNLOCK_VAL, MPI_WIN_UNLOCK_LABEL},
	{MPI_PACK_VAL, MPI_PACK_LABEL},
	{MPI_UNPACK_VAL, MPI_UNPACK_LABEL},
	{MPI_OP_CREATE_VAL, MPI_OP_CREATE_LABEL},
	{MPI_OP_FREE_VAL, MPI_OP_FREE_LABEL},
	{MPI_REDUCE_SCATTER_VAL, MPI_REDUCE_SCATTER_LABEL},
	{MPI_ATTR_DELETE_VAL, MPI_ATTR_DELETE_LABEL},
	{MPI_ATTR_GET_VAL, MPI_ATTR_GET_LABEL},
	{MPI_ATTR_PUT_VAL, MPI_ATTR_PUT_LABEL},
	{MPI_GROUP_DIFFERENCE_VAL, MPI_GROUP_DIFFERENCE_LABEL},
	{MPI_GROUP_EXCL_VAL, MPI_GROUP_EXCL_LABEL},
	{MPI_GROUP_FREE_VAL, MPI_GROUP_FREE_LABEL},
	{MPI_GROUP_INCL_VAL, MPI_GROUP_INCL_LABEL},
	{MPI_GROUP_INTERSECTION_VAL, MPI_GROUP_INTERSECTION_LABEL},
	{MPI_GROUP_RANK_VAL, MPI_GROUP_RANK_LABEL},
	{MPI_GROUP_RANGE_EXCL_VAL, MPI_GROUP_RANGE_EXCL_LABEL},
	{MPI_GROUP_RANGE_INCL_VAL, MPI_GROUP_RANGE_INCL_LABEL},
	{MPI_GROUP_SIZE_VAL, MPI_GROUP_SIZE_LABEL},
	{MPI_GROUP_TRANSLATE_RANKS_VAL, MPI_GROUP_TRANSLATE_RANKS_LABEL},
	{MPI_GROUP_UNION_VAL, MPI_GROUP_UNION_LABEL},
	{MPI_GROUP_COMPARE_VAL, MPI_GROUP_COMPARE_LABEL},
	{MPI_INTERCOMM_CREATE_VAL, MPI_INTERCOMM_CREATE_LABEL},
	{MPI_INTERCOMM_MERGE_VAL, MPI_INTERCOMM_MERGE_LABEL},
	{MPI_KEYVAL_FREE_VAL, MPI_KEYVAL_FREE_LABEL},
	{MPI_KEYVAL_CREATE_VAL, MPI_KEYVAL_CREATE_LABEL},
	{MPI_ABORT_VAL, MPI_ABORT_LABEL},
	{MPI_ERROR_CLASS_VAL, MPI_ERROR_CLASS_LABEL},
	{MPI_ERRHANDLER_CREATE_VAL, MPI_ERRHANDLER_CREATE_LABEL},
	{MPI_ERRHANDLER_FREE_VAL, MPI_ERRHANDLER_FREE_LABEL},
	{MPI_ERRHANDLER_GET_VAL, MPI_ERRHANDLER_GET_LABEL},
	{MPI_ERROR_STRING_VAL, MPI_ERROR_STRING_LABEL},
	{MPI_ERRHANDLER_SET_VAL, MPI_ERRHANDLER_SET_LABEL},
	{MPI_GET_PROCESSOR_NAME_VAL, MPI_GET_PROCESSOR_NAME_LABEL},
	{MPI_INITIALIZED_VAL, MPI_INITIALIZED_LABEL},
	{MPI_WTICK_VAL, MPI_WTICK_LABEL},
	{MPI_WTIME_VAL, MPI_WTIME_LABEL},
	{MPI_ADDRESS_VAL, MPI_ADDRESS_LABEL},
	{MPI_BSEND_INIT_VAL, MPI_BSEND_INIT_LABEL},
	{MPI_BUFFER_ATTACH_VAL, MPI_BUFFER_ATTACH_LABEL},
	{MPI_BUFFER_DETACH_VAL, MPI_BUFFER_DETACH_LABEL},
	{MPI_REQUEST_FREE_VAL, MPI_REQUEST_FREE_LABEL},
	{MPI_RECV_INIT_VAL, MPI_RECV_INIT_LABEL},
	{MPI_SEND_INIT_VAL, MPI_SEND_INIT_LABEL},
	{MPI_GET_COUNT_VAL, MPI_GET_COUNT_LABEL},
	{MPI_GET_ELEMENTS_VAL, MPI_GET_ELEMENTS_LABEL},
	{MPI_PACK_SIZE_VAL, MPI_PACK_SIZE_LABEL},
	{MPI_RSEND_INIT_VAL, MPI_RSEND_INIT_LABEL},
	{MPI_SSEND_INIT_VAL, MPI_SSEND_INIT_LABEL},
	{MPI_START_VAL, MPI_START_LABEL},
	{MPI_STARTALL_VAL, MPI_STARTALL_LABEL},
	{MPI_TESTALL_VAL, MPI_TESTALL_LABEL},
	{MPI_TESTANY_VAL, MPI_TESTANY_LABEL},
	{MPI_TEST_CANCELLED_VAL, MPI_TEST_CANCELLED_LABEL},
	{MPI_TESTSOME_VAL, MPI_TESTSOME_LABEL},
	{MPI_TYPE_COMMIT_VAL, MPI_TYPE_COMMIT_LABEL},
	{MPI_TYPE_CONTIGUOUS_VAL, MPI_TYPE_CONTIGUOUS_LABEL},
	{MPI_TYPE_EXTENT_VAL, MPI_TYPE_EXTENT_LABEL},
	{MPI_TYPE_FREE_VAL, MPI_TYPE_FREE_LABEL},
	{MPI_TYPE_HINDEXED_VAL, MPI_TYPE_HINDEXED_LABEL},
	{MPI_TYPE_HVECTOR_VAL, MPI_TYPE_HVECTOR_LABEL},
	{MPI_TYPE_INDEXED_VAL, MPI_TYPE_INDEXED_LABEL},
	{MPI_TYPE_LB_VAL, MPI_TYPE_LB_LABEL},
	{MPI_TYPE_SIZE_VAL, MPI_TYPE_SIZE_LABEL},
	{MPI_TYPE_STRUCT_VAL, MPI_TYPE_STRUCT_LABEL},
	{MPI_TYPE_UB_VAL, MPI_TYPE_UB_LABEL},
	{MPI_TYPE_VECTOR_VAL, MPI_TYPE_VECTOR_LABEL},
	{MPI_FILE_OPEN_VAL, MPI_FILE_OPEN_LABEL},
	{MPI_FILE_CLOSE_VAL, MPI_FILE_CLOSE_LABEL},
	{MPI_FILE_READ_VAL, MPI_FILE_READ_LABEL},
	{MPI_FILE_READ_ALL_VAL, MPI_FILE_READ_ALL_LABEL},
	{MPI_FILE_WRITE_VAL, MPI_FILE_WRITE_LABEL},
	{MPI_FILE_WRITE_ALL_VAL, MPI_FILE_WRITE_ALL_LABEL},
	{MPI_FILE_READ_AT_VAL, MPI_FILE_READ_AT_LABEL},
	{MPI_FILE_READ_AT_ALL_VAL, MPI_FILE_READ_AT_ALL_LABEL},
	{MPI_FILE_WRITE_AT_VAL, MPI_FILE_WRITE_AT_LABEL},
	{MPI_FILE_WRITE_AT_ALL_VAL, MPI_FILE_WRITE_AT_ALL_LABEL}
};


#define IPROBE_CNT_INDEX                                    0
#define TIME_OUTSIDE_IPROBES_INDEX                          1
#define TEST_CNT_INDEX                                      2
#define COLLECTIVE_INDEX                                    3
#define MPI_REQUEST_GET_STATUS_CNT_INDEX                    4
#define TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_INDEX           5

#define MAX_SOFTCNT                                         6

int MPI_SoftCounters_used[MAX_SOFTCNT] = { FALSE, FALSE, FALSE, FALSE, FALSE, FALSE };

void Enable_MPI_Soft_Counter (unsigned int EvType)
{
	if (EvType == MPI_IPROBE_COUNTER_EV)
		MPI_SoftCounters_used[IPROBE_CNT_INDEX] = TRUE;
	else if (EvType == MPI_TIME_OUTSIDE_IPROBES_EV)
		MPI_SoftCounters_used[TIME_OUTSIDE_IPROBES_INDEX] = TRUE;
	else if (EvType == MPI_TEST_COUNTER_EV)
		MPI_SoftCounters_used[TEST_CNT_INDEX] = TRUE;
	else if (EvType == MPI_REQUEST_GET_STATUS_COUNTER_EV)
		MPI_SoftCounters_used[MPI_REQUEST_GET_STATUS_CNT_INDEX] = TRUE;
	else if (EvType == MPI_TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_EV)
		MPI_SoftCounters_used[TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_INDEX] = TRUE;
	else if (EvType == MPI_REDUCE_EV || EvType == MPI_ALLREDUCE_EV ||
	         EvType == MPI_BARRIER_EV || EvType == MPI_BCAST_EV ||
	         EvType == MPI_ALLTOALL_EV || EvType == MPI_ALLTOALLV_EV ||
	         EvType == MPI_ALLGATHER_EV || EvType == MPI_ALLGATHERV_EV ||
	         EvType == MPI_GATHER_EV || EvType == MPI_GATHERV_EV ||
	         EvType == MPI_SCATTER_EV || EvType == MPI_SCATTERV_EV ||
	         EvType == MPI_REDUCESCAT_EV || EvType == MPI_SCAN_EV)
		MPI_SoftCounters_used[COLLECTIVE_INDEX] = TRUE;
}


/******************************************************************************
 **      Function name : busca_event_mpit
 **      
 **      Description : 
 ******************************************************************************/

static int busca_event_mpit (int tmpit)
{
  int i;

  for (i = 0; i < NUM_MPI_PRV_ELEMENTS; i++)
    if (event_mpit2prv[i].tipus_mpit == tmpit)
      break;
  if (i < NUM_MPI_PRV_ELEMENTS)
    return i;
  return -1;
}




/******************************************************************************
 **      Function name : Enable_MPI_Operation
 **      
 **      Description : 
 ******************************************************************************/

void Enable_MPI_Operation (int Op)
{
  int index;

  index = busca_event_mpit (Op);
  if (index >= 0)
    event_mpit2prv[index].utilitzada = TRUE;
}



/******************************************************************************
 **      Function name : get_mpi_prv_val_label
 **      
 **      Description : 
 ******************************************************************************/

static char *get_mpi_prv_val_label (int val)
{
  int i;

  /*
   * Cal buscar aquest valor 
   */
  for (i = 0; i < NUM_MPI_PRV_ELEMENTS; i++)
    if (mpi_prv_val_label[i].value == val)
      break;
  if (i < NUM_MPI_PRV_ELEMENTS)
    return mpi_prv_val_label[i].label;
  return NULL;
}

#if defined(PARALLEL_MERGE)

#include <mpi.h>
#include "mpi-aux.h"

void Share_MPI_Softcounter_Operations (void)
{
	int res, i, tmp_in[MAX_SOFTCNT], tmp_out[MAX_SOFTCNT];
  
	for (i = 0; i < MAX_SOFTCNT; i++)
		tmp_in[i] = MPI_SoftCounters_used[i];
    
	res = MPI_Reduce (tmp_in, tmp_out, MAX_SOFTCNT, MPI_INT, MPI_BOR, 0, MPI_COMM_WORLD);
	MPI_CHECK(res, MPI_Reduce, "While sharing MPI enabled operations");

	for (i = 0; i < MAX_SOFTCNT; i++)
		MPI_SoftCounters_used[i] = tmp_out[i];
}

void Share_MPI_Operations (void)
{
	int res;
	int i, tmp_in[NUM_MPI_PRV_ELEMENTS], tmp_out[NUM_MPI_PRV_ELEMENTS];

	for (i = 0; i < NUM_MPI_PRV_ELEMENTS; i++)
		tmp_in[i] = event_mpit2prv[i].utilitzada;

	res = MPI_Reduce (tmp_in, tmp_out, NUM_MPI_PRV_ELEMENTS, MPI_INT, MPI_BOR, 0, MPI_COMM_WORLD);
	MPI_CHECK(res, MPI_Reduce, "While sharing MPI enabled operations");

	for (i = 0; i < NUM_MPI_PRV_ELEMENTS; i++)
		event_mpit2prv[i].utilitzada = tmp_out[i];
}
#endif /* PARALLEL_MERGE */



/******************************************************************************
 **      Function name : MPITEvent_WriteEnabledOperations
 **      
 **      Description : 
 ******************************************************************************/

void MPITEvent_WriteEnabled_MPI_Operations (FILE * fd)
{
	int ii, jj;
	int cnt;
	int Type;
	char *etiqueta;

	for (ii = 0; ii < NUM_MPI_BLOCK_GROUPS; ii++)
	{
		Type = prv_block_groups[ii].type;

		/*
		 * Primer comptem si hi ha alguna operacio MPI del grup actual 
		 */
		cnt = 0;
		for (jj = 0; jj < NUM_MPI_PRV_ELEMENTS; jj++)
		{
			if ((Type == event_mpit2prv[jj].tipus_prv) &&
			    (event_mpit2prv[jj].utilitzada))
				cnt++;
		}

		if (cnt)
		{
		  fprintf (fd, "%s\n", "EVENT_TYPE");
		  fprintf (fd, "%d   %d    %s\n", prv_block_groups[ii].flag_color,
		           prv_block_groups[ii].type, prv_block_groups[ii].label);

		  fprintf (fd, "%s\n", "VALUES");
		  for (jj = 0; jj < NUM_MPI_PRV_ELEMENTS; jj++)
		  {
		  	  if ((Type == event_mpit2prv[jj].tipus_prv) &&
		  	      (event_mpit2prv[jj].utilitzada))
		  	  {
		  	  	  etiqueta = get_mpi_prv_val_label (event_mpit2prv[jj].valor_prv);
		  	  	  fprintf (fd, "%d   %s\n", event_mpit2prv[jj].valor_prv, etiqueta);
		  	  }
		  }
		  fprintf (fd, "%d   %s\n", 0, "Outside MPI");
		  fprintf (fd, "\n\n");
		}
	}
}

/******************************************************************************
 *   Software counters labels
 ******************************************************************************/

#define IPROBE_COUNTER_LBL                          "MPI_Iprobe misses"
#define TIME_OUTSIDE_IPROBES_LBL                    "Elapsed time outside MPI_Iprobe"
#define TEST_COUNTER_LBL                            "MPI_Test misses"
#define MPI_REQUEST_GET_STATUS_COUNTER_LBL          "MPI_Request_get_status counter"
#define TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_LBL     "Elapsed time outside MPI_Request_get_status"

void SoftCountersEvent_WriteEnabled_MPI_Operations (FILE * fd)
{
	if (MPI_SoftCounters_used[IPROBE_CNT_INDEX])
	{
		fprintf (fd, "EVENT_TYPE\n");
		fprintf (fd, "%d    %d    %s\n\n", 0, 
			MPI_IPROBE_COUNTER_EV, IPROBE_COUNTER_LBL);
		LET_SPACES(fd);
	}
	if (MPI_SoftCounters_used[TIME_OUTSIDE_IPROBES_INDEX])
	{
		fprintf (fd, "EVENT_TYPE\n");
		fprintf (fd, "%d    %d    %s\n\n", 0, 
			MPI_TIME_OUTSIDE_IPROBES_EV, TIME_OUTSIDE_IPROBES_LBL);
		LET_SPACES(fd);
	}
	if (MPI_SoftCounters_used[TEST_CNT_INDEX])
	{
		fprintf (fd, "EVENT_TYPE\n");
		fprintf (fd, "%d    %d    %s\n\n", 0, 
			MPI_TEST_COUNTER_EV, TEST_COUNTER_LBL);
		LET_SPACES(fd);
	}
	if (MPI_SoftCounters_used[COLLECTIVE_INDEX])
	{
		fprintf (fd, "%s\n", TYPE_LABEL);
		fprintf (fd, "%d    %d    %s\n", MPI_GRADIENT, MPI_GLOBAL_OP_SENDSIZE,
		         MPI_GLOBAL_OP_SENDSIZE_LBL);
		fprintf (fd, "%d    %d    %s\n", MPI_GRADIENT, MPI_GLOBAL_OP_RECVSIZE,
		         MPI_GLOBAL_OP_RECVSIZE_LBL);
		fprintf (fd, "%d    %d    %s\n", MPI_GRADIENT, MPI_GLOBAL_OP_ROOT,
		         MPI_GLOBAL_OP_ROOT_LBL);
		fprintf (fd, "%d    %d    %s\n", MPI_GRADIENT, MPI_GLOBAL_OP_COMM,
		         MPI_GLOBAL_OP_COMM_LBL);
		LET_SPACES (fd);
	}
	if (MPI_SoftCounters_used[MPI_REQUEST_GET_STATUS_CNT_INDEX])
	{
		fprintf (fd, "EVENT_TYPE\n");
		fprintf (fd, "%d    %d    %s\n\n", 0, 
			MPI_REQUEST_GET_STATUS_COUNTER_EV, MPI_REQUEST_GET_STATUS_COUNTER_LBL);
		LET_SPACES(fd);
	}
	if (MPI_SoftCounters_used[TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_INDEX])
	{
		fprintf (fd, "EVENT_TYPE\n");
		fprintf (fd, "%d    %d    %s\n\n", 0, 
			MPI_TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_EV, TIME_OUTSIDE_MPI_REQUEST_GET_STATUS_LBL);
		LET_SPACES(fd);
	}

}

void Translate_MPI_MPIT2PRV (int typempit, UINT64 valuempit, int *typeprv, UINT64 *valueprv)
{
	int index = busca_event_mpit(typempit);

	if (index >= 0)
	{
		*typeprv = event_mpit2prv[index].tipus_prv;
		*valueprv = (valuempit!=0)?event_mpit2prv[index].valor_prv:0;
	}
	else
	{
		*typeprv = typempit;
		*valueprv = valuempit;
	}
}
