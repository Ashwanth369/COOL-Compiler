package cool;

import java.util.*;
import cool.Node;
import cool.AST;


public class InheritanceGraph {

	public String filename;														//Filename of the program.
	public int numofclasses;													//Number of classes in the program.
	public HashMap<String,Integer> nameToIndx;									//Hashmap of a class name to it's index in the graph.
	public List<Node> graph;													//List of all class nodes.
	

	public InheritanceGraph(String fname) {										//Constructor for inheritance graph. Adds the basic classes such as Object,IO,String,Int,Bool to the graph.

		filename = fname;
		numofclasses = 0;
		nameToIndx = new HashMap<>();
		graph = new ArrayList<>();

		List<AST.feature> obj = new ArrayList<>();									//Features list for class Object.
		obj.add(new AST.method("abort",new ArrayList<>(),"Object",null,0));			//abort method of class Object.
		obj.add(new AST.method("type_name",new ArrayList<>(),"String",null,0));		//type_name method of class Object.
		obj.add(new AST.method("copy",new ArrayList<>(),"Object",null,0));			//copy method of class Object.
		Node obj_ = new Node(new AST.class_("Object",filename,null,obj,0));			//Create a node for the class object.
		obj_.addAttrMethod();														//Adds the attributes and methods of the class Object to it's node.
		graph.add(obj_);															//Add the node to the graph.
		nameToIndx.put("Object",numofclasses++);									//Add the class name and it's index to the Hashmap nameToIndx.


		List<AST.feature> io = new ArrayList<>();																			//Features list for class IO.
		io.add(new AST.method("out_string",new ArrayList<>(Arrays.asList(new AST.formal("x","String",0))),"IO",null,0));	//out_string method of class IO.
		io.add(new AST.method("in_string",new ArrayList<>(),"String",null,0));												//in_string method of class IO.
		io.add(new AST.method("out_int",new ArrayList<>(Arrays.asList(new AST.formal("x","Int",0))),"IO",null,0));			//out_int method of class IO.
		io.add(new AST.method("in_int",new ArrayList<>(),"Int",null,0));													//in_int method of class IO.
		Node io_ = new Node(new AST.class_("IO",filename,"Object",io,0));													//Create a node for the class IO.
		io_.parent = obj_;																									//Parent node of IO is Object.
		graph.add(io_);																										//Add the node to the graph.
		graph.get(0).addChild(io_);																							//Add IO to the List children of node Object.
		nameToIndx.put("IO",numofclasses++);																				//Add the class name and it's index to the HashMap nameToIndx.
		

		List<AST.feature> str = new ArrayList<>();																										//Features list for class String.
		str.add(new AST.method("length",new ArrayList<>(),"Int",null,0));																				//length method of class String.
		str.add(new AST.method("concat",new ArrayList<>(Arrays.asList(new AST.formal("x","String",0))),"Int",null,0));									//concat method of class String.
		str.add(new AST.method("substr",new ArrayList<>(Arrays.asList(new AST.formal("x","Int",0),new AST.formal("y","Int",0))),"Int",null,0));			//substr method of class String.
		Node str_ = new Node(new AST.class_("String",filename,"Object",str,0));																			//Create a node for class String.
		str_.parent = obj_;																																//Parent node of String is Object.
		graph.add(str_);																																//Add the node to the graph.
		graph.get(0).addChild(str_);																													//Add String to the List children of node Object.
		nameToIndx.put("String",numofclasses++);																										//Add the class name and it's index to the HashMap nameToIndx.
		

		Node int_ = new Node(new AST.class_("Int",filename,"Object",new ArrayList<AST.feature>(),0));				//Create a node for class Int.
		int_.parent = obj_;																							//Parent node of Int is Object.
		graph.add(int_);																							//Add the node to the graph.
		graph.get(0).addChild(int_);																				//Add Int to the List children of node Object.
		nameToIndx.put("Int",numofclasses++);																		//Add the class name and it's index to the HashMap nameToIndx.


		Node bool_ = new Node(new AST.class_("Bool",filename,"Object",new ArrayList<AST.feature>(),0));				//Create a node for class Bool.
		bool_.parent = obj_;																						//Parent node of Bool is Object.
		graph.add(bool_);																							//Add the node to the graph.
		graph.get(0).addChild(bool_);																				//Add Bool to the List children of node Object.
		nameToIndx.put("Bool",numofclasses++);																		//Add the class name and it's index to the HashMap nameToIndx.

	}

	
	public void buildGraph(AST.program p) {								//Adds all the classes to the graph. Also checks for errors.

		boolean error = false;

		for(AST.class_ cls : p.classes) {							//Goes through all the classes in the program.
			if(cls.name.equals("Object") || cls.name.equals("IO") || cls.name.equals("String") || cls.name.equals("Int") || cls.name.equals("Bool")) {
				//Classes Object,IO,String,Int,Bool are basic classes. Cannot be redefined.
				Semantic.reportError(filename,cls.lineNo,"Basic class "+cls.name+" cannot be redefined.");
				error = true;
			}
			else if(nameToIndx.containsKey(cls.name)) {
				//Class already defined.
				Semantic.reportError(filename,cls.lineNo,"Class "+cls.name+" has already been defined.");
				error = true;
			}
			else {
				//Creates a node for the class and adds it to the graph.Add the class name and it's index to the HashMap nameToIndx.
				Node n = new Node(cls);
				graph.add(n);
				nameToIndx.put(cls.name,numofclasses++);
			}
		}

		//Following for loop is for setting the parent and children of every node correctly. This cannot be done above because the parent class can be defined later in the program.
		for(Node n : graph) {			//Goes through all the nodes in the graph.
			if(!(n.clas.name.equals("Object") || n.clas.name.equals("IO") || n.clas.name.equals("String") || n.clas.name.equals("Int") || n.clas.name.equals("Bool"))){
				//If the node is not a basic class.
				if(n.clas.parent == null) {
					//Parent of the class is not mentioned. Default parent is the class Object.
					n.parent = graph.get(0);
					graph.get(0).addChild(n);
				}
				else if(n.clas.parent.equals("String") || n.clas.parent.equals("Int") || n.clas.parent.equals("Bool")) {
					//Cannot inherit from String,Int,Bool classes.
					Semantic.reportError(filename,n.clas.lineNo,"Class "+n.clas.name+" cannot inherit from Basic class "+n.clas.parent+".");
					error = true;
				}
				else if(nameToIndx.containsKey(n.clas.parent)) {
					//Parent class has been defined. Update the parent in the class node and add the class node to the list of children nodes in the parent node.
					n.parent = graph.get(nameToIndx.get(n.clas.parent));					
					graph.get(nameToIndx.get(n.clas.parent)).addChild(n);
				}
				else{
					//Parent class has not been defined.
					Semantic.reportError(filename,n.clas.lineNo,"Parent class "+n.clas.parent+" of class "+n.clas.name+" has not been defined.");
					error = true;
				}
			}
		}

		if(error) {
			return;
		}

	}

