package uia.sim;

public class EnvHighTest {

	public void test() {
		Env env = new Env();
		env.process("1", y -> {
			y.call(env.timeout(10));
		});
	}
}
