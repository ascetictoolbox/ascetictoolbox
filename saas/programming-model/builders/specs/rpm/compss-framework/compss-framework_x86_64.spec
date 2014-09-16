%define name	 compss-framework 
%define release		1.linux
%define version 	1.2

Requires: java >= 1.6.0, xdg-utils, graphviz
Summary: The BSC COMP Superscalar Framework 
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
Packager: Roger Rafanell <roger.rafanell@bsc.es>

%description
The BSC COMP Superscalar Framework.

%prep
%setup -q

%build
find . -name .svn -print0 | xargs -0 rm -rf

echo " * Building COMP Superscalar Framework..."
echo " "
export IT_HOME=$PWD/opt/COMPSs
export GAT_LOCATION=$IT_HOME/JAVA_GAT
cd opt/COMPSs/Runtime/rt/compss/
mvn clean package
echo " "

mv compss-rt/rt/target/lib ..
mv compss-rt/rt/target/*.jar ..
mv compss-monitor/target/*.war ../../../Monitor

#Copying included connectors
mv compss-connectors/emotive/target/compss-*.jar ../../../Runtime/rt/connectors/
mv compss-connectors/one/target/compss-*.jar ../../../Runtime/rt/connectors/
mv compss-connectors/rocci/target/compss-*.jar ../../../Runtime/rt/connectors/
mv compss-connectors/amazon/target/compss-*.jar ../../../Runtime/rt/connectors/
mv compss-connectors/azure/target/compss-*.jar ../../../Runtime/rt/connectors/

cd ..
rm -rf compss

cd ../../doc
rm -rf *.html

%install

cp -r opt/ $RPM_BUILD_ROOT/
cp -r etc/ $RPM_BUILD_ROOT/

%post 
echo " * Setting COMPSs permissions..."
chmod 755 -R /opt/COMPSs/
chmod 777 -R /opt/COMPSs/Runtime/xml/

%postun 
rm -rf /opt/COMPSs/
rm -f /etc/profile.d/compss.sh

%clean
rm -rf $RPM_BUILD_ROOT

%files 
%defattr(-,root,root)
/opt/COMPSs/
/etc/profile.d/compss.sh
