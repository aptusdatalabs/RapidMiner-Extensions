package com.rapidminer.extension.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeText;
import com.rapidminer.parameter.TextType;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;

public class POST extends Operator {
	private static final String PARAM_URL_TEXT = "url";
	private OutputPort outputPort = getOutputPorts().createPort("output");

	// private InputPort data_input = getInputPorts().createPort("examples set");
	public POST(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doWork() throws OperatorException, UndefinedParameterError {
		String baseURL = this.getParameterAsString(PARAM_URL_TEXT).trim();
		List<String[]> selectedList;
		List<String[]> headers;
	
		selectedList = this.getParameterList("parameters");
		headers = this.getParameterList("headers");
		String params = "";
		params = (selectedList.size() == 0 ? "" : "?");

		Map<String, String> head = new HashMap<String, String>();
		for (String i[] : headers) {
			head.put(i[0], i[1]);
		}

		for (String i[] : selectedList) {
			params = params + i[0] + "=" + i[1] + "&";
		}
		String param = "";
		if (params.length() != 0) {
			param = params.substring(0, params.length() - 1);
		}
		int condition = getParameterAsInt("body category");
		HttpResponse<String> jsonResponse = null;

		String url = baseURL + param;

		switch (condition) {
		case 0:
			List<String[]> keyval = getParameterList("key-value data");
			String keyvaldata = "";
			head.put("Content-Type", "application/x-www-form-urlencoded");
			for (String i[] : keyval)
				keyvaldata += i[0] + "=" + i[1] + "&";
			String text1 = "";
			if (!((keyvaldata.length()) == 0))
				text1 = keyvaldata.substring(0, keyvaldata.length() - 1);

			try {
				jsonResponse = Unirest.post(url).headers(head).body(text1).asString();
			} catch (UnirestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 1:
			int response = getParameterAsInt("raw data");
			String text = "";
			switch (response) {
			case 0:
				text = getParameterAsString("text");

				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1:
				text = getParameterAsString("text/plain");
				head.put("Content-Type", "text/plain");
				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 2:
				text = getParameterAsString("json body");
				head.put("Content-Type", "application/json");
				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 3:
				text = getParameterAsString("javascript body");
				head.put("Content-Type", "application/javascript");
				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 4:
				text = getParameterAsString("xml body");
				head.put("Content-Type", "application/xml");
				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 5:
				text = getParameterAsString("xml text");
				head.put("Content-Type", "text/xml");
				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 6:
				text = getParameterAsString("html text");
				head.put("Content-Type", "text/html");
				try {
					jsonResponse = Unirest.post(url).headers(head).body(text).asString();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			}

			break;
		}

		NewModel model = new NewModel();
		model.setmodel(
				jsonResponse.getBody().toString() + "\n\n\n API Request with status: " + jsonResponse.getStatus());
		outputPort.deliver(model);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(PARAM_URL_TEXT, "url"));
		// types.add(new ParameterTypeInt(PARAMETER_TEXT1,"Number of
		// items",0,10000000,false));

		String bodyarray[] = { "x-www-form-urlencoded", "raw" };
		String raw_category[] = { "text", "text/plain", "json(application/json)", "javascript", "xml(application/xml)",
				"xml(text/xml)", "html(text/html)" };

		types.add(new ParameterTypeList("parameters", "Specifies a list key value pairs as parameters.",
				new ParameterTypeString("key", "Specifies the name of the attribute"),
				new ParameterTypeString("value", "Value corresponding to the key"), false));
		
		types.add(new ParameterTypeList("headers", "Specifies a list key value pairs as Header",
				new ParameterTypeString("key", "Specifies the name of the attribute"),
				new ParameterTypeString("value", "Value corresponding to the key"), false));
		
		types.add(new ParameterTypeCategory("body category", "Body for POST method", bodyarray, 0));

		ParameterType type = new ParameterTypeList("key-value data", "Specifies a list key value pairs as parameters.",
				new ParameterTypeString("key", "Specifies the name of the attribute"),
				new ParameterTypeString("value", "Value corresponding to the key"), false);
		
		type.registerDependencyCondition(
				new EqualTypeCondition(this, "body category", bodyarray, false, new int[] { 0 }));
		types.add(type);

		type = new ParameterTypeCategory("raw data", "Enter Body", raw_category, 0);
		type.registerDependencyCondition(
				new EqualTypeCondition(this, "body category", bodyarray, true, new int[] { 1 }));
		types.add(type);

		type = (new ParameterTypeText("text", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 0 }));
		types.add(type);

		type = (new ParameterTypeText("text/plain", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 1 }));
		types.add(type);

		type = (new ParameterTypeText("json body", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 2 }));
		types.add(type);

		type = (new ParameterTypeText("javascript body", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 3 }));
		types.add(type);

		type = (new ParameterTypeText("xml body", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 4 }));
		types.add(type);

		type = (new ParameterTypeText("xml text", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 5 }));
		types.add(type);

		type = (new ParameterTypeText("html text", "Enter Body", TextType.PLAIN));
		type.registerDependencyCondition(new EqualTypeCondition(this, "raw data", raw_category, true, new int[] { 6 }));
		types.add(type);

		return types;
	}
}