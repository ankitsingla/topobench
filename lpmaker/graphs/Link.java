/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import lpmaker.ProduceLP;

public class Link {
	public Integer linkTo;
	public int linkcapacity;

	public Link(Integer _linkTo){
		linkTo = _linkTo;
		linkcapacity = 1;
	}

	public Link(Integer _linkTo, int capacity){
		linkTo = _linkTo;
		linkcapacity = capacity;
	}

	public int intValue() {
		return linkTo;
	}
}
