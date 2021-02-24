class A {
	x():Int {
		1
	};
	y():Int {
		3
	};
};
 
class B inherits A {
	x():Int {
		2
	};
};

class Main inherits IO{
	b:B<-new B;
	main():Object {{
		out_int(b.x());
		out_int(b@A.x());
		out_int(b.y());
		out_int(b@A.y());
	}};
};