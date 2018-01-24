/**
 * RapidMiner PMML Extension
 *
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 *      http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.io.pmml.Association;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.pmml.AbstractPMMLObjectWriter;
import com.rapidminer.operator.io.pmml.PMMLVersion;
import com.rapidminer.operator.learner.associations.AssociationRule;
import com.rapidminer.operator.learner.associations.AssociationRules;
import com.rapidminer.operator.learner.associations.BooleanAttributeItem;
import com.rapidminer.operator.learner.associations.FrequentItemSet;
import com.rapidminer.operator.learner.associations.Item;


/**
 * A PMML Writer for AssociationRules. The DataDictionary, MiningSchema and Output are hardcoded
 * because they are not directly supported by RapidMiner. The Output will give you the three rules
 * with the highest confidence (see the http://dmg.org example for more information). You can
 * activate the debug function by changing debug = false to debug = true. This will give you more
 * information about all rules and their relationship.
 *
 * @author Miguel Buescher
 */
public class AssociationRulePMMLWriter extends AbstractPMMLObjectWriter {

	// fields used for pmml
	private AssociationRules associationRules;
	private Set<FrequentItemSet> itemSetSets = new HashSet<FrequentItemSet>();
	private Set<Item> itemsSet = new HashSet<Item>();

	// model attribute fields
	private double minimumSupport = Double.MAX_VALUE;
	private double minimumConfidence = Double.MAX_VALUE;

	public AssociationRulePMMLWriter(AssociationRules rules) {
		super();
		this.associationRules = rules;
	}

	// hardcoded MiningSchema
	private void createMiningSchema(Document pmmlDocument, Element modelElement, AssociationRules model) {
		Element miningSchema = createElement(pmmlDocument, modelElement, "MiningSchema");

		Element fieldElement1 = createElement(pmmlDocument, miningSchema, "MiningField");
		fieldElement1.setAttribute("name", "transaction");
		fieldElement1.setAttribute("usageType", "group");
		fieldElement1.setAttribute("optype", "categorical");
		Element fieldElement2 = createElement(pmmlDocument, miningSchema, "MiningField");
		fieldElement2.setAttribute("name", "item");
		fieldElement2.setAttribute("usageType", "predicted");
		fieldElement2.setAttribute("optype", "categorical");
	}

	// hardcoded DataDictionary
	private void createDataDictionary(Document pmml, Element father, AssociationRules trainingsSignature, PMMLVersion version) {

		Element dictionary = createElement(pmml, father, "DataDictionary");
		dictionary.setAttribute("numberOfFields", "2");
		Element dataFieldElement1 = createElement(pmml, dictionary, "DataField");
		dataFieldElement1.setAttribute("name", "transaction");
		dataFieldElement1.setAttribute("optype", "categorical");
		dataFieldElement1.setAttribute("dataType", "string");

		Element dataFieldElement2 = createElement(pmml, dictionary, "DataField");
		dataFieldElement2.setAttribute("name", "item");
		dataFieldElement2.setAttribute("optype", "categorical");
		dataFieldElement2.setAttribute("dataType", "string");
		for (Item item : itemsSet) {
			BooleanAttributeItem baItem = (BooleanAttributeItem) item;
			Element valueElement = createElement(pmml, dataFieldElement2, "Value");
			valueElement.setAttribute("value", baItem.getName());
		}

	}

	@Override
	public void appendBody(Document pmmlDocument, Element pmmlRoot, PMMLVersion version) throws UserError {

		// create AssociationModel specific parts
		Element modelBody = export(pmmlDocument, pmmlRoot, version);
		pmmlRoot.appendChild(modelBody);
	}

	public Element export(Document pmmlDocument, Element pmmlRoot, PMMLVersion version) throws UserError {

		// Construction of Items and ItemSets from AssociationRules
		preprocess(associationRules);

		// Create data dictionary based on AssociationRules NOT the ExampleSets --> atm only
		// ExampleSets based on models accepted
		// ExampleSet trainingsSignature = getTrainingHeader();
		this.createDataDictionary(pmmlDocument, pmmlRoot, associationRules, version);

		Element modelElement = pmmlDocument.createElement("AssociationModel");

		this.createMiningSchema(pmmlDocument, modelElement, associationRules);
		this.createOutput(pmmlDocument, modelElement); 							// hardcoded see below

		// Create all PMML Elements
		createItems(pmmlDocument, modelElement, itemsSet);						// Insert all Items into Pmml
		createItemSets(pmmlDocument, modelElement, itemSetSets); 				// Insert all ItemSets into Pmml
		createAssociationRules(pmmlDocument, modelElement, associationRules);  	// Insert all
		// AssoicationRules
		// into Pmml

		// create modelAttributes -> based on AssociationRules
		createModelAttributes(pmmlDocument, modelElement, associationRules);

		return modelElement;
	}

