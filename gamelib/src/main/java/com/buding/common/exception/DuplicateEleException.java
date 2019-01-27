package com.buding.common.exception;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class DuplicateEleException extends RuntimeException {	
	static final long serialVersionUID = -7034897190745766949L;
	
	public DuplicateEleException(String msg) {
		super(msg);
	}
}
