#!/bin/bash

#Example output
#  12,051 cache-references                                            
#  2,669 cache-misses              #   22.148 % of all cache refs    
#  162 page-faults                                                 
#  3,882 LLC-loads                                                   
#  475 LLC-stores 

# 13179,cache-references 2969,cache-misses 162,page-faults 3863,LLC-loads 551,LLC-stores

SLEEP_TIME=1
FIRST=true
#,LLC-loads,LLC-stores 
while true;
do 
  TOPARSE=$(perf stat -a -x, --log-fd 1 -e cache-references,cache-misses,page-faults sleep $SLEEP_TIME 2>&1)
  read -a PARSED <<< $(echo $TOPARSE | tr ',' ' ')
  CACHEREF=${PARSED[0]//[[:blank:]]/}
  CACHEMISS=${PARSED[2]//[[:blank:]]/}
  CACHEMISSFRACTION=$(echo "scale = 10;$CACHEMISS/$CACHEREF" | bc)
  PAGEFAULT=${PARSED[4]//[[:blank:]]/}
  #LLCLOAD=${PARSED[6]//[[:blank:]]/}
  #LLCSTORED=${PARSED[8]//[[:blank:]]/}

  #Making values per second
  CACHEREF=$(bc <<< "scale = 10;$CACHEREF/$SLEEP_TIME")
  CACHEMISS=$(bc <<< "scale = 10;$CACHEMISS/$SLEEP_TIME")
  PAGEFAULT=$(bc <<< "scale = 10;$PAGEFAULT/$SLEEP_TIME")
  #LLCLOAD=$(bc <<< "scale = 10;$LLCLOAD/$SLEEP_TIME")
  #LLCSTORED=$(bc <<< "scale = 10;$LLCSTORED/$SLEEP_TIME")

  CACHEREF=$(printf "%.0f\n" $CACHEREF)
  CACHEMISS=$(printf "%.0f\n" $CACHEMISS)
  #scale to 0..100
  CACHEMISSFRACTION=$(bc <<< "scale = 10;$CACHEMISSFRACTION*100")
  CACHEMISSFRACTION=$(printf "%.2f\n" $CACHEMISSFRACTION)
  PAGEFAULT=$(printf "%.0f\n" $PAGEFAULT)
  #LLCLOAD=$(printf "%.0f\n" $LLCLOAD)
  #LLCSTORED=$(printf "%.0f\n" $LLCSTORED)

  if [ "$FIRST" = false ] ; then
    echo $1 cache-ref $CACHEREF | tail -n 1 >> spot-metrics.log
    echo $1 cache-miss $CACHEMISS | tail -n 1 >> spot-metrics.log
    echo $1 cache-miss-fraction $CACHEMISSFRACTION | tail -n 1 >> spot-metrics.log  
    echo $1 page-fault $PAGEFAULT | tail -n 1 >> spot-metrics.log
    #echo $1 llc-load $LLCLOAD | tail -n 1 >> spot-metrics.log 
    #echo $1 llc-stored $LLCSTORED | tail -n 1 >> spot-metrics.log
  fi
	FIRST=false
done
