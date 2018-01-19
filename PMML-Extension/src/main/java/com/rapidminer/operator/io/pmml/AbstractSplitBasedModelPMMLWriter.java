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

import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.learner.tree.SplitCondition;

/**
 * @author Sebastian Land
 *
 */
public abstract class AbstractSplitBasedModelPMMLWriter extends AbstractPredictionModelPMMLWriter {

	public AbstractSplitBasedModelPMMLWriter(PredictionModel model) {
		super(model);
		
	}

	protected void createSimplePredicate(Document pmmlDocument, Element predicateContainer, SplitCondition term) {
		Element predicateElement = createElement(pmmlDocument, predicateContainer, "SimplePredicate");
		predicateElement.setAttribute("field", term.getAttributeName());
		predicateElement.setAttribute("operator", getOperator(term));
		predicateElement.setAttribute("value", term.getValueString());
	}

	private String getOperator(SplitCondition term) {
		String relation = term.getRelation();
		if (relation.equals("="))
			return "equal";
		if (relation.equals(">"))
			return "greaterThan";
		return "lessOrEqual";
	}

}