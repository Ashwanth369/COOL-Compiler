package cool;

import java.util.*;
import cool.Node;
import cool.InheritanceGraph;
import cool.ScopeTable;
import cool.AST;

public class ExprChecks {

	String filename;
	String currentClass;											//This variable helps in finding the class of self.
	public InheritanceGraph ig;

	public ExprChecks(InheritanceGraph inheritanceGraph,String fname) {
		ig = inheritanceGraph;
		filename = fname;
	}

	//The following function checks expression for all the attributes and methods in a class. First initializes the scope table with all 
	//the attributes and their typeids just to make sure that later declarations of an attribute do not result in an error.
	public void check(AST.class_ cls,ScopeTable<String> st) {
		currentClass = cls.name;
		Node n = ig.graph.get(ig.nameToIndx.get(cls.name));
		for(Map.Entry<String,AST.attr> it : n.attributes.entrySet()) {
			st.insert(it.getValue().name,it.getValue().typeid);
		}
		for(Map.Entry<String,AST.attr> it : n.attributes.entrySet()) {
			check(it.getValue(),st);
		}
		for(Map.Entry<String,AST.method> it : n.methods.entrySet()) {
			check(it.getValue(),st);
		}
	}

	//The following checks for an attribute if it's typeid is declared and it's value confirms to it's typeid.
	public void check(AST.attr a,ScopeTable<String> st) {

		check(a.value,st);
		String type = st.lookUpGlobal(a.name);

		if(a.name.equals("self")) {																	//Attribute name is self which is not possible.
			Semantic.reportError(filename,a.lineNo,"Attribute cannot be named self.");
			st.replace(a.name,"Object");
		}
		else if(!(ig.nameToIndx.containsKey(a.typeid))) {											//The declared typeid of the attribute is not defined.
			Semantic.reportError(filename,a.lineNo,"Undefined Class "+a.typeid+" of attribute "+a.name+".");
			st.replace(a.name,"Object");
		}
		else if(!(a.value instanceof AST.no_expr)) {

			if(!(ig.confirmcheck(a.typeid,a.value.type))) {											//check if the value of the attribute confirms to it's typeid.
				Semantic.reportError(filename,a.value.lineNo,"Attribute value type "+a.value.type+" does not confirm to the defined type "+a.typeid+".");
			}
		}

	}

	//The following two functions checks for an method if it's formal's typeids are declared correctly and it's return type confirms to it's defined typeid.
	public void check(AST.method m,ScopeTable<String> st) {
		st.enterScope();

		for(AST.formal f : m.formals) {							//Goes through all the formals and checks each formal.
			check(f,st);
		}
		if(m.body!=null) {
			check(m.body,st);									//Checks for the body expression of the method and
			if(!(ig.confirmcheck(m.typeid,m.body.type))) {		//checks if the body expression's type confirms to the method defined typeid.
				Semantic.reportError(filename,m.body.lineNo,"Method return type "+m.body.type+" does not confirm to the method defined type "+m.typeid+".");
			}
		}
		st.exitScope();
	}
	public void check(AST.formal f,ScopeTable<String> st) {
		if(!(ig.nameToIndx.containsKey(f.typeid))) {			//Checks if the formals typeid is defined.
			Semantic.reportError(filename,f.lineNo,"Undefined type "+f.typeid+" of formal "+f.name+".");
		}
		else {
			String formalname = st.lookUpLocal(f.name);			//This looks for the formals that are added to the scope table so far and checks if formal has already been added.
			if(formalname == null) {
				st.insert(f.name,f.typeid);
			}
			else {
				Semantic.reportError(filename,f.lineNo,"Formal "+f.name+" cannot be redefined.");
			}
		}
		
	}

