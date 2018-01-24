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
package com.rapidminer.operator.io.pmml.regression;

import java.util.Collection;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.pmml.AbstractPredictionModelPMMLWriter;
import com.rapidminer.operator.io.pmml.PMMLVersion;
import com.rapidminer.operator.learner.functions.LinearRegressionModel;
import com.rapidminer.tools.LogService;

/**
 * A PMML Writer for LinearRegressionModels which currently support only regression or 
 * binominal classification.
 * 
 * 
 * 
 * @author Sebastian Land
 */
public class LinearRegressionModelPMMLWriter extends AbstractPredictionModelPMMLWriter {

	private LinearRegressionModel model;
	
	public LinearRegressionModelPMMLWriter(LinearRegressionModel model) {
		super(model);
		
		this.model = model;

	}

	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
		boolean isClassification = model.getFirstLabel() != null;
		
		Element modelElement = pmmlDocument.createElement("RegressionModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", (isClassification)? "classification": "regression");
		modelElement.setAttribute("algorithmName", "LinearRegression");
		modelElement.setAttribute("modelType", "linearRegression");
		modelElement.setAttribute("normalizationMethod", (isClassification) ? "logit": "none");
		
		LogService.getRoot().log(Level.INFO, "Testttttt................." +model.getSecondLabel());
		// creating mining schema
		createMiningSchema(pmmlDocument, modelElement, model);
		
		// creating output and target values
		createOutput(pmmlDocument, modelElement, model);
		createTargetValues(pmmlDocument, modelElement, model);
		
		// creating rule model specific parts
		if (isClassification)
			createClassificationTables(pmmlDocument, modelElement, model);
		else
			createRegressionTables(pmmlDocument, modelElement, model);
		
		return modelElement;
	}

	private void createRegressionTables(Document pmmlDocument, Element modelElement, LinearRegressionModel model) {
		Element tableElement = createElement(pmmlDocument, modelElement, "RegressionTable");
		double[] coefficients = model.getCoefficients();
		tableElement.setAttribute("intercept", coefficients[coefficients.length - 1] + "");
		
		String[] attributeNames = model.getAttributeNames();
		boolean[] isAttributeUsed = model.getSelectedAttributes();
		
		int j = 0;
		for(int i = 0; i < attributeNames.length; i++) {
			if (isAttributeUsed[i]) {
				Element coeffcientElement = createElement(pmmlDocument, tableElement, "NumericPredictor");
				coeffcientElement.setAttribute("name", attributeNames[i]);
				coeffcientElement.setAttribute("coefficient", coefficients[j] + "");
				j++;
			}
		}
	}

	/**
	 * This method will create one real table containing all parameters of the linear regression model for the second
	 * label and an empty table for the first label. This reflects the 0-1 encoding.
	 */
	private void createClassificationTables(Document pmmlDocument, Element modelElement, LinearRegressionModel model) {
		Element firstTableElement = createElement(pmmlDocument, modelElement, "RegressionTable");
		firstTableElement.setAttribute("intercept", "0");
		firstTableElement.setAttribute("targetCategory", model.getFirstLabel());

		Element secondTableElement = createElement(pmmlDocument, modelElement, "RegressionTable");
		double[] coefficients = model.getCoefficients();
		secondTableElement.setAttribute("intercept", coefficients[coefficients.length - 1] + "");
		secondTableElement.setAttribute("targetCategory", model.getSecondLabel());
		
		String[] attributeNames = model.getAttributeNames();
		boolean[] isAttributeUsed = model.getSelectedAttributes();

		int coeffIndex = 0;
		for(int i = 0; i < attributeNames.length; i++) {
			if (isAttributeUsed[i]) {
				Element coeffcientElement = createElement(pmmlDocument, secondTableElement, "NumericPredictor");
				coeffcientElement.setAttribute("name", attributeNames[i]);
				coeffcientElement.setAttribute("coefficient", coefficients[coeffIndex] + "");
				coeffIndex++;
			}
		}
	}

	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}
	
	
}
