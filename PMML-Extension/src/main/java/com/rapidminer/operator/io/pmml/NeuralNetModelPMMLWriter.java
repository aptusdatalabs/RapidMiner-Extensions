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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.example.Attribute;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.functions.neuralnet.ImprovedNeuralNetModel;
import com.rapidminer.operator.learner.functions.neuralnet.InnerNode;
import com.rapidminer.operator.learner.functions.neuralnet.InputNode;
import com.rapidminer.operator.learner.functions.neuralnet.Node;
import com.rapidminer.operator.learner.functions.neuralnet.OutputNode;
import com.rapidminer.tools.Tools;

/**
 * A PMML Writer for Neural Net models using the Sigmoid Function.
 * All models with a categorical output and numerical input are supported. 
 * Models with a numerical output will throw an exception.
 * 
 * @author Miguel B�scher
 */
public class NeuralNetModelPMMLWriter extends AbstractPredictionModelPMMLWriter {

    private ImprovedNeuralNetModel model;
    private int numberOfHiddenLayers=0; // counter for #hiddenlayer used in NeuralNetwork
    
    public NeuralNetModelPMMLWriter(ImprovedNeuralNetModel model) {
        super(model);
        this.model = model;
    }
    
    @Override
    public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
    	
        Element modelElement = pmmlDocument.createElement("NeuralNetwork");
        modelElement.setAttribute("modelName", model.getName());
        
        if (model.getTrainingHeader().getAttributes().getLabel().isNumerical())
        	modelElement.setAttribute("functionName", "regression");
        else
        	modelElement.setAttribute("functionName", "classification");
        modelElement.setAttribute("algorithmName", "NeuralNet");
        
        // ATTRIBUTES of ImprovedNeuralNetModel:
        // --> Attributes: activationFunction, normalizationMethod and threshold can be set NeuralNetwork and NeuralLayer (innermost overrides parentElement) ==> hold for whole network or for each hiddenlayer  
        // --> Attributes: width and altitude can be set NeuralNetwork, NeuralLayer and in a Neuron Element (innermost overrides parentElement) ==> hold for whole network, for each hiddenlayer or for a single Neuron 
        modelElement.setAttribute("activationFunction", "logistic"); 	// >>> required  Sigmoid-Function in RM==logistic activation function in PMML ==> used for whole neural network
        // modelElement.setAttribute("normalizationMethod", "none"); 	// >>> optional  (softmax, simplemax and none in PMML: default=none)
        // modelElement.setAttribute("threshold", 0+""); 				// >>> optional  (default=0 in PMML)
        // modelElement.setAttribute("width", ); 						// >>> optional  (only used if activationFunction==radialBasis)
        // modelElement.setAttribute("altitude", 1.0+""); 				// >>> optional  (default=1.0 PMML)
        
        // creating mining schema
        createMiningSchema(pmmlDocument, modelElement, model);

        // creating output and target values
        createOutput(pmmlDocument, modelElement, model);
        createTargetValues(pmmlDocument, modelElement, model);

        // creating neural net specific parts
        createNeuralNetInputs(pmmlDocument, modelElement, model);
        createNeuralNetLayers(pmmlDocument, modelElement, model);
        createNeuralNetOutputs(pmmlDocument, modelElement, model);
        
        //# hidden layers --> added at last after all NeuralNetLayers haven been processed
        modelElement.setAttribute("numberOfLayers", numberOfHiddenLayers+"");
        
