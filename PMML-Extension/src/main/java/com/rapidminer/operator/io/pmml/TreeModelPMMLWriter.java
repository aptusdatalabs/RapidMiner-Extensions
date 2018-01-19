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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.tree.Edge;
import com.rapidminer.operator.learner.tree.Tree;
import com.rapidminer.operator.learner.tree.TreeModel;
import com.rapidminer.tools.LogService;

/**
 * This is the writer for Decision Tree models
 * 
 * @author Sebastian Land
 */
public class TreeModelPMMLWriter extends AbstractSplitBasedModelPMMLWriter {

	private TreeModel model;

	public TreeModelPMMLWriter(TreeModel model) {
		super(model);
		
		this.model = model;
	}
	
	

	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
		Element modelElement = pmmlDocument.createElement("TreeModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", "classification");
		modelElement.setAttribute("algorithmName", "DecisionTree");
		modelElement.setAttribute("missingValueStrategy", "lastPrediction");
		modelElement.setAttribute("noTrueChildStrategy", "returnLastPrediction");
		modelElement.setAttribute("splitCharacteristic", "multiSplit");
		
		// creating mining schema
		createMiningSchema(pmmlDocument, modelElement, model);
		
		// creating output and target values
		createOutput(pmmlDocument, modelElement, model);
		createTargetValues(pmmlDocument, modelElement, model);
		
		// creating rule model specific parts
		LogService.getRoot().log(Level.INFO, "Treeeeeeeeeeeee" +model.getRoot());
		createTree(pmmlDocument, modelElement, model, model.getRoot(), null);
		
		return modelElement;
	}

	/**
	 * This recursively called method will generate the hole tree by constructing 
	 * an element per node, attaching this to the father element and letting recursively 
	 * attach all children to itself. 
	 */
	private void createTree(Document pmmlDocument, Element fatherElement, TreeModel model, Tree tree, Edge incomingEdge) {
		// label distribution in current node
		Map<String, Integer> counterMap = tree.getSubtreeCounterMap();
		double totalCounts = 0;
		for (Map.Entry<String, Integer> entry: counterMap.entrySet()) {
			totalCounts += entry.getValue();
		}

		// creating node element
		Element nodeElement = createElement(pmmlDocument, fatherElement, "Node");
		if (tree.getLabel() != null)
			nodeElement.setAttribute("score", tree.getLabel());
		else {  // most frequent label has to be found
			int maxCount = 0;
			for (Map.Entry<String, Integer> entry: counterMap.entrySet()) {
				if (entry.getValue() > maxCount) {
					nodeElement.setAttribute("score", entry.getKey());
					maxCount = entry.getValue();
				}
			}
		}
		nodeElement.setAttribute("recordCount", tree.getFrequencySum() + "");

		// condition of this node
		if (incomingEdge == null) {  // then its root
			createElement(pmmlDocument, nodeElement, "True");
		} else {
			createSimplePredicate(pmmlDocument, nodeElement, incomingEdge.getCondition());
		}

		// setting label distribution info
		for (String label: counterMap.keySet()) {
			Element labelDistributionElement = createElement(pmmlDocument, nodeElement, "ScoreDistribution");
			labelDistributionElement.setAttribute("value", label);
			labelDistributionElement.setAttribute("recordCount", counterMap.get(label) + "");
			labelDistributionElement.setAttribute("confidence", counterMap.get(label) / totalCounts + "");
		}

		// recursivly add children
		Iterator<Edge> childIterator = tree.childIterator();
		while (childIterator.hasNext()) {
			Edge edge = childIterator.next();
			Tree child = edge.getChild();
			createTree(pmmlDocument, nodeElement, model, child, edge);
		}

	}

	
	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}
}
