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
public class Edge extends Processable {
    private Map<String, Container> allRes;
    private String from;
    private String to;
    private Script script;

    public Edge(String id, String from, String to, Script script,Map<String, Container> allRes) {
        super(id);
        this.allRes = allRes;
        this.from = from;
        this.to = to;
        this.script = script;
    }

    @Override
    protected void run() {
        Container lineRes=allRes.get(from+"-out");
        Container.Request request=lineRes.request();
        yield(request);
        Object result=script.run();
        if((Boolean)result) {
            //脚本通过，到下个节点
            Container nodeRes = allRes.get(to + "-in");
            nodeRes.release(1);
        }else {
            //脚本不通过或异常要释放资源
            request.exit();
        }
    }
    @Override
    protected void initial() {

    }


}
