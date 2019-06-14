package com.rapidminer.extension.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleSparseArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeList;
import com.github.opendevl.JFlat;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class GET extends Operator {
	private static final String PARAM_URL_TEXT = "url";

	private OutputPort outputPort = getOutputPorts().createPort("output");

	public GET(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException, UndefinedParameterError {

		String baseURL = this.getParameterAsString(PARAM_URL_TEXT).trim();

		List<String[]> selectedList;
		List<String[]> headers;
		String outputFormat;

		selectedList = this.getParameterList("parameters");
		headers = this.getParameterList("header");
		outputFormat = this.getParameterAsString("output format").trim();

		String params = "";
		params = (selectedList.size() == 0 ? "" : "?");

		Map<String, String> head = new HashMap<String,String>();
		
		for (String i[] : headers) {
			LogService.getRoot().log(Level.INFO, i[0].toString()+" "+i[1].toString());
			head.put(i[0], i[1]);
		}

		for (String i[] : selectedList) {
			params = params + i[0] + "=" + i[1] + "&";
		}
		String param = (params.length() > 0 ? params.substring(0, params.length() - 1) : "");

		String url = baseURL + param;

		HttpResponse<String> jsonResponse = null;

		try {
			jsonResponse = Unirest.get(url).headers(head).asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}

		if (outputPort.isConnected()) {

			switch (outputFormat.toLowerCase()) {

			case "csv":
				List<Object[]> json2csv = null;
				JFlat jf = new JFlat(jsonResponse.getBody().toString());

				try {
					json2csv = jf.json2Sheet().headerSeparator("_").getJsonAsSheet();
				} catch (Exception e) {
					e.printStackTrace();
				}

				int x = json2csv.get(0).length;
				
				Attribute[] attribute = new Attribute[x];
				for (int i = 0; i < x; i++) {
					attribute[i] = AttributeFactory.createAttribute(json2csv.get(0)[i].toString(), Ontology.STRING);
				}
				json2csv.remove(0);

				MemoryExampleTable table = new MemoryExampleTable();
				for (int i = 0; i < x; i++) {
					table.addAttribute(attribute[i]);
				}

				for (Object[] is : json2csv) {
					DataRow dr2 = new DoubleSparseArrayDataRow();
					for (int i = 0; i < x; i++) {
						dr2.set(attribute[i],
								attribute[i].getMapping().mapString(is[i].toString().replaceAll("\"", "")));
					}
					table.addDataRow(dr2);
				}

				ExampleSet exampleSet2 = table.createExampleSet();
				outputPort.deliver(exampleSet2);
				break;

			default:
				NewModel model = new NewModel();
				model.setmodel(jsonResponse.getBody().toString() + "\n\n\n API Request with status: "
						+ jsonResponse.getStatus());
				outputPort.deliver(model);
			}
		}

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(PARAM_URL_TEXT, "url", false));

		types.add(new ParameterTypeList("parameters", "Specifies a list key-value pairs as parameters.",
				new ParameterTypeString("key", "Specifies the name of the attribute"),
				new ParameterTypeString("value", "Value corresponding to the key"), true));

		types.add(new ParameterTypeList("header", "Specifies a list key-value pairs as Header",
				new ParameterTypeString("key", "Specifies the name of the attribute"),
				new ParameterTypeString("value", "Value corresponding to the key"), true));

		String[] categories = { "json", "csv" };

		types.add(new ParameterTypeCategory("output format", "Specify the output format", categories, 0));

		return types;
	}
}