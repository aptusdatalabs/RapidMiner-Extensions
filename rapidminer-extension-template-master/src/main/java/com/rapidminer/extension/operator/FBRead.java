package com.rapidminer.extension.operator;

import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

public class FBRead extends Operator{

	private OutputPort resultsPort = getOutputPorts().createPort("Results");

	
	public FBRead(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}
	
	public List<ParameterType> getParameterTypes() {
	    List<ParameterType> types = super.getParameterTypes();
	    ParameterType type = new ParameterTypeConfigurable("FB Connection", "Choose a FB connection", "FB");
	    type.setOptional(false);
	    types.add(type);
	    return types;
	}
}
