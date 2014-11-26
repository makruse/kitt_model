package de.zmt.kitt.sim;

import java.lang.reflect.Field;
import java.util.List;

import sim.util.Double2D;

public class Vec {

	/** reflect vector 'v' against l with normal n  */
	static public Double2D reflect2(Double2D v,Double2D n){
	    // R = 2 * (V dot N) * N +V
	    double d = dot(new Double2D( -v.x, -v.y) ,n);
	    return new Double2D(2 * d * n.x +v.x, 2 * d * n.y + v.y );
	}
	
	/** dot product of two vectors */
	static public double  dot(Double2D v1,Double2D v2){
	    return (v1.x*v2.x) + (v1.y*v2.y);
	}

	/** normalize vector (make length = 1) */
	static public Double2D normalize(Double2D v){
	    double len = getLength(v);
	    return new Double2D(v.x / len,   v.y / len );
	}

	/** find the length of a vector */
	static public double getLength(Double2D v){
	    return Math.sqrt(v.x*v.x+v.y*v.y);
	}

	static public Double2D reflectVector(Double2D v, Double2D at){
		// s3++;
		// get normal vector for obstacle
		Double2D no = new Double2D(  -at.x, at.y );		
		// normalized normal vector of obstacle
		Double2D n = normalize(no);
		// get the reflected vector R for agents direction V at vector n
		Double2D r = reflect2( new Double2D(v.x,v.y), n);
		
		return r;
	}	
}
