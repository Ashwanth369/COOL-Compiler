package cool;

import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.*;


public class Visitor extends ExprPrinter {

	ScopeTable<String> st;


	// class constructor
	Visitor(PrintWriter o, String fname) {
		filename = fname;
		out = o;
		methodParams = new HashSet<>();
		classSize = new HashMap<>();
		st = new ScopeTable<>(); 
		labelcount = new HashMap<>();
		functions = new HashSet<>();
		ig = new InheritanceGraph(fname);
		numOfRegisters = 0;
	}

	// generating IR hierarchically from program
	public void process(AST.program p) {
		
		// adding default classes
		classSize.put("Object",0);
		classSize.put("IO",0);
		classSize.put("String",8);
		classSize.put("Int",4);
		classSize.put("Bool",1);

		// adding default functions
		functions.add("ZN6Object9type_name");
        functions.add("ZN6Object5abort");
        functions.add("ZN2IO10out_string");
        functions.add("ZN2IO6in_int");
        functions.add("ZN2IO9in_string");
        functions.add("ZN6String6concat");
        functions.add("ZN6String6length");
        functions.add("ZN2IO7out_int");
        functions.add("ZN6String6substr");

        // default string constants
		stringRegister.put("Object","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("IO","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("String","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("Int","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("Bool","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		
		// building the inheritance graph
		ig.buildGraph(p);
		ig.addAttrsMethods();
		for(AST.class_ cls : p.classes) {
			stringRegister.put(cls.name,"@.str."+stringConstantsCounter);
			stringConstantsCounter++;
		}


		// declaring string constants globally 
		String output1 = "; String constant declarations";
		out.println(output1);
		stringRegister.put("","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("\n","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("%s","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("%d","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("%d\n","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("%1024[^\n]","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("Divide by 0 exception at line ","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("Dispatch to void at line ","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		stringRegister.put("Abort called from class ","@.str."+stringConstantsCounter);
		stringConstantsCounter++;
		for(Map.Entry<String,String> it : stringRegister.entrySet()) {
			String output2 = it.getValue()+" = private unnamed_addr constant ["+Integer.toString(it.getKey().length()+1)+" x i8] c\""
							+ it.getKey()+"\\00\",align 1";
			out.println(output2);
		}

		// create structs for all classes
		String output3 = "; Struct declarations";
		out.println(output3);
		String output4 = "%class.Object = type {i8*}";
		out.println(output4);
		classtoattrtoindex.put("Object",new HashMap<>());
		List<Node> c = ig.graph.get(0).children;
		for(Node n : c) {
			// DFS of inheritance graph to declare structs
			classStructs(n);
		}
		// print the IR for all classes by doing DFS traversal of all classes
		programCheck(ig.graph.get(0));
		// constructors of all classes
		classConstructors(ig.graph.get(0));
		// Default methods like IO methods and printing error messages
		addDefaultMethods();

	}

	public void classStructs(Node n) {
		AST.class_ cls = n.clas;
		if(cls.name.equals("String") || cls.name.equals("Int") || cls.name.equals("Bool")) {
			return;
		}

		int size = 8;
		String output = "%class."+cls.name;
		size += classSize.get(cls.parent);
		output += " = type { %class."+cls.parent;
	
		Map<String,String> classattrtoindex = new HashMap<>();
		Map<String,String> parentattrtoindex = classtoattrtoindex.get(cls.parent);

		for(Map.Entry<String,String> it : parentattrtoindex.entrySet()) {
			classattrtoindex.put(it.getKey()," i32 0,"+it.getValue());
		}
		int index = 0;
		for(Map.Entry<String,AST.attr> it : n.attributes.entrySet()) {
			index++;
			AST.attr a = it.getValue();
			
			if(a.typeid.equals("Int")) {
				size += 4;
				output += ", i32";
			}
			else if(a.typeid.equals("Bool")) {
				size += 1;
				output += ", i8";
			}
			else if(a.typeid.equals("String")){
				size += 8;
				output += ", i8*";
			}
			else if(a.typeid.equals("i1")){
				size += 8;
				output += ", i1";
			}
			else if(a.typeid.equals("i64")){
				size += 8;
				output += ", i64";
			}
			else {
				size += 8;
				output += ", %class."+a.typeid + "*";
			}

			classattrtoindex.put(a.name," i32 0, i32 "+Integer.toString(index));
		}

		for(Map.Entry<String,AST.method> mthds : n.methods.entrySet()) {
			AST.method mthd = mthds.getValue();
			// mangled names
			functions.add("ZN"+Integer.toString(cls.name.length())+cls.name + Integer.toString(mthd.name.length()) + mthd.name);
		}

		output += " }";
		out.println(output);
		classtoattrtoindex.put(cls.name,classattrtoindex);
		classSize.put(cls.name,size);

		for(Node nn : n.children) {
			classStructs(nn);
		}

	}

	public void programCheck(Node n) {
		// DFS of all non trivial classes and printing the IR
		st.enterScope();
		if(!(n.clas.name.equals("Object") || n.clas.name.equals("IO") || n.clas.name.equals("String") ||
			n.clas.name.equals("Int") || n.clas.name.equals("Bool"))) {
			process(n.clas);
		}
		for(Node nn : n.children) {
			programCheck(nn);
		}
		st.exitScope();
	}

	public void classConstructors(Node n) {
		if(!(n.clas.name.equals("String") ||
			n.clas.name.equals("Int") || n.clas.name.equals("Bool"))) {

			st.enterScope();
			AST.class_ cls = n.clas;
			numOfRegisters = 0;
			String output1 = "\n; Constructor of class "+cls.name;
			out.println(output1);
			currentClass = cls.name;
			String output2 = "define void @ZN"+Integer.toString(cls.name.length())+cls.name+Integer.toString(cls.name.length())+cls.name + "( %class."+cls.name
							+ "* %this) {\nentry:\n";

			if(cls.parent != null) {
				if(cls.name.equals("i8*")) {
					output2 += "%"+Integer.toString(numOfRegisters)+" = bitcast i8* %this to ";
					numOfRegisters++;
				}
				else if(cls.name.equals("i32")) {
					output2 += "%"+Integer.toString(numOfRegisters)+" = bitcast i32 %this to ";
					numOfRegisters++;
				}
				else if(cls.name.equals("i8")) {
					output2 += "%"+Integer.toString(numOfRegisters)+" = bitcast i8 %this to ";
					numOfRegisters++;
				}
				else if(cls.name.equals("i64")) {
					output2 += "%"+Integer.toString(numOfRegisters)+" = bitcast i64 %this to ";
					numOfRegisters++;
				}
				else if(cls.name.equals("i1")) {
					output2 += "%"+Integer.toString(numOfRegisters)+" = bitcast i1 %this to ";
					numOfRegisters++;
				}
				else {
					output2 += "%"+Integer.toString(numOfRegisters)+" = bitcast %class."+cls.name + "*" +" %this to ";
					numOfRegisters++;
				}

				if(cls.parent.equals("i8*")) {
					output2 += "i8*";
				}
				else if(cls.parent.equals("i32")) {
					output2 += "i32";
				}
				else if(cls.parent.equals("i8")) {
					output2 += "i8";
				}
				else if(cls.parent.equals("i64")) {
					output2 += "i64";
				}
				else if(cls.parent.equals("i1")) {
					output2 += "i1";
				}
				else {
					output2 += "%class."+cls.parent + "*";
				}
				out.println(output2);
				String output3 = "call void @ZN"+Integer.toString(cls.parent.length())+cls.parent+ Integer.toString(cls.parent.length())+cls.parent+"(%class."+cls.parent+"* %"+Integer.toString(numOfRegisters-1)+")";
				out.println(output3);
			}
			else {
				out.println(output2);
			}

			for(Map.Entry<String,AST.attr> it : n.attributes.entrySet()) {
				st.insert(it.getValue().name,it.getValue().typeid);
			}
			
			for(Map.Entry<String,AST.attr> it : n.attributes.entrySet()) {
				process(it.getValue());
			}

			out.println("\nret void\n}");

			List<Node> c = n.children;
			Collections.reverse(c);
			for(Node nn : c) {
				classConstructors(nn);
			}
			st.exitScope();
		}

	}

	public void addDefaultMethods() {

		// declaring printf, scanf and other default string methods
		out.println("\n; C malloc declaration\n");
		out.println("declare noalias i8* @malloc(i64)");


		out.println("\n; C exit declaration\n");
		out.println("declare void @exit(i32)");

		out.println("\n; C printf declaration\n");
		out.println("declare i32 @printf(i8*,...)");

		out.println("\n; C scanf declaration\n");
		out.println("declare i32 @scanf(i8*,...)");

		out.println("\n; C strlen declaration\n");
		out.println("declare i64 @strlen(i8*)");

		out.println("\n; C strcpy declaration\n");
		out.println("declare i8* @strcpy(i8*, i8*)");

		out.println("\n; C strcat declaration\n");
		out.println("declare i8* @strcat(i8*, i8*)");

		out.println("\n; C strncpy declaration\n");
		out.println("declare i8* @strncpy(i8*, i8*, i64)");
		

		// abort method for class Object
		numOfRegisters = 0;
		out.println("\n; Class: Object, Method: abort");
		out.println("define %class.Object* @ZN6Object5abort(%class.Object* %this) {");
		out.println("entry:");

		String temp1 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp1+" = getelementptr inbounds %class.Object, %class.Object* %this, i32 0, i32 0");
		numOfRegisters++;

		String temp2 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp2+" = load i8*, i8** %"+Integer.toString(numOfRegisters-1)+", align 8");
		numOfRegisters++;

		String temp3 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp3+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%s")+", i32 0, i32 0");

		String temp4 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp4+" = getelementptr inbounds [25 x i8], [25 x i8]*"+stringRegister.get("Abort called from class ")+", i32 0, i32 0");

		out.println("\t%"+Integer.toString(numOfRegisters)+" = call i32 (i8*,...) @printf(i8* "+temp3+", i8* "+temp4+")");
		numOfRegisters++;

		out.println("\t%"+Integer.toString(numOfRegisters)+" = call i32 (i8*,...) @printf(i8* "+temp3+", i8* "+temp2+")");
		numOfRegisters++;

		temp4 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp4+" = getelementptr inbounds [2 x i8], [2 x i8]*"+stringRegister.get("\n")+", i32 0, i32 0");

		out.println("\t%"+Integer.toString(numOfRegisters)+" = call i32 (i8*,...) @printf(i8* "+temp3+", i8* "+temp4+")");
		numOfRegisters++;

		out.println("\tcall void @exit(i32 0)");

		String temp5 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp5+" = call noalias i8* @malloc(i64 0)");
		numOfRegisters++;

		String temp6 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp6+" = bitcast i8* "+temp5+" to %class.Object*");
		numOfRegisters++;

		out.println("\tcall void @ZN6Object6Object(%class.Object* "+temp6+")");
		out.println("\tret %class.Object* "+temp6);
		out.println("}");

		numOfRegisters = 0;
		out.println("\n; Class: Object, Method: type_name");
		out.println("define i8* @ZN6Object9type_name(%class.Object* %this) {");
		out.println("entry:");

		temp1 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp1+" = getelementptr inbounds %class.Object, %class.Object* %this, i32 0, i32 0");
		numOfRegisters++;

		temp2 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp2+" = load i8*, i8** %"+Integer.toString(numOfRegisters-1)+", align 8");
		numOfRegisters++;

