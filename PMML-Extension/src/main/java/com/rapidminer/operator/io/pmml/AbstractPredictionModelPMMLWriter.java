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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Ontology;

/**
 * Abstract superclass for all writers of PredictionModels
 * @author Sebastian Land
 */
public abstract class AbstractPredictionModelPMMLWriter extends AbstractPMMLModelWriter {

	public AbstractPredictionModelPMMLWriter(PredictionModel model) {
		super(model);
	}

	protected void createTargetValues(Document pmmlDocument, Element modelElement, PredictionModel model) {
		Element targetsElement = createElement(pmmlDocument, modelElement, "Targets");
		
		// for classification and regression add target
		ExampleSet exampleSet = model.getTrainingHeader();
		Attribute label = exampleSet.getAttributes().getLabel(); // cannot be null otherwise learning could not have taken place for supervised learning!
		
		// target attribute
		Element targetElement = createElement(pmmlDocument, targetsElement, "Target");
		targetElement.setAttribute("field", label.getName());
		targetElement.setAttribute("optype", PMMLTranslation.getOpType(label));

		if (label.isNominal()) {
			// adding values if label is nominal
			for (String value: label.getMapping().getValues()) {
				Element targetValueElement = createElement(pmmlDocument, targetElement, "TargetValue");
				targetValueElement.setAttribute("value", value);
			}
		}
	}
	
	protected void createOutput(Document pmmlDocument, Element modelElement, PredictionModel model) {
		Element outputElement = createElement(pmmlDocument, modelElement, "Output");
		
		// for classification and regression add prediction
		ExampleSet exampleSet = model.getTrainingHeader();
		Attribute label = exampleSet.getAttributes().getLabel(); // cannot be null otherwise learning could not have taken place for supervised learning!

		// prediction
		Element predictionElement = createElement(pmmlDocument, outputElement, "OutputField");
		predictionElement.setAttribute("name", Attributes.PREDICTION_NAME + "(" + label.getName() + ")");
		predictionElement.setAttribute("optype", PMMLTranslation.getOpType(label));
		predictionElement.setAttribute("dataType", PMMLTranslation.getValueType(label));
		predictionElement.setAttribute("targetField", label.getName());
		predictionElement.setAttribute("feature", "predictedValue");

		// for classification add confidences
		if (label.isNominal()) {
			for (String value: label.getMapping().getValues()) {
				Element confidenceElement = createElement(pmmlDocument, outputElement, "OutputField");
				confidenceElement.setAttribute("name", Attributes.CONFIDENCE_NAME + "(" + value + ")");
				confidenceElement.setAttribute("optype", PMMLTranslation.getOpType(Ontology.NUMERICAL));
				confidenceElement.setAttribute("dataType", PMMLTranslation.getValueType(Ontology.REAL));
				confidenceElement.setAttribute("targetField", label.getName());
				confidenceElement.setAttribute("value", value);
				confidenceElement.setAttribute("feature", "probability");
			}
		}
	}


}
