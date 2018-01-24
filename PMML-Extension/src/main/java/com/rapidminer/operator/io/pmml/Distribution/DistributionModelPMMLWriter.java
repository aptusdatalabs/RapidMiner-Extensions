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
package com.rapidminer.operator.io.pmml.Distribution;

import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.example.Attribute;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.pmml.AbstractPredictionModelPMMLWriter;
import com.rapidminer.operator.io.pmml.PMMLVersion;
import com.rapidminer.operator.learner.bayes.SimpleDistributionModel;
import com.rapidminer.tools.math.distribution.DiscreteDistribution;
import com.rapidminer.tools.math.distribution.Distribution;

/**
 * This is the writer for distribution models.
 * 
 * @author Sebastian Land
 */
public class DistributionModelPMMLWriter extends AbstractPredictionModelPMMLWriter {

    private SimpleDistributionModel model;

    public DistributionModelPMMLWriter(SimpleDistributionModel model) {
        super(model);
        this.model = model;
    }

    @Override
    public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
        Element modelElement = pmmlDocument.createElement("NaiveBayesModel");
        modelElement.setAttribute("modelName", model.getName());
        modelElement.setAttribute("threshold", (1d / model.getTotalWeight()) + "");
        modelElement.setAttribute("functionName", "classification");
        modelElement.setAttribute("algorithmName", "NaiveBayes");

        // creating mining schema
        createMiningSchema(pmmlDocument, modelElement, model);

        // creating output and target values
        createOutput(pmmlDocument, modelElement, model);
        createTargetValues(pmmlDocument, modelElement, model);

        // creating bayes specific parts
        createBayesInput(pmmlDocument, modelElement, model);
        createBayesOutput(pmmlDocument, modelElement, model);

        return modelElement;
    }

    private void createBayesOutput(Document pmmlDocument, Element modelElement, SimpleDistributionModel model) {
        Element outputElement = createElement(pmmlDocument, modelElement, "BayesOutput");
        outputElement.setAttribute("fieldName", model.getTrainingHeader().getAttributes().getLabel().getName());

        Element targetValueCounts = createElement(pmmlDocument, outputElement, "TargetValueCounts");
        for (int labelIndex = 0; labelIndex < model.getNumberOfClasses(); labelIndex ++) {
            Element targetValueCount = createElement(pmmlDocument, targetValueCounts, "TargetValueCount");
            targetValueCount.setAttribute("value", model.getClassName(labelIndex));
            targetValueCount.setAttribute("count", model.getClassWeights()[labelIndex] + "");
        }
    }

    private void createBayesInput(Document pmmlDocument, Element modelElement, SimpleDistributionModel model) {
        Element inputsElement = createElement(pmmlDocument, modelElement, "BayesInputs");

        // now define counts for each nominal attribute
        for (int attributeIndex = 0; attributeIndex < model.getNumberOfAttributes(); attributeIndex++) {
            // TODO: Add support for continuous attributes to PMML
            Distribution distribution = model.getDistribution(0, attributeIndex);
            if (distribution.isDiscrete()) {
                // currently only discrete distributions supported
                DiscreteDistribution discreteDistribution = ((DiscreteDistribution) model.getDistribution(0, attributeIndex));

                Element inputElement = createElement(pmmlDocument, inputsElement, "BayesInput");
                inputElement.setAttribute("fieldName", discreteDistribution.getAttributeName());

                String[] valuesNames = discreteDistribution.getValueNames();
                for (int valueIndex = 0; valueIndex < valuesNames.length; valueIndex ++) {
                    Element pairCountElement = createElement(pmmlDocument, inputElement, "PairCounts");
                    pairCountElement.setAttribute("value", valuesNames[valueIndex]);

                    Element targetValueCounts = createElement(pmmlDocument, pairCountElement, "TargetValueCounts");
                    for (int labelIndex = 0; labelIndex < model.getNumberOfClasses(); labelIndex ++) {
                        Element targetValueCount = createElement(pmmlDocument, targetValueCounts, "TargetValueCount");
                        targetValueCount.setAttribute("value", model.getClassName(labelIndex));
                        targetValueCount.setAttribute("count", (model.getDistribution(labelIndex, attributeIndex).getParameterValue(valueIndex) * model.getClassWeights()[labelIndex]) + "");
                    }
                }
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
    protected String getMissingValueReplacement(Attribute attribute) {
        return "MISSING_VALUE";
    }
}
