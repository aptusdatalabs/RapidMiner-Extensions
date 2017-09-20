package com.rapidminer.extension.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeOAuth;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.config.AbstractConfigurator;

public class FBConfigurator extends AbstractConfigurator<FBConfigurable> {

	@Override
	public Class<FBConfigurable> getConfigurableClass() {
		return FBConfigurable.class;
	}

	@Override
	public String getI18NBaseKey() {
		return "fbconfig";
	}

	@Override
	public List<ParameterType> getParameterTypes(ParameterHandler parameterHandler) {
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		FBOAuth obj = new FBOAuth();
		
		ParameterType type = new ParameterTypeOAuth("access_token", "Access Token", obj);

		type.setOptional(false);
		parameters.add(type);

		return parameters;
	}

	@Override
	public String getTypeId() {
		return "FB";
	}

}