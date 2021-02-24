class Looptest inherits IO {
	a : Int;
	b : Int;
	c : Object;
	loopTest(x : Int) : Int {
		{
			self@IO.out_string("Print the factorial of n\n");
			self@IO.out_string("n = ");
			self@IO.out_int(x);
			self@IO.out_string("\n");
			a <- 1;
			b <- 1;
			c <- while a <= x loop {
				b <- a*b;
				a <- a+1;
			} pool;
			self@IO.out_string("The factorial is : \n");
			self@IO.out_int(b);
			self@IO.out_string("\nCompleted\n");
			0;
		}
	};
};

class Main {
	e : Looptest <- new Looptest;
	main() : Int {
		{
			e@Looptest.loopTest(10);
		}
	};
};