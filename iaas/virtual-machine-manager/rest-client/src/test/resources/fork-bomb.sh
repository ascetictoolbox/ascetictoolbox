#!/bin/sh

forkfunc() {
	a=2;
	b=3;

	while [ 1 == 1 ]
	do
		a=`expr '(' '(' $a '+' '1' ')' '*' $b ')' '/' $a`
		b=`expr '(' $b '*' $b ')'`

		if [ "$a" -ge 100000 ]
		then
			a=2
		fi
		if [ "$b" -ge 100000 ]
		then
			b=3
		fi

		echo "hola tio $a $b"
	done
}

fork(){
    count=0
    while (($count<=10))
    do
      forkfunc &
      count=$(( count+1 ))
    done
}

fork