	private void createModelAttributes(Document pmmlDocument, Element modelElement, AssociationRules rules2) {

		// modelElement.setAttribute("modelName", "dummyAssociationRuleModelName"); // optional
		modelElement.setAttribute("functionName", "associationRules"); 											// required
		// modelElement.setAttribute("algorithmName", ""); // optional
		modelElement.setAttribute("numberOfTransactions", "0");									 				// required how to get?
		// modelElement.setAttribute("maxNumberOfItemsPerTA", ); // optional how to get?
		modelElement.setAttribute("avgNumberOfItemsPerTA", "0");												// required how to get?
		modelElement.setAttribute("minimumSupport", minimumSupport + ""); 										// required
		modelElement.setAttribute("minimumConfidence", minimumConfidence + "");									// required
		// modelElement.setAttribute("lengthLimit", ""); // optional how to get?
		modelElement.setAttribute("numberOfItems", itemsSet.size() + "");											// required
		modelElement.setAttribute("numberOfItemsets", itemSetSets.size() + "");									// required
		modelElement.setAttribute("numberOfRules", rules2.getNumberOfRules() + "");								// required
	}

	private void preprocess(AssociationRules rules2) {
		// Iterating rules and add Items/ItemSets to fields
		for (AssociationRule aRule : rules2) {
			addItemsFromRule(aRule); 			// add all items to item hashset field
			addItemSetsFromRule(aRule); 		// add all itemsets to itemset hashset field

			updateModelAttributes(aRule); 		// re-calculate all neccessary model attributes based on
			// this rule
		}
	}

	private void updateModelAttributes(AssociationRule rule) {

		double ruleSupport = rule.getTotalSupport();
		double ruleConfidence = rule.getConfidence();

		if (ruleSupport <= minimumSupport) {
			minimumSupport = ruleSupport;
		}

		if (ruleConfidence <= minimumConfidence) {
			minimumConfidence = ruleConfidence;
		}

	}

	private void createItems(Document pmmlDocument, Element modelElement, Set<Item> items2) {
		for (Item item : items2) {
			Element itemElement = createElement(pmmlDocument, modelElement, "Item");
			if (item instanceof BooleanAttributeItem) {
				BooleanAttributeItem booleanAttributeItem = (BooleanAttributeItem) item;
				itemElement.setAttribute("id", "Item" + getIdForItem(booleanAttributeItem) + "");	// required,
				// unique
				// id
				itemElement.setAttribute("value", booleanAttributeItem.getName() + ""); 			// required
				// itemElement.setAttribute("mappedValue",items2[i]. ""); // optional
				// itemElement.setAttribute("weight",items2[i]+"" ); // optional
			}
		}
	}

	private void createItemSets(Document pmmlDocument, Element modelElement, Set<FrequentItemSet> itemSets2) {
		for (FrequentItemSet aItemSet : itemSets2) {
			Element itemSetElement = createElement(pmmlDocument, modelElement, "Itemset");

			// Setting the Items in an ItemSet
			for (Item anItem : aItemSet.getItems()) {
				Element itemElement = createElement(pmmlDocument, itemSetElement, "ItemRef");
				itemElement.setAttribute("itemRef", "Item" + getIdForItem(anItem)); 	// required,
				// unique id
			}

			// Setting ItemSetAttributes
			itemSetElement.setAttribute("id", "Itemset" + getIdForItemSet(aItemSet) + ""); 							// required,
			// unique
			// id
			// itemSetElement.setAttribute("support", ""); // optional --> how calculate support?
			// [relative support of the Itemset: // support(set) = (number of transactions
			// containing the set) / (total number of transactions)]
			itemSetElement.setAttribute("numberOfItems", aItemSet.getNumberOfItems() + ""); 						// optional
		}
	}

