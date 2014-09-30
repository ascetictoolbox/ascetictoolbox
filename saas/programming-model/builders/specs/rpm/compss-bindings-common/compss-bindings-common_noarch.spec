%define name	 compss-bindings-common 
%define release		1.linux
%define version 	1.2
%define _ithome	/opt/COMPSs/Runtime/
%define _cbhome	%_ithome/bindings/bindings-common

Requires: compss-framework, java >= 1.6.0, java-devel >= 1.6.0, libtool, automake, make, gcc-c++ 
Summary: C libraries shared by the COMPSs bindings.
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
C libraries shared by the COMPSs bindings.

%prep
%setup -q

%build

mkdir -p %buildroot/%_cbhome/sources
cp -r ./%_cbhome/* %buildroot/%_cbhome/sources

mkdir %buildroot/%_cbhome/include
cp ./%_cbhome/src/*.h %buildroot/%_cbhome/include

%install

%post 

echo " "
echo "Installing COMPSs bindings-common..."
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

# Build bindings common
cd %_cbhome/sources
./configure --prefix=%_cbhome --libdir=%_cbhome/lib
if [ $? -ne 0 ]; then
        echo "Error in COMPSs bindings-common package";
        exit 1;
fi

make
if [ $? -ne 0 ]; then
        echo "Error in COMPSs bindings-common package";
        exit 1;
fi

# Install bindings common
make install
if [ $? -ne 0 ]; then
        echo "Error in COMPSs bindings-common package";
        exit 1;
fi

# Remove sources 
rm -rf %_cbhome/sources

echo " "
echo "COMPSs bindings-common sucessfully installed!"
echo " "

%postun
rm -rf %_cbhome

%clean
rm -rf %buildroot 

%files 
%defattr(-,root,root)
%_cbhome

