/* CS
 * @Luis Pamintuan
 * @Ivan Hernandez 
 * 
 * 	16 October 2018
 */

package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @SuppressWarnings("incomplete-switch")
    public Value evaluate(Environment env) {
    	
    	IntVal num1 = (IntVal)e1.evaluate(env);
    	IntVal num2 = (IntVal)e2.evaluate(env);
    	Value returnVal = new NullVal();
    	switch(op) {
    		case ADD: 
    			returnVal = new IntVal(num1.toInt() + num2.toInt());
    			break;
    		case SUBTRACT:
    			returnVal = new IntVal(num1.toInt() - num2.toInt());
    			break;
    		case MULTIPLY:
    			returnVal = new IntVal(num1.toInt() * num2.toInt());
    			break;
    		case DIVIDE:
    			returnVal = new IntVal(num1.toInt()/num2.toInt());
    			break;
    		case MOD:
    			returnVal = new IntVal(num1.toInt() % num2.toInt());
    			break;
    		case GT:
    			returnVal = new BoolVal(num1.toInt() > num2.toInt());
    			break;
    		case GE:
    			returnVal = new BoolVal(num1.toInt() >= num2.toInt());
    			break;
    		case LT: 
    			returnVal = new BoolVal(num1.toInt() < num2.toInt());
    			break;
    		case LE:
    			returnVal = new BoolVal(num1.toInt() <= num2.toInt());
    			break;
    		case EQ:
    			returnVal = new BoolVal(num1.toInt() == num2.toInt());
    			break;
    		
    	}
    	return returnVal;
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
    public Value evaluate(Environment env) {
        Value v = cond.evaluate(env);
        Value returnVal = null;
        if(v instanceof IntVal && ((IntVal)v).toInt() == 0)
			throw new RuntimeException("Error");
        else if(v instanceof BoolVal && ((BoolVal)v).toBoolean()) 
        	returnVal = thn.evaluate(new Environment());
        else
        	returnVal = els.evaluate(new Environment());
        return returnVal;
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	while(true) {
			Value v = cond.evaluate(env);
			if(!(v instanceof BoolVal))
				throw new RuntimeException("Condition does not evaluate to boolean val.");
			else if (((BoolVal)v).toBoolean()){
				body.evaluate(env);
			} else {
				return new NullVal();
			}
		}
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
    	if(e1 == null)
    		return new NullVal();
        e1.evaluate(env);
        if(e2 == null)
        	return new NullVal();
    	return e2.evaluate(env);
        
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = new NullVal();
        if(exp == null) 
        	env.createVar(varName, v);
        else {
        	v = exp.evaluate(env);     
        	env.createVar(varName, v);
        }
        return v;
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
    	Value v = new NullVal();
        if(e == null) {
        	return v;
        }
        v = e.evaluate(env);
        env.updateVar(varName, v);
        return v;
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	return new ClosureVal(params, body, env);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
        Value v = f.evaluate(env);
        List<Value> vArgs = new ArrayList<Value>();
        for(int i = 0; i < args.size(); i++) {
        	vArgs.add(i, args.get(i).evaluate(env));
        }
        ClosureVal cVal = (ClosureVal)v;
        
        return cVal.apply(vArgs);
    }
}

