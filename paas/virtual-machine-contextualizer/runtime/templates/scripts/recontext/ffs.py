#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright 2013 University of Leeds
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#  
#       http://www.apache.org/licenses/LICENSE-2.0
#  
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import errno
import fuse
import stat
import time
import os
import sys

try:
    import ConfigParser as configparser
except ImportError:
    import configparser

fuse.fuse_python_api = (0, 2)


"""This class implements a custom FUSE file system that supports unifying several directories into a joint directory tree dynamically."""
class FFS(fuse.Fuse):
   
    #Configured during init 
    contextpath = ''
    recontextpath = ''

    def __init__(self, *args, **kw):
        """create a FFS instance with properties read from ffs.properties""" 
	print "FFS init executed"
        fuse.Fuse.__init__(self, *args, **kw)
        config = configparser.ConfigParser()
        config.read('ffs.properties')
        self.contextpath = config.get('paths', 'contextpath')
        self.recontextpath = config.get('paths', 'recontextpath')

    def getFileAttr(self, relPath):
        """creates a fuse.Stat() struct based on actual file system attributes"""
        (mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime) = os.stat(relPath)
        st = fuse.Stat()
        st.st_mode = mode
        st.st_ino = 0
        st.st_dev = 0
        st.st_nlink = 1
        st.st_uid = uid
        st.st_gid = gid
        st.st_atime = atime
        st.st_mtime = mtime
        st.st_ctime = ctime
        st.st_size = size
        return st

    def getRelativeFilePath(self, path):
        """get relative file path, primary the recontext one, than context, than None if it can't be found"""
        if os.path.exists(self.recontextpath + path):
            return self.recontextpath + path
        if os.path.exists(self.contextpath + path):
            return self.contextpath + path
        return None

    def getattr(self, path):
        """get file attributes, either custom attributes for '/' (the mount point) or the actual file attribute for the relative file"""
	print "getattr on path: ", path
        if path == '/':
            st = fuse.Stat()  
            st.st_mode = stat.S_IFDIR | 0755  
            st.st_nlink = 2  
            st.st_atime = int(time.time())  
            st.st_mtime = st.st_atime  
            st.st_ctime = st.st_atime
            return st

        relPath = self.getRelativeFilePath(path)
        if relPath is not None:
            attr = self.getFileAttr(relPath)
            return attr
        
        return -errno.ENOENT

    def readdir(self, path, offset):
        """returns the union of the sets of available files and folders from context and recontext combined"""
	print "readdir on path: ", path
        yield fuse.Direntry('.')  
        yield fuse.Direntry('..')  
        for e in self.generateReaddir(path, offset):
            yield fuse.Direntry(e)

    def open(self, path, flags):
        """returns the file rights"""
	print "open on path: ", path
        # Only support for 'READ ONLY' flag
        access_flags = os.O_RDONLY | os.O_WRONLY | os.O_RDWR
        if flags & access_flags != os.O_RDONLY:
            return -errno.EACCES
        else:
            return 0

    def read(self, path, size, offset):
        """read the content of the actual file, primarily recontext and secondarily context"""
        relPath = self.getRelativeFilePath(path)
	print "read on path: ", path
        #TODO: if relPath is None: return errno.XXXXX

        in_file = open(relPath, "rb")
        in_file.seek(offset)
        data = in_file.read(size)
        in_file.close()
        return data

    def getListingTuples(self, path):
        """creates the union of directory listings in context and recontext"""
        cpath = self.contextpath + path
        if os.path.exists(cpath):
            contextListing = os.listdir(cpath)
        else:
            contextListing = ()

        recpath = self.recontextpath + path
        if os.path.exists(recpath):
            recontextListing = os.listdir(recpath)
        else:
            recontextListing = ()

        directoryListing = list(set(contextListing) | set(recontextListing))
        return directoryListing

    def generateReaddir(self, path, offset):
        """yielding set of directory items"""
        directoryListing = self.getListingTuples(path)
        for item in directoryListing:
            yield item
        
if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "usage: ffs.py <mount point>"
        sys.exit(-1)

    #move cwd to script directory
    os.chdir(os.path.dirname(sys.argv[0]))

    fs = FFS()
    fs.parse(errex=1)
    fs.main()
