package fi.nls.oskari.printout.output.factory;

import org.geotools.feature.NameImpl;
import org.geotools.filter.FunctionFactory;
import org.opengis.feature.type.Name;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RenderSupportFuncFactory implements FunctionFactory {

	
	public Function function(Name name, List<Expression> args, Literal fallback) {
		if (RenderPropertyAccessorFunction.NAME.getFunctionName().equals(name)) {
			return new RenderPropertyAccessorFunction(args, fallback);
		}
		return null; // we do not implement that function
	}

	
	public Function function(String name, List<Expression> args,
			Literal fallback) {
		return function(new NameImpl(name), args, fallback);
	}

	
	public List<FunctionName> getFunctionNames() {
		List<FunctionName> functionList = new ArrayList<FunctionName>();
		functionList.add(RenderPropertyAccessorFunction.NAME);
		return Collections.unmodifiableList(functionList);
	}
}
