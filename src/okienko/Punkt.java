package okienko;

import java.lang.Thread.State;

public class Punkt {

    public Integer x;
	public Integer y;
	
    public Punkt(final int x,final int y) {
		this.x = x;
		this.y = y;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public void setY(Integer y) {
		this.y = y;
	}
}