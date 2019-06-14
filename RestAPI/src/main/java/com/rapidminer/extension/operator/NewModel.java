package com.rapidminer.extension.operator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.HeaderExampleSet;
import com.rapidminer.example.set.ModelViewExampleSet;
import com.rapidminer.example.set.NonSpecialAttributesExampleSet;
import com.rapidminer.example.set.RemappedExampleSet;
import com.rapidminer.gui.renderer.RendererService;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.OperatorProgress;
import com.rapidminer.operator.UnsupportedApplicationParameterError;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ViewModel;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.operator.preprocessing.MaterializeDataInMemory;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.XMLSerialization;
import com.rapidminer.tools.container.Pair;

public class NewModel implements Model, ViewModel{
	private String source = null;
	private transient LoggingHandler loggingHandler;
	private HashMap<String, Object> parameterMap = new HashMap<>();
	private transient LinkedList<ProcessingStep> processingHistory = new LinkedList<>();

	private transient HashMap<String, Object> userData = new HashMap<>();
	
	
	private HeaderExampleSet headerExampleSet;

	/**
	 * This Operator will be used to check whether the currently running Process was stopped. If it
	 * is <code>null</code> nothing will happen else checkForStop will be called.
	 */
	private Operator operator = null;
	
	/**
	 * This flag signalizes the apply method if progress in the {@link OperatorProgress} from {@link #getOperator()} should be shown.
	 */
	private boolean showProgress = false;
	
	String name=null;
	private Annotations annotations;
	
	public void setmodel(String arg)
	{
	this.name=arg;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
	    buffer.append(name);
		return buffer.toString();
}
	
	
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Output";
	}

	@Override
	public String toResultString() {
		// TODO Auto-generated method stub
		return toString();
	}

	@Override
	public Icon getResultIcon() {
		// TODO Auto-generated method stub
		return RendererService.getIcon(this.getClass());
	}

	@Override
	public List<Action> getActions() {
		// TODO Auto-generated method stub
		return new LinkedList<>();
	}

	@Override
	public void setSource(String sourceName) {
		// TODO Auto-generated method stub
		this.source=sourceName;
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return source;
	}

	@Override
	public void appendOperatorToHistory(Operator operator, OutputPort port) {
		// TODO Auto-generated method stub
		if (processingHistory == null) {
			processingHistory = new LinkedList<>();
			if (operator.getProcess() != null) {
				processingHistory.add(new ProcessingStep(operator, port));
			}
		}
		ProcessingStep newStep = new ProcessingStep(operator, port);
		if (operator.getProcess() != null && (processingHistory.isEmpty() || !processingHistory.getLast().equals(newStep))) {
			processingHistory.add(newStep);
		}
	}

	@Override
	public List<ProcessingStep> getProcessingHistory() {
		// TODO Auto-generated method stub
		if (processingHistory == null) {
			processingHistory = new LinkedList<>();
		}
		return processingHistory;
	}

	@Override
	public IOObject copy() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		initWriting();
		XMLSerialization.getXMLSerialization().writeXML(this, out);
	}

	private void initWriting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LoggingHandler getLog() {
		// TODO Auto-generated method stub
		if (this.loggingHandler != null) {
			return this.loggingHandler;
		} else {
			return LogService.getGlobal();
		}
	}

	@Override
	public void setLoggingHandler(LoggingHandler loggingHandler) {
		// TODO Auto-generated method stub
		this.loggingHandler = loggingHandler;
	}

	@Override
	public Annotations getAnnotations() {
		// TODO Auto-generated method stub
		Annotations a =new Annotations();
		return a;
	}

	@Override
	public Object getUserData(String key) {
		// TODO Auto-generated method stub
		if (userData == null) {
			userData = new HashMap<>();
		}
		return userData.get(key);
	}

	@Override
	public Object setUserData(String key, Object value) {
		// TODO Auto-generated method stub
		return userData.put(key, value);
	}

	@Override
	public boolean isInTargetEncoding() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HeaderExampleSet getTrainingHeader() {
		// TODO Auto-generated method stub
		return this.headerExampleSet;
	}

	@Override
	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		// TODO Auto-generated method stub
		boolean createView = isCreateView();

		// materialize if the model writes into existing data
		if (!createView && writesIntoExistingData()) {
			exampleSet = MaterializeDataInMemory.materializeExampleSet(exampleSet);
		}
		// adapting example set to contain only attributes, which were present during learning time
		// and remove roles if necessary
		ExampleSet nonSpecialRemapped = RemappedExampleSet.create(
				isSupportingAttributeRoles() ? exampleSet : NonSpecialAttributesExampleSet.create(exampleSet),
				getTrainingHeader(), false, needsRemapping());

		LinkedList<AttributeRole> unusedList = new LinkedList<>();
		Iterator<AttributeRole> iterator = exampleSet.getAttributes().allAttributeRoles();
		while (iterator.hasNext()) {
			AttributeRole role = iterator.next();
			if (nonSpecialRemapped.getAttributes().get(role.getAttribute().getName()) == null) {
				unusedList.add(role);
			}
		}

		ExampleSet result;
		if (createView) {
			// creating only view
			result = ModelViewExampleSet.create(nonSpecialRemapped, this);
		} else {
			result = apply(nonSpecialRemapped);
		}

		// restoring roles if possible
		Iterator<Attribute> attributeIterator = result.getAttributes().allAttributes();
		List<Pair<Attribute, String>> roleList = new LinkedList<>();
		Attributes inputAttributes = exampleSet.getAttributes();
		while (attributeIterator.hasNext()) {
			Attribute resultAttribute = attributeIterator.next();
			AttributeRole role = inputAttributes.getRole(resultAttribute.getName());
			if (role != null && role.isSpecial()) {
				// since underlying connection is changed
				roleList.add(new Pair<>(resultAttribute, role.getSpecialName()));
			}
		}
		for (Pair<Attribute, String> rolePair : roleList) {
			result.getAttributes().setSpecialAttribute(rolePair.getFirst(), rolePair.getSecond());
		}

		// adding unused
		Attributes resultAttributes = result.getAttributes();
		for (AttributeRole role : unusedList) {
			resultAttributes.add(role);
		}
		return result;
	}
	private boolean needsRemapping() {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean isSupportingAttributeRoles() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean writesIntoExistingData() {
		// TODO Auto-generated method stub
		return false;
	}

	protected boolean isCreateView() {
		boolean createView = false;
		if (parameterMap.containsKey(PreprocessingOperator.PARAMETER_CREATE_VIEW)) {
			Boolean booleanObject = (Boolean) parameterMap.get(PreprocessingOperator.PARAMETER_CREATE_VIEW);
			if (booleanObject != null) {
				createView = booleanObject.booleanValue();
			}
		}
		return createView;
	}

	@Override
	public void setParameter(String key, Object value) throws OperatorException {
		// TODO Auto-generated method stub
		throw new UnsupportedApplicationParameterError(null, getName(), key);
	}

	@Override
	public boolean isUpdatable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateModel(ExampleSet updateExampleSet) throws OperatorException {
		// TODO Auto-generated method stub
		throw new UserError(null, 135, getClass().getName());
		
	}

	@Override
	public Attributes getTargetAttributes(ExampleSet viewParent) {
		// TODO Auto-generated methgetTargetAttributesod stub
		return null;
	}

	@Override
	public double getValue(Attribute targetAttribute, double value) {
		// TODO Auto-generated method stub
		return 0;
	}

}
