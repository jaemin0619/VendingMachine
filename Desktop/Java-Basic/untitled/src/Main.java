/*
백준 2903번
0 - 4
1- 9
2 - 25
3 -
4 -
5 - 1089
(2^n+1)^n
*/


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
     Scanner scanner=new Scanner(System.in);
     int n= scanner.nextInt();
     double result=0;

     //점의 개수 계산: (2^n+1)^2
     result=Math.pow(Math.pow(2,n)+1,2);

        System.out.println((int)result);
    }
}

