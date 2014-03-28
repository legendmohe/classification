package model;

public class ObjectPair<T1, T2> {
	private T1 one;
	private T2 two;
	
	public ObjectPair(T1 one, T2 two) {
		this.setOne(one);
		this.setTwo(two);
	}

	public T1 getOne() {
		return one;
	}

	public void setOne(T1 one) {
		this.one = one;
	}

	public T2 getTwo() {
		return two;
	}

	public void setTwo(T2 two) {
		this.two = two;
	}
}
