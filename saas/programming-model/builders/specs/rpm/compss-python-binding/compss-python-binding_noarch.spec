%define name	 compss-python-binding
%define release		1.linux
%define version 	1.2
%define _ithome /opt/COMPSs/Runtime/
%define _pyhome %_ithome/bindings/python

Requires: compss-framework, compss-bindings-common, python >= 2.6, python-devel >= 2.6
Summary: The BSC COMP Superscalar Python binding.
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
The BSC COMP Superscalar Python binding.

%prep
%setup -q

%build

mkdir -p %buildroot/%_pyhome
cp -r ./%_pyhome/* %buildroot/%_pyhome

%install


%post 
echo " "
echo "Installing COMPSs Python binding..."
echo " "

cd %_pyhome
./install %_ithome
if [ $? -ne 0 ]; then
        echo "Error in Python binding package";
        exit 1;
fi

cp %_pyhome/bin/worker_python.sh %_ithome/scripts/system
chmod 755 %_ithome/scripts/system/worker_python.sh

rm -f  %_pyhome/setup.py %_pyhome/README.txt %_pyhome/LICENSE.txt %_pyhome/install
rm -rf %_pyhome/apps %_pyhome/src

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

