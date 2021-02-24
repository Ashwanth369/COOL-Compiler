class A {
	io : IO <- new IO;
	s : String;
	c : Int;
	testIO() : Int {
		{
			io@IO.out_string("in_string() :\n");
			s <- io@IO.in_string();
			io@IO.out_string("string given : ");
			io@IO.out_string(s);
			io@IO.out_string("\nin_int() : ");
			c <- io@IO.in_int();
			io@IO.out_string("number given : ");
			io@IO.out_int(c);
			io@IO.out_string("\n");
			0;
		}
	};
};

class Main {
	a : A <- new A;
	main() : Int {
		{
			a@A.testIO();
			0;
		}
	};
};