package com.rapidminer.extension.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleSparseArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

public class FBOperator extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	public FBOperator(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeConfigurable("FB Connection", "Choose a FB connection", "FB");
		type.setOptional(false);
		types.add(new ParameterTypeString("Page ID", "This parameter is the Search String.", "", false));
		types.add(type);
		return types;
	}
	
	public void doWork() {

		String connectionName = "";
		String token = "";
		String response = "";
		String pageId = "";

		try {

			try {
				connectionName = this.getParameter("FB Connection");
			} catch (UndefinedParameterError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			pageId = this.getParameterAsString("Page ID");
			FBConfigurable connection = null;
			connection = (FBConfigurable) ConfigurationManager.getInstance().lookup("FB", connectionName, getProcess().getRepositoryAccessor());
			token = connection.getParameter("access_token");

			FBConfigurable connectObj = new FBConfigurable();
			response = connectObj.connect(token, pageId);
			//LogService.getRoot().log(Level.INFO, "Response String: " + response);

			try {

				JSONObject obj = new JSONObject(response);
				JSONObject obj1 = obj.getJSONObject("posts");
				List<String> list1 = new ArrayList<String>();
				List<String> list2 = new ArrayList<String>();
				List<String> list3 = new ArrayList<String>();
				JSONArray array = new JSONArray();
				array = obj1.getJSONArray("data");

				for (int i = 0; i < array.length(); i++) {
					list1.add(array.getJSONObject(i).getString("message"));
					list2.add(array.getJSONObject(i).getString("created_time"));
					list3.add(array.getJSONObject(i).getString("id"));
				}

				Attribute[] attributes = new Attribute[3];
				attributes[0] = AttributeFactory.createAttribute("Message", Ontology.STRING);
				attributes[1] = AttributeFactory.createAttribute("Created At", Ontology.STRING);
				attributes[2] = AttributeFactory.createAttribute("ID", Ontology.STRING);
				MemoryExampleTable table = new MemoryExampleTable();
				table.addAttribute(attributes[0]);
				table.addAttribute(attributes[1]);
				table.addAttribute(attributes[2]);
				for (int i = 0; i < list1.size(); i++) {
						// LogService.getRoot().log(Level.INFO, strarray1[i]);
					DataRow row = new DoubleSparseArrayDataRow();
					row.set(attributes[0], attributes[0].getMapping().mapString(list1.get(i)));
					row.set(attributes[1], attributes[1].getMapping().mapString(list2.get(i)));
					row.set(attributes[2], attributes[2].getMapping().mapString(list3.get(i)));
					
					table.addDataRow(row);
					
				}

				ExampleSet exampleSet = table.createExampleSet();
				exampleSetOutput.deliver(exampleSet);

			} catch (Exception e) {
				e.printStackTrace();
				LogService.getRoot().log(Level.INFO, "Exception: " + e.toString());
			}

		} catch(Exception e) {
			LogService.getRoot().log(Level.INFO, "Exception: " + e.toString());
		}
	}
}