	//The following function checks for different of expressions.
	public void check(AST.expression exp,ScopeTable<String> st) {
		if(exp instanceof AST.bool_const) {		//If the expression is an instance of bool_const.
			exp.type = "Bool";							//expression type for such an expression is Bool.
		}
		else if(exp instanceof AST.string_const) {		//If the expression is an instance of string_const.
			AST.string_const expr = (AST.string_const)exp;
			ExprPrinter.stringRegister.put(expr.value,"@.str."+ExprPrinter.stringConstantsCounter);
			ExprPrinter.stringConstantsCounter++;
			exp.type = "String";						//expression type for such an expression is String.
		}
		else if(exp instanceof AST.int_const) {			//If the expression is an instance of int_const.
			exp.type = "Int";							//expression type for such an expression is Int.
		}
		else if(exp instanceof AST.object) {			//If the expression is an instance of object.
			AST.object expr = (AST.object)exp;
			if(expr.name.equals("self")) {				//If the object is self, then it's type is the current class.
				expr.type = currentClass;
			}
			else {										//Check if the object is defined in the scope.
				String type = st.lookUpGlobal(expr.name);
				if(type == null) {
					Semantic.reportError(filename,expr.lineNo,"The object "+expr.name+" is undeclared.");
					expr.type = "Object";
				}
				else {
					expr.type = type;
				}
			}
		}
		else if(exp instanceof AST.comp) {				//If the expression is an instance of comp. The argument passed must be Bool and return type must also be Bool.
			AST.comp expr = (AST.comp)exp;

			check(expr.e1,st);
			if(!(expr.e1.type.equals("Bool"))) {
				Semantic.reportError(filename,expr.lineNo,"The argument passed to complement is of type non Integer.");
			}
			expr.type = "Bool";
		}
		else if(exp instanceof AST.eq) {				//If the expression is an instance of equals. If the arguments are basic classes such as Int,String,Bool there will be a compilation error.
			AST.eq expr = (AST.eq)exp;					//Else they are checked during the run time and we just set the return type to be Bool.

			check(expr.e1,st);
			check(expr.e2,st);

			if((expr.e1.type.equals("Int") || expr.e1.type.equals("Bool") || expr.e1.type.equals("String")) || (expr.e2.type.equals("Int") || expr.e2.type.equals("Bool") || expr.e2.type.equals("String"))) {
				if(!(expr.e1.type.equals(expr.e2.type))) {
					Semantic.reportError(filename,expr.lineNo,"The arguments in equals expression are of different basic type(s).");
				}
			}
			expr.type = "Bool";
		}
		else if(exp instanceof AST.leq) {				//If the expression is an instance of less equals. The two arguments passed must be both of type Int. Return type is bool.
			AST.leq expr = (AST.leq)exp;

			check(expr.e1,st);
			check(expr.e2,st);

			if(!expr.e1.type.equals("Int") || !expr.e2.type.equals("Int")) {
				Semantic.reportError(filename,expr.lineNo,"The argument(s) in less than equal expression is of type non Integer.");
			}
			expr.type = "Bool";
		}
		else if(exp instanceof AST.lt) {				//If the expression is an instance of less than. The two arguments passed must be both of type Int. Return type is bool.
			AST.lt expr = (AST.lt)exp;

			check(expr.e1,st);
			check(expr.e2,st);

			if(!expr.e1.type.equals("Int") || !expr.e2.type.equals("Int")) {
				Semantic.reportError(filename,expr.lineNo,"The argument(s) in less than expression is of type non Integer.");
			}
			expr.type = "Bool";
		}
		else if(exp instanceof AST.neg) {				//If the expression is an instance of neg. The argument passed must be Int and return type must also be Int.
			AST.neg expr = (AST.neg)exp;

			check(expr.e1,st);

			if(!(expr.e1.type.equals("Int"))) {
				Semantic.reportError(filename,expr.lineNo,"The argument passed to negation is of type non Boolean.");
			}
			expr.type = "Int";
		}
		else if(exp instanceof AST.divide) {			//If the expression is an instance of divide. The two arguments passed must be both of type Int. Return type is Int.
			AST.divide expr = (AST.divide)exp;

			check(expr.e1,st);
			check(expr.e2,st);

			if(!expr.e1.type.equals("Int") || !expr.e2.type.equals("Int")) {
				Semantic.reportError(filename,expr.lineNo,"The argument(s) in division expression is of type non Integer.");
			}
			expr.type = "Int";
		}
		else if(exp instanceof AST.mul) {				//If the expression is an instance of mul. The two arguments passed must be both of type Int. Return type is Int.
			AST.mul expr = (AST.mul)exp;

			check(expr.e1,st);
			check(expr.e2,st);

			if(!expr.e1.type.equals("Int") || !expr.e2.type.equals("Int")) {
				Semantic.reportError(filename,expr.lineNo,"The argument(s) in multiplication expression is of type non Integer.");
			}
			expr.type = "Int";
		}
		else if(exp instanceof AST.sub) {				//If the expression is an instance of sub. The two arguments passed must be both of type Int. Return type is Int.
			AST.sub expr = (AST.sub)exp;

			check(expr.e1,st);
			check(expr.e2,st);

			if(!expr.e1.type.equals("Int") || !expr.e2.type.equals("Int")) {
				Semantic.reportError(filename,expr.lineNo,"The argument(s) in subtraction expression is of type non Integer.");
			}
			expr.type = "Int";
		}
		else if(exp instanceof AST.plus) {				//If the expression is an instance of plus. The two arguments passed must be both of type Int. Return type is Int.
			AST.plus expr = (AST.plus)exp;

			check(expr.e1,st);
			check(expr.e2,st);

			if(!expr.e1.type.equals("Int") || !expr.e2.type.equals("Int")) {
				Semantic.reportError(filename,expr.lineNo,"The argument(s) in addition expression is of type non Integer.");
			}
			expr.type = "Int";
		}
		else if(exp instanceof AST.isvoid) {			//If the expression is an instance of isvoid. Return type is Bool.
			exp.type = "Bool";
		}
		else if(exp instanceof AST.new_) {				//If the expression is an instance of new_. Checks if the typeid has been defined. Return type is the typeid.
			AST.new_ expr = (AST.new_)exp;

			if(!(ig.nameToIndx.containsKey(expr.typeid))) {
				Semantic.reportError(filename,expr.lineNo,"The class "+expr.typeid+" has not been defined.");
				expr.type = "Object";
			}
			else {
				expr.type = expr.typeid;
			}
		}
		else if(exp instanceof AST.assign) {			//If the expression is an instance of assign.
			AST.assign expr = (AST.assign)exp;
			
			if(expr.name.equals("self")) {				//Checks if the variable's name is self.
				Semantic.reportError(filename,expr.lineNo,"Cannot assign to self.");
			}
			else {
				check(expr.e1,st);						
					
				String typeid = st.lookUpGlobal(expr.name);				//To check if the variable has already been defined in the scope.
				if(typeid == null){
					Semantic.reportError(filename,expr.lineNo,"Undeclared Variable "+expr.name+".");		
				}
				else if(!(ig.confirmcheck(typeid,expr.e1.type))) {		//If declared, then check if the type of the expression confirms with the varaiable's typeid.
					Semantic.reportError(filename,expr.lineNo,"Declared type of identifier "+expr.e1.type+" does not confirm with that of declared type of identifier "+expr.name+" ("+typeid+").");
				}
			}
			expr.type = expr.e1.type;
		}
		else if(exp instanceof AST.block) {				//If the expression is an instance of block. Return type is the type of the last expression.
			AST.block expr = (AST.block)exp;

			for(AST.expression e : expr.l1) {
				check(e,st);
			}

			expr.type = expr.l1.get(expr.l1.size()-1).type;
		}
		else if(exp instanceof AST.loop) {				//If the expression is an instance of loop. Check the predicate's type and body's type. Predicate type must a Bool. Return type is Object.
			AST.loop expr = (AST.loop)exp;

			check(expr.predicate,st);
			check(expr.body,st);

			if(!expr.predicate.type.equals("Bool")) {
				Semantic.reportError(filename,expr.predicate.lineNo,"Predicate type does not match with type Bool.");
			}

			expr.type = "Object";
		}
		else if(exp instanceof AST.cond) {				//If the expression is an instance of condition. Check the predicate's type. Predicate type must a Bool. Return type is the join of ifbody's type and elsebody's type.
			AST.cond expr = (AST.cond)exp;

			check(expr.predicate,st);

			if(!expr.predicate.type.equals("Bool")) {
				Semantic.reportError(filename,expr.predicate.lineNo,"Predicate type does not match with type Bool.");
			}

			check(expr.ifbody,st);
			check(expr.elsebody,st);
			expr.type = ig.join(ig.graph.get(ig.nameToIndx.get(expr.ifbody.type)),ig.graph.get(ig.nameToIndx.get(expr.elsebody.type))).clas.name;
		}
		else if(exp instanceof AST.let) {				//If the expression is an instance of let.
			AST.let expr = (AST.let)exp;

			if(expr.name.equals("self")) {				//Checks if the variable's name is self.
				Semantic.reportError(filename,expr.lineNo,"Cannot assign to self in let statement.");
			}
			else {

				if(!(ig.nameToIndx.containsKey(expr.typeid))) {					//If the typeid of the variable is already defined.
					Semantic.reportError(filename,expr.lineNo,"Undefined type "+expr.typeid+".");
				}

				if(!(expr.value instanceof AST.no_expr)) {
					check(expr.value,st);
					if(!(ig.confirmcheck(expr.typeid,expr.value.type))) {				//If the value of the variable confirms to it's declared type.
						Semantic.reportError(filename,expr.lineNo,"Identifier type "+expr.value.type+" does not match with the declared type of let expression "+expr.typeid+".");
					}
				}

				st.enterScope();	
				st.insert(expr.name,expr.typeid);				//Add the variable to the scope table.
				
				check(expr.body,st);
				expr.type = expr.body.type;
				
				st.exitScope();
			}
		}
		else if(exp instanceof AST.typcase) {				//If the expression is an instance of typcase. Return type is the join of all the branches.
			AST.typcase expr = (AST.typcase)exp;

			check(expr.predicate,st);						//Predicate is checked although it is not used anywhere for setting the expression type.
			
			if(expr.branches.get(0).name.equals("self")) {					//If the branch has variable self.
				Semantic.reportError(filename,expr.branches.get(0).lineNo,"Cannot assign to self in branch statement.");	
			}
			List<String> types = new ArrayList<>();
			st.enterScope();
			st.insert(expr.branches.get(0).name,expr.branches.get(0).type);
			types.add(expr.branches.get(0).type);
			check(expr.branches.get(0).value,st);
			st.exitScope();

			expr.type = expr.branches.get(0).value.type;

			for(int i=1;i<expr.branches.size();i++) {
				AST.branch b = expr.branches.get(i);
				if(types.contains(b.type)) {
					Semantic.reportError(filename,b.lineNo,"Duplicate branch "+b.type+" in case statement.");
				}
				else {
					types.add(b.type);
				}

				if(b.name.equals("self")) {									//If the branch has variable self.
					Semantic.reportError(filename,b.lineNo,"Cannot assign to self in branch statement.");
				}
				st.enterScope();
				
				if(!(ig.nameToIndx.containsKey(b.type))) {
					Semantic.reportError(filename,b.lineNo,"Undefined type "+b.type+".");
				}
				st.insert(b.name,b.type);									//Insert the variable in the scope table.
				check(b.value,st);
				
				st.exitScope();
				expr.type = ig.join(ig.graph.get(ig.nameToIndx.get(expr.type)),ig.graph.get(ig.nameToIndx.get(b.value.type))).clas.name;
				
			}
		}
		else if(exp instanceof AST.dispatch) {				//If the expression is an instance of dispatch.
			AST.dispatch expr = (AST.dispatch)exp;

			check(expr.caller,st);							//check the caller expression and set it's typeid. Note that it can also be self as we have set self's typeid as current class.
			Node n = ig.graph.get(ig.nameToIndx.get(expr.caller.type));
			if(n == null) {									//If the caller's typeid is not defined,
				Semantic.reportError(filename,expr.lineNo,"Undefined Class of dispatch caller "+expr.caller.type+".");
			}

			if(!(n.methods.containsKey(expr.name))) {				//If there is no such method in the class.
				Semantic.reportError(filename,expr.lineNo,"Method "+ expr.name +" not defined.");
			}
			else {
				for(AST.expression e : expr.actuals) {						//Check the expression type for all the actuals.
					check(e,st);
				}
				
				AST.method m = n.methods.get(expr.name);
				if(m.formals.size()!=expr.actuals.size()) {					//If the number of formals defined is equal to the number of actuals passed.
					Semantic.reportError(filename,expr.lineNo,"Invalid number of arguments passed to method "+ expr.name +".");
				}
				else {
					for(int i=0;i<m.formals.size();i++) {					//Check if the actual's typeid confirms with that of the formal's.
						if(!(ig.confirmcheck(m.formals.get(i).typeid,expr.actuals.get(i).type))) {
							Semantic.reportError(filename,expr.lineNo,"Actual's type "+ expr.actuals.get(i).type +" does not confirm to that of method formal's type "+m.formals.get(i).typeid+".");
						}
					}
				}
				expr.type = m.typeid;										//Expression's type is the method's typeid.
			}
		}
		else if(exp instanceof AST.static_dispatch) {					//If the expression is an instance of static dispatch.
			AST.static_dispatch expr = (AST.static_dispatch)exp;

			check(expr.caller,st);										//check the caller expression and set it's typeid. Note that it can also be self as we have set self's typeid as current class.
			Node n = ig.graph.get(ig.nameToIndx.get(expr.typeid));

			if(n == null) {												//If the caller's typeid is not defined,
				Semantic.reportError(filename,expr.lineNo,"Undefined class "+expr.typeid+".");
			}

			if(!(ig.confirmcheck(expr.typeid,expr.caller.type))) {				//If the caller's tyepid confirms with the expression's typeid.
				Semantic.reportError(filename,expr.lineNo,"The type to the left of @ "+expr.caller.type+" does not confirm to the type specified to the right of @ "+expr.typeid+".");
			}
			
			if(!(n.methods.containsKey(expr.name))) {							//If there is no such method in the class.
				Semantic.reportError(filename,expr.lineNo,"Method "+ expr.name +" not defined.");
			}
			else {

				for(AST.expression e : expr.actuals) {						//Check the expression type for all the actuals.
					check(e,st);
				}

				AST.method m = n.methods.get(expr.name);
				if(m.formals.size()!=expr.actuals.size()) {					//If the number of formals defined is equal to the number of actuals passed.
					Semantic.reportError(filename,expr.lineNo,"Invalid number of arguments passed to method "+ expr.name +".");
				}
				else {
					for(int i=0;i<m.formals.size();i++) {
						if(!(ig.confirmcheck(m.formals.get(i).typeid,expr.actuals.get(i).type))) {					//Check if the actual's typeid confirms with that of the formal's.
							Semantic.reportError(filename,expr.lineNo,"Actual's type "+ expr.actuals.get(i).type +" does not confirm to that of method formal's type "+m.formals.get(i).typeid+".");
						}
					}
				}
				expr.type = m.typeid;										//Expression's type is the method's typeid.
			}
		}

	}
	
}