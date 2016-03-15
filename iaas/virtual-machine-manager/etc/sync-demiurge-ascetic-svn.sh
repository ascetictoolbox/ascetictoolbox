
#svn directory where you want to move files to
DEMIURGE_SVN_PATH=/home/raimon/ascetic/trunk/iaas/virtual-machine-manager

#tmp vars needed for execution
DEMIURGE_GIT_PATH=/tmp/demiurge-ascetic

rm -rf $DEMIURGE_GIT_PATH
git clone -b feature/self-adaptation https://github.com/mariomac/demiurge.git $DEMIURGE_GIT_PATH
#git clone https://github.com/mariomac/demiurge.git $DEMIURGE_GIT_PATH

rm -rf $DEMIURGE_SVN_PATH/*
cp -r $DEMIURGE_GIT_PATH/* $DEMIURGE_SVN_PATH/.
rm -rf $DEMIURGE_SVN_PATH/.git 

#TO PERFORM MANUALLY:
# COMMIT_MESSAGE_EXAMPLE="Importing VMM vertical scalability changes to Ascetic."
# cd $DEMIURGE_SVN_PATH
# svn diff | tee /tmp/svn-diff.log
# svn commit -m $COMMIT_MESSAGE_EXAMPLE

