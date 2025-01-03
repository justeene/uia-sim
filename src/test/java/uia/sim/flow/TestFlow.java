package uia.sim.flow;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.junit.Test;
import uia.sim.Env;
import uia.sim.resources.Container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiezhigang
 * @Description:
 * @date 2024/12/31
 */
public class TestFlow {

    /**
     * 验证链路
     * 流程仿真：
     *     start -> node1 -> node2
     * @throws Exception
     */
    @Test
    public void testSingle1() throws Exception {
        Env env = new Env();
        GroovyShell groovyShell=new GroovyShell();
        groovyShell.setProperty("a",0);
        Map<String, Container> allRes =new HashMap<>();
        Script script=groovyShell.parse("return true");
        List<Node> nodes= List.of(new Node("start","开始",allRes,groovyShell.parse("a++")),
                        new Node("node1","节点1",allRes,groovyShell.parse("a++")),
                        new Node("node2","节点2",allRes,groovyShell.parse("a++"))
                        );
        List<Edge> edges=List.of(
                new Edge("1","start","node1", script,allRes),
                new Edge("2","node1","node2", script,allRes)
        );
        //初始化资源
        nodes.forEach(v->{
            allRes.put(v.getId()+"-in",new Container(env,0));
            allRes.put(v.getId()+"-out",new Container(env,0));
        });
        //开始节点默认有一个资源
        allRes.get("start-in").release(1);
        //将节点和边加入process
        nodes.forEach(env::process);
        edges.forEach(env::process);
        //运行流程仿真
        env.run();
        assert (int)groovyShell.getProperty("a")==3;
    }

    /**
     * 验证中断
     * 流程仿真：
     *     start -> node1 -> node2
     * @throws Exception
     */
    @Test
    public void testSingle2() throws Exception {
        Env env = new Env();
        GroovyShell groovyShell=new GroovyShell();
        groovyShell.setProperty("a",0);
        Map<String, Container> allRes =new HashMap<>();
        Script script=groovyShell.parse("return true");
        List<Node> nodes= List.of(new Node("start","开始",allRes,groovyShell.parse("a++")),
                new Node("node1","节点1",allRes,groovyShell.parse("a++")),
                new Node("node2","节点2",allRes,groovyShell.parse("a++"))
        );
        List<Edge> edges=List.of(
                new Edge("1","start","node1", script,allRes),
                new Edge("2","node1","node2", groovyShell.parse("return false"),allRes)
        );
        //初始化资源
        nodes.forEach(v->{
            allRes.put(v.getId()+"-in",new Container(env,0));
            allRes.put(v.getId()+"-out",new Container(env,0));
        });
        //开始节点默认有一个资源
        allRes.get("start-in").release(1);
        //将节点和边加入process
        nodes.forEach(env::process);
        edges.forEach(env::process);
        //运行流程仿真
        env.run();
        assert (int)groovyShell.getProperty("a")==2;
    }

    /**
     * 验证分支
     * 流程仿真：
     *     start -> node1 -> node2
     *                  ∟--> node3
     * @throws Exception
     */
    @Test
    public void testSingle3() throws Exception {
        Env env = new Env();
        GroovyShell groovyShell=new GroovyShell();
        groovyShell.setProperty("a",0);
        Map<String, Container> allRes =new HashMap<>();
        Script script=groovyShell.parse("return true");
        List<Node> nodes= List.of(new Node("start","开始",allRes,groovyShell.parse("a++")),
                new Node("node1","节点1",allRes,groovyShell.parse("a++")),
                new Node("node2","节点2",allRes,groovyShell.parse("a++")),
                new Node("node3","节点3",allRes,groovyShell.parse("a++"))
        );
        List<Edge> edges=List.of(
                new Edge("1","start","node1", script,allRes),
                new Edge("2","node1","node2", script,allRes),
                new Edge("3","node1","node3", script,allRes)
        );
        //初始化资源
        nodes.forEach(v->{
            allRes.put(v.getId()+"-in",new Container(env,0));
            allRes.put(v.getId()+"-out",new Container(env,0));
        });
        //开始节点默认有一个资源
        allRes.get("start-in").release(1);
        //将节点和边加入process
        nodes.forEach(env::process);
        edges.forEach(env::process);
        //运行流程仿真
        env.run();
        assert (int)groovyShell.getProperty("a")==3;
    }


