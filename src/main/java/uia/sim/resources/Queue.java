package uia.sim.resources;

import uia.sim.Env;
import uia.sim.SimException;

import java.util.LinkedList;

/**
 * A amount based container.
 *
 * @author Kan
 *
 */
public final class Queue extends BaseResource<Queue> {

    private LinkedList<Object> list;

    /**
     * The constructor.
     *
     * @param env The environment.
     */
    public Queue(Env env) {
        super(env);
        this.list = new LinkedList<>();
    }

    /**
     * Requests a amount from the container.
     *
     * @param id The request id.
     * @return The request event.
     */
    public Request request(String id) throws SimException {
        return new Request(this, id);
    }

    /**
     * Adds new amount to the container.
     *
     * @param id The release id.
     * @param amount Amount to be added to the container.
     * @return The release event.
     */
    public Release release(String id, int amount) {
        return new Release(this, id, amount);
    }

    @Override
    protected synchronized boolean doRequest(BaseRequest<Queue> request) {
        if (!this.list.isEmpty()) {
            ((Request)request).setItem(this.list.removeLast());
            request.succeed(null);      // resume the process to work.
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected synchronized boolean doRelease(BaseRelease<Queue> release) {
        this.list.add(((Release) release).object);
        release.succeed(null);      // resume the resource to refresh requests.
        return true;
    }

    /**
     * The request event of the container.
     *
     * @author Kan
     *
     */
    public static final class Request extends BaseRequest<Queue> {
        private Object item;

        public Object getItem() {
            return item;
        }

        public void setItem(Object item) {
            this.item = item;
        }

        /**
         * The constructor.
         *
         * @param container The container.
         * @param id The request id.
         */
        protected Request(Queue container, String id) {
            super(container, id);
            this.resource.addRequest(this);
        }

        @Override
        public void exit() {
        }
    }

    /**
     * The release event of the container.
     *
     * @author Kan
     *
     */
    public static final class Release extends BaseRelease<Queue> {

        /**
         * The amount to be added to the container.
         */
        public final Object object;

        /**
         * The constructor.
         *
         * @param container The container.
         * @param id The release id.
         * @param object The amount to be added.
         */
        protected Release(Queue container, String id, Object object) {
            super(container, id);
            this.object = object;
            this.resource.addRelease(this);
        }

        @Override
        public void exit() {
        }
    }
}
