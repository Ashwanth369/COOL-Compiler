class Main inherits IO{
	a:Int <- 1;
	d:Int <- 0;
	c:String <- "new String";
	b:Int <- 2;
	main():IO {{
		case c of
			e : Int => b<-a;
			e : Int => c<-"HI";
			e : Object => d<-a+1;
		esac;
		out_int(d);
		out_int(b);
		out_string(c);
	}};
};