#!/usr/bin/expect
 
#Usage sshsudologin.expect <host> <ssh user> <ssh password> <su user> <su password>
 
set timeout 60
 
spawn ssh $USER@$ILO_IP -oHostKeyAlgorithms=ssh-rsa 
 
expect "yes/no" { 
    send "yes\r"
    expect "*?assword" { send "$PASS/r" }
    } "*?assword" { send "$PASS\r" }
expect "?*->" { send "show system1 oemhp_PresentPower\r" } 
expect "?*->" { send "exit\r" }
interact
