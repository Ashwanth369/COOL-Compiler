package cool;

import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class ExprPrinter {

	String filename;
	String currentClass;											//This variable helps in finding the class of self.
	Map<String,Integer> classSize;
	public static Map<String,String> stringRegister = new HashMap<>();
	static Map<String,Map<String,String>> classtoattrtoindex = new HashMap<>();
	Map<String,Integer> labelcount;
	Integer numOfRegisters;
	InheritanceGraph ig;
	Set<String> functions;
	Set<String> methodParams;
	PrintWriter out;
	String mainReturnType;
	public static Integer stringConstantsCounter = 0;

	public String process(AST.expression exp,ScopeTable<String> st) {
		if(exp instanceof AST.no_expr) {				//If the expression is an instance of no_expr.
			return null;
		}
		else if(exp instanceof AST.bool_const) {		//If the expression is an instance of bool_const.
			AST.bool_const expr = (AST.bool_const)exp;
			if(expr.value) return "1";
			else return "0";							//expression type for such an expression is Bool.
		}
		else if(exp instanceof AST.string_const) {		//If the expression is an instance of string_const.

			AST.string_const expr = (AST.string_const)exp;
			String str = expr.value ;						//expression type for such an expression is String.
			String output = "%"+Integer.toString(numOfRegisters)+" = getelementptr inbounds ["+Integer.toString(str.length()+1)
							+" x i8], ["+Integer.toString(str.length()+1)+" x i8]* "+stringRegister.get(str)+", i32 0, i32 0";
			out.println(output);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.int_const) {			//If the expression is an instance of int_const.
			AST.int_const expr = (AST.int_const)exp;
			return String.valueOf(expr.value);							//expression type for such an expression is Int.
		}
		else if(exp instanceof AST.comp) {				//If the expression is an instance of comp. The argument passed must be Bool and return type must also be Bool.
			AST.comp expr = (AST.comp)exp;
			String temp = process(expr.e1,st);
			String output = "%"+Integer.toString(numOfRegisters)+" = xor i8 "+temp+", 1";
			out.println(output);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.lt) {
			AST.lt expr = (AST.lt)exp;
			String temp1 = process(expr.e1,st);
			String temp2 = process(expr.e2,st);
			String output1 = "%"+Integer.toString(numOfRegisters)+" = icmp slt i32 "+temp1+", "+temp2;
			out.println(output1);
			numOfRegisters++;
			String output2 = "%"+Integer.toString(numOfRegisters)+" = zext i1 %"+Integer.toString(numOfRegisters-1)+"to i8";
			out.println(output2);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.leq) {
			AST.leq expr = (AST.leq)exp;
			String temp1 = process(expr.e1,st);
			String temp2 = process(expr.e2,st);
			String output1 = "%"+Integer.toString(numOfRegisters)+" = icmp sle i32 "+temp1+", "+temp2;
			out.println(output1);
			numOfRegisters++;
			String output2 = "%"+Integer.toString(numOfRegisters)+" = zext i1 %"+Integer.toString(numOfRegisters-1)+"to i8";
			out.println(output2);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.eq) {
			AST.eq expr = (AST.eq)exp;
			String temp1 = process(expr.e1,st);
			String temp2 = process(expr.e2,st);
			String output1 = "%"+Integer.toString(numOfRegisters)+" = icmp eq i32 "+temp1+", "+temp2;
			out.println(output1);
			numOfRegisters++;
			String output2 = "%"+Integer.toString(numOfRegisters)+" = zext i1 %"+Integer.toString(numOfRegisters-1)+"to i8";
			out.println(output2);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.neg) {				//If the expression is an instance of comp. The argument passed must be Bool and return type must also be Bool.
			AST.neg expr = (AST.neg)exp;
			String temp = process(expr.e1,st);
			String output = "%"+Integer.toString(numOfRegisters)+" = sub i32 0, "+temp;
			out.println(output);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.new_) {
			// printing IR for new expr
			AST.new_ expr = (AST.new_)exp;
			String output1;
			String output2 = null;
			if(expr.typeid.equals("Int")) {
				output1 = "%"+Integer.toString(numOfRegisters)+" = call noalias i8* @malloc(i64 4)";
				out.println(output1);
				numOfRegisters++;
				output2 = "%"+Integer.toString(numOfRegisters)+" = bitcast i8* %"+Integer.toString(numOfRegisters-1)+" to i32*";
				out.println(output2);
				numOfRegisters++;
			}
			else if(expr.typeid.equals("String") || expr.typeid.equals("Bool")) {
				output1 = "%"+Integer.toString(numOfRegisters)+" = call noalias i8* @malloc(i64 1)";
				out.println(output1);
				numOfRegisters++;
			}
			else {
				output1 = "%"+Integer.toString(numOfRegisters)+" = call noalias i8* @malloc(i64 "+classSize.get(expr.typeid)+")";
				out.println(output1);
				numOfRegisters++;
				String ret = "%"+Integer.toString(numOfRegisters);
				output2 = "%"+Integer.toString(numOfRegisters)+" = bitcast i8* %"+Integer.toString(numOfRegisters-1)+" to %class."+expr.typeid+"*";
				out.println(output2);
				numOfRegisters++;
				String temp = Integer.toString(expr.typeid.length());
				String output3 = "call void @ZN"+temp+expr.typeid+temp+expr.typeid+"(%class."+expr.typeid+"* "+ret+")";
				out.println(output3);
				String temp2 = ret;
				if(!expr.typeid.equals("Object")) {
					String temp3 = "%"+Integer.toString(numOfRegisters);
					String result = "\t"+temp3+" = bitcast ";
					if(!(expr.typeid.equals("i8*") || expr.typeid.equals("i8") || expr.typeid.equals("i32") || expr.typeid.equals("i64") || expr.typeid.equals("i1"))) {
						result += "%class."+expr.typeid+"*";
					}
					else {
						result += expr.typeid;
					}
					result += " "+ret+" to %class.Object*";
					out.println(result);
					numOfRegisters++;
					temp2 = temp3;
				}
				String temp4 = "%"+Integer.toString(numOfRegisters);
				out.println(temp4+" = getelementptr inbounds %class.Object, %class.Object* "+temp2+", i32 0, i32 0");
				numOfRegisters++;
				String temp5 = null;
				if(stringRegister.containsKey(expr.typeid)) {
					temp5 = "%"+Integer.toString(numOfRegisters);
					out.println("\t"+temp5+" = getelementptr inbounds ["+Integer.toString(expr.typeid.length()+1)+" x i8], ["+Integer.toString(expr.typeid.length()+1)+" x i8]* "+stringRegister.get(expr.typeid)+", i32 0, i32 0");
					numOfRegisters++;
				}
				out.println("\tstore i8* "+temp5+", i8** "+temp4+", align 8");
				return ret;
				
			}
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.assign) {
			// printing IR for assignment expression
			AST.assign expr = (AST.assign)exp;
			
			String temp = process(expr.e1,st);
			String temp1 = temp;
			String output1;
			String output2;
			String output3;
			String temp2;
			
			String typeid = st.lookUpGlobal(expr.name);

			// type doen't match bitcast into the required type
			if(!(typeid.equals(expr.e1.type))) {
				if(expr.e1.type.equals("String") || expr.e1.type.equals("Bool")) {
					output2 = "%"+Integer.toString(numOfRegisters)+" = bitcast i8* "+temp1+" to %class.Object*";
					out.println(output2);
					temp1 = "%"+Integer.toString(numOfRegisters);
					numOfRegisters++;
				}
				else if(expr.e1.type.equals("Int")) {
					output2 = "%"+Integer.toString(numOfRegisters)+" = bitcast i32* "+temp1+" to %class.Object*";
					out.println(output2);
					temp1 = "%"+Integer.toString(numOfRegisters);
					numOfRegisters++;
				}
				else {
					output2 = "%"+Integer.toString(numOfRegisters)+" = bitcast %class."+expr.e1.type+" "+temp1+" to %class."+typeid;
					out.println(output2);
					temp1 = "%"+Integer.toString(numOfRegisters);
					numOfRegisters++;
				}
			}
			if(methodParams.contains(expr.name)) {
				temp2 = "%"+expr.name+".addr";
			}
			else {
				output3 = "%"+Integer.toString(numOfRegisters)+" = getelementptr inbounds %class."+currentClass+", %class."+currentClass
							+"* %this, "+classtoattrtoindex.get(currentClass).get(expr.name);
				out.println(output3);
				temp2 = "%"+Integer.toString(numOfRegisters);
				numOfRegisters++;
			}
			if(typeid.equals("String")) {
				output1 = "store i8* "+temp1+", i8** "+temp2+", align 8";
			}
			else if(typeid.equals("Int")) {
				output1 = "store i32 "+temp1+", i32* "+temp2+", align 4";
			}
			else if(typeid.equals("Bool")) {
				output1 = "store i8 "+temp1+", i8* "+temp2+", align 4";
			}
			else if(typeid.equals("i64")) {
				output1 = "store i64 "+temp1+", i64* "+temp2+", align 4";
			}
			else if(typeid.equals("i1")) {
				output1 = "store i1 "+temp1+", i1* "+temp2+", align 4";
			}
			else {
				output1 = "store %class."+typeid+"* "+temp1+", %class."+typeid+"** "+temp2+", align 8";
			}
			out.println(output1);
			return temp;
		}
		else if(exp instanceof AST.isvoid) {
			AST.isvoid expr = (AST.isvoid)exp;
			String temp = process(expr.e1,st);
			if(expr.e1.type == "String" || expr.e1.type == "Int" || expr.e1.type == "Bool") {
				return "0";
			}
			String output1 = "%"+Integer.toString(numOfRegisters)+" = icmp eq %class."+expr.e1.type+"* "+temp+", null";
			out.println(output1);
			numOfRegisters++;
			String output2 = "%"+Integer.toString(numOfRegisters)+" = zext i1 %"+Integer.toString(numOfRegisters-1)+"to i8";
			out.println(output2);
			numOfRegisters++;
			return ("%"+Integer.toString(numOfRegisters-1));
		}
		else if(exp instanceof AST.block) {
			AST.block expr = (AST.block)exp;
			String temp = null;
			for(AST.expression e: expr.l1) {
				temp = process(e,st);
			}
			return temp;
		}
		else if(exp instanceof AST.loop) {
			// IR for loops
			AST.loop expr = (AST.loop)exp;
			String temp1 = null;
			if(labelcount.containsKey("while.cond")) {
				temp1 = "while.cond."+Integer.toString(labelcount.get("while.cond"));
				labelcount.put("while.cond",labelcount.get("while.cond")+1);
			}
			else {
				temp1 = "while.cond";
				labelcount.put("while.cond",1);	
			}
			String temp2 = null;
			if(labelcount.containsKey("while.body")) {
				temp2 = "while.body."+Integer.toString(labelcount.get("while.body"));
				labelcount.put("while.body",labelcount.get("while.body")+1);
			}
			else {
				temp2 = "while.body";
				labelcount.put("while.body",1);	
			}
			String temp3 = null;
			if(labelcount.containsKey("while.end")) {
				temp3 = "while.end."+Integer.toString(labelcount.get("while.end"));
				labelcount.put("while.end",labelcount.get("while.end")+1);
			}
			else {
				temp3 = "while.end";
				labelcount.put("while.end",1);	
			}
			out.println("\tbr label %"+temp1);
			out.println("\n"+temp1+":");

			String temp4 = process(expr.predicate,st);

			String temp5 = "%"+Integer.toString(numOfRegisters);
			out.println("\t"+temp5+" = trunc i8 "+temp4+" to i1");
			numOfRegisters++;

			out.println("\tbr i1 "+temp5+", label %"+temp2+", label %"+temp3);
			
			out.println("\n"+temp2+":");

			String temp6 = process(expr.body,st);
			out.println("\tbr label %"+temp1);

			out.println("\n"+temp3+":");
			return "null";
		}
		else if(exp instanceof AST.cond) {
			//IR for conditional statements
			AST.cond expr = (AST.cond)exp;
			String temp1 = null;
			if(labelcount.containsKey("if.then")) {
				temp1 = "if.then."+Integer.toString(labelcount.get("if.then"));
				labelcount.put("if.then",labelcount.get("if.then")+1);
			}
			else {
				temp1 = "if.then";
				labelcount.put("if.then",1);	
			}
			String temp2 = null;
			if(labelcount.containsKey("if.else")) {
				temp2 = "if.else."+Integer.toString(labelcount.get("if.else"));
				labelcount.put("if.else",labelcount.get("if.else")+1);
			}
			else {
				temp2 = "if.else";
				labelcount.put("if.else",1);	
			}
			String temp3 = null;
			if(labelcount.containsKey("if.end")) {
				temp3 = "if.end."+Integer.toString(labelcount.get("if.end"));
				labelcount.put("if.end",labelcount.get("if.end")+1);
			}
			else {
				temp3 = "if.end";
				labelcount.put("if.end",1);	
			}
			String join = ig.join(ig.graph.get(ig.nameToIndx.get(expr.ifbody.type)),ig.graph.get(ig.nameToIndx.get(expr.elsebody.type))).clas.name;
			String join1 = null;
			if(join.equals("String")) {
				join1 = "i8*";
			}
			else if(join.equals("Int")) {
				join1 = "i32";
			}
			else if(join.equals("Bool")) {
				join1 = "i8";
			}
			else if(join.equals("i64")) {
				join1 = "i64";
			}
			else if(join.equals("i1")) {
				join1 = "i1";
			}
			else{
				join1 = "%class."+join;
			}
			
			String temp4 = "%"+Integer.toString(numOfRegisters);
			out.println("\t"+temp4+" = alloca "+join1+", align 8");
			numOfRegisters++;

			String temp5 = process(expr.predicate,st);
			
			String temp6 = "%"+Integer.toString(numOfRegisters);
			out.println("\t"+temp6+" = trunc i8 "+temp5+" to i1");
			numOfRegisters++;
			out.println("\tbr i1 "+temp6+", label %"+temp1+", label %"+temp2);

			out.println("\n"+temp1+":");
			String temp7 = process(expr.ifbody,st);
			if(!join.equals(expr.ifbody.type)) {
				String t = "%"+Integer.toString(numOfRegisters);
				String iftype = expr.ifbody.type;
				if(!(iftype.equals("i8*") || iftype.equals("i32") || iftype.equals("i8") || iftype.equals("i64") || iftype.equals("i1"))) {
					iftype = "%class."+iftype+"*";
				}
				String jointype = join;
				if(!(jointype.equals("i8*") || jointype.equals("i32") || jointype.equals("i8") || jointype.equals("i64") || jointype.equals("i1"))) {
					jointype = "%class."+jointype+"*";
				}
				out.println("\t"+t+" = bitcast "+iftype+" "+temp7+" to "+jointype);
				numOfRegisters++;
				temp7 = t;
			}
			if(join.equals("String") || join.equals("Int") || join.equals("Bool")) {
				if(join1.substring(join1.length()-1) == "*") {
					out.println("\tstore "+join1+" "+temp7+", "+join1+"* "+temp4+", align 8");
				}
				else {
					out.println("\tstore "+join1+" "+temp7+", "+join1+"* "+temp4+", align 4");
				}
			}
			else {
				String reg = "%"+Integer.toString(numOfRegisters);
				numOfRegisters++;
				if(join1.substring(join1.length()-1) == "*") {
					out.println("\t"+reg+" = load "+join1+", "+join1+"* "+temp7+", align 8");
					out.println("\tstore "+join1+" "+reg+", "+join1+"* "+temp4+", align 8");
				}
				else {
					out.println("\t"+reg+" = load "+join1+", "+join1+"* "+temp7+", align 4");
					out.println("\tstore "+join1+" "+reg+", "+join1+"* "+temp4+", align 4");
				}
			}
			out.println("\tbr label %"+temp3);

			out.println("\n"+temp2+":");
			String temp8 = process(expr.elsebody,st);
			if(!join.equals(expr.elsebody.type)) {
				String t = "%"+Integer.toString(numOfRegisters);
				String elsetype = expr.elsebody.type;
				if(!(elsetype.equals("i8*") || elsetype.equals("i32") || elsetype.equals("i8") || elsetype.equals("i64") || elsetype.equals("i1"))) {
					elsetype = "%class."+elsetype+"*";
				}
				String jointype = join;
				if(!(jointype.equals("i8*") || jointype.equals("i32") || jointype.equals("i8") || jointype.equals("i64") || jointype.equals("i1"))) {
					jointype = "%class."+jointype+"*";
				}
				out.println("\t"+t+" = bitcast "+elsetype+" "+temp8+" to "+jointype);
				numOfRegisters++;
				temp8 = t;
			}
			if(join.equals("String") || join.equals("Int") || join.equals("Bool")) {
				if(join1.substring(join1.length()-1) == "*") {
					out.println("\tstore "+join1+" "+temp8+", "+join1+"* "+temp4+", align 8");
				}
				else {
					out.println("\tstore "+join1+" "+temp8+", "+join1+"* "+temp4+", align 4");
				}
			}
			else {
				String reg = "%"+Integer.toString(numOfRegisters);
				numOfRegisters++;
				if(join1.substring(join1.length()-1).equals("*")) {
					out.println("\t"+reg+" = load "+join1+", "+join1+"* "+temp8+", align 8");
					out.println("\tstore "+join1+" "+reg+", "+join1+"* "+temp4+", align 8");
				}
				else {
					out.println("\t"+reg+" = load "+join1+", "+join1+"* "+temp8+", align 4");
					out.println("\tstore "+join1+" "+reg+", "+join1+"* "+temp4+", align 4");
				}
			}
			out.println("\tbr label %"+temp3);
			out.println("\n"+temp3+":");
			if(join.equals("String") || join.equals("Int") || join.equals("Bool")) {
				String reg = "%"+Integer.toString(numOfRegisters);
				numOfRegisters++;
				if(join1.substring(join1.length()-1).equals("*")) {
					out.println("\t"+reg+" = load "+join1+", "+join1+"* "+temp4+", align 8");
				}
				else {
					out.println("\t"+reg+" = load "+join1+", "+join1+"* "+temp4+", align 4");
				}
				return reg;
			}
			else {
				return temp4;
			}

		}
		else if(exp instanceof AST.static_dispatch) {
			// handling static dispatch
			AST.static_dispatch expr = (AST.static_dispatch)exp;
			
			String result = null;
			
			String reg = process(expr.caller,st);
			if(expr.name.equals("length") && expr.typeid.equals("String")) {
				String t = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+t+" = call i64 @strlen(i8*"+reg+")");
				numOfRegisters++;
				result = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+result+" = trunc i64 "+t+" to i32");
				numOfRegisters++;
			}
			else if((expr.caller.type.equals("String") || expr.caller.type.equals("Int") || expr.caller.type.equals("Bool")) && expr.name.equals("abort")) {
				String temp1 = null;
				if(stringRegister.containsKey(expr.caller.type)) {
					temp1 = "%"+Integer.toString(numOfRegisters);
					out.println("\t"+temp1+" = getelementptr inbounds ["+Integer.toString(expr.caller.type.length()+1)+" x i8], ["+Integer.toString(expr.caller.type.length()+1)+" x i8*] "+stringRegister.get(expr.caller.type));
					numOfRegisters++;
				}
				String temp2 = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+temp2+" = getelementptr inbounds [3 x i8], [3 x i8*] "+stringRegister.get("%s"));
				numOfRegisters++;

				String temp3 = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+temp3+" = getelementptr inbounds [25 x i8], [25 x i8*] "+stringRegister.get("Abort called from class "));
				numOfRegisters++;

				out.println("\t%"+Integer.toString(numOfRegisters)+" = call i32 (i8*, ...) @printf(i8* "+temp2+", i8* "+temp3+")");
				numOfRegisters++;
				out.println("\t%"+Integer.toString(numOfRegisters)+" = call i32 (i8*, ...) @printf(i8* "+temp2+", i8* "+temp1+")");
				numOfRegisters++;

				temp3 = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+temp3+" = getelementptr inbounds [2 x i8], [2 x i8*] "+stringRegister.get("\n"));
				numOfRegisters++;
				
				out.println("\t%"+Integer.toString(numOfRegisters)+" = call i32 (i8*, ...) @printf(i8* "+temp2+", i8* "+temp3+")");
				numOfRegisters++;
				
				out.println("\tcall void @exit(i32 0)");

				String temp4 = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+temp4+" = call noalias i8* @malloc(i64 0)");
				numOfRegisters++;

				result = "%"+Integer.toString(numOfRegisters);
				out.println("\t"+result+" = bitcast i8* "+temp4+" to %class.Object*");
				numOfRegisters++;
				
				out.println("\tcall void @ZN6Object6Object(%class.Object* "+result+")");
			}
			else if((expr.caller.type.equals("String") || expr.caller.type.equals("Int") || expr.caller.type.equals("Bool")) && expr.name.equals("type_name")) {
				if(stringRegister.containsKey(expr.caller.type)) {
					String temp1 = "%"+Integer.toString(numOfRegisters);
					out.println("\t"+temp1+" = getelementptr inbounds ["+Integer.toString(expr.caller.type.length()+1)+" x i8], ["+Integer.toString(expr.caller.type.length()+1)+" x i8*] "+stringRegister.get(expr.caller.type));
					numOfRegisters++;
				}
			}
			if(result == null) {
				if(!(expr.caller.type.equals("String") || expr.caller.type.equals("Int") || expr.caller.type.equals("Bool"))) {
					String temp1 = null;
					if(labelcount.containsKey("if.then")) {
						temp1 = "if.then."+Integer.toString(labelcount.get("if.then"));
						labelcount.put("if.then",labelcount.get("if.then")+1);
					}
					else {
						temp1 = "if.then";
						labelcount.put("if.then",1);
					}
					String temp2 = null;
					if(labelcount.containsKey("if.else")) {
						temp2 = "if.else."+Integer.toString(labelcount.get("if.else"));
						labelcount.put("if.else",labelcount.get("if.else")+1);
					}
					else {
						temp2 = "if.else";
						labelcount.put("if.else",1);
					}
					String temp3 = null;
					if(labelcount.containsKey("if.end")) {
						temp3 = "if.end."+Integer.toString(labelcount.get("if.end"));
						labelcount.put("if.end",labelcount.get("if.end")+1);
					}
					else {
						temp3 = "if.end";
						labelcount.put("if.end",1);
					}

					String temp4 = "%"+Integer.toString(numOfRegisters);
					String t = null;
					if(expr.caller.type.equals("String")) {
						t = "i8*";
					}
					else if(expr.caller.type.equals("Int")) {
						t = "i32";
					}
					else if(expr.caller.type.equals("Bool")) {
						t = "i8";
					}

					else if(expr.caller.type.equals("i64")) {
						t = "i64";
					}
					else if(expr.caller.type.equals("i1")) {
						t = "i1";
					}
					else {
						t = "%class."+expr.caller.type+"*";
					}
					out.println("\t"+temp4+" = icmp eq "+t+" "+reg+", null");
					numOfRegisters++;

					out.println("\tbr i1 "+temp4+", label %"+temp1+", label %"+temp2);

					out.println("\n"+temp1+":");
					out.println("\tcall void @print_dispatch_on_void_error(i32 "+expr.lineNo+")");
					out.println("\t call void @exit(i32 1)");
					out.println("\tbr label %"+temp3);

					out.println("\n"+temp2+":");
					out.println("\tbr label %"+temp3);

					out.println("\n"+temp3+":");
				}

				String cls = expr.typeid;
				while(!functions.contains("ZN"+Integer.toString(cls.length())+cls+Integer.toString(expr.name.length())+expr.name)) {
					for(Node n : ig.graph) {
						if(n.clas.name.equals(cls)) {
							cls = n.clas.parent;
							break;
						}
					}
				}
				String clas = cls;
				if(!cls.equals(expr.caller.type)) {
					String t = "%"+Integer.toString(numOfRegisters);
					numOfRegisters++;
					String exprtype = expr.caller.type;
					if(!(exprtype.equals("i8*") || exprtype.equals("i32") || exprtype.equals("i64") || exprtype.equals("i8") || exprtype.equals("i1"))) {
						exprtype = "%class."+exprtype+"*";
					}
					String clstype = cls;
					if(!(cls.equals("i8*") || cls.equals("i32") || cls.equals("i64") || cls.equals("i8") || cls.equals("i1"))) {
						cls = "%class."+cls+"*";
					}
					out.println("\t"+t+" = bitcast "+exprtype+" "+reg+" to "+cls);
					reg = t;
				}
				else {
					cls = "%class."+cls+"*";
				}

				String output = null;
				if(cls.equals("String")) {
					output = "i8* "+reg;
				}
				else if(cls.equals("Int")) {
					output = "i32 "+reg;
				}
				else if(cls.equals("Bool")) {
					output = "i8 "+reg;
				}
				else if(cls.equals("i64")) {
					output = "i64 "+reg;
				}
				else if(cls.equals("i1")) {
					output = "i1 "+reg;
				}
				else {
					output = "%class."+clas+"* "+reg;
				}
				for(AST.expression e : expr.actuals) {
						
					output += ", ";
					if(e.type.equals("String")) {
						output += "i8* ";
					}
					else if(e.type.equals("Int")) {
						output += "i32 ";
					}
					else if(e.type.equals("Bool")) {
						output += "i8 ";
					}
					else if(e.type.equals("i64")) {
						output += "i64 ";
					}
					else if(e.type.equals("i1")) {
						output += "i1 ";
					}
					else {
						output += clas+"* ";
					}
					output += process(e,st);

				}

				result = "%"+Integer.toString(numOfRegisters);
				numOfRegisters++;
				if(expr.type.equals("Int")) {
					out.println("\t"+result+" = call "+ "i32" +" @ZN"+Integer.toString(clas.length())+clas+Integer.toString(expr.name.length())+expr.name+"("+output+")");
				}
				else if(expr.type.equals("String")) {
					out.println("\t"+result+" = call "+ "i8*" +" @ZN"+Integer.toString(clas.length())+clas+Integer.toString(expr.name.length())+expr.name+"("+output+")");
				}
				else if(expr.type.equals("Bool")) {
					out.println("\t"+result+" = call "+ "i8" +" @ZN"+Integer.toString(clas.length())+clas+Integer.toString(expr.name.length())+expr.name+"("+output+")");
				}
				else {
					out.println("\t"+result+" = call %class."+ expr.type +"* @ZN"+Integer.toString(clas.length())+clas+Integer.toString(expr.name.length())+expr.name+"("+output+")");
				}
			}
			return result;
		}
		else if(exp instanceof AST.object) {			//If the expression is an instance of object.
			AST.object expr = (AST.object)exp;
			if(expr.name.equals("self")) {				//If the object is self, then it's type is the current class.
				return "%this";
			}
			if(methodParams.contains(expr.name)) {
				if(expr.type.equals("String")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i8* " + ", i8** " + "%"+expr.name+".addr" + ", align 8");
				}
				else if(expr.type.equals("Int")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i32 " + ", i32* " + "%"+expr.name+".addr" + ", align 4");
				}
				else if(expr.type.equals("Bool")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i8 " + ", i8* " + "%"+expr.name+".addr" + ", align 4");
				}
				else if(expr.type.equals("i64")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i64 " + ", i64* " + "%"+expr.name+".addr" + ", align 4");
				}
				else if(expr.type.equals("i1")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i1 " + ", i1* " + "%"+expr.name+".addr" + ", align 4");
				}
				else {
					out.println("%" + Integer.toString(numOfRegisters) + " = load %class." + expr.type + "*" + ", %class." + expr.type + "** " + "%"+expr.name+".addr" + ", align 8");
				}
				String ret = "%"+Integer.toString(numOfRegisters);
				numOfRegisters++;
				return ret; 
			}
			else {
				String reg = printGEP(currentClass,expr.name);
				if(expr.type.equals("String")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i8* " + ", i8** " + reg + ", align 8");
				}
				else if(expr.type.equals("Int")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i32 " + ", i32* " + reg + ", align 8");
				}
				else if(expr.type.equals("Bool")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i8 " + ", i8* " + reg + ", align 8");
				}
				else if(expr.type.equals("i64")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i64* " + ", i64** " + reg + ", align 4");
				}
				else if(expr.type.equals("i1")) {
					out.println("%" + Integer.toString(numOfRegisters) + " = load " + "i1* " + ", i1** " + reg + ", align 4");
				}
				else {
					out.println("%" + Integer.toString(numOfRegisters) + " = load %class." + expr.type + "*" + ", %class." + expr.type + "** " + reg + ", align 8");
				}
			}
			String ret = "%"+Integer.toString(numOfRegisters);	
			numOfRegisters++;
			return ret;
		}

		// IR for basic operands
		else if(exp instanceof AST.plus) {
			AST.plus expr = (AST.plus)exp;
			String op1 = process(expr.e1,st);
			String op2 = process(expr.e2,st);

			if(expr.type.equals("Int")) {
				out.println("%" + Integer.toString(numOfRegisters) + " = add nsw " + "i32 " + op1 + ", " + op2);
			}
			String ret = "%"+Integer.toString(numOfRegisters);	
			numOfRegisters++;
			return ret; 
		}

		else if(exp instanceof AST.sub) {
			AST.sub expr = (AST.sub)exp;
			String op1 = process(expr.e1,st);
			String op2 = process(expr.e2,st);
			if(expr.type.equals("Int")) {
				out.println("%" + Integer.toString(numOfRegisters) + " = sub nsw " + "i32 " + op1 + ", " + op2);
			}
			String ret = "%"+Integer.toString(numOfRegisters);	
			numOfRegisters++;
			return ret;	 
		}

		else if(exp instanceof AST.mul) {
			AST.mul expr = (AST.mul)exp;
			String op1 = process(expr.e1,st);
			String op2 = process(expr.e2,st);

			if(expr.type.equals("Int")) {
				out.println("%" + Integer.toString(numOfRegisters) + " = mul nsw " + "i32 " + op1 + ", " + op2);
			}
			String ret = "%"+Integer.toString(numOfRegisters);	
			numOfRegisters++;
			return ret;	 
		}

		else if(exp instanceof AST.divide) {
			AST.divide expr = (AST.divide)exp;
			String op1 = process(expr.e1,st);
			String op2 = process(expr.e2,st);
			String ifthenLabel = "if.then";
			String ifelseLabel = "if.else";
			String ifendLabel = "if.end";

			// checking for divide by zero error by placing conditional statements
			if(labelcount.containsKey("if.then")) {
				int value = labelcount.get("if.then");
				ifthenLabel = ifthenLabel + "." + value;
				labelcount.put("if.then",value+1);
			}
			else {
				labelcount.put("if.then",1);
			}

			if(labelcount.containsKey("if.else")) {
				int value = labelcount.get("if.else");
				ifelseLabel = ifelseLabel + "." + value;
				labelcount.put("if.else",value+1);
			}
			else {
				labelcount.put("if.else",1);
			}

			if(labelcount.containsKey("if.end")) {
				int value = labelcount.get("if.end");
				ifendLabel = ifendLabel + "." + value;
				labelcount.put("if.end",value+1);
			}
			else {
				labelcount.put("if.end",1);
			}

			// condition to check if op2 is zero
			String register = "%" + Integer.toString(numOfRegisters);
			numOfRegisters++;
			out.println("	" + register + " = " + "icmp eq" + " " + "i32 " + op2 + ", " + "0");
			out.println("	" + "br i1 " + register + ", " + "label %" + ifthenLabel + ", label%" + ifelseLabel);

			out.println(ifthenLabel + ":");
			out.println("	" + "call void @" + "print_div_by_zer_err_msg" + "(" + "i32 " + expr.lineNo +")");
			out.println("	" + "call void @exit(i32 1)");
			out.println("	"+"br label %" + ifendLabel);

			// else continue 
			out.println(ifelseLabel + ":");
			out.println("	"+"br label %" + ifendLabel);

			out.println(ifendLabel + ":");

			if(expr.type.equals("Int")) {
				out.println("%" + Integer.toString(numOfRegisters) + " = sdiv " + "i32 " + op1 + ", " + op2);
			}
			String ret = "%"+Integer.toString(numOfRegisters);	
			numOfRegisters++;
			return ret;	 
		}
		return null;
	}

	// printing IR for getelementptr
	public String printGEP(String className, String attrName) {
		String temp;
		String register = "%"+Integer.toString(numOfRegisters);
		numOfRegisters++;
		temp = "	" + "%"+ Integer.toString(numOfRegisters) + " = getelementptr inbounds " + "%class."+className ;
		temp +=  ", " + "%class."+className + "* " + "%this,"; 
		temp += classtoattrtoindex.get(className).get(attrName) ;
		out.println(temp);
		return register;
	}

}