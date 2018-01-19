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

import com.rapidminer.example.Tools;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.functions.kernel.KernelModel;
import com.rapidminer.operator.learner.functions.kernel.SupportVector;

/**
 * This is the abstract class for all KernelModelWriter. It already exports as much as possible
 * and supported by the KernelModel interface, but needs additional information about the chosen
 * kernel and it's parameter but is restricted to binominal classification and regression.
 * 
 * @author Sebastian Land
 *
 */
public abstract class AbstractKernelModelPMMLWriter extends AbstractPredictionModelPMMLWriter {

	private KernelModel model;
	
	public AbstractKernelModelPMMLWriter(KernelModel model) {
		super(model);
		
		this.model = model;
	}
	
	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
		Element modelElement = pmmlDocument.createElement("SupportVectorMachineModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", model.isClassificationModel() ? "classification" : "regression");
		modelElement.setAttribute("algorithmName", getAlgorithmName());
		
		// creating mining schema
		createMiningSchema(pmmlDocument, modelElement, model);
		
		// creating output and target values
		createOutput(pmmlDocument, modelElement, model);
		createTargetValues(pmmlDocument, modelElement, model);
		
		// creating element describing the kernel
		modelElement.appendChild(createKernel(pmmlDocument));
		
		// creating Vector Dictionary
		createVectorDictionary(pmmlDocument, modelElement, model);
		createMachine(pmmlDocument, modelElement, model);
		
		return modelElement;
	}

	

	private void createMachine(Document pmmlDocument, Element modelElement, KernelModel model) {
		boolean isClassification = model.isClassificationModel();
		
		Element machineElement = createElement(pmmlDocument, modelElement, "SupportVectorMachine");
		if (isClassification) {
			machineElement.setAttribute("targetCategory", model.getLabel().getMapping().getNegativeString());
			machineElement.setAttribute("alternateTargetCategory", model.getLabel().getMapping().getPositiveString());
			machineElement.setAttribute("threshold", "0.5");
		}
		
		// define vectors
		Element vectorsElement = createElement(pmmlDocument, machineElement, "SupportVectors");
		vectorsElement.setAttribute("numberOfSupportVectors", model.getNumberOfSupportVectors() + "");
		vectorsElement.setAttribute("numberOfAttributes", Tools.getRegularAttributeNames(model.getTrainingHeader()).length + "");

		for (int i = 0; i < model.getNumberOfSupportVectors(); i++) {
			Element vectorElement = createElement(pmmlDocument, vectorsElement, "SupportVector");
			vectorElement.setAttribute("vectorId", "sv_" + i);
		}

		// define coefficients
		Element coefficientsElement = createElement(pmmlDocument, machineElement, "Coefficients");
		coefficientsElement.setAttribute("numberOfCoefficients", model.getNumberOfSupportVectors() + "");
		coefficientsElement.setAttribute("absoluteValue", model.getBias() + "");
		
		for (int i = 0; i < model.getNumberOfSupportVectors(); i++) {
			Element coefficientElement = createElement(pmmlDocument, coefficientsElement, "Coefficient");
			coefficientElement.setAttribute("value", model.getSupportVector(i).getAlpha() + "");
		}
		
		
	}

	private void createVectorDictionary(Document pmmlDocument, Element modelElement, KernelModel model) {
		Element dictionaryElement = createElement(pmmlDocument, modelElement, "VectorDictionary");
		dictionaryElement.setAttribute("numberOfVectors", model.getNumberOfSupportVectors() + "");
		
		// define vector fields
		String[] attributes = Tools.getRegularAttributeNames(model.getTrainingHeader());
		Element fieldsElement = createElement(pmmlDocument, dictionaryElement, "VectorFields");
		fieldsElement.setAttribute("numberOfFields", attributes.length + "");
		
		for (String attributeName: attributes) {
			Element fieldElement = createElement(pmmlDocument, fieldsElement, "FieldRef");
			fieldElement.setAttribute("field", attributeName);
		}
		
		// define vectors
		for (int i = 0; i < model.getNumberOfSupportVectors(); i++) {
			Element vectorElement = createElement(pmmlDocument, dictionaryElement, "VectorInstance");
			vectorElement.setAttribute("id", "sv_" + i);
			SupportVector vector = model.getSupportVector(i);
			vectorElement.appendChild(PMMLTranslation.toArray(pmmlDocument, vector.getX()));
		}
	}
	
	/**
	 * This method must return the algorithm name of the algorithm generated this model type.
	 */
	protected abstract String getAlgorithmName();

	/**
	 * This method has to return an element describing the used kernel. The kernel format depends on 
	 * the actual kernel model and so this must be implemented by the subclasses.
	 * Subclasses are advised to write a special constructor for the class they handle and store the object
	 * locally to get access to this object.
	 * @throws NotSupportedByPMMLException 
	 */
	protected abstract Element createKernel(Document pmmlDocument) throws NotSupportedByPMMLException;
	
	
	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}
	
}
