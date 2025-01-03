package uia.sim.flow;

import uia.sim.Processable;
import uia.sim.resources.Container;

import java.util.List;

/**
 * @author xiezhigang
 * @Description:
 * @date 2024/12/31
 */
public class BlockNode extends ParallelNode {

    public BlockNode(String id, String name) {
        super(id,name);
    }


    @Override
    protected void run() {
        yield(getRes().request());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO 运行了活动脚本
        System.out.println("运行节点："+getName());
        //TODO 设置耗时
        yield(env().timeout(1));
        getLineRes().forEach(v->v.release(1));
    }
    @Override
    protected void initial() {

    }


}