		out.println("\tret i8* "+temp2);
		out.println("}");


		//IO METHODS
		numOfRegisters = 0;
		out.println("\n; Class: IO, Method: out_string");
		out.println("define %class.IO* @ZN2IO10out_string(%class.IO* %this, i8* %s) {");
		out.println("entry:");

		String temp7 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp7+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%s")+", i32 0, i32 0");

		out.println("\t%call = call i32 (i8*,...) @printf(i8* "+temp7+", i8* %s)");

		String temp8 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp8+" = call noalias i8* @malloc(i64 0)");
		numOfRegisters++;

		String temp9 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp9+" = bitcast i8* "+temp8+" to %class.IO*");
		numOfRegisters++;

		out.println("\tcall void @ZN2IO2IO(%class.IO* "+temp9+ ")");
		out.println("\tret %class.IO* "+temp9);
		out.println("}");

		numOfRegisters = 0;
		out.println("\n; Class: IO, Method: out_int");
		out.println("define %class.IO* @ZN2IO7out_int(%class.IO* %this, i32 %d) {");
		out.println("entry:");

		temp7 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp7+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%d")+", i32 0, i32 0");
		out.println("\t%call = call i32 (i8*,...) @printf(i8* "+temp7+", i32 %d)");
		temp8 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp8+" = call noalias i8* @malloc(i64 0)");
		numOfRegisters++;

		temp9 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp9+" = bitcast i8* "+temp8+" to %class.IO*");
		numOfRegisters++;

		out.println("\tcall void @ZN2IO2IO(%class.IO* "+temp9+ ")");
		out.println("\tret %class.IO* "+temp9);
		out.println("}");


		numOfRegisters = 0;
		out.println("\n; Class: IO, Method: in_string");
		out.println("define i8* @ZN2IO9in_string(%class.IO* %this) {");
		out.println("entry:");

		String temp10 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp10+" = alloca i8*, align 8");
		numOfRegisters++;

		String temp11 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp11+" = getelementptr inbounds [10 x i8], [10 x i8]*"+stringRegister.get("%1024[^\n]")+", i32 0, i32 0");

		String temp12 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp12+" = load i8*, i8** "+temp10+", align 8");
		
		out.println("\t%call = call i32 (i8*,...) @scanf(i8* "+temp11+", i8* "+temp12+")");

		String temp13 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp13+" = load i8*, i8** "+temp10+", align 8");

		out.println("\tret i8* "+temp12);
		out.println("}");

	
		numOfRegisters = 0;
		out.println("\n; Class: IO, Method: in_int");
		out.println("define i32 @ZN2IO6in_int(%class.IO* %this) {");
		out.println("entry:");

		temp10 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp10+" = alloca i32, align 8");
		numOfRegisters++;

		if(!stringRegister.containsKey("%d")) {
			temp11 = null;
		}
		else {
			temp11 = "%"+Integer.toString(numOfRegisters);
			numOfRegisters++;
			out.println("\t"+temp11+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%d")+", i32 0, i32 0");
		}
		out.println("\t%call = call i32 (i8*,...) @scanf(i8* "+temp11+", i32* "+temp10+")");

		temp12 = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		out.println("\t"+temp12+" = load i32, i32* "+temp10+", align 8");

		out.println("\tret i32 "+temp12);
		out.println("}");

		// Default string methods

		numOfRegisters = 0;
		out.println("\n; Class: String, Method: concat");
		out.println("define i8* @ZN6String6concat(i8* %s1,i8* %s2) {");
		out.println("entry:");
		
		temp10 = "%"+numOfRegisters;
		out.println(temp10+" = call i64 @strlen(i8* %s1)");
		numOfRegisters++;
		temp11 = "%"+numOfRegisters;
		out.println(temp11+" = call i64 @strlen(i8* %s2)");
		numOfRegisters++;

		temp12 = "%"+numOfRegisters;
		out.println(temp12+" = add nsw i64 "+temp10+", "+temp11);
		numOfRegisters++;
		
		String temp13_ = "%"+Integer.toString(numOfRegisters);
		out.println(temp13_+" = add nsw i64 "+temp12+", 1");
		numOfRegisters++;

		String temp14 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp14+" = call noalias i8* @malloc(i64 "+temp13+")");
		numOfRegisters++;

		String temp15 = "%"+Integer.toString(numOfRegisters);
		out.println(temp15+" = call i8* @strcpy(i8* "+temp14+", i8* %s1)");
		numOfRegisters++;

		String temp16 = "%"+Integer.toString(numOfRegisters);
		out.println(temp16+" = call i8* @strcat(i8* "+temp14+", i8* %s2)");
		numOfRegisters++;

		out.println("\tret i8* "+temp14);
		out.println("}");


		numOfRegisters = 0;
		out.println("\n; Class: String, Method: substr");
		out.println("define i8* @ZN6String6substr(i8* %s1,i32 %index,i32 %len) {");
		out.println("entry:");

		temp10 = "%"+Integer.toString(numOfRegisters);
		out.println(temp10+" = zext i32 %len to i64");
		numOfRegisters++;

		temp11 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp11+" = call noalias i8* @malloc(i64 "+temp10+")");
		numOfRegisters++;

		temp12 = "%"+Integer.toString(numOfRegisters);
		out.println(temp12+" = getelementptr inbounds i8, i8* %s1, i32 %index");
		numOfRegisters++;

		temp13 = "%"+Integer.toString(numOfRegisters);
		out.println(temp13+" = call i8* @strncpy(i8* "+temp11+", i8* "+temp12+", i64 "+temp10+")");
		numOfRegisters++;

		out.println("\tret i8* "+temp11);
		out.println("}");


		// function to print divide by zero error
		numOfRegisters = 0;
		out.println("define void @print_div_by_zer_err_msg(i32 %line) {");
		out.println("entry:");

		temp10 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp10+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%s")+", i32 0, i32 0");
		numOfRegisters++;

		temp11 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp11+" = getelementptr inbounds [31 x i8], [31 x i8]*"+stringRegister.get("Divide by 0 exception at line ")+",i32 0, i32 0");
		numOfRegisters++;

		temp12 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp12+" = call i32 (i8*, ...) @printf(i8* "+temp10+", i8* "+temp11+")");
		numOfRegisters++;

		temp13 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp13+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%d")+", i32 0, i32 0");
		numOfRegisters++;

		temp14 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp14+" = call i32 (i8*, ...) @printf(i8* "+temp13+", i32 %line)");
		numOfRegisters++;

		temp15 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp15+" = getelementptr inbounds [2 x i8], [2 x i8]*"+stringRegister.get("\n")+", i32 0, i32 0");
		numOfRegisters++;

		temp16 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp16+" = call i32 (i8*, ...) @printf(i8* "+temp10+", i8* "+temp15+")");
		numOfRegisters++;

		out.println("\tret void");
		out.println("}");

		// function to print dispatch on void error
		numOfRegisters = 0;
		out.println("define void @print_dispatch_on_void_error(i32 %line) {");
		out.println("entry:");

		temp10 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp10+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%s")+", i32 0, i32 0");
		numOfRegisters++;

		temp11 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp11+" = getelementptr inbounds [26 x i8], [26 x i8]*"+stringRegister.get("Dispatch to void at line ")+",i32 0, i32 0");
		numOfRegisters++;

		temp12 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp12+" = call i32 (i8*, ...) @printf(i8* "+temp10+", i8* "+temp11+")");
		numOfRegisters++;

		temp13 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp13+" = getelementptr inbounds [3 x i8], [3 x i8]*"+stringRegister.get("%d")+", i32 0, i32 0");
		numOfRegisters++;

		temp14 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp14+" = call i32 (i8*, ...) @printf(i8* "+temp13+", i32 %line)");
		numOfRegisters++;

		temp15 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp15+" = getelementptr inbounds [2 x i8], [2 x i8]*"+stringRegister.get("\n")+", i32 0, i32 0");
		numOfRegisters++;

		temp16 = "%"+Integer.toString(numOfRegisters);
		out.println("\t"+temp16+" = call i32 (i8*, ...) @printf(i8* "+temp10+", i8* "+temp15+")");
		numOfRegisters++;

		out.println("\tret void");
		out.println("}");

		// main method of class Main IR
		numOfRegisters = 0;
		out.println("\n; C main() function");
		out.println("define i32 @main() {");
		out.println("entry:");
		out.println("\t%main = alloca %class.Main, align 8");
		out.println("\tcall void @ZN4Main4Main(%class.Main* %main)");
		if(mainReturnType == "Int") {
            out.println("	"+"%retval = call i32 @"+ "ZN4Main4main" + "(%class.Main* %main)");
            out.println("	"+"ret i32 %retval");
        } else {
            out.println("	"+"%dummyretval = call "+getType(mainReturnType)+" @"+"ZN4Main4main"+"(%class.Main* %main)");
            out.println("	"+"ret i32 0");
        }
		out.println("}");

	}

	// IR for class
	public void process(AST.class_ cl) {
		currentClass = cl.name;
		for(AST.feature f : cl.features) {
            if(f instanceof AST.attr) {
                AST.attr at = ((AST.attr) f);
                st.insert(at.name, at.typeid);
            }
        }
        for(AST.feature f : cl.features) {
            if(f instanceof AST.method) {
                process((AST.method) f);
            }
        }

	}

	// IR for method
	public void process(AST.method mthd) {
		st.enterScope();
		numOfRegisters = 0;
		if(currentClass.equals("Main") && mthd.name.equals("main")) {
			mainReturnType = mthd.typeid;
		}
		methodParams.clear();
		String output = "\n; Class: "+ currentClass+", Method: " + mthd.name + "\n" + "define " + getType(mthd.typeid) + " @";
		// Mangled Name
		output = output +"ZN" + Integer.toString(currentClass.length()) + currentClass + Integer.toString(mthd.name.length()) + mthd.name + "(" + "%class." + currentClass + "* %this" ;

		for(AST.formal fm: mthd.formals) {
            output = output + ",";
            output = output + process(fm);
		}
		
		output += ") {" + "\n";
		output += "entry:\n";
		out.println(output);

		for(AST.formal fm: mthd.formals) {
			out.println("	" + "%" + fm.name + ".addr = alloca " + getType(fm.typeid) + " , align 8");
        	printStoreInstruction("%"+fm.name, "%"+fm.name+".addr", getType(fm.typeid));
		}

		String register = process(mthd.body,st);

		if(!mthd.typeid.equals(mthd.body.type) && !mthd.body.type.equals("_no_type")) {
			String tempRegister = "%" + Integer.toString(numOfRegisters);
			numOfRegisters++;
			String out_p = "	" + tempRegister + " = " + "bitcast" + " " + getType(mthd.body.type) + " " + register + " to " + getType(mthd.typeid);
			out.println(out_p);
			register = tempRegister; 
		}

		output = "	" +  "ret " + getType(mthd.typeid) + " " + register + "}";
		out.println(output);
		st.exitScope();
	}

	// generating IR for attributes
	public void process(AST.attr attribute) {
		String prevReg = printGEP(currentClass , attribute.name);
		String register = process(attribute.value,st);
		if(attribute.typeid.equals("String")) {
			if(register == null) {
				if(stringRegister.containsKey(attribute.value.type)) {
					register = null;
				}
				else {
					register = "%" + Integer.toString(numOfRegisters);
					out.println("	" + register + " = getelementptr inbounds [" + (attribute.value.type.length()+1) + " x i8], [" + (attribute.value.type.length()+1) + " x i8]* " + stringRegister.get(attribute.value.type) + ", i32 0, i32 0");  
					numOfRegisters++;
				}
				printStoreInstruction(register,prevReg,getType(attribute.typeid));
			}
			else {
				printStoreInstruction(register,prevReg,getType(attribute.typeid));
			}
		}
		else if(attribute.typeid.equals("Int")) {

			if(register == null) {
				printStoreInstruction("0",prevReg,getType(attribute.typeid));
			}
			else {
				printStoreInstruction(register,prevReg,getType(attribute.typeid));
			}

		}
		else if(attribute.typeid.equals("Bool")){

			if(register == null) {
				printStoreInstruction("0",prevReg,getType(attribute.typeid));
			}
			else {
				printStoreInstruction(register,prevReg,getType(attribute.typeid));
			}

		}
		else {
			if(register == null) {
				if(attribute.typeid.substring(attribute.typeid.length()-1).equals("*")) {
					out.println("	" + "store " + "%class." + attribute.typeid +"* " + "null" + ", %class." + attribute.typeid + "** " + prevReg + ", align 8");
				}
				else {
					out.println("	" + "store " + "%class." + attribute.typeid +"* " + "null" + ", %class." + attribute.typeid + "** " + prevReg + ", align 4");	
				}
			}
			else {
				if(!attribute.typeid.equals(attribute.value.type)) {
					if(attribute.typeid.equals("Object") && (attribute.value.type == "Int" || attribute.value.type == "String" || attribute.value.type == "Bool" )) {
						AST.new_ newObj = new AST.new_("Object", 0);
						newObj.type = "Object";
						register = process(newObj,st);
						out.println(register + " = getelementptr inbounds " +"%class.Object, " + "%class.Object* " + register + ", i32 0, i32 0");
						numOfRegisters++;
						String registerHere = "%"+Integer.toString(numOfRegisters);
						if(stringRegister.containsKey(attribute.value.type)) {
							registerHere = null;
						}
						else {
							out.println("	" + registerHere + " = getelementptr inbounds [" + (attribute.value.type.length()+1) + " x i8], [" + (attribute.value.type.length()+1) + " x i8]* " + stringRegister.get(attribute.value.type) + ", i32 0, i32 0");  
							numOfRegisters++;
						}
						printStoreInstruction(registerHere,register,getType(attribute.typeid));
					}
					else {
						String oc = attribute.value.type;
						String p = ig.graph.get(ig.nameToIndx.get(attribute.value.type)).parent.clas.name;
						while(!p.equals(attribute.typeid)) {
							String tempregister = "%" + Integer.toString(numOfRegisters);
							String out_p = "	" + tempregister + " = " + "bitcast" + " " + getType(oc) + " " + register + " to " + getType(p) ; 
							out.println(out_p);
							numOfRegisters++;
							oc = p;
							p = ig.graph.get(ig.nameToIndx.get(p)).parent.clas.name;
							register = tempregister;
						}
						String tempregister = "%" + Integer.toString(numOfRegisters);
						numOfRegisters++;
						String out_p = "	" + tempregister + " = " + "bitcast" + " " + getType(oc) + " " + register + " to " + getType(p) ; 
						register = tempregister;
						out.println(out_p);
					}
				}
			}
			if(attribute.typeid.substring(attribute.typeid.length()-1) == "*") {
				out.println("	" + "store " + "%class." + attribute.typeid +"* " + register + ", %class." + attribute.typeid + "** " + prevReg + ", align 8");
			}
			else {
				out.println("	" + "store " + "%class." + attribute.typeid +"* " + register + ", %class." + attribute.typeid + "** " + prevReg + ", align 4");	
			}
		}

	}

	public String process(AST.formal fm) {
		// adding the parameters to methodParams
		// returning the type
		st.insert(fm.name, fm.typeid);
		methodParams.add(fm.name);
		return getType(fm.typeid) + "%" + fm.name;

	}

	public String getType(String type) {
		// function to process the type
		if(type.equals("String")) {
			return "i8*";
		}
		else if(type.equals("Int")) {
			return "i32";
		}
		else if(type.equals("Bool")) {
			return "i8";
		}
		else if(type.equals("i64")) {
			return "i64";
		}
		else if(type.equals("i1")) {
			return "i1";
		}
		else {
			return "%class." + type + "*"; 
		}
	}

	public void printStoreInstruction(String register, String addr, String type) {
		// If it is a ptr we ensure the address is a multiple of 8, else we ensure address is a multiple of 4
		String temp = "		" + "store " + type + " " + register + ", " + type + "*" + " " + addr + ", align ";
		if(type.substring(type.length()-1).equals("*")){
			temp += "8";
		}
		else {
			temp += "4";
		}
		out.println(temp);
	}

	public String printGEP(String className, String attrName) {
		// function to print IR for getelementptr
		String temp;
		String register = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		temp = "	" + register + " = getelementptr inbounds " + "%class."+className ;
		temp +=  ", " + "%class."+className + "* " + "%this,"; 
		temp += classtoattrtoindex.get(className).get(attrName) ;
		out.println(temp);
		return register;
	}	
}	