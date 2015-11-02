#!/bin/zsh

PREV_READ=0
PREV_WRITE=0
SECTOR_SIZE=$(cat /sys/block/vda/queue/hw_sector_size)
SLEEP_TIME=5
FIRST=true

#Get list of available disks
#read -a DISKS <<< $(echo "$fullName"  | grep 'vda[^1-9]' /proc/diskstats | awk '{ print $3 }' | tr '\n' ' ')
DISKS=$(grep 'vd[a-z][^0-9]' /proc/diskstats | awk '{ print $3 }' | tr '\n' ' ')
DISKS=("${(@s/ /)DISKS}")
#loop through list of disks

while true;
do
  TOTAL_READ=0
  TOTAL_WRI=0
  for element in "${DISKS[@]}"
  do
    DISK=(`cat /proc/diskstats | grep "$element "` )
    READ=${DISK[5]//[[:blank:]]/}
    WRITE=${DISK[9]//[[:blank:]]/}
    #VALUES are in sectors read so convert to bytes by multiplying by sector size
    READ=$((READ * SECTOR_SIZE))
    WRITE=$((WRITE * SECTOR_SIZE))
    let "TOTAL_READ=$TOTAL_READ+$READ"
    let "TOTAL_WRI=$TOTAL_WRI+$WRITE"
  done

    DIFF_READ_TOTAL=0
    let "DIFF_READ_TOTAL=$TOTAL_READ-$PREV_READ"
    DIFF_WRITE_TOTAL=0
    let "DIFF_WRITE_TOTAL=$TOTAL_WRI-$PREV_WRITE"
    #into seconds
    #INT ONLY   DIFF_READ_TOTAL=$((DIFF_READ_TOTAL / SLEEP_TIME))
    DIFF_READ_TOTAL=$(bc <<< "scale = 10;$DIFF_READ_TOTAL/$SLEEP_TIME")
    DIFF_WRITE_TOTAL=$(bc <<< "scale = 10;$DIFF_WRITE_TOTAL/$SLEEP_TIME")
    #Into kilobytes/s
    #INT ONLY DIFF_READ_TOTAL=$((DIFF_READ_TOTAL / 1024))
    #DIFF_READ_TOTAL=$(bc <<< "scale = 10;$DIFF_READ_TOTAL/1024")
    #DIFF_WRITE_TOTAL=$(bc <<< "scale = 10;$DIFF_WRITE_TOTAL/1024")
    DIFF_READ_TOTAL=$(printf "%.2f\n" $DIFF_READ_TOTAL)
    DIFF_WRITE_TOTAL=$(printf "%.2f\n" $DIFF_WRITE_TOTAL)
	if [ "$FIRST" = false ] ; then
      echo $1 disk-read $DIFF_READ_TOTAL | tail -n 1 >> spot-metrics.log
      echo $1 disk-write $DIFF_WRITE_TOTAL | tail -n 1 >> spot-metrics.log 
	fi
	FIRST=false
    PREV_READ="$TOTAL_READ"
    PREV_WRITE="$TOTAL_WRI"
  sleep $SLEEP_TIME
done
