Overview of Code :

Visitor.java

-> This file contains methods to traverse all of the program and print IR for classes,methods,attributes etc..
-> We print IR for default methods to be used like IO mehods(out_string(), in_string(), out_int(), in_int()).
-> We perform a DFS traversal of Inheritance Graph to visit all the classes in the program and print IR for methods.
-> We also print the string constants globally at the begining.
-> string constants are collected during semantic analysis which is the design decision we have made.

ExprPrinter.java

-> This file contains methods to print IR for expressions.
-> These methods are called by functions in Visitor class.
-> for each expression we manage the register count carefully.
-> register count has to be returned to be used by functions in Visitor class.

Global constants used

-> stringRegister is a HashMap used to store all the string constants in the program.
-> numOfRegisters is an int to store the registers used in a method's scope, it is set to zero for every method's begining.
-> ig is the InheritanceGraph, this helps in program traversal and other useful information like getting parent class.
-> functions is a set to maintain all the manglednames of all the methods, we insert default method names at the begining of traversal.
-> labelcount is used to manage the labels for the conditional statements.
-> methodParams is used to maintain the method paramters, it is cleared at the begining of every method.
-> classSize is a mapping from classes to it's size, it is populated at the time of printing IR for class constructors.





Traversing the Program : 

-> process(AST.program)
	* The program is passed into this method, we construct inheritanceGraph using the InheritanceGraph.java module and in this method we declare the string constants globally.
	* We do a DFS on all the classes to define the structs for every class and also print the methods for every class.
	* At last we print the IR for class constructors.
	* Then we print the IR for default methods and main().

-> process(AST.class) 
	* we iterate over all the attributes and insert them into the scopetable to be used at printing the IR for constructors.
	* we iterate over all the methods and print the IR for each method.

-> process(AST.method)
	* we use a mangled name at the definition.
	* Next process is called on body of the method.
	* If the method body type and return type doesn't match we call bitcast

-> process(AST.attribute) 
	* This method is used at the time of creating constructor.
	* If there is an assignment store is called.
	* For primitive types default value is stored if no assignment is made for other types we store null.

-> process(AST.cond)
	* This method is used for printing the IR of conditional expressions
	* labels for if.then, if.else and if.end are created.
	* labels are used to jump to the appropriate block based upon the condition.
	* First the predicate is processed and branch instruction is created using the result of the predicate.
	* Then the body is processed.

-> process(AST.loop)
    * Similar to the conditional statement first the predicate is visited and the result is used to jump to the corresponding block.
    * Except after the body is processed jump is again made to the predicate.

-> process(AST.block)
	* Block contains a list of expressions, all the expressions are processed(IR generated) and the register value is returned

-> process(AST.static_dispatch)
	* dispatch on void condition is checked in this method.
	* The appropriate class and name of function is found using mangledNames and call instruction is created.

-> process(AST.new_)
	* For primitive types memory is already allocated using default values.
	* For Non Primitive types we allocate the memory using malloc, the classSize Map helps to get the size information to be passed to malloc.

-> process(AST.object)
	* If it is a method paramter it is simply loaded and returned.
	* We check the above condition using methodParams Map.
	* If it is an attribute we use getelementptr, and load the value.





Assumptions Made :
	* The code generator doesn't handle 
	SELF_TYPE,
	let,
	case and 
	dynamic dispatch.


Changes to the given files :
	* We used our own semantic analyzer.
	* We changed codegenTest.java to pass the filename to be printed in the IR as a comment.





TestCases :

-> test1.cl
	* This test case is used for checking the basic binary and unary operations.
	* The results of +,-,/,* are printed in the terminal.
	* This test case also checks the divide by zero case.

-> test2.cl
	* This test case checks the ifelse statements
	* The program compares two numbers and returns the greater of the two.

-> test3.cl
	* This test case checks the loop statements
	* The program prints the factorial of given number.

-> test_ioMethods.cl
	* This test case checks all the ioMethods (out_string,in_string,out_int,in_int)

-> test_staticdispatch.cl
	* This test case checks the static dispatch.
	* The method foo() is defined in both classes where one inherits from another.
	* Then foo() is called for different configurations.
	* This test case also checks for dispatch on void error.

-> test_trivial.cl
	* This test case is a simple trivial program to print "Hello, World !". 



Contributors :
    BOGA SRINIVAS - CS17BTECH11009
    GUNDETI ASHWANTH KUMAR - CS17BTECH11017