/**
 * RapidMiner PMML Extension
 *
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 * http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.io.pmml;

import java.nio.charset.Charset;
import java.util.List;

import org.w3c.dom.Document;

import com.rapidminer.PluginInitPMML;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.XMLException;


/**
 * This operator tries to export the given models to pmml. If this is not possible, because of too
 * rigid standards or not supported models, an error will be thrown.
 *
 * Currently supported:
 * <ul>
 * <li>Association Rule</li>
 * <li>Centroid Cluster</li>
 * <li>Distribution Model</li>
 * <li>Linear Regression</li>
 * <li>Logistic Regression</li>
 * <li>Naive Bayes</li>
 * <li>Neural Net</li>
 * <li>Rule Model</li>
 * <li>Tree Model</li>
 * </ul>
 *
 *
 * @author Sebastian Land
 */
public class PMMLExporter extends Operator {

	/*
	 * checking user permissions
	 */
	static {
		PluginInitPMML.verifyInstallation();
	}

	public static final String PARAMETER_FILE = "file";

	private static final String PARAMETER_VERSION = "version";

	private InputPort modelInput = getInputPorts().createPort("model", IOObject.class);
	private OutputPort modelOutput = getOutputPorts().createPort("model output");

	public PMMLExporter(OperatorDescription description) {
		super(description);

		getTransformer().addPassThroughRule(modelInput, modelOutput);
	}

	@Override
	public void doWork() throws OperatorException {
		IOObject object = modelInput.getData(IOObject.class);

		int versionIndex = getParameterAsInt(PARAMETER_VERSION);
		if (!(versionIndex >= 0 && versionIndex < PMMLVersion.values().length)) {
			versionIndex = 0;
		}
		PMMLVersion version = PMMLVersion.values()[versionIndex];

		Document pmml = exportAsPMML(object, version);

		// write pmml to file
		try {
			XMLTools.stream(pmml, getParameterAsFile(PARAMETER_FILE, true), Charset.forName("UTF-8"));
		} catch (XMLException e) {
			e.printStackTrace();
		}
		modelOutput.deliver(object);
	}

	public static Document exportAsPMML(IOObject object, PMMLVersion version) throws UserError {

		// create writer for special data and append its result
		PMMLObjectWriter writer = AbstractPMMLObjectWriter.getWriterForObject(object);

		return writer.export(version);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_FILE, "Specifies the file for saving the pmml.", "pmml", false));
		types.add(new ParameterTypeCategory(PARAMETER_VERSION, "Determines which PMML version should be used for export.",
				PMMLVersion.getVersionIdentifiers(), 0));
		return types;
	}
}
