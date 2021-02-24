Node.java:
-> Defines the class for a node of a Inheritance Graph. It contains information about the class, it's parent, it's children, it's attributes and methods. It contains functions for adding the class' attributes and methods along with the parent's attributes and methods to the node.
-> addChild(): adds a child node to the current node.
-> addAttrMethod(): Checks if a class' feature is attribute/method and accordingly adds it to the attributes/methods Hashmap.If an attribute or method has been redefined, it returns an error.
-> addAttrMethodParent(): Adds the parent's attributes and methods to the Hashmap accordingly. Also checks if the attribute/method of a parent class has been defined again in the current class.


InheritanceGraph.java:

Builds a graph which contains the list of all nodes. Adds all the attributes and methods of a class to it's node with the help of the function in Node.java. Also if it has a parent, it adds the parent's attributes and methods as well, again with the help of the function in Node.java. checkCycles is used to check for cycles in the graph built and checkMain checks if the class Main and method main are defined. confirmcheck checks if a type confirms to the other type and join gets the least common ancestor of the two classes.

-> checkCycles() : Function does a BFS of graph to detect a cycle in Inheritance Graph. Reports an error if cycle is found.
-> buildGraph() : Function builds the InheritanceGraph from all the classes in program and checks the redefinition of classes.
-> addAttrMethods() : Function traverses the InheritanceGraph(BFS) adding all the attributes and methods of a node to it's children using addAttrMethod() and addAttrMethodParent() functions in Node.java.
-> checkMain() : Function checks if main method is defined correctly.
-> confirmcheck() : Function checks if a class is an ancestor of some other class by traversing using parent till you find the ancestor.
-> join() : Function accepts two nodes and returns a node which is the least common ancestor of both the nodes by traversing using parent until root from both the nodes and returns if an ancestor is equal.



ExprChecks.java:

Checks for the type confirmation of different types of statements and expression in the program and adds variable name and it's typeid in the scopetable.
check method is overloaded which provides a hierarchy while checking a particular class in the program.

-> check(AST.class_ cls,ScopeTable<String> st) : Function checks all the attributes and methods in a class and also adds them to the scopeTable
-> check(AST.attr a,ScopeTable<String> st) : Funcion checks for type Confirmation of attribute.
-> check(AST.method m,ScopeTable<String> st) : Function checks all the expressions and statements in the body of that method It also checks all the arguments of a function.
-> check(AST.method m,ScopeTable<String> st) : Function checks for type confirmation of a function argument.
-> check(AST.expression exp,ScopeTable<String> st) : Function checks for type confirmation of a particular expression.

    -> For basic type definitions it is straight forward.
    -> For object instances we check if a class is defined.
    -> For binary operators like eq,leq,lt,sub,plus,divide,mul we check two expressions and necessary conditions using confirmcheck().
    -> For unary operators like comp,neg we check one expression and necessary conditions for a particular operator using confirmcheck().
    -> For loops we have a predicate and body of the loop we check both predicate and body of the loop and if predicate type is a bool.
    -> For conditional statements we check if the predicate is a bool and check for type confirmation of predicate and expressions in body of "if" and body of "else".
    -> For let we check for type confirmations in all the expressions in the body of let. 
    -> For typcase we check the predicate and the whole body of each case.
    -> For dispatch(method call) check if the class and method is properly defined, check if the number of arguments and the type of each argument matches with the method definition.
    -> For static_dispatch() we check method defined in parent class which has been redefined in child class, we follow similar procedure of dispatch with added checks of type confirmation of class left of '@' and right of '@'.

scopeTable.java : 
-> repalce() : Function replaces the typeid of an attribute in scopetable with a valid typeid if the given typeid is not defined.

Semantic.java :
-> Build the InheritanceGraph using buildGraph() from InheritanceGraph.java and check for errors upto the point, if no errors are found continue else halt and show errors.
-> check InheritanceGraph for cycles if cycle is found halt and show error.
-> add all the attributes and methods to all nodes in InheritanceGraph using addAttrMethods() while checking for errors in attributes and methods definitions if no error found continue else halt and show errors.
-> check all the classes in program for type comfirmation using methods in ExprChecks.java, if no error the program is semantically correct display the AST else show errors.


B.SRINIVAS       -CS17BTECH11009
G.ASHWANTH KUMAR -CS17BTECH11017