        return modelElement;
    }

    
    private void createNeuralNetInputs(Document pmmlDocument, Element modelElement, ImprovedNeuralNetModel model) {
    	 Element neuralInputsElement = createElement(pmmlDocument, modelElement, "NeuralInputs");
    	 InputNode[] inputNodes = model.getInputNodes();
    	 neuralInputsElement.setAttribute("numberOfInputs", inputNodes.length+"");
    	
    	 // add NeuralInput entry for each InputNode
    	 for (InputNode inputNode : inputNodes) {
    		 Attribute nodeAttribute = inputNode.getAttribute(); // Each InputNode represents an Attribute
    		 
    		 Element inputElement = createElement(pmmlDocument, neuralInputsElement, "NeuralInput");
    		 
    		 inputElement.setAttribute("id", inputNode.getNodeName()+""); 
    		 //  Check if normalization used for InputNode 
    		 if (inputNode.isNormalize()) {
    			 // default value of normalize in PMML== true ==> normalization between  -1 und 1 
	    		 Element derivedFieldElement =  createElement(pmmlDocument, inputElement, "DerivedField");
	    		 // derivedFieldElement.setAttribute("name", "");  											// >>> optional
	    		 // derivedFieldElement.setAttribute("displayName", ""); 									// >>> optional 
	    		 derivedFieldElement.setAttribute("datatype",PMMLTranslation.getValueType(nodeAttribute)); 	// >>> required 
	    		 derivedFieldElement.setAttribute("optype", PMMLTranslation.getOpType(nodeAttribute)); 		// >>> required
	    		
	    		 // Add NormContinuous with LinearNorm or NormDisrcete --> Distinction if attribute is Nominal(string) or Numerical(double)
	    		 if (nodeAttribute.isNumerical()) { //Numerical Attribute --> NormContinuous 
	    		 
		    		 Element normContinuousElement =  createElement(pmmlDocument, derivedFieldElement, "NormContinuous");
		    		 normContinuousElement.setAttribute("field",nodeAttribute.getName()); 					// >>> required
		    		 
		    		 // Add Element LinearNorm --> 2 Elements minimum required -> setAttribute + double values: rounded 3 digits
		    		 Element normLinearFormElement1 = createElement(pmmlDocument, normContinuousElement, "LinearNorm");
		    		 normLinearFormElement1.setAttribute("orig",Tools.formatNumber(inputNode.getAttributeBase()-inputNode.getAttributeRange())); // >>> required 
		    		 normLinearFormElement1.setAttribute("norm","-1"); 														// >>> required --> in RM normalized from -1 to 1 (min value)														// >>> required --> in RM normalized from -1 to 1 (min value)
		    		 Element normLinearFormElement2 = createElement(pmmlDocument, normContinuousElement, "LinearNorm");
		    		 normLinearFormElement2.setAttribute("orig",Tools.formatNumber(inputNode.getAttributeBase()+inputNode.getAttributeRange())); // >>> required 
		    		 normLinearFormElement2.setAttribute("norm","1"); 														// >>> required --> in RM normalized from -1 to 1 (max value)														// >>> required --> in RM normalized from -1 to 1 (max value)
	    		 } else if (nodeAttribute.isNominal()) { //Nominal Attribute --> NormDiscrete 
	    			 // NOTE: NominalAttributes not used in NeuralNet as only numerical values can be used in RM -> in RM: nominal attributes transformed into numerical BEFORE usage with NN-Operator
	    			 Element normDiscreteElement =  createElement(pmmlDocument, derivedFieldElement, "NormDiscrete");
	    			 normDiscreteElement.setAttribute("field", nodeAttribute.getName()); 									// >>> required
	    			 normDiscreteElement.setAttribute("value", inputNode.getCurrentValue()+""); // >>> required (mapped value in node)
	    		 }
    		 }else { 
    			 //no normalization done for inputNode -> do nothing
    		 }
    	 }
    }
    
    private void createNeuralNetLayers(Document pmmlDocument, Element modelElement, ImprovedNeuralNetModel model) {

    	// Check if InnerNodes exist (MLP) or no hidden layers defined [ InnerNodes have 2 Types with Type INNER and OUTPUT]
    	// --> NOT needed: in RM -->  if no hidden layer exists,  one hidden layer is automatically added ==> at least one hidden layer  
    	
        // Store a list of InnerNodes for each hidden layer
    	Map<Integer, List<InnerNode>> hiddenLayers = new LinkedHashMap<Integer, List<InnerNode>>();
    	
    	//Added list  for inner output nodes --> layerIndex of InnerNode is -2 
    	List<InnerNode> outputLayer = new ArrayList<InnerNode>();
    	 
    	// Step 1: Process all InnerNodes for each HiddenLayer inclusive inner outputNodes
   	 	for (InnerNode innerNode : model.getInnerNodes()) {
			int layerIndex = innerNode.getLayerIndex();
			if (layerIndex != Node.OUTPUT) { // selecting only InnerNodes with Type hidden
				List<InnerNode> layer = hiddenLayers.get(layerIndex);
				if (layer == null) {
					layer = new ArrayList<InnerNode>();
					hiddenLayers.put(layerIndex, layer);
				}
				layer.add(innerNode);
			}else {
				outputLayer.add(innerNode);
			}
   	 	}
   	 	//Step 2: Generate PMML-Elements for each hiddenLayer --> distinguish between different layerIndex starting from layerIndex=0 (Hidden inner nodes)
		for (Integer layerIndex : hiddenLayers.keySet()) {
			createNeuralNetLayerElements(pmmlDocument, modelElement, hiddenLayers.get(layerIndex));
			numberOfHiddenLayers++; //incrementing counter for hidden layer count --> later used for setting the # hiddenLayers of NN in PMML document
		}
		
		//Add outputlayer to the model
		createNeuralNetLayerElements(pmmlDocument, modelElement, outputLayer);
		numberOfHiddenLayers++;
    }
    
    // Generate for each hidden layer appropriate XML Element (inclusive inner outputLayer)
    private void createNeuralNetLayerElements(Document pmmlDocument, Element modelElement, List<InnerNode> innerNodes) {
    	
        Element neuralLayerElement = createElement(pmmlDocument, modelElement, "NeuralLayer");
   	 	neuralLayerElement.setAttribute("numberOfNeurons", innerNodes.size()+"");

		// ATTRIBUTES for each hidden layer:
		//neuralLayerElement.setAttribute("activationFunction", innerNodes.get(0).getActivationFunction().getTypeName() +""); 	// >>> required, but already defined in NeuralNetwork element (see above)
		if (innerNodes.get(0).getActivationFunction().getTypeName().equals("Sigmoid"))
		{
			neuralLayerElement.setAttribute("activationFunction", "logistic" +""); 	// >>> required, but already defined in NeuralNetwork element (see above)	
		}

		
		// neuralLayerElement.setAttribute("normalizationMethod", "simplemax" +"");	// >>> Using simplemax
		// neuralLayerElement.setAttribute("threshold", model. +"");			// >>> optional
		// neuralLayerElement.setAttribute("width", model. +"");				// >>> optional
		// neuralLayerElement.setAttribute("altitude", model. +"");				// >>> optional
   	
   	 	for (InnerNode aNode : innerNodes) {
   	 		Element innerNodeElement = createElement(pmmlDocument, neuralLayerElement, "Neuron");

   	 		// Id of InnerNodes has to be unique, but if more than one hidden layer used in RM duplicate ids used for each layer --> RM internal issue
   	 		// BugFix --> add layerNumber of each innerNode as Prefix in id
   	 		// Distincition if node is hidden InnerNode(layerIndex >=0)  or output InnerNode (layerIndex -2)
   	 		if (aNode.getLayerIndex() >=0) {  	 			
   	 			innerNodeElement.setAttribute("id", aNode.getLayerIndex()+"_"+aNode.getNodeName()+ "");
   	 		} else {
   	 			innerNodeElement.setAttribute("id", aNode.getNodeName()+ "");
   	 			neuralLayerElement.setAttribute("normalizationMethod", "simplemax" +"");   //setting normalization Method for output layers to simplemax.
   	 		}
				
			// ATTRIBUTES for each hidden node(neuron)
			// innerNodeElement.setAttribute("width", aNode.	+ "");			// >>> optional 
			// innerNodeElement.setAttribute("altitude", aNode.	+ "");			// >>> optional  
			// innerNodeElement.setAttribute("bias", aNode.	+ "");				// >>> optional  

			// Fetching connection and weights(input weights) from previous layer (see toString-method in ImprovedNeuralNetModel.java)
			double[] weights = aNode.getWeights(); // weight[0] is threshold node --> bias node
			innerNodeElement.setAttribute("bias", weights[0]+ "");				// >>> optional setting bias/threshold value
			
			Node[] fromNodes = aNode.getInputNodes();
			for (int j = 0; j < fromNodes.length; j++) {
				Element fromNodeElement = createElement(pmmlDocument, innerNodeElement, "Con");
				
				// 2 distinctions from inputNodes
				// 1. fromNode is an InputNode --> from Attribute is name of InputNode
				// 2. fromNode is an InnerNode --> from Attribute is concatenation of layerIndex and Name of InnerNode!! see above
				if (fromNodes[j] instanceof InputNode) {
					fromNodeElement.setAttribute("from",fromNodes[j].getNodeName() + "");
				}else {
					fromNodeElement.setAttribute("from",fromNodes[j].getLayerIndex()+"_"+fromNodes[j].getNodeName() + "");
				}
				
				fromNodeElement.setAttribute("weight", weights[j + 1] + ""); 
			}
   	 	}
    }
    
    private void createNeuralNetOutputs(Document pmmlDocument, Element modelElement, ImprovedNeuralNetModel model) throws UserError{
        
    	Element neuralOutputsElement = createElement(pmmlDocument, modelElement, "NeuralOutputs");
        OutputNode[] outputNodes = model.getOutputNodes();
        neuralOutputsElement.setAttribute("numberOfOutputs", outputNodes.length+"");
   	 
        // add NeuralOutput entry for each InputNode
        for (OutputNode outputNode : outputNodes) {
        	
       		Attribute outputNodeLabel = outputNode.getLabel(); // Each OutputNode represents an Attribute
			Element outputElement = createElement(pmmlDocument,	neuralOutputsElement, "NeuralOutput");
			// "outputNeuron" is the id of the input from this outputNeuron
			// --> only if categorical => only one input
			// --> if numerical then take outputNeuronNodeName --> WORKAROUND
			// outputElement.setAttribute("outputNeuron", outputNode.getNodeName() + "");
			if (outputNodeLabel.isNominal()) {
				outputElement.setAttribute("outputNeuron", outputNode.getInputNodes()[0].getNodeName());
			}else {
				//Regression --> only one node input to this outputNeuron
				outputElement.setAttribute("outputNeuron", outputNode.getInputNodes()[0].getNodeName() + "");
			}
			
			// Normalization used for OutputNode ==> 2 types: NormContinuous with LinearForm or NormDiscrete 
			Element derivedFieldElement =  createElement(pmmlDocument, outputElement, "DerivedField");
			// 	derivedFieldElement.setAttribute("name", "");  											// >>> optional
			// 	derivedFieldElement.setAttribute("displayName", ""); 									// >>> optional 
   		 	derivedFieldElement.setAttribute("datatype",PMMLTranslation.getValueType(outputNodeLabel)); // >>> required 
   		 	derivedFieldElement.setAttribute("optype", PMMLTranslation.getOpType(outputNodeLabel)); 	// >>> required
   		
   		 	// Add NormContinuous with LinearNorm or NormDisrcete --> Distinction if attribute is Nominal(string) or Numerical(double)
   			if (outputNodeLabel.isNumerical()) { //Numerical Attribute --> NormContinuous with LinearNorm [Regression ==> RegressionModel needed and to be saved in Targets-Element]
   				
   				//Because of normalization problems the NN Model does not support numerical outputs.
   				throw new RuntimeException("A numerical output is not supported for converting a NeuralNet Model to PMML.");	
   				
   				// Numerical output does not work because of normalization
   				// For testing purpose you can active this part.   				
   				//NumericalAttributes for OutputNode in RapdiMiner--> tbd
   				//Element normContinuousElement =  createElement(pmmlDocument, derivedFieldElement, "NormContinuous");
   				//normContinuousElement.setAttribute("field", outputNodeLabel.getName()); // required
	    		 
   				// Add Element LinearNorm --> 2 Elements minimum required -> setAttribute + double values: rounded 3 digits
   				//Element normLinearFormElement1 = createElement(pmmlDocument, normContinuousElement, "LinearNorm");
   				//normLinearFormElement1.setAttribute("orig",Tools.formatNumber(calculateMinMaxValue(outputNode, true)));
   				//normLinearFormElement1.setAttribute("orig",Tools.formatNumber(outputNode.getLabelBase()-outputNode.getLabelRange()));
   				//normLinearFormElement1.setAttribute("norm","0"); 														 	
   				//Element normLinearFormElement2 = createElement(pmmlDocument, normContinuousElement, "LinearNorm");
   				//normLinearFormElement2.setAttribute("orig",Tools.formatNumber(calculateMinMaxValue(outputNode, false))); 	// >>> required
   				//normLinearFormElement2.setAttribute("orig",Tools.formatNumber(outputNode.getLabelBase()+outputNode.getLabelRange()));
   				//normLinearFormElement2.setAttribute("norm","1"); 															

   			} else if (outputNodeLabel.isNominal()) { //Nominal Attribute --> NormDiscrete  == Classification
   			    Element normDiscreteElement =  createElement(pmmlDocument, derivedFieldElement, "NormDiscrete");
	   			normDiscreteElement.setAttribute("field", outputNodeLabel.getName()); 											// >>> required
	   			normDiscreteElement.setAttribute("value", outputNodeLabel.getMapping().mapIndex(outputNode.getClassIndex())); 	// >>> required (mapped value from innerNode connected to this outputNode)
   			}
		}
    }
        
    @Override
    public Collection<String> checkCompatibility() {
        return null;
    }

    @Override
    protected String getMissingValueTreatment() {
        return "asValue";
    }

    @Override
    //How do we calculate this value? Currently it will just not give anything out. This will cause some warnings
    //on the ADAPA webpage.
    protected String getMissingValueReplacement(Attribute attribute) {
    	//return "MISSING_VALUE";
    	return null;
    }
}
