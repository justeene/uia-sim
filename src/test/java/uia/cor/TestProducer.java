package uia.cor;

import uia.sim.Env;
import uia.sim.Event;
import uia.sim.Processable;
import uia.sim.resources.Container;
import uia.sim.resources.Queue;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiezhigang
 * @Description:
 * @date 2024/12/24
 */
public class TestProducer {
    private Env env;
    private Queue res;
    public static void main(String[] args) {
        new TestProducer().start();
    }
    public void start(){
        env = new Env();
        res=new Queue(env);
        env.process("producer",this::producer);
        env.process("inspector",this::inspector);
        env.run();
    }

    private void producer(Yield2Way<Event, Object> yield) {
        for (int i=0;i<10;i++){
            res.release("a",i);
            System.out.println(env.getNow()+"生产者生产了物品："+i);
            yield.call(env.timeout(2));
        }
    }

    private void inspector(Yield2Way<Event, Object> yield) {
        while (true){
            Queue.Request request=res.request("a");
            yield.call(request);

            System.out.println(env.getNow()+"检查员接收到物品"+request.getItem()+"的检查");
            yield.call(env.timeout(2));
            System.out.println(env.getNow()+"检查员完成物品"+request.getItem()+"的检查");

        }
    }


}
