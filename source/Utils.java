public class Utils {
    static void println() {
        System.out.println("static print");
    }

    static {
        System.out.println("print static block");
    }

    {
        System.out.println("print normal block");
    }
}
