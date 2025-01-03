package uia.sim.flow;

import groovy.lang.Script;
import uia.sim.Processable;
import uia.sim.resources.Container;

import java.util.Map;

/**
 * @author xiezhigang
 * @Description:
 * @date 2024/12/31
 */
public class Node extends Processable {
    private Map<String, Container> allRes;
    private String name;
    private Script script;

    public Node(String id,  String name, Map<String, Container> allRes,Script script) {
        super(id);
        this.allRes = allRes;
        this.name = name;
        this.script = script;
    }

    @Override
    protected void run() {
        Container nodeRes=allRes.get(getId()+"-in");
        yield(nodeRes.request());
        script.run();
        System.out.println("运行节点："+name);
        yield(env().timeout(1));
        Container lineRes=allRes.get(getId()+"-out");
        lineRes.release(1);
    }
    @Override
    protected void initial() {

    }


}