	//The following function is for adding all the feasible attributes and methods to a node. By all, meaning, adding the parent's attributes and methods as well.
	public void addAttrsMethods() {
		Queue<Integer> bfs = new LinkedList<Integer>();	
		bfs.offer(0);
		while(bfs.isEmpty()==false){									//Traverses in BFS style and adds all the attributes and methods to the current class. This makes
			int indx = bfs.poll();										//sure that all the attributes and methods of the ancestors are added to the current class as well.
			Node n = graph.get(indx);
			for(Node nn : n.children) {
				nn.addAttrMethod();
				nn.addAttrMethodParent();
				bfs.offer(nameToIndx.get(nn.clas.name));
			}
		}
	}


	//Check for cycles in the inheritance graph.
	public void checkCycles() {												//Checks for cycles in the inheritance graph.

		boolean error = false;
		Boolean[] visited = new Boolean[numofclasses];						//Keeps a note on which nodes have been visited.
		Arrays.fill(visited,Boolean.FALSE);									//Initialize this to false.
		
		Queue<Integer> bfs = new LinkedList<Integer>();						//Queue to maintain the classes.
		bfs.offer(0);

		while(bfs.isEmpty()==false){
			
			int indx = bfs.poll();
			if(visited[indx]==false){										//If the class has not been visited.
				visited[indx]=true;
				Node n = graph.get(indx);	
				for(Node nn : n.children) {	
					bfs.offer(nameToIndx.get(nn.clas.name));				//Add the children of the current class to the Queue.
				}

				if(bfs.isEmpty()){											//If there are no children and the queue has become empty, check if there are any non visited nodes.
					for(int i=0;i<numofclasses;i++){
						if(visited[i]==false){
							bfs.offer(i);
							break;
						}
					}	
				}
			}
			else{
				//Reached a node that has already been visited
				Semantic.reportError(filename,graph.get(indx).clas.lineNo ,"Class "+graph.get(indx).clas.name+" or it's relation class is a part of inheritance cycle.");
				error = true;
				if(bfs.isEmpty()){											//Check if there are any non visited nodes and continues to find if there are any other errors.
					for(int i=0;i<numofclasses;i++){
						if(visited[i]==false){
							bfs.offer(i);
							break;
						}
					}
				}
			}

		}

		if(error) {
			return;
		}

	}


