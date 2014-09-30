%define name	 compss-bindings-common 
%define release		1.linux
%define version 	1.2
%define _ithome	/opt/COMPSs/Runtime/
%define _cbhome	%_ithome/bindings/bindings-common

Requires: compss-framework, java >= 1.6.0
Summary: C libraries shared by the COMPSs bindings.
Name: %{name}
Version: %{version}
Release: %{release}
Source: %{name}-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildArch: x86_64
BuildRoot: %{_tmppath}/%{name}-%{version}-buildroot
Prefix: %{_prefix}
URL: http://www.bsc.es/compss
Packager: Enric Tejedor <enric.tejedor@bsc.es>

%description
C libraries shared by the COMPSs bindings.

%prep
%setup -q

%build

mkdir -p %buildroot/%_cbhome
cd .%_cbhome

./configure --prefix=%buildroot/%_cbhome --libdir=%buildroot/%_cbhome/lib
if [ $? -ne 0 ]; then
        echo "Error in COMPSs bindings-common package";
        exit 1;
fi

make
if [ $? -ne 0 ]; then
        echo "Error in COMPSs bindings-common package";
        exit 1;
fi


%install

cd .%_cbhome 
make install
if [ $? -ne 0 ]; then
        echo "Error in COMPSs bindings-common package";
        exit 1;
fi


%post

echo " "
echo "Installing COMPSs bindings-common..."
echo " "

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

