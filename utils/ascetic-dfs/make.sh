#! /bin/sh

clean() {
	[ -d "glusterfs/" ] && rm gluserfs/ -Rf
	return 0
}

generate() {
	clean
	#git clone hhttps://github.com/gluster/glusterfs glusterfs
	#for patch in patches/*.patch; do
	#	echo "Applying $patch..."
	#	patch -p1 -d glusterfs/  < $patch || return 1
	#done
	return 0
}

deploy() {
	echo "Nothing to do" >&2
	return 1
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
