%define name	 compss-extrae 
%define release		1.linux
%define version 	1.2
%define _extraehome     /opt/COMPSs/extrae

Requires: compss-framework, java >= 1.6.0, java-devel >= 1.6.0, libxml2 >= 2.5.0, libxml2-devel >= 2.5.0, libtool, automake, make, gcc-c++
Summary: Extrae trace extraction tool.
Name: %{name}
Version: %{version}
Release: %{release}
Source: %{name}-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildArch: noarch
BuildRoot: %{_tmppath}/%{name}-%{version}-buildroot
Prefix: %{_prefix}
URL: http://www.bsc.es/compss
Packager: Enric Tejedor <enric.tejedor@bsc.es>

%description
Extrae trace extraction tool.

%prep
%setup -q

%build

mkdir -p %buildroot/%_extraehome/sources
cp -r ./%_extraehome/sources/* %buildroot/%_extraehome/sources

%install

%post 

echo " "
echo "Installing COMPSs compss-extrae..."
echo " "

# Find JAVA_HOME
openjdk=`rpm -qa | grep openjdk | grep -v devel | tail -n 1`
libjvm=`rpm -ql $openjdk | grep libjvm.so | head -n 1`
export JAVA_LIB_DIR=`dirname $libjvm`
if test "${libjvm#*/jre/lib/amd64/server/libjvm.so}" != "$libjvm"
then
        export JAVA_HOME="${libjvm/\/jre\/lib\/amd64\/server\/libjvm.so/}"
elif test "${libjvm#*/jre/lib/i386/client/libjvm.so}" != "$libjvm"
then
        export JAVA_HOME="${libjvm/\/jre\/lib\/i386\/client\/libjvm.so/}"
else
        if [ -z $JAVA_HOME ]
        then
                echo "Please define \$JAVA_HOME"
                exit 1
        fi
fi
echo "Using JAVA_HOME=$JAVA_HOME"

# Build compss-extrae
cd %_extraehome/sources
./configure --enable-gettimeofday-clock --without-mpi --without-unwind --without-dyninst --without-papi --without-binutils --with-java=$JAVA_HOME --prefix=%_extraehome --libdir=%_extraehome/lib
if [ $? -ne 0 ]; then
        echo "Error in COMPSs extrae package";
        exit 1;
fi

make
if [ $? -ne 0 ]; then
        echo "Error in COMPSs extrae package";
        exit 1;
fi

# Install compss-extrae
make install
if [ $? -ne 0 ]; then
        echo "Error in COMPSs extrae package";
        exit 1;
fi

cp -r cfgs %_extraehome

# Remove sources 
rm -rf %_extraehome/sources

echo " "
echo "COMPSs compss-extrae sucessfully installed!"
echo " "

%postun
rm -rf %_extraehome

%clean
rm -rf %buildroot 

%files 
%defattr(-,root,root)
%_extraehome

