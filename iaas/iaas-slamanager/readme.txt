SKELETON - SLAM
(@author:  Miguel Rojas - miguel.rojas@uni-dortmund.de)

This project aims to provide a generic structure of modules commonly used for 
the easy implementation of any domain specific SLAM, adopting the SLASOI framework.

Its structure is listed in following table:

---------------------------------------------------------------------------
iaas-slamanager ..................................... project (container)
iaas-main ........................................... slam  (main bundle)
iaas-core ........................................... core
iaas-planning-optimization .......................... poc
iaas-provisioning-adjustment ........................ pac
----------------------------------------------------------------------------

Each module contains following generic files:
	- readme.txt :  this file contains basic information
					about the module  
	- pom.xml :  contains maven definitions and provides
				 basic functionality for assisting on the
				 artifact bundelization.
	- spring/osgi files:  those files are located under
	              src\main\resources\META-INF\spring. 
				  Those files allow the definition of beans 
				  and osgi-services dependencies
				  
		            


SUMMARY OF FEATURES 
====================

** iaas slam-main
----------------------
This module is responsible for:
    .  Loading of its properties file (using generic-slam services)
	.  Invoking generic-slam services for creation of generic components.
	   The result of this invocation will be a referece to a SLAManagerContext
	.  Linking and Injection of domain specific PAC and POC into the
	   SLAManagerContext

** iaas slam-core
-------------------
	.  This module contains interfaces and classes that can be used by any
	   module in nonameslam project.  
	   
** iaas slam-planning-optimization
-------------------------------------
	.  This is the domain specific POC implementation. The injection of this
	   component will be handled by the main bundle (iaas-slam4osgi).
	
** iaas slam-provisioning-adjustment
-------------------------------------
	.  This is the domain specific PAC implementation. The injection of this
	   component will be handled by the main bundle (iaas-slam4osgi).


	   
====================== 
  *** HOW TO USE ***
======================

The iaas-SLAM has been designed in such way, it can be reused easily and with
minor effort for implementing a new SLA-manager.  To do that, follow next maven command:

*  Skeleton Maven Plugin:  
		mvn skeleton:generate
