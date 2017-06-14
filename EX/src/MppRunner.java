import mpp.Ex2q7;
import mpp.Ex3q8;

/**
 * Created by Dror Nir on 05/05/2017.
 */
public class MppRunner {
    final static String[][] THREAD_COUNT = {{"1"}, {"2"}, {"4"}, {"8"}, {"16"}, {"32"}};
    final static String[][] IMPLEMENTATION = {{"1"}, {"2"}};

    public static void main(String[] args) {
        main8();


//        for (int i = 0; i < THREAD_COUNT.length; i++) {
//            String[] threads = THREAD_COUNT[i];
//            for (int j = 0; j < IMPLEMENTATION.length; j++) {
//                String[] impl = IMPLEMENTATION[j];
//                for (int k = 0; k < 10; k++) {
//                    System.out.print(threads[0] + "," + impl[0] + "," + k + ",");
//                    Ex2q7.main(new String[]{threads[0], impl[0]});
//                }
//            }
//        }
    }

    static void main7(){
        System.out.println("threads,implementation,iteration,throughput");
        String[] threads = THREAD_COUNT[5];
        for (int j = 0; j < IMPLEMENTATION.length; j++) {
            String[] impl = IMPLEMENTATION[j];
            for (int k = 0; k < 10; k++) {
                System.out.print(threads[0] + "," + impl[0] + "," + k + ",");
                Ex2q7.main(new String[]{threads[0], impl[0]});
            }
        }
    }

    static void main8(){
            System.out.println("threads, small_bo, large_bo, clh");
            for (int i = 0; i < THREAD_COUNT.length; i++) {
                String[] threads = THREAD_COUNT[i];
                Ex3q8.main(new String[]{threads[0]});
            }

    }
}
