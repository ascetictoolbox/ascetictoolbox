/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.ovf.api;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.xmlbeans.XmlOptions;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfDefinitionTest extends TestCase {

	public void testOvfDefinition() {
		OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance(
				"threeTierWebApp", "/DFS/ascetic/vm-images/3tierweb");

		// Global product details

		// Stores the Application's ID
		String applicationId = ovfDefinition.getVirtualSystemCollection()
				.getId();
		assertNotNull(applicationId);
		;
		ovfDefinition.getVirtualSystemCollection().getProductSectionAtIndex(0)
				.setDeploymentId("101");
		String deploymentId = ovfDefinition.getVirtualSystemCollection()
				.getProductSectionAtIndex(0).getDeploymentId();
		assertNotNull(deploymentId);

		// @formatter:off
		ovfDefinition.getVirtualSystemCollection().getProductSectionAtIndex(0)
				.setSecurityKeys("\n        " +
				"-----BEGIN PUBLIC KEY-----\n        " +
				"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0\n        " +
				"FPqri0cb2JZfXJ/DgYSF6vUpwmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/\n        " +
				"3j+skZ6UtW+5u09lHNsj6tQ51s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQAB\n        " +
				"-----END PUBLIC KEY-----\n        " +
				"-----BEGIN RSA PRIVATE KEY-----\n        " +
				"MIICXAIBAAKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0FPqri0cb2JZfXJ/DgYSF6vUp\n        " +
				"wmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/3j+skZ6UtW+5u09lHNsj6tQ5\n        " +
				"1s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQABAoGAFijko56+qGyN8M0RVyaRAXz++xTqHBLh\n        " +
				"3tx4VgMtrQ+WEgCjhoTwo23KMBAuJGSYnRmoBZM3lMfTKevIkAidPExvYCdm5dYq3XToLkkLv5L2\n        " +
				"pIIVOFMDG+KESnAFV7l2c+cnzRMW0+b6f8mR1CJzZuxVLL6Q02fvLi55/mbSYxECQQDeAw6fiIQX\n        " +
				"GukBI4eMZZt4nscy2o12KyYner3VpoeE+Np2q+Z3pvAMd/aNzQ/W9WaI+NRfcxUJrmfPwIGm63il\n        " +
				"AkEAxCL5HQb2bQr4ByorcMWm/hEP2MZzROV73yF41hPsRC9m66KrheO9HPTJuo3/9s5p+sqGxOlF\n        " +
				"L0NDt4SkosjgGwJAFklyR1uZ/wPJjj611cdBcztlPdqoxssQGnh85BzCj/u3WqBpE2vjvyyvyI5k\n        " +
				"X6zk7S0ljKtt2jny2+00VsBerQJBAJGC1Mg5Oydo5NwD6BiROrPxGo2bpTbu/fhrT8ebHkTz2epl\n        " +
				"U9VQQSQzY1oZMVX8i1m5WUTLPz2yLJIBQVdXqhMCQBGoiuSoSjafUhV7i1cEGpb88h5NBYZzWXGZ\n        " +
				"37sJ5QsW+sJyoNde3xH8vdXhzU7eT82D6X/scw9RZz+/6rCJ4p0=\n        " +
				"-----END RSA PRIVATE KEY-----");
		// @formatter:on

		ovfDefinition
				.getVirtualSystemCollection()
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticWorkloadVmId",
						ProductPropertyType.STRING, "jmeter");
		ovfDefinition
				.getVirtualSystemCollection()
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticWorkloadType",
						ProductPropertyType.STRING, "user-count");
		ovfDefinition
				.getVirtualSystemCollection()
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticWorkloadRange",
						ProductPropertyType.STRING, "10-200");
		ovfDefinition
				.getVirtualSystemCollection()
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticWorkloadIncrement",
						ProductPropertyType.STRING, "10");
		ovfDefinition
				.getVirtualSystemCollection()
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticWorkloadInterval",
						ProductPropertyType.STRING, "1min");

		// Virtual Machine product details

		// Stores the Virtual Machine's ID
		String virtualMachineId = ovfDefinition.getVirtualSystemCollection()
				.getVirtualSystemAtIndex(1).getId();
		assertNotNull(virtualMachineId);
		ovfDefinition
				.getVirtualSystemCollection()
				.getVirtualSystemAtIndex(1)
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticProbeUri-1",
						ProductPropertyType.STRING,
						"uri://some-end-point/application-monitor");
		String probeUri = ovfDefinition.getVirtualSystemCollection()
				.getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
				.getPropertyByKey("asceticProbeUri-1").getValue();
		assertNotNull(probeUri);
		ovfDefinition
				.getVirtualSystemCollection()
				.getVirtualSystemAtIndex(1)
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticProbeType-1",
						ProductPropertyType.STRING, "cpu");
		ovfDefinition
				.getVirtualSystemCollection()
				.getVirtualSystemAtIndex(1)
				.getProductSectionAtIndex(0)
				.addNewProperty("asceticProbeInterval-1",
						ProductPropertyType.STRING, "1sec");

		System.out.println(ovfDefinition.toString());

		writeToFile(ovfDefinition.getXmlObject(), "3tier-webapp.ovf");
	}

	protected void writeToFile(XmlBeanEnvelopeDocument ovfDefinition,
			String fileName) {
		try {
			// If system property is not set (i.e. test case was started from
			// IDE )
			// we use the current directory to store the file
			String targetDir = System.getProperty("ovfSampleDir", "target");

			File file = new File(targetDir + File.separator + File.separator
					+ fileName + ".xml");
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(ovfDefinition.xmlText(new XmlOptions()
					.setSavePrettyPrint()));
			System.out.println(fileName + ".xml was written to "
					+ file.getAbsolutePath());
			// Close the output stream
			out.close();
		} catch (Exception e) {
			// Catch exception if any
			System.err.println("Error: " + e.getMessage());
			fail(e.getMessage());
		}
	}
}
