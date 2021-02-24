class Ifelsetest inherits IO {
	a : Int;
	ifElseTest(x : Int, y : Int) : Int {
		{
			self@IO.out_string("Testing if-else by finding the smallest of three numbers\n");
			self@IO.out_string("The numbers are : \n");
			self@IO.out_int(x);
			self@IO.out_string(" ");
			self@IO.out_int(y);
			self@IO.out_string("\nThe smallest among them is : ");
			a <- if x <= y then x else y fi;
			self@IO.out_int(a);
			self@IO.out_string("\n");
			0;
		}
	};	
};

class Main {
	e : Ifelsetest <- new Ifelsetest;
	main() : Int {
		{
			e@Ifelsetest.ifElseTest(3,5);
		}
	};
};