class A {
    i : IO <- new IO;
    print() : Int {
    	{
    		i@IO.out_string("Hello, World !");
    		i@IO.out_string("\n");
    		0;
    	}
    };
};

class Main {
    a : A <- new A;
    main() : Int {
    	{
    		a@A.print();
    		0;
    	}
    };
};
