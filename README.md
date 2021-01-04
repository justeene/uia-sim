UIA-SIM
===

UIA-SIM is a Java port of [SimPy](https://simpy.readthedocs.io/en/latest/), __process-based discrete event simulation__ framework.

UIA-SIM aims to port the concepts used in SimPy to the Java world. Because there is no `yield` keyword in Java, the framework also implements a __yield-like__ API in package `uia.cor` to meet some coroutine scenarios.


## package uia.cor
The package provides __yield-like__ API.
```java
public class YieldTest {

	@Test
	public void testCallFor() {
        // 1
        Generator<Integer> gen = Yield.accept(this::callFor); 
        // 2, 5
		while(gen.next()) {
            // 4
			System.out.println("value=" + gen.getValue());
		}
	}

	public void callFor(Yield<Integer> yield) {
		try {
			for(int i = 0; i < 10; i++) {
                // 3
				yield.call(i);
			}
		} catch (InterruptedException e) {

        }
	}
}
```

1. `Generator<Integer> gen = Yield.accept(this::callFor)` - Create a `Yield` object and pass to `callFor` method. Return a generator.

2. `gen.next()` - Ask if there is a new value or not.

2. `yield.call(i)` - Pass a new value to the generator and block until invoking `gen.next()` again.
3. `gen.getValue()` - Get the new value passed by `yield.call(i)`.
5. `while(gen.next())` Repeat until completing the for loop.
 

## package uia.sim
The package is core framework of __process-based discrete event simulation__.

Because all design based on SimPy, I try to write some Java test cases to compare with Python version.

### Python

``` Python
class School:
    def __init__(self, env):
        self.env = env
        self.class_ends = env.event()
        self.pupil_procs = [env.process(self.pupil()) for i in range(3)]
        self.bell_proc = env.process(self.bell())

    def bell(self):
        while True:
            yield self.env.timeout(45)
            self.class_ends.succeed()
            self.class_ends = self.env.event()
            print()

    def pupil(self):
        while True:
            yield self.class_ends
            print(r' \o/', end='')

env = Environment()
school = School(env)
env.run(200)
```

### Java
``` Java
public class SchoolTest {
	
	private Env env;

	private Event classEnd;
	
	public SchoolTest() {
		this.env = new Env();
		this.classEnd = this.env.event("classEnd");
		env.process("pupil-1", this::pupil);
		env.process("pupil-2", this::pupil);
		env.process("bell", this::bell);
	}

	public void bell(Yield<Event> yield) {
		try {
			while(yield.isAlive()) {
				yield.call(env.timeout(45));
				this.classEnd.succeed(null);
				this.classEnd = this.env.event("classEnd");
				System.out.println(String.format("\n%3d> bell is ringing...", this.env.getNow()));
			}
		} catch (InterruptedException e) {
        
        }
	}
	
	public void pupil(Yield<Event> yield) {
		try {
			while(yield.isAlive()) {
				yield.call(this.classEnd);
				System.out.print("\\o/ ");
			}
		} catch (InterruptedException e) {
        
        }
	}
	
	@Test
	public void test1() throws Exception {
		this.env.run(200);
	}
}
```

The framework is still building and testing. The next tasks are

1. Add `AnyOf`, `AllOf` events.
2. Add `resources` implementation.
3. Add `RealtimeEnvironment` implementation. 
4. More stable of `uia.cor` package.
5. More reasonable Exception control.


# Reference

[SimPy Home](https://simpy.readthedocs.io/en/latest/)

[SimPy GitLab](https://gitlab.com/team-simpy/simpy)

