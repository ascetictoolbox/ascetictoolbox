%define name	 compss-extrae 
%define release		1.linux
%define version 	1.2
%define _extraehome	/opt/COMPSs/extrae

Requires: compss-framework, java >= 1.6.0, libxml2 >= 2.5.0, libxml2-devel >= 2.5.0
Summary: Extrae trace extraction tool.
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
Extrae trace extraction tool.

%prep
%setup -q

%build

mkdir -p %buildroot/%_extraehome
cd ./%_extraehome/sources
./configure --enable-gettimeofday-clock --without-mpi --without-unwind --without-dyninst --without-papi --without-binutils --with-java=$JAVA_HOME --prefix=%buildroot/%_extraehome --libdir=%buildroot/%_extraehome/lib
if [ $? -ne 0 ]; then
        echo "Error creating COMPSs extrae package";
        exit 1;
fi

make
if [ $? -ne 0 ]; then
        echo "Error creating COMPSs extrae package";
        exit 1;
fi


%install

cd ./%_extraehome/sources
make install
if [ $? -ne 0 ]; then
        echo "Error creating COMPSs extrae package";
        exit 1;
fi

cp -r cfgs %buildroot/%_extraehome 


%post

echo " "
echo "Installing COMPSs compss-extrae..."
echo " "

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

