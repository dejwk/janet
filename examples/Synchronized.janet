/* -*-Java-*- */

package examples;

public class Synchronized {

    public void test() {
        Object o1 = new Object();
        Object o2 = new Object();
        
        try {
            synchronizedTest(o1, o2);
        } catch (Exception e) {
            System.out.println("Exception from within synchronized code " +
                "caught (as expected)");
            System.out.println("Check if the monitor is unlocked");
            Thread t = new TestThread(o2);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e1) {}
        }
        
        
    }

    class TestThread extends Thread {
        Object obj;
        TestThread(Object obj) { this.obj = obj; }
        public void run() {
            synchronized (obj) {
                System.out.println("OK!");
            }
        }
    }

    public native "C" void synchronizedTest(Object o1, Object o2) 
        throws Exception
    {
        `synchronized(o1)` {
            /* this is synchronized */
            printf("In synchronized code\n");
        }
        `synchronized(o2)` {
            printf("In synchronized code, about to throw an exception\n");
            `throw new Exception();`
            /* monitor is unlocked */
        }
    }

}
