package uia.sim.flow;

import groovy.lang.Script;
import uia.sim.Processable;
import uia.sim.resources.Container;

import java.util.List;
import java.util.Map;

/**
 * @author xiezhigang
 * @Description:
 * @date 2024/12/31
 */
public class ParallelNode extends Processable {
    private String name;
    private Container res;
    private List<Container> lineRes;
    private Script script;

    public ParallelNode(String id, String name, Script script) {
        super(id);
        this.name = name;
        this.script = script;
    }

    public void setRes(Container res) {
        this.res = res;
    }

    public void setLineRes(List<Container> lineRes) {
        this.lineRes = lineRes;
    }

    public String getName() {
        return name;
    }

    public Container getRes() {
        return res;
    }

    public List<Container> getLineRes() {
        return lineRes;
    }

    @Override
    protected void run() {
        yield(res.request());
        //TODO 运行了活动脚本
        script.run();
        System.out.println("运行节点："+name);
        //TODO 设置耗时
        yield(env().timeout(1));
        lineRes.forEach(v->v.release(1));
    }
    @Override
    protected void initial() {

    }


}
