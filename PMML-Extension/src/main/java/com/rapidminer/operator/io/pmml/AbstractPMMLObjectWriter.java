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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.clustering.CentroidClusterModel;
import com.rapidminer.operator.learner.associations.AssociationRules;
import com.rapidminer.operator.learner.bayes.SimpleDistributionModel;
import com.rapidminer.operator.learner.functions.LinearRegressionModel;
import com.rapidminer.operator.learner.functions.LogisticRegressionModel;
import com.rapidminer.operator.learner.functions.kernel.JMySVMModel;
import com.rapidminer.operator.learner.functions.neuralnet.ImprovedNeuralNetModel;
import com.rapidminer.operator.learner.rules.RuleModel;
import com.rapidminer.operator.learner.tree.ConfigurableRandomForestModel;
import com.rapidminer.operator.learner.tree.TreeModel;
import com.rapidminer.tools.pmml.PMMLTools;

/**
 * This is the abstract class for all PMML writer, exporting any 
 * ioobject to PMML
 * 
 * @author Sebastian Land
 *
 */
public abstract class AbstractPMMLObjectWriter implements PMMLObjectWriter {
	public static final HashMap<Class<? extends IOObject>, Class<? extends PMMLObjectWriter>> WRITER = new HashMap<Class<? extends IOObject>, Class<? extends PMMLObjectWriter>>();
	static {
		WRITER.put(AssociationRules.class, AssociationRulePMMLWriter.class);
		WRITER.put(SimpleDistributionModel.class, DistributionModelPMMLWriter.class);
		WRITER.put(RuleModel.class, RuleModelPMMLWriter.class);
		WRITER.put(TreeModel.class, TreeModelPMMLWriter.class);
		WRITER.put(LinearRegressionModel.class, LinearRegressionModelPMMLWriter.class);
		WRITER.put(LogisticRegressionModel.class, LogisticRegressionModelPMMLWriter.class);
		WRITER.put(JMySVMModel.class, JMySVMModelPMMLWriter.class);
		WRITER.put(CentroidClusterModel.class, CentroidClusterModelPMMLWriter.class);
		WRITER.put(ImprovedNeuralNetModel.class, NeuralNetModelPMMLWriter.class);
		WRITER.put(ConfigurableRandomForestModel.class, RandomForestPMMLWriter.class);
	}
	
	public static PMMLObjectWriter getWriterForObject(IOObject object) throws UserError {
		Class<? extends PMMLObjectWriter> writerClass = WRITER.get(object.getClass());
		if (writerClass == null)
			throw new UserError(null, 947, object.getClass().getSimpleName());
		try {
			Constructor<? extends PMMLObjectWriter> constructor = writerClass.getConstructor(object.getClass());
			return constructor.newInstance(object);
		} catch (InstantiationException e) {
			throw new UserError(null, 948, e, object.getClass().getSimpleName());
		} catch (IllegalArgumentException e) {
			throw new UserError(null, 948, e, object.getClass().getSimpleName());
		} catch (IllegalAccessException e) {
			throw new UserError(null, 948, e, object.getClass().getSimpleName());
		} catch (InvocationTargetException e) {
			throw new UserError(null, 948, e, object.getClass().getSimpleName());
		} catch (SecurityException e) {
			throw new UserError(null, 948, e, object.getClass().getSimpleName());
		} catch (NoSuchMethodException e) {
			throw new UserError(null, 948, e, object.getClass().getSimpleName());
		}
	}
	
	/**
	 * This construct should ensure that the one argument constructor needed for construction is present.
	 * Subclasses should implement a more specific constructor getting a subclass of IOObject.
	 */
	public AbstractPMMLObjectWriter() {
	
	}
	
	/**
	 * This method must return an element of a PMML xml file, representing the
	 * given ioobject. This method should be callable with different documents.
	 * @param version 
	 * @throws UserError  This might be thrown if features of the model are unsupported.
	 */
	public abstract void appendBody(Document pmmlDocument, Element pmmlRoot, PMMLVersion version) throws UserError;

	@Override
	public Document export(PMMLVersion version) throws UserError {
        // create xml document
        Document pmmlDoc = null;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            pmmlDoc = documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Cannot create XML builder: "+e, e);
        }

        // create header
        Element pmmlRoot = PMMLTools.createHeader(pmmlDoc, version);

//        // create data dictionary from first model
//        ExampleSet trainingsSignature = ioobject.getTrainingHeader();
//        PMMLTools.createDataDictionary(pmmlDoc, pmmlRoot, trainingsSignature, version);
        
        appendBody(pmmlDoc, pmmlRoot, version);
//        Element body = export(pmmlDoc, version);
//		pmmlRoot.appendChild(body);
        
        return pmmlDoc;
	}

	
	/**
	 * This method exports the TrainingExampleSet header to the pmml MiningSchema
	 */
	protected void createMiningSchema(Document pmmlDocument, Element modelElement, Model model) {
		Element miningSchema = createElement(pmmlDocument, modelElement, "MiningSchema");

		// now iterate over all (possibly) used fields
		ExampleSet exampleSet = model.getTrainingHeader();
		Iterator<AttributeRole> roleIterator = exampleSet.getAttributes().allAttributeRoles();
		while (roleIterator.hasNext()) {
			AttributeRole role = roleIterator.next();
			Attribute attribute = role.getAttribute();
			
			Element fieldElement = createElement(pmmlDocument, miningSchema, "MiningField");
			fieldElement.setAttribute("name", attribute.getName());
			fieldElement.setAttribute("usageType", PMMLTranslation.getFieldUsage(role.getSpecialName()));
			fieldElement.setAttribute("optype", PMMLTranslation.getOpType(attribute));
			fieldElement.setAttribute("importance", "1.0");
			fieldElement.setAttribute("lowValue", "0.0");   //TODO: is this needed? If not, just remove, always using values as is
			fieldElement.setAttribute("highValue", "1.0");
			fieldElement.setAttribute("missingValueTreatment", getMissingValueTreatment());
			if (getMissingValueReplacement(attribute) != null)
				fieldElement.setAttribute("missingValueReplacement", getMissingValueReplacement(attribute));
			fieldElement.setAttribute("invalidValueTreatment", "asIs");
		}
	}
	
	
	/**
	 * This is a convenience method for subclasses for easy creating and appending a new element.
	 */
	protected Element createElement(Document document, Element father, String name) {
		Element element = document.createElement(name);
		father.appendChild(element);
		return element;
	}
	
	/**
	 * This returns the missing value treatment of the exported object.
	 */
	protected String getMissingValueTreatment() {
		return "asIs";
	}
	
	/**
	 * This returns the replacement for missing values if an appropriate MissingValueTreatment 
	 * is chosen. Otherwise return null to avoid occurrence of this element.
	 * @param attribute The attribute the replacement should be retrieved
	 */
	protected String getMissingValueReplacement(Attribute attribute) {
		return null;
	}
}