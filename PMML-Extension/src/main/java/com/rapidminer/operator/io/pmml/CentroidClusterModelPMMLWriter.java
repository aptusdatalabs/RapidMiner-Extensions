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
import com.rapidminer.operator.clustering.CentroidClusterModel;
import com.rapidminer.operator.clustering.Cluster;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.mixed.MixedEuclideanDistance;
import com.rapidminer.tools.math.similarity.nominal.JaccardNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.NominalDistance;
import com.rapidminer.tools.math.similarity.nominal.RogersTanimotoNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.SimpleMatchingNominalSimilarity;
import com.rapidminer.tools.math.similarity.numerical.ChebychevNumericalDistance;
import com.rapidminer.tools.math.similarity.numerical.EuclideanDistance;
import com.rapidminer.tools.math.similarity.numerical.ManhattanDistance;

/**
 * This will export each centroid based cluster model to pmml.
 * Please keep in mint that not all distance measures are supported by pmml.
 * 
 * @author Sebastian Land
 */
public class CentroidClusterModelPMMLWriter extends AbstractPMMLModelWriter {

	private CentroidClusterModel model;
	
	public CentroidClusterModelPMMLWriter(CentroidClusterModel object) {
		super(object);
		
		this.model = object;
	}

	@Override
	public Element createModelBody(Document pmmlDocument, PMMLVersion version) throws UserError {
		Element modelElement = pmmlDocument.createElement("ClusteringModel");
		modelElement.setAttribute("modelName", model.getName());
		modelElement.setAttribute("functionName", "clustering");
		modelElement.setAttribute("algorithmName", model.getSource());
		modelElement.setAttribute("modelClass", "centerBased");
		modelElement.setAttribute("numberOfClusters", model.getNumberOfClusters() + "");
		
		// creating mining schema
		createMiningSchema(pmmlDocument, modelElement, model);
		
		// convert distance measure
		convertDistanceMeasure(pmmlDocument, modelElement, model.getDistanceMeasure());
		
		// writing clustering fields
		createClusterFields(pmmlDocument, modelElement, model);

		// writing clusters
		createClusters(pmmlDocument, modelElement, model);
		return modelElement;
	}

	private void createClusters(Document pmmlDocument, Element modelElement, CentroidClusterModel model2) {
		for (Cluster cluster: model2.getClusters()) {
			Element clusterElement = createElement(pmmlDocument, modelElement, "Cluster");
			clusterElement.setAttribute("name", "cluster_" + cluster.getClusterId());
			double[] values = model2.getCentroidCoordinates(cluster.getClusterId());
			Element array = createElement(pmmlDocument, clusterElement, "Array");
			array.setAttribute("n", values.length + "");
			array.setAttribute("type", "real");
			StringBuffer buffer = new StringBuffer();
			for (double value: values)
				buffer.append(value + " ");
			array.setTextContent(buffer.toString());
		}
	}

	private void createClusterFields(Document pmmlDocument, Element modelElement, CentroidClusterModel model2) {
		for (Attribute attribute: model2.getTrainingHeader().getAttributes()) {
			Element clusterFieldElement = createElement(pmmlDocument, modelElement, "ClusteringField");
			clusterFieldElement.setAttribute("field", attribute.getName());
		}
	}

	/**
	 * Converts the supported distance measures.
	 * @throws NotSupportedByPMMLException 
	 */
	private void convertDistanceMeasure(Document pmmlDocument, Element modelElement, DistanceMeasure distanceMeasure) throws NotSupportedByPMMLException {
		Element measureElement = createElement(pmmlDocument, modelElement, "ComparisonMeasure");
		measureElement.setAttribute("kind", distanceMeasure.isDistance() ? "distance" : "similarity");
		
		if (distanceMeasure instanceof EuclideanDistance) {
			createElement(pmmlDocument, measureElement, "euclidean");
			measureElement.setAttribute("compareFunction", "absDiff");
		} else if (distanceMeasure instanceof ChebychevNumericalDistance) {
			createElement(pmmlDocument, measureElement, "chebychev");
			measureElement.setAttribute("compareFunction", "absDiff");
		} else if (distanceMeasure instanceof ManhattanDistance) {
			createElement(pmmlDocument, measureElement, "cityBlock");
			measureElement.setAttribute("compareFunction", "absDiff");
		
		} else if (distanceMeasure instanceof MixedEuclideanDistance) {
			createElement(pmmlDocument, measureElement, "euclidean");
			measureElement.setAttribute("compareFunction", "absDiff");
			
		} else if (distanceMeasure instanceof JaccardNominalSimilarity) {
			createElement(pmmlDocument, measureElement, "jaccard");
			measureElement.setAttribute("compareFunction", "equal");
		} else if (distanceMeasure instanceof NominalDistance) {
			createElement(pmmlDocument, measureElement, "sum");
			measureElement.setAttribute("compareFunction", "equal");
		} else if (distanceMeasure instanceof RogersTanimotoNominalSimilarity) {
			createElement(pmmlDocument, measureElement, "tanimoto");
			measureElement.setAttribute("compareFunction", "equal");
		} else if (distanceMeasure instanceof SimpleMatchingNominalSimilarity) {
			createElement(pmmlDocument, measureElement, "simpleMatching");
			measureElement.setAttribute("compareFunction", "equal");
		} else {
			throw new NotSupportedByPMMLException(distanceMeasure);
		}
		
	}

	@Override
	public Collection<String> checkCompatibility() {
		return null;
	}

}
