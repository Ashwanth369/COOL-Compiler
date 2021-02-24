class A {
	x():Int {
		20
	};
};

class B inherits A {
	x():Int {
		22
	};
	y():Int {
		self@A.x()
	};
};

class Main inherits IO {
	main():IO {
		out_string("Hello")
	};
};