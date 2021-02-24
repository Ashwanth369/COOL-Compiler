class A {
	s:String <- new String;
	out_strings(i:String,i1:String):String {
		i1
	};
};

class B inherits A {
	s:String <- new String;
	out_strings(i:String,i1:String):String {
		i
	};	
};

class Main {
	a:A <- new A;
	b:B <- new B;
	i:Int <- new Int;
	main():String {
		b@A.out_strings(i,i)
	};
};
