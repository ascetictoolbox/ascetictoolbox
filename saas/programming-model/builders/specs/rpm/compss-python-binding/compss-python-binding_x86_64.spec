%define name	 compss-python-binding
%define release		1.linux
%define version 	1.2
%define _ithome /opt/COMPSs/Runtime/
%define _cbhome %_ithome/bindings/bindings-common
%define _pyhome %_ithome/bindings/python

Requires: compss-framework, compss-bindings-common, python >= 2.6
Summary: The BSC COMP Superscalar Python binding.
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
The BSC COMP Superscalar Python binding.

%prep
%setup -q

%build
# Bindings common
cd ./%_cbhome
./configure --prefix=`pwd` --libdir=`pwd`/lib
make

%install

# Bindings common
cd ./%_cbhome
make install
if [ $? -ne 0 ]; then
        echo "Error creating Python binding package";
        exit 1;
fi

# Python
mkdir -p %buildroot/%_pyhome
cd -
cd ./%_pyhome
./install %buildroot/%_ithome
if [ $? -ne 0 ]; then
        echo "Error creating Python binding package";
        exit 1;
fi
mkdir %buildroot/%_pyhome/bin
cp bin/worker_python.sh %buildroot/%_pyhome/bin

%post 
echo " "
echo "Installing COMPSs Python binding..."
echo " "

cp %_pyhome/bin/worker_python.sh %_ithome/scripts/system
chmod 755 %_ithome/scripts/system/worker_python.sh
rm -rf %_pyhome/bin

echo " "
echo "COMPSs Python binding sucessfully installed!"
echo " "

%postun
rm -rf %_pyhome
rm -f %_ithome/scripts/system/worker_python.sh

%clean
rm -rf %buildroot

%files 
%defattr(-,root,root)
%_pyhome