	//Check if Class Main has been defined. If yes, check if it has a method main.
	public void checkMain() {

		Node main = graph.get(nameToIndx.get("Main"));
		
		if(main == null) {
			Semantic.reportError(filename,1,"Class Main has not been defined.");
			return;
		}
		else if(!(main.methods.containsKey("main"))) {
			Semantic.reportError(filename,1,"main method has not been defined in class Main");
			return;
		}
		
	}

	//The following checks if typeid2 confirms to typeid1, i.e, if typeid1 is an ancestor of typeid2.
	public boolean confirmcheck(String typeid1, String typeid2) {

		if(typeid1.equals(typeid2) || typeid1.equals("Object")) {			//If there are no cycles in the graph then the class Object is a ancestor for all the classes.
			return true;
		}
		else if(typeid1.equals("String") || typeid1.equals("Int") || typeid1.equals("Bool") || typeid2.equals("String") || typeid2.equals("Int") || typeid2.equals("Bool")) {
			//Since typeid1 is not object then if typeid2 is either String,Int,Bool then they do not confirm. If typeid1 is either String,Int,Bool then no other class can inherit
			//from it and hence they do not confirm.
			return false;
		}
		Node n1 = graph.get(nameToIndx.get(typeid1));
		Node n2 = graph.get(nameToIndx.get(typeid2));
		if(n1 == null || n2 == null) {
			return false;
		}
		while(n2.parent != null) {				//We traverse up to the parent class of typeid2 and do this till we reach the typeid1. If not they do not confirm.
			n2 = n2.parent;
			if(n1.equals(n2)) {
				return true;
			}
		}
		return false;

	}

	//The following function returns the least common ancestor for the two nodes.
	public Node join(Node n1,Node n2) {

		if(n1.equals(n2)) {
			return n1;
		}
		else {
			Node join = null;
			Boolean[] visited = new Boolean[numofclasses];					//The visited array keeps a note of all the ancestors of node1.
			Arrays.fill(visited,Boolean.FALSE);
			while(n1!=null) {
				visited[nameToIndx.get(n1.clas.name)] = true;
				n1 = n1.parent;
			}
			while(join==null && n2!=null) {
				if(visited[nameToIndx.get(n2.clas.name)]) {					//Now while traversing for node2 if one its ancestor is in the visited array then that is the least common ancestor.
					join = n2;
				}
				n2 = n2.parent;
			}
			return join;
		}

	}
}