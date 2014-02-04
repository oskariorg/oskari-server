Adding your own action routes
--
Oskari backend maps ajax services to specific handler classes based on a http parameter `action_route`. Adding an custom route to Oskari backend goes like this:

1) Create a new class implementing `fi.nls.oskari.control.ActionHandler`  
2) Add your code handling the request to `handleAction(ActionParameters params)` method  
3) Annotate the class with `@OskariActionRoute("myRoute")`  
4) Compile with oskari-control-base package on the classpath (for example as a maven dependency):  

	<dependency>
	    <groupId>fi.nls.oskari.service</groupId>
	    <artifactId>oskari-control-base</artifactId>
	</dependency>

5) Done - deploy the app and call your action with the `oskari ajaxUrl + action_route=myRoute`

**Hello world Sample of ActionHandler**


	import fi.nls.oskari.annotation.OskariActionRoute;
	import fi.nls.oskari.control.ActionException;
	import fi.nls.oskari.control.ActionHandler;
	import fi.nls.oskari.control.ActionParameters;
	import fi.nls.oskari.util.ResponseHelper;

	/**
	 * Responds with a hello message
	 */
	@OskariActionRoute("HelloUser")
	public class HelloWorldHandler extends ActionHandler {
	    public void handleAction(ActionParameters params) throws ActionException {
	        ResponseHelper.writeResponse(params, "Hello " + params.getUser().getFirstname());
	    }
	}



Adding your own view modifiers for GetAppSetup action route
--

If the Oskari application view loaded from database needs to be modified based on request parameters, it can be done by implementing a `fi.nls.oskari.control.view.modifier.param.ParamHandler`. This can be used to modify startupsequence and bundle configurations. For example http parameter coords modifies the mapfull configuration so that map location is changed based on the parameter value.

1) Create a new class implementing `fi.nls.oskari.control.view.modifier.param.ParamHandler`  
2) Add your code handling the parameter to `handleParam(final ModifierParams params)` method  
3) Annotate the class with `@OskariViewModifier("myParam")`  
4) Compile with control-base on the classpath (for example as a maven dependency)  
5) Done - deploy the app and get your modified view by calling the `oskari ajaxUrl + action_route=GetAppSetup&myParam=myValue`  


**Hello world Sample of ParamHandler**

Modifies config for "myBundle" adding the property "echoing" with value of an http parameter name "echo" if it's present.

	import fi.nls.oskari.annotation.OskariViewModifier;
	import fi.nls.oskari.util.JSONHelper;
	import fi.nls.oskari.view.modifier.ModifierException;
	import fi.nls.oskari.view.modifier.ModifierParams;
	import org.json.JSONObject;

	@OskariViewModifier("echo")
	public class HelloParamHandler extends ParamHandler {

	    public boolean handleParam(final ModifierParams params) throws ModifierException {
	        final JSONObject config = getBundleConfig(params.getConfig(), "myBundle");
	        JSONHelper.putValue(config, "echoing", params.getParamValue());
	        return false;
	    }
	}



If a bundles config needs to be modified after loading from database, it can be done by implementing a 
`fi.nls.oskari.control.view.modifier.bundle.BundleHandler`. This is used for example for the mapfull bundle with more than few modifications when a view is loaded:
* database only lists layer ids in the config, but the modifier replaces the id arrays with a layer object.
* The base ajax url is replaced with one coming from the `ModifierParams` so it can be switched based on the users locale. 
* If the `modifierParams.isLocationModified()` returns true, `Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin` is removed from the plugins.

1) Create a new class implementing `fi.nls.oskari.control.view.modifier.bundle.BundleHandler`  
2) Add your code for modifying to `modifyBundle(final ModifierParams params)` method  
3) Annotate the class with `@OskariViewModifier("mybundleid")`  
4) Compile with control-base on the classpath (for example as a maven dependency)  
5) Done - deploy the app and get your modified view by calling the `oskari ajaxUrl + action_route=GetAppSetup`. If a bundle with id "mybundleid" was present, your modifier is being called to do its magic.



**Hello world Sample of BundleHandler**

Modifies config for "myBundle" adding the property "hello" with value "world" and if the user is not logged in also modifies state by adding property "hello" with value "stranger"


	import fi.nls.oskari.annotation.OskariViewModifier;
	import fi.nls.oskari.log.Logger;
	import fi.nls.oskari.util.JSONHelper;
	import fi.nls.oskari.view.modifier.ModifierException;
	import fi.nls.oskari.view.modifier.ModifierParams;
	import org.json.JSONObject;

	@OskariViewModifier("myBundle")
	public class HelloWorldHandler extends BundleHandler {

	    /**
	     * Adds a property "hello" to bundles config with the value "world".
	     * Returning false since we didn't modify map location
	     */
	    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
	        final JSONObject bundleConfig = getBundleConfig(params.getConfig());
	        JSONHelper.putValue(bundleConfig, "hello", "world");
	        if(params.getUser().isGuest()) {
	            final JSONObject bundleState = getBundleState(params.getConfig());
	            JSONHelper.putValue(bundleState, "hello", "stranger");
	        }
	        return false;
	    }
	}


If a parameter needs to be preprocessed/sanitized before calling the actual ParamHandler you can add a
preprocessor. Whenever a ParamHandler is registered to Oskari a property is checked if any preprocessors should be added as well:

    view.modifier.param.{param name}.prepocessors={fully qualified classname for class implementing ParamHandler}

The value of the property can have one or more preprocessors classes defined as a comma-separated list. The referenced class should be a subclass
of ParamHandler. Preprocessors are run before the actual ParamHandler and in the order they are defined in the comma-separated list.
Note that preprocessors are NOT annotated with @OskariViewModifier (otherwise they would be added as normal paramhandlers).

**Example for preprocessor ParamHandler**

If param value contains "something offensive", changes the value to "something less offensive". The actual ParamHandler will then receive the preprocessed
value("something less offensive") for the parameter.

	import fi.nls.oskari.view.modifier.ModifierException;
	import fi.nls.oskari.view.modifier.ModifierParams;

	public class HelloParamHandler extends ParamHandler {

	    public boolean handleParam(final ModifierParams params) throws ModifierException {
	        if(params.getParamValue().indexOf("something offensive") != -1) {
	            params.setParamValue("something less offensive");
	            return true;
	        }
	        return false;
	    }
	}

The return values are not used for anything at the moment but a good practice is to return true if
something was modified and false if the preprocessor let the parameter pass as is.