package com.rapidminer.extension.operator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bouncycastle.crypto.tls.ECBasisType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleSparseArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.associations.AssociationRules;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class MyOperator2 extends Operator {

	
	private static final String ACCESS_TOKEN = "Access-Token";
	private final String USER_AGENT = "Google Chrome";
	
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	
	
	public MyOperator2(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<ParameterType> getParameterTypes(){
		
		List<ParameterType> types = super.getParameterTypes();
		
		types.add(new ParameterTypeString(
				ACCESS_TOKEN,
				"This parameter is the Access Token.",
				"",
				false
				));
		types.add(new ParameterTypeString(
				"Query",
				"This parameter is the Search String.",
				"",
				false
				));
		
		return types;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void doWork() throws OperatorException{
		LogService.getRoot().log(Level.INFO, "Doing Something...");
			
		String accessToken;
		int recordNo = 25;
		String searchString;
		
		
		try {
			accessToken = this.getParameterAsString("Access-Token");
			searchString = this.getParameterAsString("Query");
			
			LogService.getRoot().log(Level.INFO, "no. of records ---> "+recordNo);
			
			String url = "https://graph.facebook.com/v2.10/search/?q="+URLEncoder.encode(searchString,"UTF-8")+"&type=page&access_token="+accessToken;

			
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = con.getResponseCode();
			LogService.getRoot().log(Level.INFO, responseCode+"");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			String response1 = response.toString();
			//LogService.getRoot().log(Level.INFO, "resp -- "+response1);
			
			
			JSONObject obj1 = new JSONObject(response1);
			List<String> list1 = new ArrayList<String>();
			List<String> list2 = new ArrayList<String>();
			
			
			JSONArray array = new JSONArray();
			array = obj1.getJSONArray("data");
			//LogService.getRoot().log(Level.INFO, array.toString());
		    
			for(int i = 0 ; i < array.length() ; i++){
			    list1.add(array.getJSONObject(i).getString("name"));	
			    list2.add(array.getJSONObject(i).getString("id"));	
			    
			    //LogService.getRoot().log(Level.INFO, i+"OBJ ---> "+array.getJSONObject(i).getString("story"));
			}

			Attribute[] attributes = new Attribute[3];
			attributes[0] = AttributeFactory.createAttribute("Page Name", Ontology.STRING);
			attributes[1] = AttributeFactory.createAttribute("Page ID", Ontology.STRING);
			
			MemoryExampleTable table = new MemoryExampleTable();
			table.addAttribute(attributes[0]);
			table.addAttribute(attributes[1]);
			
			for(int i = 0; i<list1.size(); i++ ){
				//LogService.getRoot().log(Level.INFO, strarray1[i]);
				DataRow row = new DoubleSparseArrayDataRow();
				row.set(attributes[0], attributes[0].getMapping().mapString(list1.get(i)));
				row.set(attributes[1], attributes[1].getMapping().mapString(list2.get(i)));				
				table.addDataRow(row);
			}
			
			ExampleSet exampleSet = table.createExampleSet();
			exampleSetOutput.deliver(exampleSet);
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		//fetch example set from input port
		ExampleSet exampleSet = exampleSetInput.getData(ExampleSet.class);
		//get attributes from example set
		Attributes attributes = exampleSet.getAttributes();
		//create a new attribute
		String newName = "story";
		//set name and type of attribute
		Attribute targetAttribute = AttributeFactory.createAttribute(newName, Ontology.STRING);
		//set index of attribute
		targetAttribute.setTableIndex(attributes.size());
		//add attribute
		exampleSet.getExampleTable().addAttribute(targetAttribute);
		attributes.addRegular(targetAttribute);
		//go through example set 
		for(Example example: exampleSet){
			example.setValue(targetAttribute, array.getJSONObject(i).getString("story"));
		}
		//deliver example set to output port
		exampleSetOutput.deliver(exampleSet);
		
		*/
		
	}
	

}
