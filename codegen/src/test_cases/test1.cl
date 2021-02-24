class ExprTests inherits IO {
	a : Int;
	b : Bool;

	operations(p : Int, q : Int) : Int {
		{
			self@IO.out_string("Operands are : ");
			self@IO.out_int(p);
			self@IO.out_string(" ");
			self@IO.out_int(q);
			self@IO.out_string("\nSum = ");
			a <- p + q;
			self@IO.out_int(a);
			self@IO.out_string("\nDifference = ");
			a <- p - q;
			self@IO.out_int(a);
			self@IO.out_string("\nProduct = ");
			a <- p * q;
			self@IO.out_int(a);
			self@IO.out_string("\nDivsion Result = ");
			a <- p / q;
			self@IO.out_int(a);
			self@IO.out_string("\nNegation of first : ");
			a <- ~p;
			self@IO.out_int(a);
			self@IO.out_string("\nComparing these operands now\n");
			self@IO.out_string("Less than : ");
			a <- if p < q then p else q fi;
			if a = p
			then self@IO.out_string("First is lesser or equal\n")
			else self@IO.out_string("First is greater or equal\n")
			fi;
			if p<=q
			then self@IO.out_string("First is lesser or equal\n")
			else self@IO.out_string("First is greater\n")
			fi;
			if p = q
			then self@IO.out_string("Both are equal\n")
			else self@IO.out_string("Both are not equal\n")
			fi;
			b <- true;
			if b
			then self@IO.out_string("Bool value is true\n")
			else self@IO.out_string("Bool value is false\n")
			fi;
			if not b
			then self@IO.out_string("It's complement is true\n")
			else self@IO.out_string("It's complement is false\n")
			fi;
			self@IO.out_string("Completed\n\n");
			0;
		}	
	};
};

class Main {
	h : ExprTests <- new ExprTests;
	main() : Int {
		{
			h@ExprTests.operations(3,4);
			h@ExprTests.operations(4,0);
		}
	};
};