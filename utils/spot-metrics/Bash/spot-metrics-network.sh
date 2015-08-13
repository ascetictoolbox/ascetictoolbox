#!/bin/bash

PREV_READ=0
PREV_WRITE=0
SLEEP_TIME=5
FIRST=true

#Get list of network adaptors
read -a ADAPTORS <<< $(echo "$fullName"  | grep 'eth' /proc/net/dev | awk '{ print $1 }' | tr '\n' ' ')
#loop through list of network adaptors

while true;
do
  TOTAL_READ=0
  TOTAL_WRI=0
  for element in "${ADAPTORS[@]}"
  do
    DISK=(`cat /proc/net/dev | grep "$element "` )
    READ=${DISK[1]//[[:blank:]]/}
    WRITE=${DISK[9]//[[:blank:]]/}
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
	  echo $1 network-in $DIFF_READ_TOTAL | tail -n 1 >> spot-metrics.log
      echo $1 network-out $DIFF_WRITE_TOTAL | tail -n 1 >> spot-metrics.log
	fi
	FIRST=false	
    PREV_READ="$TOTAL_READ"
    PREV_WRITE="$TOTAL_WRI"
  sleep $SLEEP_TIME
done
