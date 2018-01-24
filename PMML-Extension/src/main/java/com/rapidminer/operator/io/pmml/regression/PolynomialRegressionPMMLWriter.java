package com.rapidminer.operator.io.pmml.regression;

import java.util.Collection;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.pmml.AbstractPredictionModelPMMLWriter;
import com.rapidminer.operator.io.pmml.PMMLVersion;
import com.rapidminer.operator.learner.functions.PolynomialRegressionModel;
import com.rapidminer.operator.learner.functions.LinearRegressionModel;
import com.rapidminer.tools.LogService;

public class PolynomialRegressionPMMLWriter extends AbstractPredictionModelPMMLWriter {
	 
	private PolynomialRegressionModel model;
	
	public PolynomialRegressionPMMLWriter(PolynomialRegressionModel model) {
		super(model);
		
		this.model = model;
	}
	
	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
	    
		
		boolean isClassification = true;
	   
		Element modelElement = pmmlDocument.createElement("RegressionModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", (isClassification? "classification": "regression"));
		modelElement.setAttribute("algorithmName", "LinearRegression");
		modelElement.setAttribute("modelType", "linearRegression");
		modelElement.setAttribute("normalizationMethod", (isClassification) ? "logit": "none");
	
		// creating mining schema
		createMiningSchema(pmmlDocument, modelElement, model);
		
		// creating output and target values
		createOutput(pmmlDocument, modelElement, model);
		createTargetValues(pmmlDocument, modelElement, model);
		
		// creating rule model specific parts
		/*if (isClassification)
			createClassificationTables(pmmlDocument, modelElement, model);
		else
			createRegressionTables(pmmlDocument, modelElement, model);*/
		
		return modelElement;
	}



	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}
	
	
}
