class B inherits A{
	y:Int;
	g(x:Int):Int {
		x+1
	};
};

class A inherits B{
	yy:Int;
	gg(x:Int):Int {
		x+1
	};
};

class Main {
	io:IO<-new IO;
	main():IO {
		io.out_string("HELLO WORLD")
	};
};