package uia.cor;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Yield<T> {
		
    private static final Logger logger = LogManager.getLogger(Yield.class);

    private String id;
	
	private Object nextHandle;
	
	private T value;
	
	private boolean closed;
	
	private boolean terminated;

	private final Consumer<Yield<T>> iterable;

	private Yield(String id, Consumer<Yield<T>> iterable) {
		this.id = id;
		this.iterable = iterable;
		this.nextHandle = new Object();
		this.closed = false;
	}
	
	public static <T> Generator<T> accept(Consumer<Yield<T>> iterable) {
		return accept(null, iterable);
	}

	public static <T> Generator<T> accept(String yieldId, Consumer<Yield<T>> iterable) {
		final Yield<T> yield = new Yield<>(yieldId, iterable);
		synchronized(yield) {
			Thread th = new Thread(yield::running);
			th.setPriority(Thread.MAX_PRIORITY);
			th.start();
			try {
				yield.wait(1000);
			} catch (Exception e) {
				
			}
		}
		return new Generator<>(yield);
	}
	
	public String getId() {
		return this.id;
	}
	
	public void interrupt() {
		if(this.closed) {
			return;
		}
		synchronized(this) {
			this.closed = true;
			this.terminated = true;
			this.notifyAll();
			logger.debug(String.format("%s> terminate() release call()", this.id));	
		}
	}

	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a new value.
	 */
	public boolean next() {
		if(this.closed) {
			return false;
		}
		synchronized(this.nextHandle) {
			synchronized(this) {
				this.notifyAll();
				logger.debug(String.format("%s> next() release call()", this.id));	
			}

			try {
				logger.debug(String.format("%s> next() is blocking, waiting call() to notify", this.id));	
				this.nextHandle.wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// mode1 
		return !this.closed;
		// mode2
		//return true;
	}
	
	public void pause() {
		synchronized(this) {
			try {
				logger.debug(String.format("%s> pause()", this.id));	
				this.notifyAll();
				this.wait();
			} catch (Exception ex) {

			}
		}
	}

	/**
	 * Submit a new value to the generator of this instance.
	 * 
	 * @param value The new value.
	 */
	public void call(T value) throws InterruptedException {
		if(this.closed) {
			return;
		}

		// mode1: prepare the value first, then block this call.
		synchronized(this.nextHandle) {
			this.value = value;
			this.nextHandle.notifyAll();
			logger.debug(String.format("%s> call(v) notify next() to get new value:%s", this.id, value));	
		}
		
		synchronized(this) {
			try {
				logger.debug(String.format("%s> call(v) is blocking, waiting next() to release", this.id));	
				this.wait();
			} catch (Exception ex) {

			}
		}

		// mode2: block this call, then prepare the value.
		/**
		synchronized(this.handle) {
			this.value = value;
			this.handle.notifyAll();
			logger.debug(String.format("%s> call(v) notify next() to get new value", this.id));	
		}
		*/

		testTerminated();
	}

	/**
	 * Submit a new value.
	 * 
	 * @param supplier The function to get the new value..
	 */
	public void call(Supplier<T> supplier) throws InterruptedException {
		if(this.closed) {
			return;
		}

		// mode1: prepare the value first, then block this call.
		synchronized(this.nextHandle) {
			this.value = supplier.get();
			this.nextHandle.notifyAll();
			logger.debug(String.format("%s> call(s) notify next() to get new value", this.id));	
		}

		synchronized(this) {
			try {
				logger.debug(String.format("%s> call(s) is blocking, waiting next() to release", this.id));	
				this.wait();
			} catch (Exception ex) {

			}
		}

		// mode2: block this call, then prepare the value.
		/**
		synchronized(this.handle) {
			this.value = supplier.get();
			this.handle.notifyAll();
			logger.debug(String.format("%s> call(s) notify next() to get new value", this.id));	
		}
		*/

		testTerminated();
	}

	/**
	 * Submit the last value.
	 * 
	 * @param value The last value.
	 */
	public void callLast(T value) {
		if(this.closed) {
			return;
		}

		synchronized(this.nextHandle) {
			this.value = value;
			this.nextHandle.notifyAll();
			logger.debug(String.format("%s> callLast(v) notify next() to get new value", this.id));	
		}
		this.closed = true;
	}

	/**
	 * Submit the last value.
	 * 
	 * @param supplier The function to get the last value..
	 */
	public void callLast(Supplier<T> supplier) {
		if(this.closed) {
			return;
		}

		synchronized(this.nextHandle) {
			this.value = supplier.get();
			this.nextHandle.notifyAll();
			logger.debug(String.format("%s> callLast(s) notify next() to get new value", this.id));	
		}
		this.closed = true;
	}
	
	/**
	 * Returns the current value.
	 * @return
	 */
	public synchronized T getValue() {
		return this.value;
	}

	public synchronized void close() {
		this.closed = true;
		logger.debug(String.format("%s> close()", this.id));	

		synchronized(this.nextHandle) {
			this.nextHandle.notifyAll();
		}
		this.notifyAll();
	}
	
	public synchronized boolean isAlive() {
		return !this.closed;
	}
	
	public synchronized boolean isClosed() {
		return this.closed;
	}
	
	private void running() {
		try {
			if(this.id == null) {
				this.id = "yield-" + Thread.currentThread().getId();
			}

			// mode1: pause first.
			pause();
			// mode2: no necessary to pause
			this.iterable.accept(this);	// blocking
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			logger.debug(String.format("%s> ruuning() done", this.id));	
			close();
		}
	}
	
	private void testTerminated() throws InterruptedException {
		if(this.terminated) {
			throw new InterruptedException(this.id + " has been terminated");
		}
	}
	
	@Override
	public String toString() {
		return this.id;
	}
	
	
}
