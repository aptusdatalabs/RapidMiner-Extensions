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

import com.rapidminer.operator.learner.functions.kernel.JMySVMModel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelDot;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelPolynomial;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelGaussianCombination;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelAnova;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelEpanechnikov;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelNeural;


/**
 * This class writes a JMySVM Kernel model to PMML
 * 
 * @author Sebastian Land
 */
public class JMySVMModelPMMLWriter extends AbstractKernelModelPMMLWriter {

	private JMySVMModel model;
	
	public JMySVMModelPMMLWriter(JMySVMModel model) {
		super(model);
		
		this.model = model;
	}

	@Override
	protected Element createKernel(Document pmmlDocument) throws NotSupportedByPMMLException {
		Element kernelElement;
		Kernel kernel = model.getKernel();
		if (kernel instanceof KernelDot) {
			return pmmlDocument.createElement("LinearKernelType");
		} else if (kernel instanceof KernelPolynomial) {
			kernelElement = pmmlDocument.createElement("PolynomialKernelType");
			KernelPolynomial polyKernel = (KernelPolynomial) kernel;
			kernelElement.setAttribute("degree", polyKernel.getDegree() + "");
			return kernelElement;
		} else if (kernel instanceof KernelRadial) {
			kernelElement = pmmlDocument.createElement("RadialBasisKernelType");
			KernelRadial radialKernel = (KernelRadial) kernel;
			kernelElement.setAttribute("gamma", radialKernel.getGamma() + "");
			return kernelElement;
		}  else if (kernel instanceof KernelGaussianCombination) {
			return pmmlDocument.createElement("GaussianCombinationKernelType");
		}  else if (kernel instanceof KernelAnova) {
			return pmmlDocument.createElement("AnovaKernelType");
		}  else if (kernel instanceof KernelEpanechnikov) {
			return pmmlDocument.createElement("EpanechnikovKernelType");
		}  else if (kernel instanceof KernelNeural) {
			return pmmlDocument.createElement("NeuralKernelType");
		} 
		throw new NotSupportedByPMMLException(model);
	}

	@Override
	protected String getAlgorithmName() {
		return "JMySVM";
	}
}
