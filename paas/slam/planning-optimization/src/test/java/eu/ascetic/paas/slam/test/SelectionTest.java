/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.ascetic.paas.slam.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.impl.provider.selection.OfferSelector;
import eu.ascetic.paas.slam.poc.impl.provider.selection.OfferSelectorImpl;
import eu.ascetic.paas.slam.poc.impl.provider.selection.WeightedSlat;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplate;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplateParser;


public class SelectionTest {

	OfferSelector selectionAlgorithm;
	static SLATemplate proposal, proposalDummy, proposalFull;
	static List<SLATemplate> offersList, offersListDummy;
	
	
	
	@Before
	public void setUp() throws Exception {
		
	    SLASOITemplateParser tp = new SLASOITemplateParser();

		String xmlProposal = FileUtils.readFileToString(
    			new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSRequest.xml"));

	    proposal = tp.parseTemplate(xmlProposal);
		
	    offersList = new ArrayList<SLATemplate>();
	    
		String xmlOffer = FileUtils.readFileToString(
	    			new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSOffer.xml"));
		offersList.add(tp.parseTemplate(xmlOffer));
		String xmlOffer2 = FileUtils.readFileToString(
    			new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSOffer2.xml"));
		offersList.add(tp.parseTemplate(xmlOffer2));
		String xmlOffer3 = FileUtils.readFileToString(
    			new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSOffer3.xml"));
		offersList.add(tp.parseTemplate(xmlOffer3));

	    proposalDummy = tp.parseTemplate(FileUtils.readFileToString(
    			new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSRequest.xml")));

	    offersListDummy = new ArrayList<SLATemplate>();
	    offersListDummy.add(tp.parseTemplate(FileUtils.readFileToString(
	    	new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSOffer.xml"))));
	    
		String xmlProposalFull = FileUtils.readFileToString(
    			new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSRequest.xml"));

	    proposalFull = tp.parseTemplate(xmlProposalFull);


	}

	
	@After
	public void tearDown() throws Exception {
		System.out.println("\n---- end of test ----");	
	}

	
	@Test
	public void parseProposalSimple() throws Exception {
		String xmlProposal = FileUtils.readFileToString(
    			new File("src/test/resources/slats/selection/SlatProposalSimple.xml"));
		SLASOITemplateParser tp = new SLASOITemplateParser();
	    proposal = tp.parseTemplate(xmlProposal);
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(proposal);
		System.out.println("PROPOSAL: " + cst);
		Assert.assertNotNull(cst.getVirtualSystems());
	}


	@Test
	public void parseProposalDummy() throws Exception {
		String xmlProposal = FileUtils.readFileToString(new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSRequest.xml"));
		SLASOITemplateParser tp = new SLASOITemplateParser();
	    proposal = tp.parseTemplate(xmlProposal);
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(proposal);
		System.out.println("PROPOSAL: " + cst);
		Assert.assertNotNull(cst.getVirtualSystems());
	}


	@Test
	public void parseOfferDummy() throws Exception {
		String xmlProposal = FileUtils.readFileToString(new File("src/test/resources/slats/selection/ASCETiC-SlaTemplateIaaSOffer.xml"));
		SLASOITemplateParser tp = new SLASOITemplateParser();
	    proposal = tp.parseTemplate(xmlProposal);
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(proposal);
		System.out.println("OFFER: " + cst);
		Assert.assertNotNull(cst.getVirtualSystems());
	}

	
	@Test
	public void parseOfferIntegrationTest() throws Exception {
		String slat = FileUtils.readFileToString(
    			new File("src/test/resources/slats/SlatOfferIntegrationTest.xml"));
		SLASOITemplateParser tp = new SLASOITemplateParser();
	    proposal = tp.parseTemplate(slat);
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(proposal);
		System.out.println("PROPOSAL: " + cst);
		Assert.assertNotNull(cst.getVirtualSystems());
	}

	
	@Test
	public void parseProposal() {
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(proposal);
		System.out.println("PROPOSAL: " + cst);
		Assert.assertNotNull(cst.getVirtualSystems());
	}

	
	@Test
	public void parseOffers() {
		int i = 0;
		for (SLATemplate offer : offersList) {
			AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(offer);
			System.out.println("\n----- OFFER[" + i++ + "] ----- " + cst);
			Assert.assertNotNull(cst.getVirtualSystems());
		}
	}

	
	@Test
	public void offerSelectorImplTest() {
		selectionAlgorithm = new OfferSelectorImpl();
		SLATemplate[] bestSlats = selectionAlgorithm
				.selectOptimaSlaTemplates(offersList, proposal);
		
		Assert.assertEquals(offersList.get(0), bestSlats[2]);
		Assert.assertEquals(offersList.get(1), bestSlats[1]);
		Assert.assertEquals(offersList.get(2), bestSlats[0]);

	}
	
	
	
	@Test
	public void orderTest() throws SecurityException, NoSuchFieldException {
		List<WeightedSlat> weightedSlats = new ArrayList<WeightedSlat>();
		weightedSlats.add(new WeightedSlat(null, 20));
		weightedSlats.add(new WeightedSlat(null, 5));
		weightedSlats.add(new WeightedSlat(null, 1));
		weightedSlats.add(new WeightedSlat(null, 3));
		weightedSlats.add(new WeightedSlat(null, 10));
		Collections.sort(weightedSlats, new WeightedSlat.WeightComparator());
		Assert.assertEquals((int) weightedSlats.get(0).getWeight(), 1);
		Assert.assertEquals((int) weightedSlats.get(4).getWeight(), 20);
	}
	
	

	
}
