-----------------------------------------------------------------
-----------------------------------------------------------------
Install the scripts to the following directories:

	* network_setup.sh   :   /opt/vmc/
	* vmc_network_setup.service   :   /etc/systemd/system/

Make the files executable

	chmod u+x /opt/vmc/network_setup.sh
	chmod u+x /etc/systemd/system/vmc_network_setup.service 

Install the script in Systemd: 

	systemctl start vmc_network_setup.service
	systemctl enable vmc_network_setup.service

-----------------------------------------------------------------
-----------------------------------------------------------------

Upgrade Instructions init.d to systemd:

Uninstall the previous init.d config file.

	update-rc.d context_network disable

Delete the file

	rm /etc/init.d/context_network

Go through the normal setup instructions for systemd. 

-----------------------------------------------------------------
-----------------------------------------------------------------

Notes: If the system hasn't contextulised correctly you can run the contextulisation script manually, restart networking and log out and then back in again.

/etc/init.d/context_network
/etc/init.d/networking restart
exit



