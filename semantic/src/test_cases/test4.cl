class B{
	y:Int;
	g(x:Int):Int {
		x+1
	};
	h():Int {
		20
	};
};

class A inherits B {
	x:Int<-10;
	y:Bool;
	x:String<-"Hi";

	f():Int {
		0
	};
	g():Int {
		1
	};
	f():String {
		"0"
	};
	h():Int {
		21
	};
};


class Main {
	io:IO<-new IO;	
	main():IO {
		io.out_string("HELLO WORLD")
	};
};