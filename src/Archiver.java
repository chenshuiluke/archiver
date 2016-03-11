package archiver;

import java.util.Scanner;

public class Archiver{
	public static void main(String[] args){
		try(Scanner inputScanner = new Scanner(System.in)){
			System.out.println("Please enter the name of a directory to compress:");
			String input = inputScanner.nextLine();
			Compressor compressor = new Compressor(input, "output", true);
			compressor.compress(0);
		}
	}
}