#! /bin/sh

clean() {
	[ -d "kwapi/" ] && rm kwapi/ -Rf
	return 0
}

generate() {
	clean
	git clone https://github.com/stackforge/kwapi kwapi
	for patch in patches/*.patch; do
		echo "Applying $patch..."
		patch -p1 -d kwapi/  < $patch || return 1
	done
	return 0
}

deploy() {
	if ! [ -d "kwapi/" ]; then
		echo "kwapi/ does not exists. Need to generate first?" >&2
		return 1
	fi

	if ! which pip >/dev/null; then
		echo "pip not found. Aborting" >&2
		return 1
	fi

	kwapi_all="kwapi-forwarder kwapi-drivers kwapi-api kwapi-rrd"
	kwapi_run=
	for proc in $kwapi_all; do
		killall $proc 2>/dev/null || continue
		echo "Scheduling $proc"
		kwapi_run="$kwapi_run $proc"
	done
	if [ "$kwapi_run" ]; then
		sleep 3s
		killall -9 $kwapi_run 2>/dev/null
	fi
	echo "Kwapi is not running (any more)"

	pip uninstall kwapi
	pip install kwapi/

	for proc in $kwapi_run; do
		echo "Starting $proc"
		$proc 1>/dev/null 2>&1 &
	done
}

case "$1" in
	clean)
		clean
		exit $?
		;;
	generate)
		generate
		exit $?
		;;
	deploy)
		deploy
		exit $?
		;;
	*)
		echo "Invalid argument" >&2
		exit 1
	;;
esac
