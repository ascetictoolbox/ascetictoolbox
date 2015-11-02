#include <pthread.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>

pthread_rwlock_t rwlock = PTHREAD_RWLOCK_INITIALIZER;

int main (int argc, char *argv[])
{
	int i;
	struct timespec start_rd, end_rd, start_wr, end_wr;
	unsigned long long rd_time, wr_time;

	clock_gettime( CLOCK_REALTIME, &start_rd);
	for (i = 0; i < 10000000; i++)
	{
		pthread_rwlock_rdlock(&rwlock);
		pthread_rwlock_unlock(&rwlock);
	}
	clock_gettime( CLOCK_REALTIME, &end_rd);

	clock_gettime( CLOCK_REALTIME, &start_wr);
	for (i = 0; i < 10000000; i++)
	{
		pthread_rwlock_wrlock(&rwlock);
		pthread_rwlock_unlock(&rwlock);
	}
	clock_gettime( CLOCK_REALTIME, &end_wr);

	rd_time = ((end_rd.tv_sec * 1000 * 1000 * 1000) + end_rd.tv_nsec)
			  -((start_rd.tv_sec * 1000 * 1000 * 1000) + start_rd.tv_nsec);
	wr_time = ((end_wr.tv_sec * 1000 * 1000 * 1000) + end_wr.tv_nsec)
			  -((start_wr.tv_sec * 1000 * 1000 * 1000) + start_wr.tv_nsec);

	printf ("rd time %llu ns aggregated, %llu per loop iteration\n", rd_time, rd_time/10000000);
	printf ("wr time %llu ns aggregated, %llu per loop iteration\n", wr_time, wr_time/10000000);

	return 0;
}
