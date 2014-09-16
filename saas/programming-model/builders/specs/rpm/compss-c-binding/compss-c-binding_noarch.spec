%define name	 compss-c-binding
%define release		1.linux
%define version 	1.2
%define _ithome /opt/COMPSs/Runtime/
%define _cbhome %_ithome/bindings/bindings-common
%define _cpphome %_ithome/bindings/c

Requires: compss-framework, compss-bindings-common, java >= 1.6.0, java-devel >= 1.6.0, libxml2-devel, libtool, automake, make, boost-devel, tcsh, gcc-c++
Summary: The BSC COMP Superscalar C binding.
Name: %{name}
Version: %{version}
Release: %{release}
Source: %{name}-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: %{_tmppath}/%{name}-%{version}-buildroot
Prefix: %{_prefix}
BuildArch: noarch 
URL: http://www.bsc.es/compss
Packager: Enric Tejedor <enric.tejedor@bsc.es>

%description
The BSC COMP Superscalar C binding.

%prep
%setup -q

%build

mkdir -p %buildroot/%_cpphome
cp -r ./%_cpphome/* %buildroot/%_cpphome
rm -rf  %buildroot/%_cpphome/apps
rm -f %buildroot/%_cpphome/buildapp
find %buildroot/%_cpphome -type d -name .svn -exec rm -rf {} +

%install


%post 
echo " "
echo "Installing COMPSs C binding..."
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

# Install binding
cd %_cpphome
./install / %_cpphome
if [ $? -ne 0 ]; then
        echo "Error in C binding package";
        exit 1;
fi

chmod 755 %_cpphome/bin/*
cp %_cpphome/bin/worker_c.sh %_ithome/scripts/system
rm -f %_cpphome/bin/worker_c.sh

rm -r  %_cpphome/README.txt %_cpphome/LICENSE.txt %_cpphome/install
rm -rf %_cpphome/src

echo " "
echo "COMPSs C binding sucessfully installed!"
echo " "

%postun
rm -rf %_cpphome
rm -f %_ithome/scripts/system/worker_c.sh

%clean
rm -rf %buildroot

%files 
%defattr(-,root,root)
%_cpphome

