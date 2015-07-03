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
 | @file: $HeadURL: https://svn.bsc.es/repos/ptools/extrae/trunk/example/CELL/SEQ/worker.c $
 | @last_commit: $Date: 2011-03-23 18:10:10 +0100 (mié, 23 mar 2011) $
 | @version:     $Revision: 567 $
\* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
static char rcsid[] = "$Id: worker.c 567 2011-03-23 17:10:10Z harald $";

#include <spu_intrinsics.h>
#include <spu_internals.h>

#include "bitmap.h"
#include "spu_trace.h"
#include <spu_mfcio.h>

void __inline__ cell_asynch_get (void *ls, unsigned long long ea, int size, int tag)
{
	/* DMA transfers must be x16 bytes */
	if ((size & 0x0f) != 0)
		return;

	while (spu_readchcnt (MFC_Cmd) < 1);
	mfc_get (ls, ea, size, tag, 0, 0);
}

void __inline__ cell_asynch_put (void *ls, unsigned long long ea, int size, int tag)
{
	/* DMA transfers must be x16 bytes */
	if ((size & 0x0f) != 0)
		return;

	while (spu_readchcnt (MFC_Cmd) < 1);
	mfc_put (ls, ea, size, tag, 0, 0);
}

static void cell_wait (int tag)
{
	spu_writech (MFC_WrTagMask, 1 << tag);
	spu_mfcstat (2);
}

static unsigned int get_mail (void)
{
	while (spu_stat_in_mbox () < 1);
	return spu_read_in_mbox();
}

static void get_arguments (unsigned int *ID, unsigned long long *image1,
	unsigned long long *image2, unsigned int *count, unsigned long long *out)
{
	unsigned long long tmp;

	*ID = get_mail ();
	*count = get_mail ();

	tmp = (unsigned long long) get_mail ();
	*image1 = tmp << 32 | (unsigned long long) get_mail();

	tmp = (unsigned long long) get_mail ();
	*image2 = tmp << 32 | (unsigned long long) get_mail();

	tmp = (unsigned long long) get_mail ();
	*out = tmp << 32 | (unsigned long long) get_mail();
}

static void cell_work (struct rgb_t *chroma, struct rgb_t *image, unsigned int count,
	struct rgb_t *out)
{
	unsigned int i;

	for (i = 0; i < count; i++)
	{
		if (chroma[i].green==255&&chroma[i].red==60&&chroma[i].blue==0)
		{
			COPY_COLOR (out[i],image[i]);
		}
		else
		{
			COPY_COLOR (out[i],chroma[i]);
		}
	}
}

static void cell_get_pixels (unsigned long long PUimage, struct rgb_t *SPUimage,
	unsigned int npixels)
{
	cell_asynch_get (SPUimage, PUimage, npixels*sizeof(struct rgb_t), 0);
	cell_wait (0);
}

static void cell_put_pixels (unsigned long long PUimage, struct rgb_t *SPUimage,
	unsigned int npixels)
{
	cell_asynch_put (SPUimage, PUimage, npixels*sizeof(struct rgb_t), 2);
	cell_wait (2);
}

#define MAX_PIXELS 640 
struct 	rgb_t chroma[MAX_PIXELS] __attribute ((aligned(128))),
				image[MAX_PIXELS] __attribute ((aligned(128))), 
				local_out[MAX_PIXELS] __attribute ((aligned(128)));

int main (int argc, char *argv[])
{
	unsigned long long image1, image2, global_out;
	unsigned int count, ID;

	get_arguments (&ID, &image1, &image2, &count, &global_out);

	Extrae_init ();

	while (count > 0)
	{
		Extrae_event (1000, count);

		/* GET nLINES FROM DMA */
		cell_get_pixels (image1, chroma, MAX_PIXELS);
		cell_get_pixels (image2, image, MAX_PIXELS);

		/* Do the work! */
		cell_work (chroma, image, MAX_PIXELS, local_out);

		/* PUT nLINES TO DMA */
		cell_put_pixels (global_out, local_out, MAX_PIXELS);

		image1     += MAX_PIXELS;
		image2     += MAX_PIXELS;
		global_out += MAX_PIXELS;
		count      -= MAX_PIXELS;
	}

	Extrae_fini ();

	return 0;
}

