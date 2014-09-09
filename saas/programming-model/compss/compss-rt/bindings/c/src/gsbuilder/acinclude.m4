# Copyright 2002-2007 Barcelona Supercomputing Center (www.bsc.es)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

AC_DEFUN([AC_CHECK_PERL],
	[
		AC_MSG_CHECKING([where to look for perl])
		AC_ARG_WITH([perl],
			[  --with-perl=[path]      build perl module using perl binary installed in path)],
			[
				ac_cv_use_perl=$withval
				if test x"$ac_cv_use_perl" == x"yes" ; then
                                       	PERL_PATH=$PATH
                               	else
                                       	PERL_PATH=$ac_cv_use_perl
                               	fi
				AC_PATH_PROG(PERL, perl, none, $PERL_PATH)
                		if test x"$PERL" = x"none" ; then
                        		AC_MSG_ERROR([perl cannot be found])
                		fi
				AC_MSG_RESULT([$PERL])
				
			],
			[
				AC_MSG_RESULT([no])	
			]
		)
	]
)


AC_DEFUN([AC_CHECK_GSMASTER_PERL],
	[
		AC_ARG_WITH([gssperl-installdir],
			[  --with-gssperl-installdir=[path]   specify the path of the GRID superscalar perl module)],
			[
				AC_MSG_CHECKING([the location of the GRID superscalar perl module])
				ac_cv_use_gssperl_dir=$withval
				AC_MSG_RESULT($ac_cv_use_gssperl_dir)
				AC_SUBST(ac_cv_use_gssperl_dir)
			],
			[
				ac_cv_use_gssperl_dir=""
				AC_SUBST(ac_cv_use_gssperl_dir)
			]
		)
		
		
		AC_MSG_CHECKING([if the GSMaster perl module is installed])
		if test x"$ac_cv_use_gssperl_dir" != x"" ; then
			echo "use lib '$ac_cv_use_gssperl_dir';" > conftest.pl
		fi
		echo "use GSMaster; 1;" >> conftest.pl
		$PERL conftest.pl > /dev/null 2>&1
		if test $? = 0 ; then
			AC_MSG_RESULT([yes])
		else
			AC_MSG_ERROR([could not find GSMaster perl module])
		fi
		rm conftest.pl
		AC_SUBST(ac_cv_use_perl)
	]
)


AC_DEFUN([AC_CHECK_GSWORKER_PERL],
	[
		AC_MSG_CHECKING([if the GSWorker perl module is installed])
		if test x"$ac_cv_use_gssperl_dir" != x"" ; then
			echo "use lib '$ac_cv_use_gssperl_dir';" > conftest.pl
		fi
		echo "use GSWorker; 1;" >> conftest.pl
		$PERL conftest.pl > /dev/null 2>&1
		if test $? = 0 ; then
			AC_MSG_RESULT([yes])
		else
			AC_MSG_ERROR([could not find GSWorker perl module])
		fi
		rm conftest.pl
	]
)


AC_DEFUN([AC_CHECK_GSS],
	[
		AC_ARG_WITH([gs-prefix],
			[  --with-gs-prefix=[prefix]  set the prefix under which the Grid Superscalar library is installed],
			[
				ac_cv_use_gs_prefix=$withval
				LIBS="-L$ac_cv_use_gs_prefix/lib $LIBS"
				CPPFLAGS="-I$ac_cv_use_gs_prefix/include $CPPFLAGS"
				AC_SUBST(ac_cv_use_gs_prefix)
			],
			[
				ac_cv_use_gs_prefix=""
				AC_SUBST(ac_cv_use_gs_prefix)
			]
		)
	]
)

AC_DEFUN([AC_CHECK_CLASSADS],
        [
                AC_MSG_CHECKING([if the classads prefix has been specified])
                AC_ARG_WITH([classads],
                        [  --with-classads=[prefix]  set the classads installation to be used],
                        [
                                ac_cv_use_classads=$withval
                                AC_MSG_RESULT([yes])
                                LIBS="-L$ac_cv_use_classads/lib $LIBS"
                                CPPFLAGS="$CPPFLAGS -I$ac_cv_use_classads/include"
                        ],
                        [
                                AC_MSG_RESULT([no])
                        ]
                )
                AC_SUBST(ac_cv_use_classads)
                AC_LANG([C++])
                AC_CHECK_HEADER(classad_distribution.h,
                        [],
                        [AC_MSG_ERROR([classads include files not found])]
                )
                AC_CHECK_LIB([classad], [cclassad_create],
                        [ LIBS="$LIBS -lclassad" ],
                        [ AC_MSG_ERROR([classad library cannot be found]) ]
                )
                AC_LANG([C])
        ]
)

AC_DEFUN([AC_CHECK_CLASSADS_SSH],
        [
                AC_MSG_CHECKING([if the classads prefix has been specified])
                AC_ARG_WITH([classads],
                        [  --with-classads=[prefix]  set the classads installation to be used],
                        [
                                ac_cv_use_classads=$withval
                                with_classads="yes";
                                AC_MSG_RESULT([yes])
                                LIBS="-L$ac_cv_use_classads/lib $LIBS"
                                CPPFLAGS="$CPPFLAGS -I$ac_cv_use_classads/include"
                        ],
                        [
                                with_classads="no";
                                AC_MSG_RESULT([no])
                        ]
                )
                AC_SUBST(ac_cv_use_classads)
                AC_SUBST(with_classads)
                if test x"$with_classads" != x"no"; then
                        AC_LANG([C++])
                        AC_CHECK_HEADER(classad_distribution.h,
                                [],
                                [AC_MSG_ERROR([classads include files not found])]
                        )
                        AC_CHECK_LIB([classad], [cclassad_create],
                                [ LIBS="$LIBS -lclassad" ],
                                [ AC_MSG_ERROR([classad library cannot be found]) ]
                        )
                        AC_LANG([C])
                fi
        ]
)
