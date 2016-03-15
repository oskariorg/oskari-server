package fi.nls.oskari.printout.output.factory;

import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.util.Converters;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

import java.util.List;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

public class RenderPropertyAccessorFunction implements Function {

	static FunctionName NAME = new FunctionNameImpl("renderproperty",
			String.class, parameter("format", String.class), parameter("this",
					Object.class));

	private final List<Expression> parameters;

	private final Literal fallback;

	public RenderPropertyAccessorFunction(List<Expression> parameters,
			Literal fallback) {
		if (parameters == null) {
			throw new NullPointerException("parameters required");
		}
		if (parameters.size() == 0) {
			throw new IllegalArgumentException(
					"labelfunction( format ) requires at least 1 parameter");
		}
		this.parameters = parameters;
		this.fallback = fallback;
	}

	
	public Object accept(ExpressionVisitor visitor, Object extraData) {
		return visitor.visit(this, extraData);
	}

	
	public Object evaluate(Object object) {
		return evaluate(object, String.class);
	}

	
	public <T> T evaluate(Object object, Class<T> context) {
		String result = evaluateFormat(object, parameters);
		return Converters.convert(result, context);
	}

	private String evaluateFormat(Object object, List<Expression> parameters) {

		Object[] values = new String[parameters.size()];

		int v = 0;
		for (Expression formatExpression : parameters) {
			values[v++] = formatExpression.evaluate(object, String.class);
		}

		String format = (String) values[values.length - 1];
		String result = String.format(format, values);

		return result;

	}

	
	public Literal getFallbackValue() {
		return fallback;
	}

	
	public FunctionName getFunctionName() {
		return NAME;
	}

	
	public String getName() {
		return NAME.getName();
	}

	
	public List<Expression> getParameters() {
		return parameters;
	}

}