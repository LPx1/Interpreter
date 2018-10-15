package edu.sjsu.fwjs;

import java.util.Map;

import javax.management.RuntimeErrorException;

import jdk.internal.org.objectweb.asm.tree.analysis.Value;

import java.util.HashMap;

public class Environment {
    private Map<String,Value> env = new HashMap<String,Value>(); 
    private Environment outerEnv;

    /**
     * Constructor for global environment ***
     */
    public Environment() {}

    /**
     * Constructor for local environment of a function ***
     */
    public Environment(Environment outerEnv) {
        this.outerEnv = outerEnv;
    }

    /**
     * Handles the logic of resolving a variable.
     * If the variable name is in the current scope, it is returned.
     * Otherwise, search for the variable in the outer scope.
     * If we are at the outermost scope (AKA the global scope)
     * null is returned (similar to how JS returns undefined.
     */
    public Value resolveVar(String varName) {
        // YOUR CODE HERE
    	//
    	//Handles and checks if variable name exists in local scope, if so return it.
    	//else if, check the outer scope and return that
    	//Otherwise if none, return the value as none.
        if (env.containsKey(varName)){
            return env.containsKey(varName)
        }
        else if (outerEnv != null
        		&& outerEnv.resolveVar(varName) != null) {
        	return outerEnv.resolveVar(varName);
        }
        return null;
    }

    /**
     * Used for updating existing variables.
     * If a variable has not been defined previously in the current scope,
     * or any of the function's outer scopes, the var is stored in the global scope.
     */
    public void updateVar(String key, Value v) {
        // YOUR CODE HERE
    	//
    	//If the local env contains variable then replace the key with the new value
    	//else, check if the global env contains the key 
    	//otherwise just create the key in the global scope 
    	if(env.containsKey(key)) {
    		env.replace(key, value);
    	}
    	else if (outerEnv != null & 
    			outerEnv.resolveVar(key) != null)
    	{
    		outerEnv.updateVar(key, v);
    	}
    	else {
    		outerEnv.createVar(key, v);
    	}
    }

    /**
     * Creates a new variable in the local scope.
     * If the variable has been defined in the current scope previously,
     * a RuntimeException is thrown.
     */
    public void createVar(String key, Value v) {
        // YOUR CODE HERE
    	//
    	//If the key already exists throw an error 
    	//otherwise, create the new variable within the local(env) scope
    	if(env.containsKey(key)) {
    		throw new RuntimeErrorException(key, " already exists in this scope.");
    	}
    	else {
    		env.put(key, value);
    	}
    }
}
