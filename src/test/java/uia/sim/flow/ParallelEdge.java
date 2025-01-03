package uia.sim.flow;

import groovy.lang.Script;
import uia.sim.Processable;
import uia.sim.resources.Container;

import java.util.Map;

/**
 * 并行边类，继承自Processable接口
 * 用于模拟并行处理流程中的边，通过执行脚本来决定是否将资源传递给下一个节点
 *
 * @author xiezhigang
 * @date 2024/12/31
 */
public class ParallelEdge extends Processable {
    // 所有资源的映射，用于访问和操作资源
    private Map<String, Container> allRes;
    // 边的起点节点ID
    private String from;
    // 边的终点节点ID
    private String to;
    // 脚本对象，用于执行条件判断逻辑
    private Script script;

    public String getFrom() {
        return from;
    }

    /**
     * 构造函数
     * 初始化ParallelEdge对象，并设置其属性
     *
     * @param id 边的唯一标识符
     * @param from 边的起点节点ID
     * @param to 边的终点节点ID
     * @param script 脚本对象，用于执行条件判断
     * @param allRes 所有资源的映射
     */
    public ParallelEdge(String id, String from, String to, Script script, Map<String, Container> allRes) {
        super(id);
        this.allRes = allRes;
        this.from = from;
        this.to = to;
        this.script = script;
    }

    /**
     * 运行方法，实现Processable接口
     * 根据脚本执行结果决定资源的流向
     */
    @Override
    protected void run() {
        // 获取当前边对应的资源
        Container lineRes=allRes.get(getId());
        // 请求资源
        Container.Request request=lineRes.request();
        // 处理请求，如果请求成功则继续执行
        yield(request);
        // 执行脚本
        Object result=script.run();
        // 根据脚本执行结果决定资源流向
        if((Boolean)result) {
            // 脚本通过，到下个节点
            Container nodeRes = allRes.get(to);
            // 给下个节点加1
            nodeRes.release(1);
        }else {
            // 脚本不通过或异常要释放资源
            request.exit();
        }
    }
    /**
     * 初始化方法，实现Processable接口
     * 用于在流程开始前进行初始化设置
     */
    @Override
    protected void initial() {

    }


}
