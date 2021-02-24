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

	f():Int {
		0
	};
};


class Main {
	a:A<- new A;
	b:B<- new B;
	io:IO<- new IO;	
	main():IO {
		io.out_int(a.h())
	};
};