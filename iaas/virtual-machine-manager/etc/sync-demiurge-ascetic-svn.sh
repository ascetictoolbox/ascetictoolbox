
#git-sha with the version from which you want to update
GIT_SHA=a370686a604f5659c73a5bd04ed605bff678ee88
#DEBUG=true


#svn directory where you want to move files to
DEMIURGE_SVN_PATH=/home/raimon/ascetic/trunk/iaas/virtual-machine-manager/

#tmp vars needed for execution
ADDED_FILES=/tmp/added.diff
MODIFIED_FILES=/tmp/modified.diff
DELETED_FILES=/tmp/deleted.diff
DEMIURGE_GIT_PATH=/tmp/demiurge-ascetic/

rm -rf $DEMIURGE_GIT_PATH
git clone -b feature/self-adaptation https://github.com/mariomac/demiurge.git $DEMIURGE_GIT_PATH
#git clone https://github.com/mariomac/demiurge.git $DEMIURGE_GIT_PATH
cd $DEMIURGE_GIT_PATH

git diff --name-status $GIT_SHA HEAD | grep -P 'D\t' | perl -pe 's/D\t//g' | sort | uniq > $DELETED_FILES
git diff --name-status $GIT_SHA HEAD | grep -P 'A\t' | perl -pe 's/A\t//g' | sort | uniq > $ADDED_FILES
git diff --name-status $GIT_SHA HEAD | grep -P 'M\t' | perl -pe 's/M\t//g' | sort | uniq > $MODIFIED_FILES

#Remove old files
for file in $(cat $DELETED_FILES)
do
    echo "rm  $DEMIURGE_SVN_PATH$file"
    rm  $DEMIURGE_SVN_PATH$file
done

#Create new directories
for file in $(cat $ADDED_FILES |  perl -pe 's/[^\/]+$/\n/g' | sort | uniq)
do
    echo "mkdir -p $DEMIURGE_SVN_PATH$file"
    mkdir -p $DEMIURGE_SVN_PATH$file
done

#Upload new and modified files
for file in $(cat $ADDED_FILES $MODIFIED_FILES)
do
    echo "cp $DEMIURGE_GIT_PATH$file $DEMIURGE_SVN_PATH$file"
    cp $DEMIURGE_GIT_PATH$file $DEMIURGE_SVN_PATH$file
done
