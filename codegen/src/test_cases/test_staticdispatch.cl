class A {
	a : Int;
	foo() : Int {
		0
	};
};

class B inherits A {
	b : Int;
	foo() : Int {
		1
	};
};

class C {
	f : B <- new B;
	g : IO <- new IO;
	h : A <- new B;
	b : B;
	i : A <- new A;

	testStaticDispatch() : Int {
		{
			g@IO.out_string("Dynamic type B, static type B, called on B : ");
			g@IO.out_int(f@B.foo());
			g@IO.out_string("\nDynamic type B, static type B, called on A : ");
			g@IO.out_int(f@A.foo());
			g@IO.out_string("\nDynamic type B, static type A, called on A : ");
			g@IO.out_int(h@A.foo());
			g@IO.out_string("\nDynamic type A, static type A, called on A : ");
			g@IO.out_int(i@A.foo());
			g@IO.out_string("\n");
			0;
		}
	};

	testStaticDispatchOnVoid() : Int {
		b@B.foo()
	};
};

class Main {
	c : C <- new C;
	main() : Int {
		{
			c@C.testStaticDispatch();
			c@C.testStaticDispatchOnVoid();
			0;
		}
	};
};
