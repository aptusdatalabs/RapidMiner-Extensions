package com.rapidminer.operator.io.pmml;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.tree.Edge;
import com.rapidminer.operator.learner.tree.ConfigurableRandomForestModel;
import com.rapidminer.operator.learner.tree.Tree;
import com.rapidminer.operator.learner.tree.TreeModel;
import com.rapidminer.tools.LogService;

public class RandomForestPMMLWriter extends AbstractSplitBasedModelPMMLWriter {

	private ConfigurableRandomForestModel model;

	public RandomForestPMMLWriter(ConfigurableRandomForestModel model) {
		super(model);

		this.model = model;
	}

	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
		Element modelElement = null;
		modelElement = pmmlDocument.createElement("MiningModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", "classification");
		createMiningSchema(pmmlDocument, modelElement, model);
	
		Element miningmodel = createElement(pmmlDocument, modelElement, "Segmentation");
        miningmodel.setAttribute("multipleModelMethod", "majorityVote");
		// Create Segementation	
		
		for (int i = 0; i < model.getModels().size(); i++) {

			// creating rule model specific parts
			    TreeModel md1 = (TreeModel) model.getModels().get(i);


				Element segmentid = createElement(pmmlDocument, miningmodel, "Segment");
				segmentid.setAttribute("id", Integer.toString(i ));
				createElement(pmmlDocument, segmentid, "True");
				Element modelElement1 = createElement(pmmlDocument, segmentid, "TreeModel");
				modelElement1.setAttribute("modelName", model.getName());
				modelElement1.setAttribute("functionName", "classification");
				modelElement1.setAttribute("algorithmName", "randomForest");
				modelElement1.setAttribute("missingValueStrategy", "lastPrediction");
				modelElement1.setAttribute("noTrueChildStrategy", "returnLastPrediction");
				modelElement1.setAttribute("splitCharacteristic", "multiSplit");
				
				createMiningSchema(pmmlDocument, modelElement1, model);
			 //LogService.getRoot().log(Level.INFO, "Testttttt................." +data);
			    createTree(pmmlDocument, modelElement1, model, md1.getRoot(), null);
		}
		return modelElement;
	}

	private void createTree(Document pmmlDocument, Element fatherElement, ConfigurableRandomForestModel model2, Tree tree, Edge incomingEdge) {
		
		Map<String, Integer> counterMap = tree.getSubtreeCounterMap();
		double totalCounts = 0;
		for (Map.Entry<String, Integer> entry: counterMap.entrySet()) {
			totalCounts += entry.getValue();
		}
		
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

		// recursivly add children
		
		
/*		for (String label: counterMap.keySet()) {
			Element labelDistributionElement = createElement(pmmlDocument, nodeElement, "ScoreDistribution");
			labelDistributionElement.setAttribute("value", label);
			labelDistributionElement.setAttribute("recordCount", counterMap.get(label) + "");
			labelDistributionElement.setAttribute("confidence", counterMap.get(label) / totalCounts + "");
		}*/
		
		
		Iterator<Edge> childIterator = tree.childIterator();
		while (childIterator.hasNext()) {
			Edge edge = childIterator.next();
			Tree child = edge.getChild();
			
			createTree(pmmlDocument, nodeElement, model2, child, edge);
			
		}

	}
	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}

}