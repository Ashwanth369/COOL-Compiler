class A {
  isPrime(num: Int): Bool {
    (let i : Int <- 2 in
    (let flag : Int <- 1 in
    (let temp : Int in
    (let temp2 : Int in
      {
        while i <= num/2 loop 
         {
            temp <- num / i ; 
            temp2 <- temp * i ;
            if temp2 = num then {
              flag <- 0 ;
              i <- i + 1 ;
            }
            else {
              i <- i + 1;
            }
            fi;
          }
        pool;
        if flag = 1 then true else false fi;
      }
    ))))
  };
};

class Main inherits IO {      -- checks if a given number is prime.
  io:IO <- new IO;
  a:A <- new A;

  main(): Object {{
    io.out_string("Enter an integer greater than or equal to 2 : ");

    let n: Int <- io.in_int() in
      if n < 1 then 
        io.out_string("Invalid Input!\n")
      else {
        if a.isPrime(n) then io.out_string("It is a Prime!") else io.out_string("It is not a Prime!") fi;
        io.out_string("\n");
      }
      fi;
  }};
};