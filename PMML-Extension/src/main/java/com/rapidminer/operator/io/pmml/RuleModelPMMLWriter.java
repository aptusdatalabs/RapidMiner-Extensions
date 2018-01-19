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
package com.rapidminer.operator.io.pmml;

import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.example.Attribute;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.rules.Rule;
import com.rapidminer.operator.learner.rules.RuleModel;
import com.rapidminer.operator.learner.tree.SplitCondition;

/**
 * This is the writer which outputs rule models as pmml
 * @author Sebastian Land
 *
 */
public class RuleModelPMMLWriter extends AbstractSplitBasedModelPMMLWriter {

	private RuleModel model;
	
	public RuleModelPMMLWriter(RuleModel model) {
		super(model);
	
		this.model = model;
	}

	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
		Element modelElement = pmmlDocument.createElement("RuleSetModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", "classification");
		modelElement.setAttribute("algorithmName", "DecisionRule");
		
		// creating mining schema
		createMiningSchema(pmmlDocument, modelElement, model);
		
		// creating output and target values
		createOutput(pmmlDocument, modelElement, model);
		createTargetValues(pmmlDocument, modelElement, model);
		
		// creating rule model specific parts
		createRuleSet(pmmlDocument, modelElement, model);
		
		return modelElement;
	}
	
	
	private void createRuleSet(Document pmmlDocument, Element modelElement, RuleModel model) {
		Element ruleSetElement = createElement(pmmlDocument, modelElement, "RuleSet");
		// comitting optional counts, because we don't have this information
		
		// setting selection method
		Element ruleSelectionMethod = createElement(pmmlDocument, ruleSetElement, "RuleSelectionMethod");
		ruleSelectionMethod.setAttribute("criterion", "firstHit");
		
		// extracting labels
		Attribute label = model.getTrainingHeader().getAttributes().getLabel();
		String[] labels = new String[label.getMapping().size()];
		int i = 0;
		for (String labelValue: label.getMapping().getValues()) {
			labels[i] = labelValue;
			i++;
		}
		
		// writing rules
		for (Rule rule: model.getRules()) {
			Element ruleElement = createElement(pmmlDocument, ruleSetElement, "SimpleRule");
			
			// general information about the rule
			String ruleLabel = rule.getLabel();
			int[] frequencies = rule.getFrequencies();
			int labelIndex = getLabelIndex(ruleLabel, labels);
			int totalExamples = 0;
			for(int frequence: frequencies)
				totalExamples += frequence;
			ruleElement.setAttribute("score", ruleLabel);
			ruleElement.setAttribute("recordCount", totalExamples + "");
			ruleElement.setAttribute("nbCorrect", frequencies[labelIndex] + "");
			ruleElement.setAttribute("confidence", ((double)frequencies[labelIndex]) / totalExamples + "");
			
			// the rule itself
			if (rule.getTerms().size() != 0) {
				// checking if and is needed if predicate is longer than 1 condition
				Element predicateContainer = ruleElement;
				if (rule.getTerms().size() > 1) {
					Element andElement = createElement(pmmlDocument, ruleElement, "CompoundPredicate");
					andElement.setAttribute("booleanOperator", "and");
					predicateContainer = andElement;
				}

				for (SplitCondition term: rule.getTerms()) {
					createSimplePredicate(pmmlDocument, predicateContainer, term);
				}
			} else {
				createElement(pmmlDocument, ruleElement, "True");
			}
		}
	}

	private int getLabelIndex(String ruleLabel, String[] labels) {
		for (int i = 0; i < labels.length; i++) {
			if (labels[i].equals(ruleLabel))
				return i;
		}
		return 0;
	}

	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}






}
