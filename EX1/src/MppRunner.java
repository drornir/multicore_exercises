import mpp.Ex1q6;
import mpp.Ex1q7;
import mpp.Ex1q8;

/**
 * Created by Dror Nir on 05/05/2017.
 */
public class MppRunner {
    final static String[][] inputs = {{"1"}, {"4"}, {"8"}, {"16"}, {"32"}};

    public static void main(String[] args) {
        for (String[] input : inputs) {
            System.out.print("Q6: ");
            Ex1q6.main(input);
        }for (String[] input : inputs) {
            System.out.print("Q7: ");
            Ex1q7.main(input);
        }for (String[] input : inputs) {
            System.out.print("Q8: ");
            Ex1q8.main(input);
        }
    }
}
