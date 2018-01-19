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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.pmml.PMMLTools;

/**
 * @author Miguel Buescher
 */
public abstract class AbstractPMMLModelWriter extends AbstractPMMLObjectWriter {

	private Model model;

	public AbstractPMMLModelWriter(Model model) {
		super();
		this.model = model;
	}
	
	public void appendBody(Document pmmlDoc, Element pmmlRoot, PMMLVersion version) throws UserError {
		appendTrainingHeader(version, pmmlDoc, pmmlRoot);

		Element modelBody = createModelBody(pmmlDoc, version);
		pmmlRoot.appendChild(modelBody);
	}

	protected void appendTrainingHeader(PMMLVersion version, Document pmmlDoc, Element pmmlRoot) throws UserError {
        // create data dictionary from first model
        ExampleSet trainingsSignature = model.getTrainingHeader();
        PMMLTools.createDataDictionary(pmmlDoc, pmmlRoot, trainingsSignature, version);
	}
	
	protected abstract Element createModelBody(Document pmmlDoc, PMMLVersion version) throws UserError;
}
