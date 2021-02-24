
class A inherits B {
	x:Int;
};

class B inherits A {
	y:Int;
};

class Main {
	obj:Object<-new Object;
	bool:Bool<-new Bool;
	str:String<-new String;
	io:IO<-new IO;
	
	mainn():IO {
		io.out_string("HELLO WORLD")
	};
};