package jplume.view;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jplume.http.HttpResponse;
import jplume.http.Request;
import jplume.http.Response;

public class ViewMethod {

	private Logger logger = LoggerFactory.getLogger(ViewMethod.class);

	private final Pattern pattern;
	private final Method method;
	
	/**
	 * key: index of pathvar
	 */
	private final ArgumentBuilder argBuilder;
	private final String[] requireMethods;

	ViewMethod(Pattern pattern, Method method, ArgumentBuilder argBuilder, 
			            String[] requireMethods) {
		this.pattern   = pattern;
		this.method    = method;
		this.argBuilder = argBuilder;
		this.requireMethods = requireMethods;
	}
	
	public Pattern getPattern() {
		return pattern;
	}

	public Method getMethod() {
		return method;
	}

	public String[] getRequireMethods() {
		return requireMethods;
	}

	public ArgumentBuilder getArgBuilder() {
		return argBuilder;
	}
	
	public Response handle(Request request, String[] indexedVars, Map<String, String> namedVars) {
		try {
			if (requireMethods.length > 0
					&& Arrays.binarySearch(requireMethods, request.getMethod().toLowerCase()) < 0) {
				return HttpResponse.forbidden();
			}
			Class<?> actionClass = this.method.getDeclaringClass();
			Object action = null;
			if ((method.getModifiers() & Modifier.STATIC) == 0) { // method is static method
				action = actionClass.newInstance();
			}
			
			Object[] args = argBuilder.build(request, indexedVars, namedVars);
			
			Response resp = null;
			Class<?> returnType = this.method.getReturnType();
			if (returnType.equals(String.class)) {
				resp = HttpResponse.ok((String) this.method.invoke(action, args));
			} else if (Response.class.isAssignableFrom(returnType)) {
				resp = (Response) this.method.invoke(action, args);
			}
			return resp;
		} catch (Exception e) {
			Throwable except = e;
			while(except.getCause() != null) {
				except = except.getCause();
			}
			throw new ViewException(except);
		}
	}

}