    /**
     * 验证多线程
     * parallel:false
     * 流程仿真：
     *     start -> node1 -> node2
     *                  ∟--> node3
     * @throws Exception
     */
    @Test
    public void testMulti1() throws Exception {
        Env env = new Env();
        GroovyShell groovyShell=new GroovyShell();
        groovyShell.setProperty("a",0);
        Map<String, Container> allRes =new HashMap<>();
        Script script=groovyShell.parse("return true");
        List<ParallelNode> nodes= List.of(new ParallelNode("start","开始",groovyShell.parse("a++")),
                new ParallelNode("node1","节点1",groovyShell.parse("a++")),
                new ParallelNode("node2","节点2",groovyShell.parse("a++")),
                new ParallelNode("node3","节点3",groovyShell.parse("a++"))
        );
        List<ParallelEdge> edges=List.of(
                new ParallelEdge("1","start","node1",script,allRes),
                new ParallelEdge("2","node1","node2",script,allRes),
                new ParallelEdge("3","node1","node3",script,allRes)
        );
        //初始化资源
        edges.forEach(edge->{
            allRes.put(edge.getId(),new Container(env,0));
        });
        nodes.forEach(node->{
            allRes.put(node.getId(),new Container(env,0));
            node.setRes(allRes.get(node.getId()));
            node.setLineRes(edges.stream().filter(edge->edge.getFrom().equals(node.getId()))
                    .map(edge->allRes.get(edge.getId())).collect(Collectors.toList()));
        });

        //开始节点默认有一个资源
        allRes.get("start").release(1);
        //将节点和边加入process
        nodes.forEach(env::process);
        edges.forEach(env::process);
        //运行流程仿真
        env.run();
        assert (int)groovyShell.getProperty("a")==4;
    }

    /**
     * 验证多线程阻塞
     * parallel:true
     * 流程仿真：
     *     start -> node1 -> node2
     *                  ∟--> node3
     * @throws Exception
     */
    @Test
    public void testMulti2() throws Exception {
        Env env = new Env();
        env.setParallel(true);
        GroovyShell groovyShell=new GroovyShell();
        groovyShell.setProperty("a",0);
        Map<String, Container> allRes =new HashMap<>();
        Script script=groovyShell.parse("return true");
        List<ParallelNode> nodes= List.of(new ParallelNode("start","开始",groovyShell.parse("a++")),
                new ParallelNode("node1","节点1",groovyShell.parse("a++")),
                new ParallelNode("node2","节点2",groovyShell.parse("Thread.sleep(5000)\n" +
                        "b=System.currentTimeMillis()\n" +
                        "a++")),
                new ParallelNode("node3","节点3",groovyShell.parse("a++"))
        );
        List<ParallelEdge> edges=List.of(
                new ParallelEdge("1","start","node1",script,allRes),
                new ParallelEdge("2","node1","node2",script,allRes),
                new ParallelEdge("3","node1","node3",script,allRes)
        );
        //初始化资源
        edges.forEach(edge->{
            allRes.put(edge.getId(),new Container(env,0));
        });
        nodes.forEach(node->{
            allRes.put(node.getId(),new Container(env,0));
            node.setRes(allRes.get(node.getId()));
            node.setLineRes(edges.stream().filter(edge->edge.getFrom().equals(node.getId()))
                    .map(edge->allRes.get(edge.getId())).collect(Collectors.toList()));
        });

        //开始节点默认有一个资源
        allRes.get("start").release(1);
        //将节点和边加入process
        nodes.forEach(env::process);
        edges.forEach(env::process);
        long temp=System.currentTimeMillis();
        //运行流程仿真
        env.run();
        assert (long)groovyShell.getProperty("b")-temp>5000;
        assert (int)groovyShell.getProperty("a")==4;
    }
}
