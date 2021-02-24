package cool;

import java.util.*;
import cool.AST;


public class Node {

	public AST.class_ clas;												//The class present in the node.
	public List<Node> children;											//The children nodes of the current class node.
	public Node parent;													//The parent node of the current class node.
	public HashMap<String,AST.attr> attributes;							//All the attributes of the current class node including that of it's parent.
	public HashMap<String,AST.method> methods;							//All the methods of the current class node including that of it's parent.

	public Node(AST.class_ cls) {										//The constructor for a class node.
		clas = cls;
		children = new ArrayList<>();
		attributes = new HashMap<>();
		methods = new HashMap<>();
		parent = null;
	}

	public void addChild(Node child) {									//Adds a child node to the children list.
		children.add(child);
	}

	public void addAttrMethod() {										//Adds the current class' attributes and methods. Also checks for redefinitions.
		for(AST.feature ftr : clas.features) {							//Goes through all features of the class.
			if(ftr instanceof AST.attr) {								//If the feature is an attribute.
				if(attributes.containsKey(((AST.attr)ftr).name)) {
					//Attribute already defined.
					Semantic.reportError(clas.filename,((AST.attr)ftr).lineNo,"Attribute "+((AST.attr)ftr).name+" has already been defined.");
				}
				else {
					//Add it to the attributes Hashmap.
					attributes.put(((AST.attr)ftr).name,(AST.attr)ftr);
				}
			}
			else {														//If the feature is an method.
				if(methods.containsKey(((AST.method)ftr).name)) {
					//Method already defined.
					Semantic.reportError(clas.filename,((AST.method)ftr).lineNo,"Method "+((AST.method)ftr).name+" has already been defined.");
				}
				else {
					//Add it to the methods Hashmap.
					methods.put(((AST.method)ftr).name,(AST.method)ftr);
				}	
			}
		}

	}

	public void addAttrMethodParent() {												//Adds the parent class' attributes and methods. Also checks for redefinitions.

		for(Map.Entry<String,AST.attr> it : parent.attributes.entrySet()) {			//Goes through the parent node's attributes Hashmap.
			if(attributes.containsKey(it.getKey())) {
				//Attribute of the parent class node is also present in the attributes Hashmap of the class node.
				Semantic.reportError(clas.filename,it.getValue().lineNo,"Attribute "+it.getValue().name+" has already been defined in parent class "+parent.clas.name+".");
			}
			else {
				//Add it to the attributes Hashmap.
				attributes.put(it.getKey(),it.getValue());
			}
		}

		for(Map.Entry<String,AST.method> it : parent.methods.entrySet()) {			//Goes through the parent node's methods Hashmap.

			boolean flag = false;
			
			if(methods.containsKey(it.getKey())) {
				//Method of the parent class node is also present in the methods Hashmap of the class node.
				AST.method parentmethod = it.getValue();
				AST.method childmethod = methods.get(it.getKey());
				if(parentmethod.formals.size() != childmethod.formals.size()) {
					//Unequal number of formal parameters.
					Semantic.reportError(clas.filename,it.getValue().lineNo,"Redefined Method "+it.getValue().name+" has different parameteres than that defined in parent class "+parent.clas.name+".");
					flag = true;
				}
				else if(!parentmethod.typeid.equals(childmethod.typeid)) {
					//Different return types for the methods defined.
					Semantic.reportError(clas.filename,it.getValue().lineNo,"Redefined Method "+it.getValue().name+" has different return types than that definied in parent class "+parent.clas.name+".");
					flag = true;
				}
				else {
					for(int i=0;i<parentmethod.formals.size();i++) {
						if(!parentmethod.formals.get(i).typeid.equals(childmethod.formals.get(i).typeid)) {
							//Different type of formal parameters.
							Semantic.reportError(clas.filename,it.getValue().lineNo,"Redefined Method "+it.getValue().name+" has different parameter typeid for "+ childmethod.formals.get(i).name +" than that definied in parent class "+parent.clas.name+".");
							flag = true;
						}
					}
				}
			}
			if(flag == false) {
				//Add it to the attributes Hashmap.
				methods.put(it.getKey(),it.getValue());
			}
		}

	}


}