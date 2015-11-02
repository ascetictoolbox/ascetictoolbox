#!/bin/zsh
PAST_TOTAL=(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)
PAST_IDLE=(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)
SUMM_PREV_TOTAL=0
SUMM_PREV_IDLE=0
corecount=$(grep -c ^processor /proc/cpuinfo)
corecount=`expr $corecount - 1`

setopt KSH_ARRAYS

while true
do
	#CPU SUMMARY BLOCK
	CPUSUMM=(`cat /proc/stat | grep '^cpu '`)
	IDLE=${CPUSUMM[4]}
	TOTAL=0
	# Don't use the last 3 columns they are already accounted for in the user column
	for i in {1..8}; do
		VALUE="${CPUSUMM[$i]}"
		let "TOTAL=$TOTAL+$VALUE"
	done
	unset VALUE
	unset CPUSUMM
	let "DIFF_IDLE=$IDLE-$SUMM_PREV_IDLE"
	#if [ "$DIFF_IDLE" == "0" ]
       if test "$DIFF_IDLE" = '0'; then
		# Prevents a divide by zero
		DIFF_IDLE='0.001'
	fi

	let "DIFF_TOTAL=$TOTAL-$SUMM_PREV_TOTAL"

	STR="scale=2;100-(100/($DIFF_TOTAL/$DIFF_IDLE))"
	DIFF_USAGE="$( echo $STR | bc )"
	unset DIFF_TOTAL
	unset DIFF_IDLE  
  
	echo $1 cpu-measured $DIFF_USAGE | tail -n 1 >> spot-metrics.log  

	SUMM_PREV_TOTAL="$TOTAL"
	SUMM_PREV_IDLE="$IDLE"
	
	unset STR
	unset DIFF_USAGE
	unset IDLE
	unset TOTAL
  
	#CPU CORE BREAKDOWN BLOCK
	for core in $(eval echo "{0..$corecount}"); do
		CPU=(`cat /proc/stat | grep "^cpu${core}"`)
		IDLE=${CPU[4]}
		TOTAL=0
		
		# Don't use the last 3 columns they are already accounted for in the user column
		for i in {1..8}; do #1st item is cpu0 etc so ignore this value
			VALUE="${CPU[$i]}"
			let "TOTAL=$TOTAL+$VALUE"
		done
		unset CPU
	    unset VALUE
		PREV_IDLE=$((PAST_IDLE[$core]))
		let "DIFF_IDLE=$IDLE-$PREV_IDLE"
		#if [ "$DIFF_IDLE" == "0" ]
        if test "$DIFF_IDLE" = '0'; then
			# Prevents a divide by zero
		       DIFF_IDLE='0.001'
		fi
		PREV_TOTAL=$((PAST_TOTAL[$core]))
		PREV_TOTAL="$( echo $PREV_TOTAL | bc )"
		((DIFF_TOTAL = $TOTAL - $PREV_TOTAL))
		STR="scale=2;100-(100/($DIFF_TOTAL/$DIFF_IDLE))"
		DIFF_USAGE="$( echo $STR | bc )"
		unset DIFF_TOTAL
		unset DIFF_IDLE
  
		echo $1 cpu-measured-$core  $DIFF_USAGE | tail -n 1 >> spot-metrics.log 

		PAST_TOTAL[$core]=$TOTAL
		PAST_IDLE[$core]=$IDLE
		
		unset STR
		unset DIFF_USAGE		
	    unset IDLE
	    unset TOTAL
		
	done

	sleep 1  
done
