import java.util.ArrayList;
import java.util.Scanner;

public class TestClass {
    static TestClass testClass = new TestClass();
    public static void main(String[] args) throws InterruptedException {
//        TestClass testClass = new TestClass();
//        Utils.println();
    }

    {
        System.out.println("normal");
    }

    static {
        System.out.println("static");
    }
}