	private void createAssociationRules(Document pmmlDocument, Element modelElement, AssociationRules rules2) {
		int ruleCounter = 1;
		for (AssociationRule aRule : rules2) {
			Element ruleElement = createElement(pmmlDocument, modelElement, "AssociationRule");
			ruleElement.setAttribute("antecedent", getItemSetForItems(aRule.getPremiseItems()));		// required
			ruleElement.setAttribute("consequent", getItemSetForItems(aRule.getConclusionItems())); // required
			ruleElement.setAttribute("support", aRule.getTotalSupport() + ""); 																		// required
			ruleElement.setAttribute("confidence", aRule.getConfidence() + ""); 																		// required
			ruleElement.setAttribute("lift", aRule.getLift() + ""); 																					// optional
			ruleElement.setAttribute("id", "Rule" + ruleCounter++); 																				// optional
		}
	}

	private void addItemsFromRule(AssociationRule rule) {
		Iterator<Item> premiseItems = rule.getPremiseItems();
		Iterator<Item> conclusionItems = rule.getConclusionItems();

		while (premiseItems.hasNext()) {
			Item it = premiseItems.next();
			itemsSet.add(it);
		}

		while (conclusionItems.hasNext()) {
			Item it = conclusionItems.next();
			itemsSet.add(it);
		}

	}

	private void addItemSetsFromRule(AssociationRule rule) {
		FrequentItemSet cachedPremises = new FrequentItemSet();
		FrequentItemSet cachedConclusions = new FrequentItemSet();

		Iterator<Item> i = rule.getPremiseItems();
		Iterator<Item> j = rule.getConclusionItems();

		while (i.hasNext()) {
			Item it = i.next();
			cachedPremises.addItem(it, it.getFrequency());
		}

		while (j.hasNext()) {
			Item it = j.next();
			cachedConclusions.addItem(it, it.getFrequency());
		}

		itemSetSets.add(cachedPremises);
		itemSetSets.add(cachedConclusions);
	}

	private String getItemSetForItems(Iterator<Item> iterator) {
		String itemSetId = "Itemset";

		FrequentItemSet i = new FrequentItemSet();

		while (iterator.hasNext()) {
			Item item = iterator.next();
			i.addItem(item, item.getFrequency());
		}

		itemSetId = "Itemset" + this.getIdForItemSet(i) + "";

		return itemSetId;
	}

	private int getIdForItemSet(FrequentItemSet itemSet) {
		if (itemSet == null) {
			return -1;
		}

		if (itemSetSets == null) {
			return -1;
		}

		int counter = 0;
		for (FrequentItemSet itSet : itemSetSets) {
			counter++;
			if (itSet.equals(itemSet)) {
				return counter;
			}
		}
		return -1;
	}

	private int getIdForItem(Item item) {
		if (item == null) {
			return -1;
		}

		if (itemsSet == null) {
			return -1;
		}

		int counter = 0;
		for (Item setItem : itemsSet) {
			counter++;
			if (setItem.equals(item)) {
				return counter;
			}
		}
		return -1;
	}

	// Implementation of output is hardcoded as not directly supported in RM -> see example PMML-HP
	private void createOutput(Document pmmlDocument, Element modelElement) {
		String[] ruleNames = { "recommendation", "confidence" };
		String[] ruleComponentNames = { "consequent", "confidence" };

		Element outputElement = createElement(pmmlDocument, modelElement, "Output");

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < ruleNames.length; j++) {
				Element outputFieldElement1 = createElement(pmmlDocument, outputElement, "OutputField");
				outputFieldElement1.setAttribute("name", ruleNames[j] + "_" + String.valueOf(i + 1));
				outputFieldElement1.setAttribute("rankBasis", "confidence");
				outputFieldElement1.setAttribute("rank", String.valueOf(i + 1));
				outputFieldElement1.setAttribute("algorithm", "exclusiveRecommendation");
				outputFieldElement1.setAttribute("feature", "ruleValue");
				outputFieldElement1.setAttribute("ruleFeature", ruleComponentNames[j]);
				outputFieldElement1.setAttribute("dataType", "string");
			}
		}
	}

	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}
}
