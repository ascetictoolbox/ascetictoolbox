/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

var WshShell = WScript.CreateObject("WScript.Shell");
var CompName = WScript.Arguments.Item(0);
WshShell.RegDelete ("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\Hostname");
WshShell.RegDelete ("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\NV Hostname");
WshShell.RegWrite ("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Computername\\Computername\\Computername", CompName, "REG_SZ");
WshShell.RegWrite ("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Computername\\ActiveComputername\\Computername", CompName, "REG_SZ");
WshShell.RegWrite ("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\Hostname", CompName, "REG_SZ");
WshShell.RegWrite ("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\NV Hostname", CompName, "REG_SZ");
WshShell.RegWrite ("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Winlogon\\AltDefaultDomainName", CompName, "REG_SZ");
WshShell.RegWrite ("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Winlogon\\DefaultDomainName", CompName, "REG_SZ");
WshShell.RegWrite ("HKEY_USERS\\.Default\\Software\\Microsoft\\Windows Media\\WMSDK\\General\\Computername", CompName, "REG_SZ");