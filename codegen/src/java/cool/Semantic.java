package cool;

import java.util.*;
import cool.AST;
import cool.Node;
import cool.InheritanceGraph;
import cool.ScopeTable;
import cool.ExprChecks;

public class Semantic{
	private static boolean errorFlag = false;
	public static void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public boolean getErrorFlag(){
		return errorFlag;
	}

/*
	Don't change code above this line
*/

	public InheritanceGraph ig;
	public ScopeTable<String> st;
	public ExprChecks ec;

	public Semantic(AST.program program){
		//Write Semantic analyzer code here

		ig = new InheritanceGraph(program.classes.get(0).filename);											//Construct the inheritance graph.
		ig.buildGraph(program);																				//Build the graph.																			//If there are no errors so far.
		
		if(!getErrorFlag()) {																				//if there are no errors so far. 	
			ig.checkCycles();																				//Check if there are cycles in the inheritance graph.
			if(!getErrorFlag()) {																			//if there are no errors so far.
				ig.addAttrsMethods();																		//Add all the attributes and method for a particular class in its corresponding node.
				if(!getErrorFlag()) {																		//if there are no errors so far.	
					ig.checkMain();																			//Check if class Main and method main are defined.
					if(!getErrorFlag()) {																	//if there are no errors so far.
						st = new ScopeTable<>();															//Construct a Scope Table.
						ec = new ExprChecks(ig,program.classes.get(0).filename);							//Constructor for expression checks.
						checkprogram(program);																//Checks the entire program.
					}
				}
			}
		}
	}

	public void checkprogram(AST.program p) {

		for(AST.class_ cls : p.classes) {						//Go through all the classes in the program and check them.
			st.enterScope();
			ec.check(cls,st);
			st.exitScope();
		}
	}
}
