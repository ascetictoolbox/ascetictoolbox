%define name	 compss-c-binding
%define release		1.linux
%define version 	1.2
%define _ithome /opt/COMPSs/Runtime/
%define _cbhome %_ithome/bindings/bindings-common
%define _cpphome %_ithome/bindings/c

Requires: compss-framework, compss-bindings-common, java >= 1.6.0, boost-devel
Summary: The BSC COMP Superscalar C binding.
Name: %{name}
Version: %{version}
Release: %{release}
Source: %{name}-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: %{_tmppath}/%{name}-%{version}-buildroot
Prefix: %{_prefix}
BuildArch: x86_64
URL: http://www.bsc.es/compss
Packager: Enric Tejedor <enric.tejedor@bsc.es>

%description
The BSC COMP Superscalar C binding.

%prep
%setup -q

%build
# Bindings common
cd ./%_cbhome
./configure --prefix=`pwd` --libdir=`pwd`/lib
if [ $? -ne 0 ]; then
        echo "Error creating C binding package";
        exit 1;
fi

make
if [ $? -ne 0 ]; then
        echo "Error creating C binding package";
        exit 1;
fi

%install

# Bindings common
cd ./%_cbhome
make install
if [ $? -ne 0 ]; then
        echo "Error creating C binding package";
        exit 1;
fi

# C
mkdir -p %buildroot/%_cpphome
cd -
cd ./%_cpphome
./install %buildroot %_cpphome
if [ $? -ne 0 ]; then
        echo "Error creating C binding package";
        exit 1;
fi
cp bin/worker_c.sh %buildroot/%_cpphome/bin

%post 
echo " "
echo "Installing COMPSs C binding..."
echo " "

chmod 755 %_cpphome/bin/*
cp %_cpphome/bin/worker_c.sh %_ithome/scripts/system
rm -f %_cpphome/bin/worker_c.sh

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

